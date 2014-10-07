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

import org.anc.io.UTF8Reader;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.experimental.annotations.CommonMetadata;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Keith Suderman
 */
@CommonMetadata(
	vendor = "http://www.anc.org",
	license = "apache2",
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
         // are good to go so we ignore it.
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
