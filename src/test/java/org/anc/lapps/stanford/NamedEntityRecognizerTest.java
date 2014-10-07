package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;

import org.lappsgrid.serialization.Container;
import org.anc.resource.ResourceLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Discriminator;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

public class NamedEntityRecognizerTest
{

   @Ignore
   public void testSANamedEntityRecognizer() throws IOException, LappsException
   {
      String text = ResourceLoader.loadString("Bartok.txt");
      
      WebService tokenizer = new Tokenizer();
      Data input = DataFactory.text(text);
      Data tokenized = tokenizer.execute(input);
      
      WebService tagger = new Tagger();
      Data tagged = tagger.execute(tokenized);
      
      WebService ner = new NamedEntityRecognizer();
      Data result = ner.execute(tagged);
      long resultType = DiscriminatorRegistry.get(result.getDiscriminator());
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
      
      service = new NamedEntityRecognizer();
      result = service.execute(input);
      System.out.println(result.getPayload());
   }

   @Test
   public void testNer() throws IOException
   {
      String taggedText = ResourceLoader.loadString("TaggedText.json");
      Data input = DataFactory.json(taggedText);
      WebService service = new NamedEntityRecognizer();
      Data result = service.execute(input);
      long resultType = DiscriminatorRegistry.get(result.getDiscriminator());
      assertTrue(result.getPayload(), resultType != Types.ERROR);
      System.out.println(result.getPayload());
   }
}
