package org.anc.lapps.stanford;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.anc.lapps.serialization.Annotation;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SATagger implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(SATagger.class);

   public SATagger()
   {
      logger.info("Stanford stand-alone tagger created.");
   }
   
   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford stand-alone Tagger.");
      logger.info("Tagger is using model english-bidirectional-distsim.");
      Container container = createContainer(input);
      if (container == null)
      {
         return input;
      }
      Data data = null;
      List<ProcessingStep> steps = container.getSteps();
      ProcessingStep tokenStep = null;
      for (ProcessingStep step : steps)
      {
         boolean hasTokens = false;
         
         // Check if this processing step contains tokens
         String contains = (String) step.getMetadata().get("contains");
         if (contains != null)
         {
            hasTokens = contains.contains("tokens");
         }
         else
         {
            String producedBy = (String) step.getMetadata().get(Metadata.PRODUCED_BY);
            hasTokens = producedBy.contains("token") || producedBy.contains("Token");
         }
         
         if (hasTokens)
         {
            tokenStep = step;
         }
      }
      
      if (tokenStep == null)
      {
         return DataFactory.error("Unable to process input; no tokenized ProessingStep found.");
      }
      
      List<Annotation> annotations = tokenStep.getAnnotations();
      List<CoreLabel> labels = new ArrayList<CoreLabel>();
      for (Annotation a : annotations)
      {
         labels.add(new LappsCoreLabel(a));
      }
      
      MaxentTagger tagger;
      try
      {
         tagger = new MaxentTagger("src/main/resources/models/english-bidirectional-distsim.tagger");
      }
      catch (OutOfMemoryError e)
      {
         return DataFactory.error("Ran out of memory training MaxentTagger.");
      }
      tagger.tagCoreLabels(labels);
      
      ProcessingStep step = Converter.addTokens(new ProcessingStep(), labels);
      step.getMetadata().put(Metadata.PRODUCED_BY, "Stanford Stand-Alone MaxentTagger");
      step.getMetadata().put("contains", "POS");
      container.getSteps().add(step);
      data = DataFactory.json(container.toJson());
      
      return data;
   }
   
   protected Container createContainer(Data input)
   {
      Container container = null;
      long inputType = input.getDiscriminator();
      if (inputType == Types.ERROR)
      {
         return null;
      }
      else if (inputType == Types.TEXT)
      {
         container = new Container();
         container.setText(input.getPayload());
      }
      else if (inputType == Types.JSON)
      {
         container = new Container(input.getPayload());
      }
      return container;
   }

   @Override
   public long[] requires()
   {
      return new long[]{Types.TOKEN};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.STANFORD, Types.TOKEN, Types.POS};
   }

   @Override
   public Data configure(Data arg0)
   {
      return DataFactory.error("Unsupported operation.");
   }

   public static void main(String[] args) throws IOException
   {
      WebService tokenizer = new SATokenizer();
      String inputText = ResourceLoader.loadString("blog-jet-lag.txt");
      Data tokenizerInput = DataFactory.text(inputText);
      Data tokenizerResult = tokenizer.execute(tokenizerInput);
      
      WebService service = new SATagger();
      Data result = service.execute(tokenizerResult);
      Container container = new Container(result.getPayload());
      
      File output = new File("src/main/resources/blog-jet-lag_tagged.json");
      PrintWriter out = new PrintWriter(output);
      out.println(container.toJson());
      out.close();
   }
}
