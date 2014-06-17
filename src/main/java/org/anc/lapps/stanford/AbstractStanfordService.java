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

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.anc.lapps.serialization.Container;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Discriminator;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.experimental.annotations.CommonMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Keith Suderman
 */
@CommonMetadata(
        vendor = "http://www.anc.org",
        allow = "any",
        format = "lapps",
        language = "en"
)
public abstract class AbstractStanfordService implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(AbstractStanfordService.class);
   protected static final int POOL_SIZE = 1;
//   protected StanfordCoreNLP service;
   protected BlockingQueue<StanfordCoreNLP> pool;

   public AbstractStanfordService(String annotators)
   {
      logger.info("Creating AbstractStanfordService with annotators: {}", annotators);
      Properties properties = new Properties();
      properties.setProperty("annotators", annotators);
      pool = new ArrayBlockingQueue<StanfordCoreNLP>(POOL_SIZE);
      for (int i = 0; i < POOL_SIZE; ++i)
      {
         pool.add(new StanfordCoreNLP(properties));
      }
   }

   @Override
   public Data configure(Data config)
   {
      return DataFactory.error("Unsupported operation.");
   }

   protected Container createContainer(Data input)
   {
      Container container = null;
      Discriminator discriminator = DiscriminatorRegistry.getByUri(input.getDiscriminator());
      if (discriminator.getId() == Types.ERROR)
      {
         return null;
      }
      else if (discriminator.getId() == Types.TEXT)
      {
         container = new Container(false);
         container.setText(input.getPayload());
      }
      else if (discriminator.getId() == Types.JSON)
      {
         container = new Container(input.getPayload());
      }
      return container;
   }
}
