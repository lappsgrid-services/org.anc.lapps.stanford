package org.anc.lapps.stanford;

import org.anc.resource.ResourceLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
//import org.lappsgrid.discriminator.*;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.lappsgrid.discriminator.Constants.Uri;
import static org.junit.Assert.assertTrue;

public class NamedEntityRecognizerTest
{

   @Test
   public void testSANamedEntityRecognizer() throws IOException, LappsException
   {
      String text = ResourceLoader.loadString("Bartok.txt");
      String input = DataFactory.text(text);

      WebService tokenizer = new Tokenizer();
      WebService tagger = new Tagger();
      WebService ner = new NamedEntityRecognizer();

      String json = tokenizer.execute(input);
      json = tagger.execute(json);
      json = ner.execute(json);
      Data result = Serializer.parse(json, Data.class);
      assertFalse(result.getPayload().toString(), TestUtils.isError(result));

//      Data<String> data = new Data<>(Uri.TEXT, null, text);
//      data.setDiscriminator(Uri.TEXT);
//      data.setPayload(text);
//      Data<String> input = new Data<>(Uri.TEXT, null, text);
//      String json = Serializer.toJson(input);
//      String tokenized = tokenizer.execute(json);
//
//      WebService tagger = new Tagger();
//      String tagged = tagger.execute(tokenized);
//
//      WebService ner = new NamedEntityRecognizer();
//      String result = ner.execute(tagged);
//      Map<String,String> map = Serializer.parse(result, HashMap.class);
//      String discriminator = map.get("discriminator");
//      String payload = map.get("payload");
//
//      assertFalse(payload, Uri.ERROR.equals(discriminator));
//      assertTrue("Expected JSON", Uri.JSON.equals(discriminator));
////      System.out.println(payload);
//      Container container = Serializer.parse(payload, Container.class);
//      System.out.println(Serializer.toPrettyJson(container));
   }

//   @Ignore
//   public void testOnlyNER() throws IOException
//   {
//      WebService service;
//      Data input;
//      Data result;
//
//      String taggedText = ResourceLoader.loadString("blog-jet-lag_tagged.json");
//      input = DataFactory.json(taggedText);
//
//      service = new NamedEntityRecognizer();
//      result = service.execute(input);
//      System.out.println(result.getPayload());
//   }

//   @Test
//   public void testNer() throws IOException
//   {
//      String taggedText = ResourceLoader.loadString("TaggedText.json");
//      Data input = DataFactory.json(taggedText);
//      WebService service = new NamedEntityRecognizer();
//      Data result = service.execute(input);
//      long resultType = DiscriminatorRegistry.get(result.getDiscriminator());
//      assertTrue(result.getPayload(), resultType != Types.ERROR);
//      System.out.println(result.getPayload());
//   }
}
