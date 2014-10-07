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
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.experimental.annotations.ServiceMetadata;
import org.lappsgrid.serialization.Container;
import org.lappsgrid.serialization.View;
import org.lappsgrid.vocabulary.Annotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

//   public static final long DELAY = 5;
//   public static final TimeUnit UNIT = TimeUnit.SECONDS;

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
   public Data execute(Data input)
   {
      logger.info("Executing Stanford sentence splitter.");
      Container container;
      long type = DiscriminatorRegistry.get(input.getDiscriminator());
      if (type == Types.TEXT)
      {
         container = new Container(false);
         container.setText(input.getPayload());
      }
      else if (type == Types.JSON)
      {
         container = new Container(input.getPayload());
      }
      else
      {
         String name = DiscriminatorRegistry.get(type);
         String message = "Invalid input type. Expected TEXT or JSON but found " + name;
         logger.warn(message);
         return DataFactory.error(message);
      }

      Annotation document = new Annotation(container.getText());
      Data data = null;
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
         data = DataFactory.json(container.toJson());
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
      return data;
   }

   public Data configure(Data input)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
