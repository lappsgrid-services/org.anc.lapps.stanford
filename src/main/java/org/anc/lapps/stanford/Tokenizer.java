package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
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
public class Tokenizer extends AbstractStanfordService
{
   public Tokenizer()
   {
      super("tokenize");
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
      Annotation document = new Annotation(input.getPayload());
      service.annotate(document);
      List<String> list = new ArrayList<String>();
      List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
      if (tokens == null)
      {
         return DataFactory.error("Stanford tokenizer returned null.");
      }
      for (CoreMap token : tokens)
      {
         list.add(token.toString());
      }
      return DataFactory.stringList(list);
   }

}
