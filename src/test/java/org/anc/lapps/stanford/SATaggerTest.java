package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;

import org.anc.lapps.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

public class SATaggerTest
{
   WebService service;
   
   @Before
   public void setup()
   {
      this.service = new SATagger();
   }
   
   @After
   public void tearDown()
   {
      this.service = null;
   }

   @Test
   public void testTagger() throws IOException
   {
      long ticks = System.nanoTime();
      String text = ResourceLoader.loadString("Bartok.txt");
      Data input = DataFactory.text(text);
      WebService tokenizer = new SATokenizer();
      Data tokenized = tokenizer.execute(input);
      
      Data result = service.execute(tokenized);
      long resultType = result.getDiscriminator();
      String payload = result.getPayload();

      assertTrue(payload, resultType != Types.ERROR);
      assertTrue("Expected JSON", resultType == Types.JSON);
      System.out.println("Time spent : " + (System.nanoTime() - ticks)/1e9 + "s");
   }

}
