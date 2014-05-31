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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.anc.lapps.serialization.Annotation;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

public class SATokenizer implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(SATokenizer.class);

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford stand-alone tokenizer");
      Container container = null;
      long type = input.getDiscriminator();
      if (type == Types.ERROR)
      {
         return input;
      }
      else if (type == Types.TEXT)
      {
         container = new Container(false);
         container.setText(input.getPayload());
      }
      else if (type == Types.JSON)
      {
         container = new Container(input.getPayload());
      }
      else {
         String typeName = DiscriminatorRegistry.get(type);
         String message = "Unknown discriminator type. Expected text or json. Found " + typeName;
         logger.warn(message);
         return DataFactory.error(message);
      }

      Data data = null;
      String text = container.getText();
      
      List<CoreLabel> tokens = new ArrayList<CoreLabel>();
      PTBTokenizer ptbt = new PTBTokenizer(new StringReader(text), new CoreLabelTokenFactory(), "ptb3Escaping=false");
      for (CoreLabel label; ptbt.hasNext(); )
      {
         label = (CoreLabel) ptbt.next();
         tokens.add(label);
      }
      if (tokens.size() == 0)
      {
         return DataFactory.error("PTBTokenizer returned no tokens.");
      }
      
      ProcessingStep step = Converter.addTokens(new ProcessingStep(), tokens);
      String producer = this.getClass().getName() + ":" + Version.getVersion();
      step.addContains(Annotations.TOKEN, producer, "stanford");
//      Map<String,String> metadata = step.getMetadata();
//      metadata.put(Metadata.PRODUCED_BY, name);
//      metadata.put(Metadata.CONTAINS, Annotations.TOKEN);
      container.getSteps().add(step);
      data = DataFactory.json(container.toJson());
      
      return data;
   }
   
   protected Container createContainer(Data input)
   {
      Container container = null;
      long inputType = input.getDiscriminator();
      if (inputType == Types.ERROR)
      {
         return null;
      }
      else if (inputType == Types.TEXT)
      {
         container = new Container();
         container.setText(input.getPayload());
      }
      else if (inputType == Types.JSON)
      {
         container = new Container(input.getPayload());
      }
      return container;
   }

   @Override
   public long[] requires()
   {
      return new long[] { Types.TEXT };
   }

   @Override
   public long[] produces()
   {
      return new long[] { Types.JSON, Types.TOKEN };
   }

   @Override
   public Data configure(Data arg0)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
