package org.anc.lapps.stanford;

//import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Keith Suderman
 */
public class NamedEntityRecognizer extends AbstractStanfordService
{
   private static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);

   public NamedEntityRecognizer()
   {
      super("tokenize, ssplit, pos, lemma, ner");
   }

   @Override
   public long[] requires()
   {
      return new long[] { Types.TEXT };
   }

   @Override
   public long[] produces()
   {
      return new long[] { Types.JSON, Types.SENTENCE, Types.TOKEN, Types.POS, Types.NAMED_ENTITES};
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford named entity recognizer.");
      Container container = createContainer(input);
      if (container == null)
      {
         return input;
      }

      Annotation document = new Annotation(container.getText());
      Data data = null;
      StanfordCoreNLP service = null;
      try
      {
         service = pool.take();
         service.annotate(document);

         // these are all the sentences in this document
         // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
         List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
         ProcessingStep step = new ProcessingStep();
         String name = this.getClass().getName() + ":" + Version.getVersion();
         step.getMetadata().put(Metadata.PRODUCED_BY, name);
         step.getMetadata().put(Metadata.CONTAINS, Annotations.NE);
         Converter.addSentences(step, sentences);
         Converter.addTokens(step, document.get(CoreAnnotations.TokensAnnotation.class));
         container.getSteps().add(step);
//         String json = Converter.toJson(sentences);
         data = DataFactory.json(container.toJson());
      }
      catch (InterruptedException e)
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
