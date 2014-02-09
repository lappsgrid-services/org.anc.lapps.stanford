package org.anc.lapps.stanford.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasOffset;
import edu.stanford.nlp.util.CoreMap;

import org.anc.lapps.serialization.Annotation;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.LappsCoreLabel;
import org.anc.util.IDGenerator;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Features;

import java.util.List;
import java.util.Map;

/**
 * @author Keith Suderman
 */
public class Converter
{
   public Converter()
   {

   }

   public static String toJson(List<CoreMap> sentences)
   {
//      return toPrettyJson(sentences);
      return toRawJson(sentences);
   }

   public static String toRawJson(List<CoreMap> sentences)
   {
      return toContainer(sentences).toJson();
   }

   public static String toPrettyJson(List<CoreMap> sentences)
   {
      return toContainer(sentences).toPrettyJson();
   }

   public static ProcessingStep getTokens(List<CoreLabel> tokens)
   {
      ProcessingStep step = new ProcessingStep();
      addTokens(step, tokens);
      return step;
   }

   public static ProcessingStep addTokens(ProcessingStep step, List<CoreLabel> tokens)
   {
      IDGenerator id = new IDGenerator();
      for (CoreLabel token : tokens)
      {
         Annotation annotation = new Annotation();
         annotation.setLabel(Annotations.TOKEN);
         annotation.setId(id.generate("tok"));
         int start = (token.beginPosition());
         int end = (token.endPosition());
//         String original = text.substring(start, end);
         annotation.setStart(start);
         annotation.setEnd(end);

         Map<String,String> features = annotation.getFeatures();
         add(features, Features.LEMMA, token.lemma());
         add(features, "category", token.category());
         add(features, Features.PART_OF_SPEECH, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));

         add(features, "ner", token.ner());
         add(features, "word", token.word());
//         add(features, "original", original);
         step.addAnnotation(annotation);
      }
      return step;
   }
   
   public static ProcessingStep addTokensWithIds(ProcessingStep step, List<LappsCoreLabel> tokens)
   {
      for (LappsCoreLabel token : tokens)
      {
         Annotation annotation = new Annotation();
         annotation.setLabel(Annotations.TOKEN);
         annotation.setId(token.id());
         int start = (token.beginPosition());
         int end = (token.endPosition());
//         String original = text.substring(start, end);
         annotation.setStart(start);
         annotation.setEnd(end);

         Map<String,String> features = annotation.getFeatures();
         add(features, Features.LEMMA, token.lemma());
         add(features, "category", token.category());
         add(features, Features.PART_OF_SPEECH, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));

         add(features, "ner", token.ner());
         add(features, "word", token.word());
//         add(features, "original", original);
         step.addAnnotation(annotation);
      }
      return step;
   }

   private static void add(Map<String,String> map, String name, String value)
   {
      if (value != null)
      {
         map.put(name, value);
      }
   }

   public static ProcessingStep addSentences(ProcessingStep step, List<CoreMap> sentences)
   {
      IDGenerator id = new IDGenerator();
      for (CoreMap sentence : sentences)
      {
         Annotation sentenceAnnotation = new Annotation();
         sentenceAnnotation.setId(id.generate("s"));
         sentenceAnnotation.setLabel(Annotations.SENTENCE);
         int start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
         int end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
         sentenceAnnotation.setStart(start);
         sentenceAnnotation.setEnd(end);
         step.addAnnotation(sentenceAnnotation);
      }
      return step;
   }

   public static Container toContainer(List<CoreMap> sentences)
   {
      Container container = new Container();
//      StringBuilder buffer = new StringBuilder();
      ProcessingStep step = new ProcessingStep();
      IDGenerator id = new IDGenerator();

      for(CoreMap sentence: sentences)
      {
         Annotation sentenceAnnotation = new Annotation();
         sentenceAnnotation.setId(id.generate("s"));
         sentenceAnnotation.setLabel(Annotations.SENTENCE);
         int start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
         int end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
         sentenceAnnotation.setStart(start);
         sentenceAnnotation.setEnd(end);

//         sentenceAnnotation.setStart(buffer.length());
         step.addAnnotation(sentenceAnnotation);

         // traversing the words in the current sentence
         // a CoreLabel is a CoreMap with additional token-specific methods
         for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
            // this is the text of the token
//            int start = buffer.length();
//            String word = token.get(CoreAnnotations.TextAnnotation.class);
//            buffer.append(word);
//            int end = buffer.length();
//            buffer.append(" ");

            Annotation tokenAnnotation = new Annotation();
            tokenAnnotation.setLabel(Annotations.TOKEN);
            tokenAnnotation.setId(id.generate("tok"));
            tokenAnnotation.setStart(token.beginPosition());
            tokenAnnotation.setEnd(token.endPosition());
            step.addAnnotation(tokenAnnotation);

            Map<String,String> features = tokenAnnotation.getFeatures();
            add(features, Features.LEMMA, token.lemma());
            add(features, Features.PART_OF_SPEECH, token.category());
            add(features, "ner", token.ner());
            add(features, "string", token.word());
            // this is the POS tag of the token
//            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//            if (pos != null)
//            {
//               features.put(Features.PART_OF_SPEECH, pos);
//            }
//
//            String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
//            if (lemma != null)
//            {
//               features.put(Features.LEMMA, lemma);
//            }
//            // this is the NER label of the token
//            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//            if (ne != null)
//            {
//               Annotation ner = new Annotation();
//               ner.setId(id.generate("ner"));
//               ner.setLabel(Annotations.translate(ne));
//               ner.setStart(start);
//               ner.setEnd(end);
//               step.addAnnotation(ner);
//            }
         }
//         sentenceAnnotation.setEnd(buffer.length());
//         buffer.append("\n");
      }
      container.getSteps().add(step);
//      container.setText(buffer.toString());
      return container;
   }
}
