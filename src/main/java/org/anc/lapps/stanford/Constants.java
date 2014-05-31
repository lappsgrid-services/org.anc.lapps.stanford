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

import org.anc.lapps.stanford.util.PathConstants;

import java.io.IOException;

/**
 * @author Keith Suderman
 */
public class Constants
{
   public static final PathConstants PATH = new PathConstants();

   public Constants()
   {

   }

   // Used to generate the default properties file.
   public static void main(String[] args)
   {
      try
      {
         PATH.save();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
