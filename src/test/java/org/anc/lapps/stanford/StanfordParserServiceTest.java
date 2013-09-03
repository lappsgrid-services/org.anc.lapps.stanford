package org.anc.lapps.stanford;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.*;
import org.lappsgrid.api.*;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import org.anc.io.UTF8Reader;
import org.anc.io.UTF8Writer;

import static org.junit.Assert.*;

public class StanfordParserServiceTest
{
   private static final long OK = Types.OK;
   private static final long ERROR = Types.ERROR;
   private static final long PARAMETER = StanfordParserService.TEXT;
   private static final long DOCUMENT = StanfordParserService.STANFORD;

   @Test
   public void testData() throws IOException
   {
      System.out.println("StanfordParserServiceTest.testData");
      Data data = getData();
      String payload = data.getPayload();
      long type = data.getDiscriminator();
      String name = DiscriminatorRegistry.get(type);
      assertTrue("Expected text, found: " + name, type == Types.TEXT);
      assertTrue("Payload is null.", payload != null);
      assertTrue("Payload is empty.", payload.length() > 0);
      System.out.println(payload);
   }

   @Test
   public void testTokenize() throws IOException
   {
      System.out.println("StanfordParserServiceTest.testTokenize");
      save("tokenize.txt", test("tokenize"));

   }

   @Test
   public void testSentenceSplit() throws IOException
   {
      System.out.println("StanfordParserServiceTest.testSentenceSplit");
      save("tokenize-split.txt", test("tokenize, ssplit"));
   }

   @Ignore
   public void testTagger() throws IOException
   {
      System.out.println("StanfordParserServiceTest.testTagger");
      save("tagger", test("tokenize, ssplit, pos"));
   }

   @Ignore
   public void testAll() throws IOException
   {
      System.out.println("StanfordParserServiceTest.testAll");
//      save("all.txt", test("tokenize, ssplit, pos, lemma, ner, parse"));
      save("all.txt", test("tokenize, ssplit, pos, parse"));
   }

   protected Data test(String annotators) throws IOException
   {
      WebService service = new StanfordParserService();
      Data parameter = new Data(PARAMETER, annotators);
      Data result = service.configure(parameter);
      assertTrue(result.getDiscriminator() == OK);
      System.out.println("Executing pipeline " + annotators);
      result = service.execute(getData());
      assertTrue(result.getDiscriminator() == DOCUMENT);
      return result;
   }

   // Loads the test file into a Data object.
   protected Data getData() throws IOException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (loader == null)
      {
         loader = StanfordParserServiceTest.class.getClassLoader();
      }
      InputStream stream = loader.getResourceAsStream("Bartok.txt");
      assertTrue(stream != null);
      UTF8Reader reader = new UTF8Reader(stream);
      try
      {
         return new Data(Types.TEXT, reader.readString());
      }
      finally
      {
         reader.close();
      }
   }

   protected void save(String filename, Data data) throws IOException, UnsupportedEncodingException
   {
      System.out.println("Attempting to save " + filename);
      File directory = new File("target/test");
      if (!directory.exists())
      {
         if (!directory.mkdirs())
         {
            fail("Unable to create " + directory.getPath());
         }
      }
      File file = new File(directory, filename);
      UTF8Writer out = new UTF8Writer(file);
      try
      {
         System.out.println("Saving " + file.getPath());
         out.write(data.getPayload());
      }
      finally
      {
         out.close();
      }
   }

}
