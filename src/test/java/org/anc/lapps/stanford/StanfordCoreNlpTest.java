package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.anc.resource.ResourceLoader;
import org.junit.*;

import java.io.IOException;
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
   public void testLemmatizer() throws IOException
   {
      String text = ResourceLoader.loadString("Bartok.txt");
      Properties properties = new Properties();
      properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
      StanfordCoreNLP stanford = new StanfordCoreNLP(properties);
      Annotation a = new Annotation(text);
      stanford.annotate(a);
      List<CoreLabel> tokens = a.get(CoreAnnotations.TokensAnnotation.class);
      printNER(tokens);
//      String ner = a.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//      System.out.println(ner);
   }

   void printTokens(List<CoreLabel> labels)
   {
      for (CoreLabel token : labels) {
         String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
         System.out.println(token.word() + " " + token.lemma() + " " + pos);
      }
   }

   void printNER(List<CoreLabel> labels)
   {
      for (CoreLabel token : labels) {
         String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
			System.out.println("Word  : " + token.word());
			System.out.println("Lemma : " + token.lemma());
			System.out.println("POS   : " + pos);
			System.out.println("Category: " + token.category());
			System.out.println("NER(1)  : " + token.ner());
			System.out.println("NER(2)  : " + ner);
			System.out.println();
		}
   }
}
