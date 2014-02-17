package org.anc.lapps.stanford.util;

import org.anc.constants.Constants;

/**
 * @author Keith Suderman
 */
public class PathConstants extends Constants
{

   @Default("/usr/share/lapps/opennlp/classifiers/english.conll.4class.distsim.crf.ser.gz")
   public final String NER_MODEL_PATH = null;

   @Default("/usr/share/lapps/opennlp/models/english-bidirectional-distsim.tagger")
   public final String TAGGER_MODEL_PATH = null;

   public PathConstants()
   {
      super.init();
   }

}
