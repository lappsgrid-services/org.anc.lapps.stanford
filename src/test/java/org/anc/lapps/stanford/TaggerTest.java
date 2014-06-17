package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;

import org.anc.lapps.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.junit.*;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

@Ignore
public class TaggerTest
{
   WebService service;
   
   @Before
   public void setup() throws LappsException
   {
      this.service = new Tagger();
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
      WebService tokenizer = new Tokenizer();
      Data tokenized = tokenizer.execute(input);
      
      Data result = service.execute(tokenized);
      long type = DiscriminatorRegistry.get(result.getDiscriminator());
      String payload = result.getPayload();

      assertTrue(payload, type != Types.ERROR);
      assertTrue("Expected JSON", type == Types.JSON);
      Container container = new Container(payload);
      System.out.println(container.toPrettyJson());
      System.out.println("Time spent : " + (System.nanoTime() - ticks)/1e9 + "s");
   }

}
