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
