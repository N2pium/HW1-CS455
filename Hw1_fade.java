/*


*/

import framebuffer.FrameBuffer;

import java.awt.Color;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

/**
   Outline of CS 45500 Assignment 1.
*/
public class Hw1_fade
{
   public static void main(String[] args)
   {
      // Use a properties file to find out
      // which PPM files to use as assets.
      final Properties properties = new Properties();
      try ( final FileInputStream fis =
                     new FileInputStream(
                        new File("assets.properties")) )
      {
         properties.load(fis);
      }
      catch (IOException e)
      {
         e.printStackTrace(System.err);
         System.exit(-1);
      }

      final String file_1 = properties.getProperty("file1"); // 1st ppm file
      final String file_2 = properties.getProperty("file2"); // 2nd ppm file

      // This framebuffer holds the image that will be embedded
      // into two viewports of the larger framebuffer.
      final FrameBuffer fbEmbedded = new FrameBuffer(file_1);

      /******************************************/

      // Copy your code from Hw1.java here, except for the loops
      // that blend the Dumbledore image. Replace those loops with
      // the code below that creates the animation frames.


      // Create a FrameBuffer that holds Dumbledore.
      // Create a FrameBuffer that holds the background for Dumbledore.
      // Create a Viewport to hold the blended result.
      int count = 0;
      // Fade Dumbledore by 1% in each iteration.
      for (float fade = 1.0f; fade >= 0.0f; fade-=0.01f)
      {
         // Use fade to blend the Dumbledore FrameBuffer with
         // the background FrameBuffer and write the result
         // into the Viewport.

         /******************************************/
         // Save the resulting animation frame in a file.
         final String savedFileName = String.format("Hw1_fade_%03d.ppm", count);
         fb.dumpFB2File(savedFileName);
         System.err.println("Saved " + savedFileName);
         ++count;
      }
   }
}
