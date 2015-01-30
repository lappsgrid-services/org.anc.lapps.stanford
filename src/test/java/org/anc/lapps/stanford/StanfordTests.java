package org.anc.lapps.stanford;

import org.anc.io.FileUtils;
import org.lappsgrid.serialization.*;
import org.anc.resource.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.api.WebService;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.vocabulary.Annotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lappsgrid.discriminator.Constants.Uri;
import static org.junit.Assert.assertTrue;

/**
 * @author Keith Suderman
 */

//@Ignore
public class StanfordTests
{
   protected String data;

   @Before
   public void setup()
   {
//      if (data != null)
//      {
//         return;
//      }
//      try
//      {
//         Container container = new Container();
//         container.setText(ResourceLoader.loadString("Bartok.txt"));
//         data = DataFactory.json(container.toJson());
//      }
//      catch (IOException e)
//      {
//         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//      }
   }

   @After
   public void tearDown()
   {
      data = null;
   }

   protected List<String> collect(Container container, String annotationType)
   {
      List<String> list = new ArrayList<String>();
//      for (View step : container.getViews())
//      {
//         for (Annotation annotation : step.getAnnotations())
//         {
//            if (annotationType.equals(annotation.getLabel()))
//            {
//               int start = annotation.getStart().intValue();
//               int end = annotation.getEnd().intValue();
//               String sentence = container.getText().substring(start, end);
//               list.add(sentence);
//            }
//         }
//      }
      return list;
   }

   @Test
   public void testSplitter()
   {
      WebService service = new SentenceSplitter();
//      Data result = service.execute(data);
//      String payload = result.getPayload();
//      long type = DiscriminatorRegistry.get(result.getDiscriminator());
//      assertTrue(payload, type != Types.ERROR);
//      assertTrue("Expected JSON", type == Types.JSON);
//      assertTrue("Payload is null", payload != null);
//      Container container = new Container(payload);
//      List<String> sentences = collect(container, Annotations.SENTENCE);
//
//      assertTrue("Sentence list is empty", sentences.size() > 0);
//      assertTrue("Expected 176 Found " + sentences.size(), sentences.size() == 176);
//
//      int count = 0;
//      for (String sentence : sentences)
//      {
//         ++count;
//         System.out.printf("%-2d: %s\n", count, sentence);
//      }
   }

   @Test
   public void testMetadata()
   {
      WebService service = new Tokenizer();
//      Data data = service.getMetadata();
//      assertTrue("Data is null.", data != null);
//      Discriminator discriminator = DiscriminatorRegistry.getByUri(data.getDiscriminator());
//      assertTrue(data.getPayload(), discriminator.getId() != Types.ERROR);
//      assertTrue("Unexpected discriminator: " + discriminator.getName(), discriminator.getId() == Types.META);
//      ServiceMetadata metadata = new ServiceMetadata(data.getPayload());
//      assertTrue("http://www.anc.org".equals(metadata.getVendor()));
   }

   @Ignore
   public void testTokenizer()
   {
      WebService service = new Tokenizer();
//      Data result = service.execute(data);
//      String payload = result.getPayload();
//      long type = DiscriminatorRegistry.get(result.getDiscriminator());
//      assertTrue(payload, type != Types.ERROR);
//      assertTrue("Expected JSON", type == Types.JSON);
//      Container container = new Container(payload);
//      System.out.println(container.toPrettyJson());
   }

   @Ignore
   public void testTagger()
   {
      WebService service = new Tagger();
//      Data result = service.execute(data);
//      long type = DiscriminatorRegistry.get(result.getDiscriminator());
//      String payload = result.getPayload();
//      assertTrue(payload, type != Types.ERROR);
//      assertTrue("Expected JSON", type == Types.JSON);
//      assertTrue("The payload is null", payload != null);
//      Container container = new Container(payload);
//      System.out.println(container.toPrettyJson());
   }

   @Ignore
   public void testNamedEntityRecognizer() throws IOException
   {
//      WebService service = new NamedEntityRecognizer();
//      Data result = service.execute(data);
//      long type = DiscriminatorRegistry.get(result.getDiscriminator());
//      String payload = result.getPayload();
//      assertTrue(payload, type != Types.ERROR);
//
//      FileUtils.write("/tmp/json.txt", payload);
   }

   private void test(WebService service)
   {
//      Data result = service.execute(data);
//      long type = DiscriminatorRegistry.get(result.getDiscriminator());
//      assertTrue(result.getPayload(), type != Types.ERROR);
//      System.out.println("Return type is " + type);
//      System.out.println("Payload: " + result.getPayload());
   }
}
