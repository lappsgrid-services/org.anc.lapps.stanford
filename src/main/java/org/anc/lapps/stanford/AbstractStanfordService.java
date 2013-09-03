package org.anc.lapps.stanford;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Keith Suderman
 */
public abstract class AbstractStanfordService implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(AbstractStanfordService.class);
   protected static final int POOL_SIZE = 4;
//   protected StanfordCoreNLP service;
   protected BlockingQueue<StanfordCoreNLP> pool;

   public AbstractStanfordService(String annotators)
   {
      logger.info("Creating AbstractStanfordService with annotators: {}", annotators);
      Properties properties = new Properties();
      properties.setProperty("annotators", annotators);
      pool = new ArrayBlockingQueue<StanfordCoreNLP>(POOL_SIZE);
      for (int i = 0; i < POOL_SIZE; ++i)
      {
         pool.add(new StanfordCoreNLP(properties));
      }
   }

//   @Override
//   public Data execute(Data input)
//   {
//      Annotation document = new Annotation(input.getPayload());
//      service.annotate(document);
////      StringWriter stringWriter = new StringWriter();
////      PrintWriter printWriter = new PrintWriter(stringWriter);
////      service.prettyPrint(document, printWriter);
////      return new Data(Types.STANFORD, stringWriter.toString());
////      Annotation result = service.process(input.getPayload());
//      return new Data(Types.STANFORD, document.toString());
//   }

   @Override
   public Data configure(Data config)
   {
      return DataFactory.error("Unsupported operation.");
   }
}
