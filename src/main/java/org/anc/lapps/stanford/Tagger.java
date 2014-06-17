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

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.anc.lapps.serialization.*;
import org.anc.lapps.stanford.util.Converter;
import org.anc.lapps.stanford.util.StanfordUtils;
import org.anc.resource.ResourceLoader;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Contents;
import org.lappsgrid.vocabulary.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

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
   public Data execute(Data input)
   {
      logger.info("Executing Stanford stand-alone Tagger.");
//      logger.info("Tagger is using model english-bidirectional-distsim.");
//      Container container = createContainer(input);
//      if (container == null)
//      {
//         return input;
//      }
      long discriminator = DiscriminatorRegistry.get(input.getDiscriminator());
      if (discriminator == Types.ERROR)
      {
         return input;
      }
      if (discriminator != Types.JSON)
      {
         String name = DiscriminatorRegistry.get(discriminator);
         String message = "Invalid input type. Expected JSON but found " + name;
         logger.warn(message);
         return DataFactory.error(message);
      }

      Container container = new Container(input.getPayload());
      Data data = null;
      List<ProcessingStep> steps = container.getSteps();
      ProcessingStep tokenStep = StanfordUtils.findStep(steps, Annotations.TOKEN);

      if (tokenStep == null)
      {
         logger.warn("No tokens were found in any processing step");
         return DataFactory.error("Unable to process input; no tokens found.");
      }
      
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
            return DataFactory.error(Messages.BUSY);
         }
         tagger.tagCoreLabels(labels);
      }
      catch (Exception e)
      {
         StringWriter stringWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(stringWriter);
         writer.println("Unable to run the stanford tagger.");
         e.printStackTrace(writer);
         return DataFactory.error(stringWriter.toString());
      }
      finally
      {
         if (tagger != null)
         {
            pool.add(tagger);
         }
      }

      ProcessingStep step = Converter.addTokens(new ProcessingStep(), labels);
      String producer = this.getClass().getName() + ":" + Version.getVersion();
      step.addContains(Features.Token.PART_OF_SPEECH, producer, Contents.TagSets.PENN);
      container.getSteps().add(step);
      data = DataFactory.json(container.toJson());
      return data;
   }
   
   @Override
   public Data configure(Data arg0)
   {
      return DataFactory.error("Unsupported operation.");
   }

   protected Container createContainer(Data input)
   {
      Container container = null;
      long inputType = DiscriminatorRegistry.get(input.getDiscriminator());
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

   public static void main(String[] args) throws IOException, LappsException
   {
      WebService tokenizer = new Tokenizer();
      String inputText = ResourceLoader.loadString("blog-jet-lag.txt");
      Data tokenizerInput = DataFactory.text(inputText);
      Data tokenizerResult = tokenizer.execute(tokenizerInput);
      
      WebService service = new Tagger();
      Data result = service.execute(tokenizerResult);
      Container container = new Container(result.getPayload());
      
      File output = new File("src/main/resources/blog-jet-lag_tagged.json");
      PrintWriter out = new PrintWriter(output);
      out.println(container.toJson());
      out.close();
   }
}
