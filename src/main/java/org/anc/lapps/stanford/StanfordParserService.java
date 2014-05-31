/*-
 * Copyright 2014 The Language Application Grid.
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

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StanfordParserService implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(StanfordParserService.class);

   protected static final int POOL_SIZE = 1;
   protected static final long DELAY = 5;
   protected static final TimeUnit UNIT = TimeUnit.SECONDS;

   //protected StanfordCoreNLP pipeline;
   protected BlockingQueue<StanfordCoreNLP> pool;
   protected Properties properties;

   public StanfordParserService()
   {
      logger.info("Initializing the Stanford Parser pool");
      pool = new ArrayBlockingQueue<StanfordCoreNLP>(POOL_SIZE);
      properties = new Properties();
      for (int i = 0; i < POOL_SIZE; ++i)
      {
         pool.add(new StanfordCoreNLP(properties));
      }
   }

   @Override
   public long[] requires()
   {
      return new long[] { Types.TEXT };
   }

   @Override
   public long[] produces()
   {
      return new long[] { Types.STANFORD };
   }

   @Override
   public Data execute(Data input)
   {
      if (input.getDiscriminator() != Types.TEXT)
      {
         String name = DiscriminatorRegistry.get(input.getDiscriminator());
         String message = "Invalid input type. Found: \"" + name +
                 "\" expected: \"text\"";
         return DataFactory.error(message);
      }
      StanfordCoreNLP pipeline = null;
      try
      {
         pipeline = pool.poll(DELAY, UNIT);
      }
      catch (InterruptedException ignored)
      {
         //e.printStackTrace();
      }
      if (pipeline == null)
      {
         return DataFactory.error(Messages.BUSY);
      }
      Annotation document = new Annotation(input.getPayload());
      pipeline.annotate(document);
      ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
      pipeline.prettyPrint(document, stream);
      return new Data(Types.STANFORD, stream.toByteArray());
   }

   @Override
   public Data configure(Data config)
   {
      if (config.getDiscriminator() != Types.TEXT)
      {
         String name = DiscriminatorRegistry.get(config.getDiscriminator());
         return DataFactory.error("Invalid parameter type. Found: \"" + name +
                 "\" expected: \"input-parameter\"");


      }
//      System.out.println("Setting annotators: " + config.getPayload());
      properties.put("annotators", config.getPayload());
      return DataFactory.ok();
   }

   private static long get(String name)
   {
      return DiscriminatorRegistry.get(name);
   }
}
