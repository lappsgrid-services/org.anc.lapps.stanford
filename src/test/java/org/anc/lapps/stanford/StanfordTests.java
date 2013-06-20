package org.anc.lapps.stanford;

import org.junit.*;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.utils.ResourceLoader;

import java.io.IOException;

import static  org.junit.Assert.*;

/**
 * @author Keith Suderman
 */
public class StanfordTests
{
    protected Data data;

    @Before
    public void setup()
    {
        if (data != null)
        {
            return;
        }
        ResourceLoader loader = new ResourceLoader();
        try
        {
            String text = loader.loadString("Bartok.txt");
            data = DataFactory.text(text);
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @After
    public void tearDown()
    {
        data = null;
    }

    @Test
    public void testSplitter()
    {
        test(new SentenceSplitter());
    }

    @Test
    public void testTokenizer()
    {
        test(new Tokenizer());
    }

    @Test
    public void testTagger()
    {
        test(new Tagger());
    }

    private void test(WebService service)
    {
        Data result = service.execute(data);
        assertTrue(result.getPayload(), result.getDiscriminator() != Types.ERROR);
        String type = DiscriminatorRegistry.get(result.getDiscriminator());
        System.out.println("Return type is " + type);
        System.out.println(result.getPayload());
    }
}
