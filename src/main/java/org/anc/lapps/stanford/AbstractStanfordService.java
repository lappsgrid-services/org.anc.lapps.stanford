package org.anc.lapps.stanford;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.anc.lapps.serialization.Container;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Keith Suderman
 */
public abstract class AbstractStanfordService implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(AbstractStanfordService.class);
   protected static final int POOL_SIZE = 1;
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

   @Override
   public Data configure(Data config)
   {
      return DataFactory.error("Unsupported operation.");
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
         container = new Container(false);
         container.setText(input.getPayload());
      }
      else if (inputType == Types.JSON)
      {
         container = new Container(input.getPayload());
      }
      return container;
   }
}
