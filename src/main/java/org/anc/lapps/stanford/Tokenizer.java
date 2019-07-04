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

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import org.anc.lapps.stanford.util.Converter;
//import org.lappsgrid.discriminator.Constants;
import org.lappsgrid.annotations.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.DataContainer;
import org.lappsgrid.serialization.LifException;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lappsgrid.discriminator.Discriminators.*;

@ServiceMetadata(
        name = "Stanford Tokenizer",
        description = "Stanford Tokenizer",
        produces = "token",
        requires_format = { "lif", "text" }
)
public class Tokenizer extends AbstractStanfordService
{
   private static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);

   public Tokenizer()
   {
      super(Tokenizer.class);
   }

   @Override
   public String execute(String input)
   {
      logger.info("Executing Stanford stand-alone tokenizer");
      Container container = null;
      Data data = null;

      try {
         data = Serializer.parse(input);
      }
      catch (Exception e) {
         StringWriter writer = new StringWriter();
         e.printStackTrace(new PrintWriter(writer));
         data.setDiscriminator(Uri.ERROR);
         data.setPayload(writer.toString());
         return data.asJson();
      }

      if (Uri.ERROR.equals(data.getDiscriminator())) {
         return input;
      }

      String text = null;
      switch(data.getDiscriminator()) {
         case Uri.TEXT:
            text = data.getPayload().toString();
            container = new Container();
            container.setText(text);
            break;
         case Uri.LIF:
            container = new Container((Map)data.getPayload());
            text = container.getText();
            break;
         case Uri.GETMETADATA:
            return super.getMetadata();
         default:
            return createError(Messages.UNSUPPORTED_INPUT_TYPE + data.getDiscriminator());
      }

      List<CoreLabel> tokens = new ArrayList<CoreLabel>();
      PTBTokenizer ptbt = new PTBTokenizer(new StringReader(text), new CoreLabelTokenFactory(), "ptb3Escaping=false");
      for (CoreLabel label; ptbt.hasNext(); )
      {
         label = (CoreLabel) ptbt.next();
         tokens.add(label);
      }
      if (tokens.size() == 0)
      {
         return createError("PTBTokenizer returned no tokens.");
      }

      View view = null;
      try
      {
         view = container.newView();
      }
      catch (LifException e)
      {
         return createError("Unable to create a new view.");
      }
      view = Converter.addTokens(view, tokens);
      String producer = this.getClass().getName() + ":" + Version.getVersion();
      //TODO The type field should be set to something more appropriate.
      // See https://github.com/oanc/org.anc.lapps.stanford/issues/4
      view.addContains(Uri.TOKEN, producer, Contents.Tokenizations.STANFORD);
      data.setDiscriminator(Uri.LIF);
      data.setPayload(container);
      return Serializer.toJson(data);
   }
   
//   protected Container createContainer(Data input)
//   {
//      Container container = null;
//      long inputType = DiscriminatorRegistry.get(input.getDiscriminator());
//      if (inputType == Types.ERROR)
//      {
//         return null;
//      }
//      else if (inputType == Types.TEXT)
//      {
//         container = new Container();
//         container.setText(input.getPayload());
//      }
//      else if (inputType == Types.JSON)
//      {
//         container = new Container(input.getPayload());
//      }
//      return container;
//   }

//   @Override
//   public Data configure(Data arg0)
//   {
//      return DataFactory.error("Unsupported operation.");
//   }
}
