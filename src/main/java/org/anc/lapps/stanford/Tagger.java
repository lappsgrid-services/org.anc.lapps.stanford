package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Keith Suderman
 */
public class Tagger extends AbstractStanfordService
{
   public Tagger()
   {
      super("tokenize, ssplit, pos");
   }

   @Override
   public long[] requires()
   {
      return new long[]{Types.STANFORD, Types.TOKEN};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.STANFORD, Types.TOKEN, Types.POS};
   }

   @Override
   public Data execute(Data input)
   {
      Annotation document = new Annotation(input.getPayload());
      service.annotate(document);
      List<String> list = new ArrayList<String>();
      List<CoreLabel> tokens = document.get(TokensAnnotation.class);
      if (tokens == null)
      {
         return DataFactory.error("Stanford tokenizer returned null.");
      }
      for (CoreMap token : tokens)
      {
         String pos = token.get(PartOfSpeechAnnotation.class);
         list.add(token.toString() + "/" + pos);
      }
      return DataFactory.stringList(list);
   }
}
