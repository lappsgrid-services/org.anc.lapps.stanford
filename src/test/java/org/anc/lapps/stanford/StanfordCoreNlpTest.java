package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.junit.*;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Keith Suderman
 */
@Ignore
public class StanfordCoreNlpTest
{
   public StanfordCoreNlpTest()
   {

   }

   @Test
   public void testLemmatizer()
   {
      Properties properties = new Properties();
      properties.setProperty("annotators", "tokenize, ssplit, pos, lemma");
      StanfordCoreNLP stanford = new StanfordCoreNLP(properties);
      Annotation text = new Annotation("Fido barks.");
      stanford.annotate(text);
      List<CoreLabel> tokens = text.get(CoreAnnotations.TokensAnnotation.class);
      for (CoreLabel token : tokens) {
         String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
         System.out.println(token.word() + " " + token.lemma() + " " + pos);
      }
   }
}
