package org.anc.lapps.stanford;

import org.anc.resource.ResourceLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
//import org.lappsgrid.discriminator.*;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.lappsgrid.discriminator.Discriminators.Uri;
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
      assertTrue("Invalid return type. Expected LIF found " + result.getDiscriminator(), TestUtils.isa(result, Uri.LAPPS));
      Container container = new Container((Map) result.getPayload());

      // Ensure the container contains the views that we think it should.
      List<View> views = container.getViews();
      assertTrue("Wrong number of views. Expected 3 found " + views.size(), views.size() == 3);
      views = container.findViewsThatContain(Uri.TOKEN);
      assertTrue("Wrong number of token views. Expected 1 found " + views.size(), views.size() == 1);

      views = container.findViewsThatContain(Uri.POS);
      assertTrue("Wrong number of pos views. Expected 1 found " + views.size(), views.size() == 1);

//      System.out.println(result.asPrettyJson());
      views = container.findViewsThatContain(Uri.NE);
      assertTrue("Wrong number of NE views. Expected 1 found " + views.size(), views.size() == 1);
   }

   @Test
   public void testMetadata()
   {
      WebService service = new NamedEntityRecognizer();
      String result = service.getMetadata();
      assertNotNull("NamedEntityRecognizer did not return metadata", result);
      Data<Object> data = Serializer.parse(result, Data.class);
      assertNotNull("Unable to parse metadata.", data);
      assertFalse(data.getPayload().toString(), TestUtils.isError(data));
      assertTrue("Wrong data type returned", TestUtils.isa(data, Uri.META));
      ServiceMetadata metadata = new ServiceMetadata((Map)data.getPayload());
      assertNotNull("Unable to parse metadata.", metadata);
//      TestUtils.check(NamedEntityRecognizer.class.getName(), metadata.getName());
      TestUtils.check("http://www.anc.org", metadata.getVendor());
      TestUtils.check(Version.getVersion(), metadata.getVersion());   }

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
