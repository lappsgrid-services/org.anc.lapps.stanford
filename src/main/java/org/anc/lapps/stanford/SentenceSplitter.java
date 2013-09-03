package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.anc.lapps.util.LappsUtils;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Keith Suderman
 */
public class SentenceSplitter extends AbstractStanfordService
{
   private static final Logger logger = LoggerFactory.getLogger(SentenceSplitter.class);

   public SentenceSplitter()
   {
      super("tokenize, ssplit");
      logger.info("Standford sentence splitter created.");
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford sentence splitter.");
      Annotation document = new Annotation(input.getPayload());
      Data data = null;
      StanfordCoreNLP service = null;
      try
      {
         service = pool.take();
         service.annotate(document);
         List<String> list = new ArrayList<String>();
         List<CoreMap> sentences = document.get(SentencesAnnotation.class);
         if (sentences == null)
         {
            return DataFactory.error("Stanford splitter returned null.");
         }
         for (CoreMap sentence : sentences)
         {
            list.add(sentence.toString());
         }
         data = DataFactory.stringList(list);
         data.setDiscriminator(Types.STANFORD);
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
//      String stringList = LappsUtils.makeStringList(list);
      logger.info("Sentence splitter complete.");
      return data;
   }


   @Override
   public long[] requires()
   {
      return new long[]{Types.TEXT};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.STANFORD, Types.SENTENCE};
   }
}
