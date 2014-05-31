/*-
 * Copyright 2014 The American National Corpus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.anc.lapps.stanford;

import java.util.Map;

import org.anc.lapps.serialization.Annotation;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Features;

import edu.stanford.nlp.ling.CoreLabel;

public class LappsCoreLabel extends CoreLabel
{
   private String id;
   
   public LappsCoreLabel(Annotation a)
   {
      super();
      this.setWord((String) a.getFeatures().get("word"));
      this.setId(a.getId());
      this.setBeginPosition((int) a.getStart());
      this.setEndPosition((int) a.getEnd());
      
      Map features = a.getFeatures();
      if (features.get(Features.Token.PART_OF_SPEECH) != null)
      {
         this.setTag((String) features.get(Features.Token.PART_OF_SPEECH));
      }
      if (features.get(Annotations.NE) != null)
      {
         this.setNER((String) features.get(Annotations.NE));
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
