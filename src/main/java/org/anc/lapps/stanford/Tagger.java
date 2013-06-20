package org.anc.lapps.stanford;

import org.lappsgrid.discriminator.Types;

/**
 * @author Keith Suderman
 */
public class Tagger extends AbstractStanfordService
{
    public Tagger()
    {
        super("tokenize, ssplit, pos");
    }

    @Override
    public long[] requires()
    {
        return new long[] {Types.STANFORD, Types.TOKEN };
    }

    @Override
    public long[] produces()
    {
        return new long[] {Types.STANFORD, Types.TOKEN, Types.POS };
    }
}
