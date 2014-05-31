/*-
 * Copyright 2014 The Language Application Grid.
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

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Contents;
import static org.lappsgrid.vocabulary.Contents.TagSets;
import org.lappsgrid.vocabulary.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Keith Suderman
 */
public class Tagger extends AbstractStanfordService
{
   private static final Logger logger = LoggerFactory.getLogger(Tagger.class);

   public Tagger()
   {
      super("tokenize, ssplit, pos");
      logger.info("Stanford tagger created.");
   }

   @Override
   public long[] requires()
   {
      return new long[]{Types.TEXT};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.JSON, Types.TOKEN, Types.POS};
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford tagger.");
      Container container = createContainer(input);
      if (container == null)
      {
         return input;
      }
      String text = container.getText();
      Annotation document = new Annotation(text);
      Data data = null;
      StanfordCoreNLP service = null;
      try
      {
         service = pool.take();
         service.annotate(document);
         List<CoreLabel> tokens = document.get(TokensAnnotation.class);
         if (tokens == null)
         {
            return DataFactory.error("Stanford tokenizer returned null.");
         }
         ProcessingStep step = Converter.addTokens(new ProcessingStep(), tokens);
         //step.getMetadata().put("produced by", "Stanford Tagger");
         String producer = this.getClass().getName() + ":" + Version.getVersion();
         step.addContains(Features.Token.PART_OF_SPEECH, producer, TagSets.PENN);
//         step.getMetadata().put(Metadata.PRODUCED_BY, name);
//         step.getMetadata().put(Metadata.CONTAINS, Features.PART_OF_SPEECH);
                 container.getSteps().add(step);

         logger.info("Stanford tagger complete.");
         data = DataFactory.json(container.toJson());
      }
      catch (Exception e)
      {
         data = DataFactory.error(e.getMessage());
      }
      finally
      {
         if (service != null)
         {
            pool.add(service);
         }
      }
      return data;
   }
}
