package org.anc.lapps.stanford;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lappsgrid.discriminator.Discriminators.Uri;

import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.DataContainer;
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

//@Ignore
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
      Data<String> input = new Data<>(Uri.TEXT, text);
      String json = service.execute(Serializer.toJson(input));
      //System.out.println(json);
      Data<Object> result = Serializer.parse(json, Data.class);

      assertNotNull("Null result.", result);
      Object payload = result.getPayload();
      assertNotNull("Null payload.", payload);
      assertFalse(payload.toString(), TestUtils.isError(result));
      assertTrue("Expected LIF. Found " + result.getDiscriminator(), TestUtils.isa(result, Uri.LAPPS));
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
      Contains contains = view.getContains(Uri.TOKEN);
//      System.out.println(contains.getProducer());
//      Discriminator discriminator = DiscriminatorRegistry.getByUri(result.getDiscriminator());
//      long resultType = discriminator.getId();
//      String payload = result.getPayload();
//
//      assertTrue(payload, resultType != Types.ERROR);
//      assertTrue("Expected JSON", resultType == Types.JSON);
//      Container container = new Container(payload);
//      System.out.println(container.toPrettyJson());
   }

   @Test
   public void testTokenizerWithLif()
   {
      Container container = new Container();
      container.setLanguage("en-US");
      container.setText("Goodbye cruel world, I am leaving you today.");
      DataContainer data = new DataContainer(container);
      WebService tokenizer = new Tokenizer();
      String json = tokenizer.execute(data.asJson());
      Data result = Serializer.parse(json, Data.class);
      assertFalse(result.getPayload().toString(), result.getDiscriminator().equals(Uri.ERROR));
      assertTrue("Wrong discriminator returned", result.getDiscriminator().equals(Uri.LAPPS));

   }

   @Test
   public void testMetadata()
   {
      WebService tokenizer = new Tokenizer();
      String result = tokenizer.getMetadata();
      assertNotNull("Tokenizer did not return metadata", result);
      Data<Object> data = Serializer.parse(result, Data.class);
      assertNotNull("Unable to parse metadata.", data);
      assertFalse(data.getPayload().toString(), TestUtils.isError(data));
      assertTrue("Wrong data type returned", TestUtils.isa(data, Uri.META));
      ServiceMetadata metadata = new ServiceMetadata((Map)data.getPayload());
      assertNotNull("Unable to parse metadata.", metadata);
//      TestUtils.check(Tokenizer.class.getName(), metadata.getName());
      TestUtils.check("http://www.anc.org", metadata.getVendor());
      TestUtils.check(Version.getVersion(), metadata.getVersion());

      IOSpecification io = metadata.getRequires();
      List<String> formats = io.getFormat();
      assert 2 == formats.size();
      assert formats.contains(Uri.TEXT);
      assert formats.contains(Uri.LIF);

      io = metadata.getProduces();
      formats = io.getFormat();
      assert 1 == formats.size();
      assert Uri.LIF.equals(formats.get(0));
   }

//   private void check(String expected, String actual)
//   {
//      if (!actual.equals(expected))
//      {
//         String message = String.format("Expected: %s Found %s", expected, actual);
//         fail(message);
//      }
//   }
//   private boolean isError(Data<?> data)
//   {
//      return isError(data.getDiscriminator());
//   }
//
//   private boolean isError(String url)
//   {
//      return Constants.Uri.ERROR.equals(url);
//   }
//
//   private boolean isa(Data<?> data, String type)
//   {
//      return isa(data.getDiscriminator(), type);
//   }
//
//   private boolean isa(String candidate, String type)
//   {
//      return type.equals(candidate);
//   }
}
