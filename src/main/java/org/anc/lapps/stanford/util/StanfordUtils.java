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
package org.anc.lapps.stanford.util;

import edu.stanford.nlp.ling.CoreLabel;
import org.anc.lapps.stanford.LappsCoreLabel;
//import org.lappsgrid.serialization.Annotation;
//import org.lappsgrid.serialization.Container;
//import org.lappsgrid.serialization.View;
import org.lappsgrid.discriminator.Constants;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StanfordUtils
{
   private static final Logger logger = LoggerFactory.getLogger(StanfordUtils.class);

   public static View findStep(List<View> steps, final String annotation)
   {
      for (View step : steps)
      {
         if (contains(step, annotation))
         {
            return step;
         }
      }
      return null;
   }

   public static boolean contains(View view, final String annotation)
   {
      Map metadata = view.getMetadata();
      Map contains = (Map) metadata.get("contains");
      return contains == null ? false : contains.get(annotation) != null;
//      if (contains.contains(annotation))
//      {
//         return true;
//      }
//      for (Annotation a : step.getAnnotations())
//      {
//         if (annotation.equals(a.getLabel()))
//         {
//            return true;
//         }
//      }
//      return false;
   }

   public static List<CoreLabel> getListOfTaggedCoreLabels(Container container)
   {
//      List<View> steps = container.getViews();
//      View taggedStep = StanfordUtils.findStep(steps, Features.Token.PART_OF_SPEECH);
//      if (taggedStep == null)
//      {
//         return null;
//      }
      List<View> views = container.findViewsThatContain(Constants.Uri.POS);
      List<CoreLabel> labels = new ArrayList<CoreLabel>();
      if (views == null || views.size() == 0) {
         return labels;
      }
      View taggedStep = views.get(0);
      List<Annotation> annotations = taggedStep.getAnnotations();
      for (Annotation a : annotations)
      {
         labels.add(new LappsCoreLabel(a));
      }
      return labels;
   }
}
