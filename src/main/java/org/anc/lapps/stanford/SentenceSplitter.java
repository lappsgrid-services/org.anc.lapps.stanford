package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.api.Data;
import org.lappsgrid.discriminator.Types;

import java.util.*;

/**
 * @author Keith Suderman
 */
public class SentenceSplitter extends AbstractStanfordService
{
   public SentenceSplitter()
   {
      super("tokenize, ssplit");
   }

   @Override
   public Data execute(Data input)
   {
      Annotation document = new Annotation(input.getPayload());
      List<CoreMap> sentences = document.get(SentencesAnnotation.class);
      for (CoreMap sentence : sentences)
      {

      }
      return null;
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
