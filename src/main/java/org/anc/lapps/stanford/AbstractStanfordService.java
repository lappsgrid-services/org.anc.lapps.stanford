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
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.*;
import org.lappsgrid.annotations.CommonMetadata;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Error;
import org.lappsgrid.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * @author Keith Suderman
 */
@CommonMetadata(
	vendor = "http://www.anc.org",
	license = "gpl3",
	format = "lif",
	language = "en"
)
public abstract class AbstractStanfordService implements WebService
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractStanfordService.class);

   protected String metadata;

   public AbstractStanfordService(Class<?> serviceClass)
   {
      try
      {
         loadMetadata(serviceClass);
      }
      catch (IOException ignore)
      {
         // The only IOException not handled by loadMetadata is the one
         // thrown when closing the InputStream. All other IOExceptions
         // have already been logged.
//         logger.error("Unable to load metadata for {}", serviceClass.getName(), e);
      }
   }

   protected boolean isError(String discriminator)
   {
      return Uri.ERROR.equals(discriminator);
   }

   protected String createError(String message)
   {
      return new Error(message).asPrettyJson();
   }

   private InputStream openStream(Class<?> serviceClass)
   {
      String resourceName = "metadata/" + serviceClass.getName() + ".json";
//      InputStream inputStream = this.getClass().getResourceAsStream(resourceName);
      ClassLoader loader = AbstractStanfordService.class.getClassLoader();
      InputStream inputStream = loader.getResourceAsStream(resourceName);
      if (inputStream != null)
      {
         return inputStream;
      }
      return null;
//      return this.getClass().getClass().getResourceAsStream("/" + resourceName);
   }

   private void loadMetadata(Class<?> serviceClass) throws IOException
   {
//      ClassLoader loader = ResourceLoader.getClassLoader();
//      String resourceName = "/metadata/" + serviceClass.getName() + ".json";
//      InputStream inputStream = this.getClass().getResourceAsStream(resourceName);
      InputStream inputStream = openStream(serviceClass);
      if (inputStream == null)
      {
			String message = "Unable to load resource for " + serviceClass.getName();
			logger.error(message);
         throw new IOException(message);
      }

      UTF8Reader reader = null;
      try
      {
         reader = new UTF8Reader(inputStream);
         String content = reader.readString();
         ServiceMetadata metadata = Serializer.parse(content, ServiceMetadata.class);
         Data<ServiceMetadata> data = new Data<>(Uri.META, metadata);
         this.metadata = data.asJson();
			logger.info("Loaded metadata.");
//         System.out.println(content);
      }
      catch (IOException e)
      {
			String message = "Unable to load metadata for " + serviceClass.getName();
         logger.error(message, e);
         metadata = Serializer.toPrettyJson(new Error(message));
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


   public String getMetadata()
   {
      return metadata;
   }
}
