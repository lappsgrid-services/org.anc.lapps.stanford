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
import org.lappsgrid.experimental.annotations.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.*;
import org.lappsgrid.vocabulary.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Keith Suderman
 */
@ServiceMetadata(
        description = "Stanford Sentence Splitter",
        produces = "sentence"
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
      logger.info("Standford sentence splitter created.");
   }

   @Override
   public String execute(String input)
   {
      logger.info("Executing Stanford sentence splitter.");

      Map<String,String> map = Serializer.parse(input, HashMap.class);
      String discriminator = map.get("discriminator");
      if (discriminator == null)
      {
         return createError(Messages.MISSING_DISCRIMINATOR);
      }
      String payload = map.get("payload");
      if (payload == null)
      {
         return createError(Messages.MISSING_PAYLOAD);
      }

      Container container = null;
      String error = null;
      switch (discriminator)
      {
         case Constants.Uri.ERROR:
            error = input;
            break;
         case Constants.Uri.JSON: // fall through
         case Constants.Uri.JSON_LD:
            container = Serializer.parse(payload, Container.class);
            break;
         default:
            error = createError(Messages.UNSUPPORTED_INPUT_TYPE + discriminator);
      }
      if (error != null)
      {
         return error;
      }

      Annotation document = new Annotation(container.getText());
      Data<Container> data = new Data<Container>();
      StanfordCoreNLP service = null;
//      try
//      {
         //service = pool.take();
//         service = pool.poll(DELAY, UNIT);
//         if (service == null) {
//            logger.warn("The SentenceSplitter was unable to respond to a request in a timely fashion.");
//            return DataFactory.error(Messages.BUSY);
//         }

      service.annotate(document);
      List<CoreMap> sentences = document.get(SentencesAnnotation.class);
      View step = Converter.addSentences(new View(), sentences);
      String producer = this.getClass().getName() + ":" + Version.getVersion();
      step.addContains(Annotations.TOKEN, producer, "tokenization:stanford");
      step.addContains(Annotations.SENTENCE, producer, "chunk:sentence");
      container.getViews().add(step);
      data.setDiscriminator(Constants.Uri.JSON_LD);
      data.setPayload(container);
      //data = DataFactory.json(container.toJson());

//      }
//      catch (InterruptedException e)
//      {
//         data = DataFactory.error(e.getMessage());
//      }
//      finally
//      {
//         if (service != null)
//         {
//            pool.add(service);
//         }
//      }
      logger.info("Sentence splitter complete.");
      return Serializer.toJson(data);
   }

//   public Data configure(Data input)
//   {
//      return DataFactory.error("Unsupported operation.");
//   }
}
