package org.anc.lapps.stanford;

import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StanfordParserService implements WebService
{
   private static final Logger logger = LoggerFactory.getLogger(StanfordParserService.class);

   protected static final int POOL_SIZE = 4;
   protected static final long DELAY = 5;
   protected static final TimeUnit UNIT = TimeUnit.SECONDS;

   //protected StanfordCoreNLP pipeline;
   protected BlockingQueue<StanfordCoreNLP> pool;
   protected Properties properties;

   public StanfordParserService()
   {
      logger.info("Initializing the Stanford Parser pool");
      properties = new Properties();
      for (int i = 0; i < POOL_SIZE; ++i)
      {
         pool.add(new StanfordCoreNLP(properties));
      }
   }

   @Override
   public long[] requires()
   {
      return new long[] { Types.TEXT };
   }

   @Override
   public long[] produces()
   {
      return new long[] { Types.STANFORD };
   }

   @Override
   public Data execute(Data input)
   {
      if (input.getDiscriminator() != Types.TEXT)
      {
         String name = DiscriminatorRegistry.get(input.getDiscriminator());
         String message = "Invalid input type. Found: \"" + name +
                 "\" expected: \"text\"";
         return DataFactory.error(message);
      }
      StanfordCoreNLP pipeline = null;
      try
      {
         pipeline = pool.poll(DELAY, UNIT);
      }
      catch (InterruptedException ignored)
      {
         //e.printStackTrace();
      }
      if (pipeline == null)
      {
         return DataFactory.error(Messages.BUSY);
      }
      Annotation document = new Annotation(input.getPayload());
      pipeline.annotate(document);
      ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
      pipeline.prettyPrint(document, stream);
      return new Data(Types.STANFORD, stream.toByteArray());
   }

   @Override
   public Data configure(Data config)
   {
      if (config.getDiscriminator() != Types.TEXT)
      {
         String name = DiscriminatorRegistry.get(config.getDiscriminator());
         return DataFactory.error("Invalid parameter type. Found: \"" + name +
                 "\" expected: \"input-parameter\"");


      }
//      System.out.println("Setting annotators: " + config.getPayload());
      properties.put("annotators", config.getPayload());
      return DataFactory.ok();
   }

   private static long get(String name)
   {
      return DiscriminatorRegistry.get(name);
   }
}
