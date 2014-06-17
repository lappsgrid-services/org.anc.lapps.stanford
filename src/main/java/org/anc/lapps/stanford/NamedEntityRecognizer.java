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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.ling.CoreAnnotations;
import org.anc.lapps.serialization.Annotation;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.StanfordUtils;
import org.anc.util.IDGenerator;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class NamedEntityRecognizer extends AbstractStanfordService
{
   public static final int POOL_SIZE = 1;

   /* How long to wait for a processing thread to become available to service an incoming request. */
   public static final long DELAY = 5;
   public static final TimeUnit UNIT = TimeUnit.SECONDS;

   private static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);

   private static final String classifierPath = Constants.PATH.NER_MODEL_PATH;

   //protected AbstractSequenceClassifier classifier;
   protected BlockingQueue<AbstractSequenceClassifier> pool;
   protected Throwable savedException = null;
   protected String exceptionMessage = null;

   public NamedEntityRecognizer()
   {
      super(NamedEntityRecognizer.class);
      pool = new ArrayBlockingQueue<AbstractSequenceClassifier>(POOL_SIZE);
      try
      {
//         classifier = CRFClassifier.getClassifier(classifierPath);
         for (int i=0; i < POOL_SIZE; ++i)
         {
            pool.add(CRFClassifier.getClassifier(classifierPath));
         }
         logger.info("Stanford Stand-Alone Named-Entity Recognizer created.");
      }
      catch (OutOfMemoryError e)
      {
         logger.error("Ran out of memory creating the CRFClassifier.", e);
         savedException = e;
      }
      catch (Exception e)
      {
         logger.error("Unable to create the CRFClassifier.", e);
         savedException = e;
      }
   }
   
   @Override
   public Data execute(Data input)
   {
      // A savedException indicates there was a problem creating the CRFClassifier
      // object.
      if (savedException != null)
      {
         if (exceptionMessage == null)
         {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            writer.println(savedException.getMessage());
            savedException.printStackTrace(writer);
            exceptionMessage = stringWriter.toString();
         }
         return DataFactory.error(exceptionMessage);
      }

      long type = DiscriminatorRegistry.get(input.getDiscriminator());
      if (type == Types.ERROR)
      {
         return input;
      }
      if (type != Types.JSON)
      {
         String name = DiscriminatorRegistry.get(type);
         String message = "Invalid input type. Expected JSON but found " + name;
         logger.warn(message);
         return DataFactory.error(message);
      }

      logger.info("Executing Stanford Stand-Alone Named Entity Recognizer.");
      Container container = new Container(input.getPayload());
      Data data = null;
      
      List<CoreLabel> labels = StanfordUtils.getListOfTaggedCoreLabels(container);
      
      if (labels == null)
      {
         String message = "Unable to initialize a list of Stanford CoreLabels.";
         logger.warn(message);
         return DataFactory.error(message);
      }

      AbstractSequenceClassifier classifier = null;
      List<CoreLabel> classifiedLabels = null;
      try
      {
//         classifier = pool.take();
         classifier = pool.poll(DELAY, UNIT);
         if (classifier == null)
         {
            logger.warn(Messages.BUSY);
            return DataFactory.error(Messages.BUSY);
         }

         classifiedLabels = classifier.classify(labels);
      }
      catch (InterruptedException e)
      {
         //e.printStackTrace();
      }
      finally
      {
         if (classifier != null)
         {
            pool.add(classifier);
         }
      }
      if (classifiedLabels != null)
      {
         IDGenerator id = new IDGenerator();
         ProcessingStep step = new ProcessingStep();
         String invalidNer = "O";
         for (CoreLabel label : classifiedLabels)
         {
            String ner = label.get(AnswerAnnotation.class);
            if (!ner.equals(invalidNer))
            {
               Annotation annotation = new Annotation();
               annotation.setLabel(correctCase(ner));
               annotation.setId(id.generate("ne"));
               int start = (label.beginPosition());
               int end = (label.endPosition());
               annotation.setStart(start);
               annotation.setEnd(end);

               Map<String,String> features = annotation.getFeatures();
               add(features, Features.Token.LEMMA, label.lemma());
               add(features, "category", label.category());
               add(features, Features.Token.PART_OF_SPEECH, label.get(CoreAnnotations.PartOfSpeechAnnotation.class));

               add(features, "ner", label.ner());
               add(features, "word", label.word());
               step.addAnnotation(annotation);

            }
         }

         //ProcessingStep step = Converter.addTokens(new ProcessingStep(), labels);
         String producer = this.getClass().getName() + ":" + Version.getVersion();
         step.addContains(Annotations.NE, producer, "ner:stanford");
         container.getSteps().add(step);
      }
      data = DataFactory.json(container.toJson());
      
      return data;
   }

   private String correctCase(String item)
   {
      String head = item.substring(0, 1);
      String tail = item.substring(1).toLowerCase();
      return head + tail;
   }

   private void add(Map<String,String> features, String name, String value)
   {
      if (value != null)
      {
         features.put(name, value);
      }
   }

   @Override
   public Data configure(Data arg0)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
