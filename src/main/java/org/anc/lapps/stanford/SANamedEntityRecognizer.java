package org.anc.lapps.stanford;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.anc.lapps.stanford.util.StanfordUtils;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
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
   private static final Logger logger = LoggerFactory.getLogger(SANamedEntityRecognizer.class);

   // TODO This path should not be hardcoded.
   private static final String classifierPath = Constants.PATH.NER_MODEL_PATH;

   protected AbstractSequenceClassifier classifier;
   protected Throwable savedException = null;
   protected String exceptionMessage = null;

   public SANamedEntityRecognizer()
   {
      try
      {
         classifier = CRFClassifier.getClassifier(classifierPath);
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

      logger.info("Executing Stanford Stand-Alone Named Entity Recognizer.");
      Container container = new Container(input.getPayload());
      Data data = null;
      
      List<CoreLabel> labels = StanfordUtils.getListOfTaggedCoreLabels(container);
      
      if (labels == null)
      {
         return DataFactory.error("Unable to initialize a list of Stanford CoreLabels.");
      }

      List<CoreLabel> classifiedLabels = classifier.classify(labels);
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
      String name = this.getClass().getName() + ":" + Version.getVersion();
      step.getMetadata().put(Metadata.PRODUCED_BY, name);
      step.getMetadata().put("contains", Annotations.NE);
      container.getSteps().add(step);
      data = DataFactory.json(container.toJson());
      
      return data;
   }

   @Override
   public long[] requires()
   {
      return new long[]{Types.POS};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.STANFORD, Types.NAMED_ENTITES};
   }

   @Override
   public Data configure(Data arg0)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
