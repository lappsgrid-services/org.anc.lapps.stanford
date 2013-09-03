package org.anc.lapps.stanford;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class StanfordParserService implements WebService
{
//   public static final long DOCUMENT = get("stanford");
//   public static final long PARAMETER = get("input-parameter");
//   public static final long SENTENCE = get("standford-sentence");
//   public static final long TOKEN = get("standford-sentence");
//   public static final long LEMMA = get("stanford-lemma");
    public static final long ERROR = Types.ERROR;
    public static final long OK = Types.OK;
    public static final long TEXT = Types.TEXT;
    public static final long STANFORD = Types.STANFORD;
    public static final long SENTENCE = Types.SENTENCE;
    public static final long TOKEN = Types.TOKEN;
    public static final long POS = Types.POS;

    protected StanfordCoreNLP pipeline;
    protected Properties properties;

    public StanfordParserService()
    {
        properties = new Properties();
    }

    @Override
    public long[] requires()
    {
        return new long[]{Types.TEXT};
    }

    @Override
    public long[] produces()
    {
        return new long[]{STANFORD};
    }

    @Override
    public Data execute(Data input)
    {
        if (input.getDiscriminator() != Types.TEXT)
        {
            String name = DiscriminatorRegistry.get(input.getDiscriminator());
            String message = "Invalid input type. Found: \"" + name +
                    "\" expected: \"text\"";
            return DataFactory.error(message);
        }
        if (pipeline == null)
        {
            pipeline = new StanfordCoreNLP(properties);
        }
        Annotation document = new Annotation(input.getPayload());
        pipeline.annotate(document);
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        pipeline.prettyPrint(document, stream);
        return new Data(STANFORD, stream.toByteArray());
    }

    @Override
    public Data configure(Data config)
    {
        if (config.getDiscriminator() != TEXT)
        {
            String name = DiscriminatorRegistry.get(config.getDiscriminator());
            return DataFactory.error("Invalid parameter type. Found: \"" + name +
                    "\" expected: \"input-parameter\"");


        }
//      System.out.println("Setting annotators: " + config.getPayload());
        properties.put("annotators", config.getPayload());
        return DataFactory.ok();
    }

    private static long get(String name)
    {
        return DiscriminatorRegistry.get(name);
    }
}
