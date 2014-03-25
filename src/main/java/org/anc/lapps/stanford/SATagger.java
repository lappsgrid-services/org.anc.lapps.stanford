package org.anc.lapps.stanford;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.anc.lapps.serialization.Annotation;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.anc.lapps.stanford.util.StanfordUtils;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Features;
import org.lappsgrid.vocabulary.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SATagger implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(SATagger.class);

   private MaxentTagger tagger;

   public SATagger() //throws LappsException
   {
      logger.info("Creating the MaxentTagger");
      tagger = new MaxentTagger(Constants.PATH.TAGGER_MODEL_PATH);
   }
   
   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford stand-alone Tagger.");
//      logger.info("Tagger is using model english-bidirectional-distsim.");
//      Container container = createContainer(input);
//      if (container == null)
//      {
//         return input;
//      }
      long type = input.getDiscriminator();
      if (type == Types.ERROR)
      {
         return input;
      }
      if (type != Types.JSON)
      {
         String name = DiscriminatorRegistry.get(type);
         String message = "Invalid input type. Expected JSON but found " + name;
         logger.warn(message);
         return DataFactory.error(message);
      }

      Container container = new Container(input.getPayload());
      Data data = null;
      List<ProcessingStep> steps = container.getSteps();
      ProcessingStep tokenStep = StanfordUtils.findStep(steps, Annotations.TOKEN);

      if (tokenStep == null)
      {
         logger.warn("No tokens were found in any processing step");
         return DataFactory.error("Unable to process input; no tokens found.");
      }
      
      List<Annotation> annotations = tokenStep.getAnnotations();
      List<CoreLabel> labels = new ArrayList<CoreLabel>();
      for (Annotation a : annotations)
      {
         labels.add(new LappsCoreLabel(a));
      }

      try
      {
         tagger.tagCoreLabels(labels);
      }
      catch (Exception e)
      {
         StringWriter stringWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(stringWriter);
         writer.println("Unable to run the stanford tagger.");
         e.printStackTrace(writer);
         return DataFactory.error(stringWriter.toString());
      }
      
      ProcessingStep step = Converter.addTokens(new ProcessingStep(), labels);
      String name = this.getClass().getName() + ":" + Version.getVersion();
      Map<String,String> metadata = step.getMetadata();
      metadata.put(Metadata.PRODUCED_BY, name);
      metadata.put(Metadata.CONTAINS, Features.PART_OF_SPEECH);
      container.getSteps().add(step);
      data = DataFactory.json(container.toJson());
      
      return data;
   }
   
   @Override
   public long[] requires()
   {
      return new long[]{ Types.JSON, Types.TOKEN };
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.JSON, Types.TOKEN, Types.POS};
   }

   @Override
   public Data configure(Data arg0)
   {
      return DataFactory.error("Unsupported operation.");
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

   public static void main(String[] args) throws IOException, LappsException
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
