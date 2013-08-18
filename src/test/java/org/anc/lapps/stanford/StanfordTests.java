package org.anc.lapps.stanford;

import org.anc.lapps.util.LappsUtils;
import org.junit.*;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import org.anc.resource.ResourceLoader;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Keith Suderman
 */
public class StanfordTests
{
   protected Data data;

   @Before
   public void setup()
   {
      if (data != null)
      {
         return;
      }
      try
      {
         String text = ResourceLoader.loadString("Bartok.txt");
         data = DataFactory.text(text);
      }
      catch (IOException e)
      {
         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
   }

   @After
   public void tearDown()
   {
      data = null;
   }

   @Test
   public void testSplitter()
   {
      WebService service = new SentenceSplitter();
      Data result = service.execute(data);
      String payload = result.getPayload();
      long type = result.getDiscriminator();
      assertTrue(payload, type != Types.ERROR);
      List<String> sentences = LappsUtils.parseStringList(payload);
      System.out.println("Sentences");
      int i = 0;
      for (String sentence : sentences)
      {
         System.out.println(++i + " " + sentence);
      }
   }

   @Test
   public void testTokenizer()
   {
      WebService service = new Tokenizer();
      Data result = service.execute(data);
      String payload = result.getPayload();
      long type = result.getDiscriminator();
      assertTrue(payload, type != Types.ERROR);
      List<String> tokens = LappsUtils.parseStringList(payload);
      System.out.println("Sentences");
      int i = 0;
      for (String token : tokens)
      {
         System.out.println(++i + " " + token);
      }
   }

   @Test
   public void testTagger()
   {
      WebService service = new Tagger();
      Data result = service.execute(data);
      String payload = result.getPayload();
      long type = result.getDiscriminator();
      assertTrue(payload, type != Types.ERROR);
      List<String> tokens = LappsUtils.parseStringList(payload);
      System.out.println("Sentences");
      int i = 0;
      for (String token : tokens)
      {
         System.out.println(++i + " " + token);
      }
   }

   private void test(WebService service)
   {
      Data result = service.execute(data);
      assertTrue(result.getPayload(), result.getDiscriminator() != Types.ERROR);
      String type = DiscriminatorRegistry.get(result.getDiscriminator());
      System.out.println("Return type is " + type);
      System.out.println("Payload: " + result.getPayload());
   }
}
