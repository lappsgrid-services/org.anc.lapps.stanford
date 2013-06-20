package org.anc.lapps.stanford;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.lappsgrid.discriminator.Types;

import java.util.Properties;

/**
 * @author Keith Suderman
 */
public class SentenceSplitter extends AbstractStanfordService
{
    public SentenceSplitter()
    {
        super("tokenize, ssplit");
    }

    @Override
    public long[] requires()
    {
        return new long[] {Types.TEXT };
    }

    @Override
    public long[] produces()
    {
        return new long[] { Types.STANFORD, Types.SENTENCE };
    }
}
