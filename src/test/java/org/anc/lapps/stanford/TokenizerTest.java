package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;

import org.anc.lapps.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Discriminator;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

@Ignore
public class TokenizerTest
{
   private WebService service;

   public TokenizerTest()
   {

   }

   @Before
   public void setup()
   {
      service = new Tokenizer();
   }

   @After
   public void tearDown()
   {
      service = null;
   }

   @Test
   public void testTokenizer() throws IOException
   {
      WebService service = new Tokenizer();
      String inputText = ResourceLoader.loadString("Bartok.txt");
      Data input = DataFactory.text(inputText);
      Data result = service.execute(input);
      Discriminator discriminator = DiscriminatorRegistry.getByUri(result.getDiscriminator());
      long resultType = discriminator.getId();
      String payload = result.getPayload();

      assertTrue(payload, resultType != Types.ERROR);
      assertTrue("Expected JSON", resultType == Types.JSON);
      Container container = new Container(payload);
      System.out.println(container.toPrettyJson());
   }
}
