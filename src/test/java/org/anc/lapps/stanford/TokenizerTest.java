package org.anc.lapps.stanford;

import org.anc.lapps.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Keith Suderman
 */
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
      String inputText = ResourceLoader.loadString("Bartok.txt");
      Data input = DataFactory.text(inputText);
      Data result = service.execute(input);
      long resultType = result.getDiscriminator();
      String payload = result.getPayload();

      assertTrue(payload, resultType != Types.ERROR);
      assertTrue("Expected JSON", resultType == Types.JSON);
      Container container = new Container(payload);
      System.out.println(container.toPrettyJson());
   }
}
