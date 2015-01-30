package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lappsgrid.discriminator.Constants;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.anc.resource.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.WebService;
import org.lappsgrid.serialization.lif.Contains;
import org.lappsgrid.serialization.lif.View;


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
      String text = ResourceLoader.loadString("Bartok.txt");
      Data<String> input = new Data<>(Constants.Uri.TEXT, null, text);
      String json = service.execute(Serializer.toJson(input));
      //System.out.println(json);
      Data<Object> result = Serializer.parse(json, Data.class);

      assertNotNull("Null result.", result);
      Object payload = result.getPayload();
      assertNotNull("Null payload.", payload);
      assertFalse(payload.toString(), isError(result));
      assertTrue("Expected JSON LD. Found " + result.getDiscriminator(), isa(result, Constants.Uri.JSON_LD));
      Container container = new Container((Map)payload);
      assertNotNull("No text in container", container.getText());
      int size = container.getViews().size();
      assertTrue("Expected one view. Found " + size, size == 1);
      Map metadata = container.getMetadata();
      assertNotNull("No metadata in container.", metadata);
      //assertFalse("Empty metadata in container.", metadata.size() == 0);
      View view = container.getView(0);
      assertNotNull("View is null.", view);
      metadata = view.getMetadata();
      assertNotNull("View metadata is null.", metadata);
      assertFalse("View metadata is empty.", metadata.size() == 0);
      Object map = metadata.get("contains");
      assertNotNull("No contains section in view metadata.", map);
      Contains contains = view.getContains(Constants.Uri.TOKEN);
      System.out.println(contains.getProducer());
//      Discriminator discriminator = DiscriminatorRegistry.getByUri(result.getDiscriminator());
//      long resultType = discriminator.getId();
//      String payload = result.getPayload();
//
//      assertTrue(payload, resultType != Types.ERROR);
//      assertTrue("Expected JSON", resultType == Types.JSON);
//      Container container = new Container(payload);
//      System.out.println(container.toPrettyJson());
   }

   public void testMetadata()
   {
      WebService tokenzier = new Tokenizer();
      Data<Void> command = new Data<Void>(Constants.Uri.GETMETADATA);

      String result = tokenzier.execute(Serializer.toJson(command));

   }

   private boolean isError(Data<?> data)
   {
      return isError(data.getDiscriminator());
   }

   private boolean isError(String url)
   {
      return Constants.Uri.ERROR.equals(url);
   }

   private boolean isa(Data<?> data, String type)
   {
      return isa(data.getDiscriminator(), type);
   }

   private boolean isa(String candidate, String type)
   {
      return type.equals(candidate);
   }
}
