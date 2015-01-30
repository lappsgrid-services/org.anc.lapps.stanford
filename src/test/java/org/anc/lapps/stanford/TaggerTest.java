package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.List;

import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Constants;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.anc.resource.ResourceLoader;
import org.junit.*;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.serialization.lif.Contains;
import org.lappsgrid.serialization.lif.View;

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
      String input = DataFactory.text(text);
      WebService tokenizer = new Tokenizer();
      String json = tokenizer.execute(input);

      json = service.execute(json);
      Data<Map> data = Serializer.parse(json, Data.class);
      assertFalse(data.getPayload().toString(), TestUtils.isError(data));
      Container container = new Container(data.getPayload());

      List<View> views = container.getViews();
      assertTrue("Wrong number of views. Expected 1 found " + views.size(), views.size() == 1);
      views = container.findViewsThatContain(Constants.Uri.TOKEN);
      assertNotNull("Find token views returned null.", views);
      assertTrue("No tokens found", views.size() > 0);
      assertTrue("Too many token views found", views.size() == 1);

      views = container.findViewsThatContain(Constants.Uri.POS);
      assertNotNull("Find pos views returned null", views);
      assertTrue("No tokens w/ pos found", views.size() > 0);
      assertTrue("Too many token w/ pos views found", views.size() == 1);

      View view = views.get(0);
      Contains contains = view.getContains(Constants.Uri.POS);
      assertNotNull("Unable to get contains section from view", contains);
      assertNotNull("contains.producer is null.", contains.getProducer());


   }

}
