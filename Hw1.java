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
public class Hw1
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
      // into two Viewports of the larger framebuffer.
      final FrameBuffer fbEmbedded = new FrameBuffer(file_1);

      /******************************************/

      // Your code goes here.
      //  1. Create a 1000-by-600 FrameBuffer filled with one of the checkerboard colors. <- DONE
      //  2. Draw into the FrameBuffer the squares with the other checkerboard color. <- DONE
      //  3. Draw the five nested square boxes. Do this using six nested Viewports. <- DONE
      //     (Save the pixels from the innermost square in a FrameBuffer.)
      //  4. Create a Viewport and draw into it the rectangle of diagonal stripes. <- DONE
      //  5. Create a Viewport and copy into it a flipped copy of the first ppm file. 
      //  6. Create another Viewport copy into it another flipped copy of the first ppm file. <- done i think
      //  7. Create a Viewport that covers the 6 checkerboard squares that need to be copied. <- DONE
      //  8. Create another Viewport to hold a "framed" copy of the previous Viewport. <- DONE
      //     Give this Viewport a grayish background color.
      //  9. Create another Viewport within the last, gray Viewport, and initialize
      //     it to hold a copy of the Viewport from step 7.
      // 10. Load Dumbledore (the second ppm file) into another FrameBuffer.
      // 11. Create a Viewport to hold Dumbledore's ghost.
      // 12. Blend Dumbledore from its FrameBuffer into the Viewport from step 11.

       // You need to construct an appropriate FrameBuffer object.
      final Color checkeredColor = new Color(255, 189, 96);   // lighter color for checkerboard
      final FrameBuffer fb = new FrameBuffer(1000, 600,checkeredColor); // You need to construct an appropriate FrameBuffer object. 
      final Color checkeredColor2 = new Color(192, 56, 11); // reddish color for checkerboard

      // Draw the 30 squares of the other checkerboard color.
      // The board is 10 squares wide by 6 squares tall, each 100-by-100 pixels.
      for (int row = 0; row < 6; ++row)
      {
         for (int col = 0; col < 10; ++col)
         {
            if ((row + col) % 2 == 1)
            {
               final FrameBuffer.Viewport square = fb.new Viewport(col * 100, row * 100, 100, 100);
               square.clearVP(checkeredColor2);
                
            }
         }
      }

      final FrameBuffer.Viewport embeddedViewport = fb.new Viewport(50,50, 100, 100);
      

      
      final Color blue = new Color(80, 100, 230); //saving for later use
      final FrameBuffer.Viewport blueViewport = fb.new Viewport(200, 400, 200, 200);
      blueViewport.clearVP(blue); 
      
      final Color green = new Color(150,200,70);
      final FrameBuffer.Viewport greenViewport = fb.new Viewport(210, 410, 180, 180); 
      greenViewport.clearVP(green);

      final Color red = new Color(240, 100, 120);
      final FrameBuffer.Viewport redViewport = fb.new Viewport(220, 420, 160, 160);
      redViewport.clearVP(red);

      final FrameBuffer.Viewport greenViewport2 = fb.new Viewport(230, 430, 140, 140);
      greenViewport2.clearVP(green);

      final FrameBuffer.Viewport blueViewport2 = fb.new Viewport(240, 440, 120, 120);
      blueViewport2.clearVP(blue);    

      final FrameBuffer.Viewport copyDat = fb.new Viewport(250, 450, embeddedViewport);

      
      final Color[] stripeColors = {red, green, blue}; //using the earlier colors

      final FrameBuffer.Viewport stripesViewport = fb.new Viewport(610, 420, 300, 120);
      stripesViewport.clearVP(Color.WHITE); //background


      for (int y = 0; y < stripesViewport.getHeightVP(); ++y)
      {
         for (int x = 0; x < stripesViewport.getWidthVP(); ++x)
         {
            final int stripeIndex = ((x + y) / 30) % 3;
            stripesViewport.setPixelVP(x, y, stripeColors[stripeIndex]);
         }
      }

      
      final int width = fbEmbedded.getWidthFB();
      final int height = fbEmbedded.getHeightFB();

      final FrameBuffer.Viewport flipViewport = fb.new Viewport(70, 120, width, height);
      for (int y = 0; y < height; ++y){

         for (int x = 0; x < width; ++x)
         {
            final Color pixelColor = fbEmbedded.getPixelFB(x, y);
            transparentVP(flipViewport, x, height - 1 - y, pixelColor); //vertical flip
         }
      }

      final FrameBuffer.Viewport troopVP = fb.new Viewport(340, 140, width, height); 
      transparentVP(troopVP, 340, 140, Color.WHITE); 
      
      



      







         
      



      /******************************************/
      // Save the resulting image in a file.
      final String savedFileName = "Hw1.ppm";
      fb.dumpFB2File( savedFileName );
      System.err.println("Saved " + savedFileName);
   }


   /**
      Set the pixel at ({@code x}, {@code y}) in {@code vp} to {@code pixelColor},
      unless {@code pixelColor} is near-white, in which case the pixel is left
      alone so whatever is already in the {@code Viewport} shows through.

      @param vp          {@code Viewport} to draw into
      @param x           horizontal coordinate within {@code vp}
      @param y           vertical coordinate within {@code vp}
      @param pixelColor  candidate {@link Color} to draw
   */
   private static void transparentVP(final FrameBuffer.Viewport vp,
                                     final int x, final int y,
                                     final Color pixelColor)
   {
      final int whiteThreshold = 250; // R,G,B all >= this counts as "background white"
      final boolean isNearWhite = pixelColor.getRed()   >= whiteThreshold
                                && pixelColor.getGreen() >= whiteThreshold
                                && pixelColor.getBlue()  >= whiteThreshold;
      if (!isNearWhite) // skip near-white pixels so the background shows through
      {
         vp.setPixelVP(x, y, pixelColor);
      }
   }
}
