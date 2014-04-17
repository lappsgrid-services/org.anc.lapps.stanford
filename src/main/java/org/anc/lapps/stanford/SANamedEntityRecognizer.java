package org.anc.lapps.stanford;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.anc.lapps.stanford.util.StanfordUtils;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;

public class SANamedEntityRecognizer implements WebService
{
   public static final int POOL_SIZE = 2;

   /* How long to wait for a processing thread to become available to service an incoming request. */
   public static final long DELAY = 5;
   public static final TimeUnit UNIT = TimeUnit.SECONDS;

   private static final Logger logger = LoggerFactory.getLogger(SANamedEntityRecognizer.class);

   private static final String classifierPath = Constants.PATH.NER_MODEL_PATH;

   //protected AbstractSequenceClassifier classifier;
   protected BlockingQueue<AbstractSequenceClassifier> pool;
   protected Throwable savedException = null;
   protected String exceptionMessage = null;

   public SANamedEntityRecognizer()
   {
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

      long type = input.getDiscriminator();
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
         String invalidNer = "O";
         for (CoreLabel label : classifiedLabels)
         {
            String ner = label.get(AnswerAnnotation.class);
            if (!ner.equals(invalidNer))
            {
               label.setNER(ner);
            }
         }

         ProcessingStep step = Converter.addTokens(new ProcessingStep(), labels);
         String producer = this.getClass().getName() + ":" + Version.getVersion();
         step.addContains(Annotations.NE, producer, "ner:stanford");
//         Map<String, String> metadata = step.getMetadata();
//         metadata.put(Metadata.PRODUCED_BY, name);
//         metadata.put(Metadata.CONTAINS, Annotations.NE);
         container.getSteps().add(step);
      }
      data = DataFactory.json(container.toJson());
      
      return data;
   }

   @Override
   public long[] requires()
   {
      return new long[] { Types.JSON, Types.TOKEN, Types.POS };
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.JSON, Types.NAMED_ENTITES};
   }

   @Override
   public Data configure(Data arg0)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
