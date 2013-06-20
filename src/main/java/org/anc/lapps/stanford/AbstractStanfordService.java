package org.anc.lapps.stanford;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

/**
 * @author Keith Suderman
 */
public abstract class AbstractStanfordService implements WebService
{
    protected StanfordCoreNLP service;

    public AbstractStanfordService(String annotators)
    {
        Properties properties = new Properties();
        properties.setProperty("annotators", annotators);
        service = new StanfordCoreNLP(properties);
    }

    @Override
    public Data execute(Data input)
    {
        Annotation document = new Annotation(input.getPayload());
        service.annotate(document);
        ByteArrayOutputStream stream = new ByteArrayOutputStream(4096);
        service.prettyPrint(document, stream);
        return new Data(Types.STANFORD, stream.toByteArray());
    }

    @Override
    public Data configure(Data config)
    {
        return DataFactory.ok();
    }
}
