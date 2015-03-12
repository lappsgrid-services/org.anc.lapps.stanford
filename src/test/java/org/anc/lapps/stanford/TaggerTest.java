package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.List;

import org.lappsgrid.core.DataFactory;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.anc.resource.ResourceLoader;
import org.junit.*;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.serialization.lif.Contains;
import org.lappsgrid.serialization.lif.View;

import static org.lappsgrid.discriminator.Discriminators.Uri;

//@Ignore
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
      String text = ResourceLoader.loadString("Bartok.txt");
      String input = DataFactory.text(text);

      // The text needs to be tokenized before it is tagged.
      WebService tokenizer = new Tokenizer();
      String json = tokenizer.execute(input);

      // Call the tagger service and parse the output back into a Container object.
      json = service.execute(json);
      Data<Map> data = Serializer.parse(json, Data.class);

//      assertFalse(data.getPayload().toString(), TestUtils.isError(data));
      Container container = new Container(data.getPayload());

      List<View> views = container.getViews();
      assertTrue("Wrong number of views. Expected 2 found " + views.size(), views.size() == 2);
      int count = 0;
      for (View view : views)
      {
         ++count;
         System.out.println("View " + count + ": ");
         Map metadata = view.getMetadata();
         for (Object key : metadata.keySet())
         {
            Object value = metadata.get(key);
            System.out.println(key.toString() + "=" + value.toString());
         }
      }

      System.out.println(views.get(0));
      views = container.findViewsThatContain(Uri.TOKEN);
      assertNotNull("Find token views returned null.", views);
      assertTrue("Wrong number of token views. Expected 1 found " + views.size(), views.size() == 1);

      views = container.findViewsThatContain(Uri.POS);
      assertNotNull("Find pos views returned null", views);
      assertTrue("Wrong number of pos views. Expected 1 found " + views.size(), views.size() == 1);

      View view = views.get(0);
      Contains contains = view.getContains(Uri.POS);
      assertNotNull("Unable to get contains section from view", contains);
      assertNotNull("contains.producer is null.", contains.getProducer());
   }

   @Test
   public void testMetadata()
   {
      Data<Void> request = new Data<>(Uri.GETMETADATA);
      String json = service.execute(request.asJson());
      Data data = Serializer.parse(json, Data.class);
      assertTrue("Wrong return type: " + data.getDiscriminator(), TestUtils.isa(data, Uri.META));
      ServiceMetadata metadata = Serializer.parse(data.getPayload().toString(), ServiceMetadata.class);
      assertNotNull("Unable to parse metadata.", metadata);
      TestUtils.check(Tagger.class.getName(), metadata.getName());
      TestUtils.check(Version.getVersion(), metadata.getVersion());
      TestUtils.check("http://www.anc.org", metadata.getVendor());
   }
}
