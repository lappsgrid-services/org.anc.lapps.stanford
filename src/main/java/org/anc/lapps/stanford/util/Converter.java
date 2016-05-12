/*-
 * Copyright 2014 The American National Corpus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.anc.lapps.stanford.util;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import org.anc.lapps.stanford.LappsCoreLabel;
import org.anc.util.IDGenerator;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.*;
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
      return Serializer.toJson(toContainer(sentences));
   }

   public static String toPrettyJson(List<CoreMap> sentences)
   {
      return Serializer.toPrettyJson(toContainer(sentences));
   }

   public static View getTokens(List<CoreLabel> tokens)
   {
		View step = new View();
      addTokens(step, tokens);
      return step;
   }

   public static View addTokens(View step, List<CoreLabel> tokens)
   {
      IDGenerator id = new IDGenerator();
      for (CoreLabel token : tokens)
      {
         Annotation annotation = new Annotation();
         annotation.setLabel("Token");
         annotation.setAtType(Discriminators.Uri.TOKEN);
         annotation.setId(id.generate("tok"));
         long start = (token.beginPosition());
         long end = (token.endPosition());
//         String original = text.substring(start, end);
         annotation.setStart(start);
         annotation.setEnd(end);

         Map<String,String> features = annotation.getFeatures();
         add(features, Features.Token.LEMMA, token.lemma());
         add(features, "category", token.category());
         add(features, Features.Token.POS, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));

         add(features, "ner", token.ner());
         add(features, "word", token.word());
//         add(features, "original", original);
         step.addAnnotation(annotation);
      }
      return step;
   }
   
   public static View addTokensWithIds(View step, List<LappsCoreLabel> tokens)
   {
      for (LappsCoreLabel token : tokens)
      {
         Annotation annotation = new Annotation();
         annotation.setLabel(Annotations.TOKEN);
         annotation.setId(token.id());
         long start = (token.beginPosition());
         long end = (token.endPosition());
//         String original = text.substring(start, end);
         annotation.setStart(start);
         annotation.setEnd(end);

         Map<String,String> features = annotation.getFeatures();
         add(features, Features.Token.LEMMA, token.lemma());
         add(features, "category", token.category());
         add(features, Features.Token.POS, token.get(CoreAnnotations.PartOfSpeechAnnotation.class));

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

   public static View addSentences(View step, List<CoreMap> sentences)
   {
      IDGenerator id = new IDGenerator();
      for (CoreMap sentence : sentences)
      {
         Annotation sentenceAnnotation = new Annotation();
         sentenceAnnotation.setId(id.generate("s"));
         sentenceAnnotation.setLabel("Sentence");
         sentenceAnnotation.setAtType(Discriminators.Uri.SENTENCE);
         long start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
         long end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
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
      View step = new View();
      IDGenerator id = new IDGenerator();

      for(CoreMap sentence: sentences)
      {
         Annotation sentenceAnnotation = new Annotation();
         sentenceAnnotation.setId(id.generate("s"));
         sentenceAnnotation.setLabel(Annotations.SENTENCE);
         long start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
         long end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
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
            tokenAnnotation.setStart((long)token.beginPosition());
            tokenAnnotation.setEnd((long)token.endPosition());
            step.addAnnotation(tokenAnnotation);

            Map<String,String> features = tokenAnnotation.getFeatures();
            add(features, Features.Token.LEMMA, token.lemma());
            add(features, Features.Token.POS, token.category());
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
      container.getViews().add(step);
//      container.setText(buffer.toString());
      return container;
   }
}
