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
package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.anc.lapps.stanford.util.Converter;
import org.lappsgrid.discriminator.*;
import org.lappsgrid.annotations.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.DataContainer;
import org.lappsgrid.serialization.LifException;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.*;
import org.lappsgrid.vocabulary.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * @author Keith Suderman
 */
@ServiceMetadata(
        description = "Stanford Sentence Splitter",
        produces = { "sentence" },
        requires_format = { "text", "lif", "json" }
)
public class SentenceSplitter extends AbstractStanfordService
{
   private static final Logger logger = LoggerFactory.getLogger(SentenceSplitter.class);

   private StanfordCoreNLP service;

   public SentenceSplitter()
   {
      super(SentenceSplitter.class);
      Properties properties = new Properties();
      properties.setProperty("annotators", "tokenize, ssplit");
//      pool = new ArrayBlockingQueue<StanfordCoreNLP>(POOL_SIZE);
//      for (int i = 0; i < POOL_SIZE; ++i)
//      {
//         pool.add(new StanfordCoreNLP(properties));
//      }
      service = new StanfordCoreNLP(properties);
      if (service == null)
      {
         logger.error("Unable to create the StanfordCoreNLP object.");
      }
      else
      {
         logger.info("Standford sentence splitter created.");
      }
   }

   @Override
   public String execute(String input)
   {
      logger.info("Executing Stanford sentence splitter.");

      Data data = Serializer.parse(input, Data.class);
      if (data.getDiscriminator() == null)
      {
         return createError(Messages.MISSING_DISCRIMINATOR);
      }

      String discriminator = data.getDiscriminator();
      Container container = null;
      String json = null;
      switch (discriminator)
      {
         case Uri.ERROR:
            json = input;
            break;
         case Uri.GETMETADATA:
            json = super.getMetadata();
            break;
         case Uri.TEXT:
            if (data.getPayload() == null)
            {
               json = createError(Messages.MISSING_PAYLOAD);
            }
            else
            {
               container = new Container();
               container.setText(data.getPayload().toString());
            }
            break;
         case Uri.LAPPS: // fall through
         case Uri.JSON:
         case Uri.JSON_LD:
//            container = Serializer.parse(payload, Container.class);
            if (data.getPayload() == null)
            {
               json = createError(Messages.MISSING_PAYLOAD);
            }
            else
            {
               DataContainer dc = Serializer.parse(input, DataContainer.class);
               container = dc.getPayload();
//               container = new Container((Map) data.getPayload());
            }
            break;
         default:
            json = createError(Messages.UNSUPPORTED_INPUT_TYPE + discriminator);
            break;
      }
      if (json != null)
      {
         return json;
      }

      Annotation document = new Annotation(container.getText());

      if (service == null)
      {
         return createError("No service object has been instantiated.");
      }
      if (document == null)
      {
         return createError("Unable to create Stanford Annotation document for text.");
      }

      service.annotate(document);
      List<CoreMap> sentences = document.get(SentencesAnnotation.class);
      View view = null;
      try
      {
         view = container.newView();
      }
      catch (LifException e)
      {
         return createError("Unable to create new view. " + e.getMessage());
      }
      view = Converter.addSentences(view, sentences);
      String producer = this.getClass().getName() + ":" + Version.getVersion();
      view.addContains(Uri.SENTENCE, producer, "sentence:stanford");
      data.setDiscriminator(Uri.LAPPS);
      data.setPayload(container);
      logger.info("Sentence splitter complete.");
      return data.asJson();
   }

//   public Data configure(Data input)
//   {
//      return DataFactory.error("Unsupported operation.");
//   }
}
