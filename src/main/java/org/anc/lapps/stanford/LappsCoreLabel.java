package org.anc.lapps.stanford;

import java.util.Map;

import org.anc.lapps.serialization.Annotation;
import org.lappsgrid.vocabulary.Features;

import edu.stanford.nlp.ling.CoreLabel;

public class LappsCoreLabel extends CoreLabel
{
   private String id;
   
   LappsCoreLabel(String word, String id)
   {
      super();
      this.setWord(word);
      this.setId(id);
   }
   
   public LappsCoreLabel(Annotation a)
   {
      super();
      this.setWord((String) a.getFeatures().get("word"));
      this.setId(a.getId());
      this.setBeginPosition((int) a.getStart());
      this.setEndPosition((int) a.getEnd());
      
      Map features = a.getFeatures();
      if (features.get(Features.PART_OF_SPEECH) != null)
      {
         this.setTag((String) features.get(Features.PART_OF_SPEECH));
      }
   }

   public String id()
   {
      return this.id;
   }

   public void setId(String newId)
   {
      this.id = newId;
   }
   
   public String toString()
   {
      StringBuffer s = new StringBuffer();
      s.append("word:" + this.word() + ", ");
      s.append("id:" + this.id);
      if (this.tag() != null)
      {
         s.append(", tag:" + this.tag());
      }
      if (this.ner() != null)
      {
         s.append(", ner:" + this.ner());
      }
      return s.toString();
   }
}
