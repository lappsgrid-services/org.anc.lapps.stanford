package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;

import org.anc.lapps.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

@Ignore
public class SANamedEntityRecognizerTest
{

   @Test
   public void testSANamedEntityRecognizer() throws IOException, LappsException
   {
      String text = ResourceLoader.loadString("Bartok.txt");
      
      WebService tokenizer = new SATokenizer();
      Data input = DataFactory.text(text);
      Data tokenized = tokenizer.execute(input);
      
      WebService tagger = new SATagger();
      Data tagged = tagger.execute(tokenized);
      
      WebService ner = new SANamedEntityRecognizer();
      Data result = ner.execute(tagged);
      long resultType = result.getDiscriminator();
      String payload = result.getPayload();
      
      assertTrue(payload, resultType != Types.ERROR);
      assertTrue("Expected JSON", resultType == Types.JSON);
//      System.out.println(payload);
      Container container = new Container(payload);
      System.out.println(container.toPrettyJson());
   }

   @Ignore
   public void testOnlyNER() throws IOException
   {
      WebService service;
      Data input;
      Data result;
      
      String taggedText = ResourceLoader.loadString("blog-jet-lag_tagged.json");
      input = DataFactory.json(taggedText);
      
      service = new SANamedEntityRecognizer();
      result = service.execute(input);
      System.out.println(result.getPayload());
   }
}
