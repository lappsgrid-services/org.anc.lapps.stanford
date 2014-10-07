package org.anc.lapps.stanford;

import org.anc.io.FileUtils;
import org.anc.resource.ResourceLoader;
import org.junit.*;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.discriminator.Uri;
import org.lappsgrid.serialization.Container;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Keith Suderman
 */
@Ignore
public class NERTest
{
   private static String text;
   private WebService service;

   @BeforeClass
   public static void init() throws IOException
   {
      text = ResourceLoader.loadString("Bartok.txt");
   }

   @AfterClass
   public static void destroy()
   {
      text = null;
   }

   @Before
   public void setup() throws IOException
   {
      service = new NamedEntityRecognizer();
   }

   @After
   public void tearDown()
   {
      service = null;
   }

   public NERTest()
   {

   }

   @Test
   public void testExecute() throws IOException
   {
      System.out.println("org.anc.lapps.stanford.NERTest.testExecute");
      Data input = new Data(Uri.TEXT, text);
      Data result = service.execute(input);
      assertTrue("Result is null", result != null);

      long type = DiscriminatorRegistry.get(result.getDiscriminator());
      String payload = result.getPayload();
      assertTrue(payload != null);

      assertTrue(payload, Types.ERROR != type);
      assertTrue("Expected JSON", type == Types.JSON);

      Container container = new Container(payload);
      FileUtils.write("/tmp/Bartok.json", container.toJson());
      FileUtils.write("/tmp/Bartok-original.txt", text);
      System.out.println(container.toPrettyJson());
//      assertTrue("Container text is null.", container.getText() != null);
//      FileUtils.write("/tmp/Bartok-processed.txt", container.getText());
//      System.out.println("Wrote the Bartok files to /tmp");
//      System.out.println(container.toPrettyJson());
   }

   @Ignore
   public void testBartok() throws IOException
   {
      String json = FileUtils.read("/tmp/Bartok.json");
      if (json == null)
      {
         throw new IOException("Unable to load Bartok.json");
      }
      Container container = new Container(json);
      System.out.println(container.toPrettyJson());
//      for (ProcessingStep step : container.getSteps())
//      {
//         for (Annotation annotation : step.getAnnotations())
//         {
//            if (annotation.getId().startsWith("ner"))
//            {
//               int start = (int) annotation.getStart();
//               int end = (int) annotation.getEnd();
//               String label = annotation.getLabel();
//               String lexeme = container.getText().substring(start, end);
//               System.out.printf("%s %s %s\n", annotation.getId(), label, lexeme);
//            }
//         }
//      }
   }

   @Ignore
   public void testFalse()
   {
      System.out.println("org.anc.lapps.stanford.NERTest.testFalse");
      assertFalse(false);
   }
}
