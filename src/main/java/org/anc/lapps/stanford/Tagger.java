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
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.anc.lapps.stanford.util.Converter;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.annotations.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.*;
import org.lappsgrid.vocabulary.Contents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.lappsgrid.discriminator.Discriminators.Uri;

@ServiceMetadata(
        name = "Stanford Tagger",
        description = "Stanford Part of Speech Tagger",
        requires = {"token"},
        produces = {"pos"}
)
public class Tagger extends AbstractStanfordService
{
   /** The number of processing threads to create. */
   public static final int POOL_SIZE = 1;
   /** The time to wait for a processing thead to become available to service incoming requests. */
   public static final long DELAY = 5;
   public static final TimeUnit UNIT = TimeUnit.SECONDS;

   private static final Logger logger = LoggerFactory.getLogger(Tagger.class);

//   private MaxentTagger tagger;
   private BlockingQueue<MaxentTagger> pool;

   public Tagger() //throws LappsException
   {
      super(Tagger.class);
      logger.info("Creating the MaxentTagger");
      pool = new ArrayBlockingQueue<MaxentTagger>(POOL_SIZE);
      for (int i = 0; i < POOL_SIZE; ++i)
      {
         pool.add(new MaxentTagger(Constants.PATH.TAGGER_MODEL_PATH));
      }
   }
   
   @Override
   public String execute(String input)
   {
      logger.info("Executing Stanford stand-alone Tagger.");
//      logger.info("Tagger is using model english-bidirectional-distsim.");
//      Container container = createContainer(input);
//      if (container == null)
//      {
//         return input;
//      }

      Data<Map> data = Serializer.parse(input, Data.class);
      if (data == null) {
         return DataFactory.error("Unable to parse input.");
      }

      String discriminator = data.getDiscriminator();
      if (discriminator == null)
      {
         return createError(Messages.MISSING_DISCRIMINATOR);
      }
		logger.info("Discriminator is {}", discriminator);
      Container container = null;
      String json = null;

      switch(discriminator)
      {
         case Uri.ERROR:
            json = input;
            break;
         case Uri.GETMETADATA:
            json = super.getMetadata();
            break;
         case Uri.LAPPS: // fall through
         case Uri.JSON:
         case Uri.JSON_LD:
            container = new Container(data.getPayload());
            if (container == null)
            {
               return createError(Messages.MISSING_PAYLOAD);
            }
            break;
         default:
            json = createError(Messages.UNSUPPORTED_INPUT_TYPE + discriminator);
      }
      if (json != null)
      {
         return json;
      }

//      List<View> steps = container.getViews();
//		View tokenStep = StanfordUtils.findStep(steps, Annotations.TOKEN);
//
//      if (tokenStep == null)
//      {
//         logger.warn("No tokens were found in any processing step");
//         return createError("Unable to process input; no tokens found.");
//      }
      List views = container.findViewsThatContain(Uri.TOKEN);
      if (views == null || views.size() == 0)
      {
         logger.warn("No tokens were found in any views.");
         return createError("Unable to process input: no tokens found");
      }
      View tokenStep = new View((Map)views.get(0));
      List<Annotation> annotations = tokenStep.getAnnotations();
      List<CoreLabel> labels = new ArrayList<CoreLabel>();
      for (Annotation a : annotations)
      {
         labels.add(new LappsCoreLabel(a));
      }

      MaxentTagger tagger = null;
      try
      {
//         tagger = pool.take();
         tagger = pool.poll(DELAY, UNIT);
         if (tagger == null)
         {
            logger.warn("The Stanford tagger was unable to respond in a timely fashion.");
            return createError(Messages.BUSY);
         }
         tagger.tagCoreLabels(labels);
      }
      catch (Exception e)
      {
         StringWriter stringWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(stringWriter);
         writer.println("Unable to run the stanford tagger.");
         e.printStackTrace(writer);
         return createError(stringWriter.toString());
      }
      finally
      {
         if (tagger != null)
         {
            pool.add(tagger);
         }
      }

		View step = Converter.addTokens(new View(), labels);
      String producer = this.getClass().getName() + ":" + Version.getVersion();
      step.addContains(Uri.POS, producer, Contents.TagSets.PENN);
      container.getViews().add(step);

//      data.setDiscriminator(Constants.Uri.JSON_LD);
//      data.setPayload(container);
//      return data.asJson();
      return new Data<Container>(Uri.LAPPS, container).asJson();
   }
   
//   @Override
//   public Data configure(Data arg0)
//   {
//      return DataFactory.error("Unsupported operation.");
//   }


   public static void main(String[] args) throws IOException, LappsException
   {
      WebService tokenizer = new Tokenizer();
      String inputText = ResourceLoader.loadString("blog-jet-lag.txt");
      Data<String> data = new Data<String>();
      data.setDiscriminator(Uri.TEXT);
      data.setPayload(inputText);
      String tokenizerInput = Serializer.toJson(data);
      String tokenizerResult = tokenizer.execute(tokenizerInput);

//      Map<String,String> map = Serializer.parse(tokenizerResult, HashMap.class);

      WebService service = new Tagger();
      String result = service.execute(tokenizerResult);
//      Container container = new Container();
      Map<String,String> map = Serializer.parse(result, HashMap.class);
      System.out.println(map.get("discriminator"));
      Container container = Serializer.parse(map.get("payload"), Container.class);
      File output = new File("src/main/resources/blog-jet-lag_tagged.json");
      PrintWriter out = new PrintWriter(output);
      out.println(Serializer.toPrettyJson(container));
      out.close();
   }
}
