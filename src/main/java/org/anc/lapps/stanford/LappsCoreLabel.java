package org.anc.lapps.stanford;

import org.anc.lapps.serialization.Annotation;

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
   }
   
   public String id()
   {
      return this.id;
   }

   public void setId(String newId)
   {
      this.id = newId;
   }
}
