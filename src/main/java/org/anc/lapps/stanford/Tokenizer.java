package org.anc.lapps.stanford;

import org.lappsgrid.discriminator.Types;

/**
 * @author Keith Suderman
 */
public class Tokenizer extends AbstractStanfordService
{
    public Tokenizer()
    {
        super("tokenize");
    }

    @Override
    public long[] requires()
    {
        return new long[] {Types.STANFORD, Types.SENTENCE };
    }

    @Override
    public long[] produces()
    {
        return new long[] { Types.STANFORD, Types.SENTENCE, Types.TOKEN };
    }
}
