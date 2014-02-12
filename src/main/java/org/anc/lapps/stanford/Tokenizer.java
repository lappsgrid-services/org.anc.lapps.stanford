package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.anc.util.IDGenerator;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Keith Suderman
 */
public class Tokenizer extends AbstractStanfordService
{
   private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);

   public Tokenizer()
   {
      super("tokenize");
      logger.info("Stanford tokenizer created.");
   }

   @Override
   public long[] requires()
   {
      return new long[]{Types.STANFORD, Types.SENTENCE};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.STANFORD, Types.SENTENCE, Types.TOKEN};
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford tokenizer.");
      Container container = createContainer(input);
      if (container == null)
      {
         return input;
      }
      String text = container.getText();
      Annotation document = new Annotation(text);
      Data data = null;
      StanfordCoreNLP service = null;
      try
      {
         service = pool.take();
         service.annotate(document);
         List<CoreLabel> tokens = document.get(TokensAnnotation.class);
         if (tokens == null)
         {
            return DataFactory.error("Stanford tokenizer returned null.");
         }
         ProcessingStep step = Converter.addTokens(new ProcessingStep(), tokens);
         //step.getMetadata().put("produced by", "Stanford Tokenizer");
         String name = this.getClass().getName() + ":" + Version.getVersion();
         step.getMetadata().put(Metadata.PRODUCED_BY, name);
         container.getSteps().add(step);
         data = DataFactory.json(container.toJson());
//         logger.info("Stanford tokenizer complete.");
//         data = DataFactory.stringList(list);
//         data.setDiscriminator(Types.STANFORD);
      }
      catch (Exception e)
      {
         data = DataFactory.error(e.getMessage());
      }
      finally
      {
         if (service != null)
         {
            pool.add(service);
         }
      }
      return data;
   }

}
