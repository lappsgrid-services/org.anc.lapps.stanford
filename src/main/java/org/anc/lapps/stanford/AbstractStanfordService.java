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
import org.anc.io.UTF8Reader;
import org.anc.lapps.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Discriminator;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.experimental.annotations.CommonMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
   protected Data metadata;

   public AbstractStanfordService(Class<?> serviceClass)
   {
      try
      {
         loadMetadata(serviceClass);
      }
      catch (IOException ignored)
      {
         // The only IOException not handled by loadMetadata is the one
         // thrown when closing the input stream, and by that point we
         // are good to go.
      }
   }

   private void loadMetadata(Class<?> serviceClass) throws IOException
   {
      ClassLoader loader = ResourceLoader.getClassLoader();
      String resourceName = "metadata/" + serviceClass.getName() + ".json";
      InputStream inputStream = loader.getResourceAsStream(resourceName);
      UTF8Reader reader = null;
      try
      {
         reader = new UTF8Reader(inputStream);
         String json = reader.readString();
         metadata = DataFactory.meta(json);
      }
      catch (IOException e)
      {
         metadata = DataFactory.error("Unable to load metadata from " + resourceName, e);
         throw e;
      }
      finally
      {
         if (reader != null)
         {
            reader.close();
         }
      }
   }


   public Data getMetadata()
   {
      return metadata;
   }
}
