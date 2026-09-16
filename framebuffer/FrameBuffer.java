/*
 * FrameBuffer. The MIT License.
 * Copyright (c) 2022 rlkraft@pnw.edu
 * See LICENSE for details.
*/

package framebuffer;

import java.awt.Color;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
    A {@code FrameBuffer} represents a two-dimensional array of pixel data.
    The pixel data is stored as a one dimensional array in row-major order.
    The first row of data should be displayed as the top row of pixels
    in the image.
<p>
    A {@link Viewport} is a two-dimensional sub array of a {@code FrameBuffer}.
<p>
    A {@code FrameBuffer} has a default {@link Viewport}. The current {@link Viewport}
    is represented by its upper-left-hand corner and its lower-right-hand
    corner.
<p>
    {@code FrameBuffer} and {@link Viewport} coordinates act like Java
    {@link java.awt.Graphics2D} coordinates; the positive x direction is
    to the right and the positive y direction is downward.
*/
public final class FrameBuffer
{
   public final int widthFB;  // framebuffer's width  (Java calls this a "blank final")
   public final int heightFB; // framebuffer's height (Java calls this a "blank final")
   public final int[] pixel_buffer; // contains each pixel's color data for a rendered frame
   public Color bgColorFB = Color.black; // default background color
   public final Viewport vp;             // default viewport (another "blank final")

   /**
      Construct a {@code FrameBuffer} with the given dimensions.
   <p>
      Initialize the {@code FrameBuffer} to hold all black pixels.
   <p>
      The default {@link Viewport} is the whole {@code FrameBuffer}.

      @param w  width of the {@code FrameBuffer}.
      @param h  height of the {@code FrameBuffer}.
   */
   public FrameBuffer(final int w, final int h) {
      this(w, h, Color.black);
   }


   /**
      Construct a {@code FrameBuffer} with the given dimensions.
   <p>
      Initialize the {@code FrameBuffer} to the given {@link Color}.
   <p>
      The default {@link Viewport} is the whole {@code FrameBuffer}.

      @param w  width of the {@code FrameBuffer}.
      @param h  height of the {@code FrameBuffer}.
      @param c  background {@link Color} for the {@code FrameBuffer}
   */
   public FrameBuffer(final int w, final int h, final Color c) {
      this.widthFB  = w; // fill in the "blank final"
      this.heightFB = h; // fill in the "blank final"

      // Create the pixel buffer (fill in the "blank final").
      this.pixel_buffer = new int[this.widthFB * this.heightFB];

      // Initialize the pixel buffer.
      this.bgColorFB = c;
      clearFB(c);

      // Create the default viewport.
      this.vp = this.new Viewport();
   }


   /**
      Create a {@code FraameBuffer} from the pixel data of another
      {@code FrameBuffer}.
   <p>
      The size of the new {@code FrameBuffer} will be the size of the
      source {@link FrameBuffer}.
   <p>
      The default {@link Viewport} is the whole {@code FrameBuffer}.

      @param sourceFB  {@link FrameBuffer} to use as the source of the pixel data
   */
   public FrameBuffer(final FrameBuffer sourceFB) {

      widthFB  = sourceFB.widthFB;  // fill in the "blank final"
      heightFB = sourceFB.heightFB; // fill in the "blank final"

      // Create the pixel buffer (fill in the "blank final").
      this.pixel_buffer = new int[widthFB * heightFB];

      // Read pixel data, one pixel at a time, from the source FrameBuffer.
      for (int y = 0; y < heightFB; ++y) {
         for (int x = 0; x < widthFB; ++x) {
            setPixelFB(x, y, sourceFB.getPixelFB(x,y));
         }
      }

      // Create the default viewport.
      this.vp = this.new Viewport();
   }


   /**
      Create a {@code FrameBuffer} from the pixel data of a {@link Viewport}.
   <p>
      The size of the new {@code FrameBuffer} will be the size of the
      source {@code Viewport}.
   <p>
      The default {@link Viewport} is the whole {@code FrameBuffer}.

      @param sourceVP  {@link Viewport} to use as the source of the pixel data
   */
   public FrameBuffer(final Viewport sourceVP) {

      widthFB  = sourceVP.getWidthVP();  // fill in the "blank final"
      heightFB = sourceVP.getHeightVP(); // fill in the "blank final"

      // Create the pixel buffer (fill in the "blank final").
      this.pixel_buffer = new int[widthFB * heightFB];

      // Read pixel data, one pixel at a time, from the source Viewport.
      for (int y = 0; y < heightFB; ++y) {
         for (int x = 0; x < widthFB; ++x) {
            setPixelFB(x, y, sourceVP.getPixelVP(x,y));
         }
      }

      // Create the default viewport.
      this.vp = this.new Viewport();
   }


   /**
      Construct a {@code FrameBuffer} from a PPM image file.
   <p>
      The size of the {@code FrameBuffer} will be the size of the image.
   <p>
      The default {@link Viewport} is the whole {@code FrameBuffer}.
   <p>
      This can be used to initialize a {@code FrameBuffer}
      with a background image.

      @param inputFileName  must name a PPM image file with magic number P6.
   */
   public FrameBuffer(final String inputFileName) {
      // Open the file to read its header data (meta data).
      Dimension fbDim = null;
      try (final var fis = new FileInputStream(inputFileName)) { // try with resources
         fbDim = getPPMdimensions(inputFileName, fis);
      }
      catch (IOException e) {
         System.err.printf("ERROR! Could not open %s\n", inputFileName);
         e.printStackTrace(System.err);
         System.exit(-1);
      }

      widthFB  = fbDim.width;  // fill in the "blank final"
      heightFB = fbDim.height; // fill in the "blank final"

      // Create the pixel buffer (fill in the "blank final").
      this.pixel_buffer = new int[widthFB * heightFB];

      // Reopen the file to read its pixel data and initialize the pixel-buffer.
      try (final var fis = new FileInputStream(inputFileName)) { // try with resources
         getPPMdimensions(inputFileName, fis); // skip over the meta data
         setPixels(0, 0, widthFB, heightFB, inputFileName, fis);
      }
      catch (IOException e) {
         System.err.printf("ERROR! Could not read %s\n", inputFileName);
         e.printStackTrace(System.err);
         System.exit(-1);
      }

      // Create the default viewport.
      this.vp = this.new Viewport();
   }


   /**
      Get the pixel data's dimensions from a PPM file.

      @param inputFile  must name a PPM image file with magic number P6
      @param fis        input stream to the PPM image file
      @throws IOException if there is a problem with {@code fis}
      @return a {@link Dimension} object holding the PPM file's width and height
   */
   private Dimension getPPMdimensions(final String inputFile, final FileInputStream fis)
   throws IOException {
   // Read the meta data in a PPM file.
   // http://stackoverflow.com/questions/2693631/read-ppm-file-and-store-it-in-an-array-coded-with-c
      // Read image format string "P6".
      String magicNumber = "";
      char c = (char)fis.read();
      while (c != '\n') {
         magicNumber += c;
         c = (char)fis.read();
      }
      if (! magicNumber.trim().startsWith("P6")) {
         System.err.printf("ERROR! Improper PPM number in file %s\n", inputFile);
         System.exit(-1);
      }

      c = (char)fis.read();
      if ( '#' == c ) { // read (and discard) IrfanView comment
         while (c != '\n') {
            c = (char)fis.read();
         }
         c = (char)fis.read();
      }

      // Read image dimensions.
      String widthDim = "";
      while (c != ' ' && c != '\n') {
         widthDim += c;
         c = (char)fis.read();
      }

      String heightDim = "";
      c = (char)fis.read();
      while (c != '\n') {
         heightDim += c;
         c = (char)fis.read();
      }

      final int width  = Integer.parseInt(widthDim.trim());
      final int height = Integer.parseInt(heightDim.trim());
      return new Dimension(width, height);
   }


   /**
      Initialize a rectangle of pixels from a PPM image file.

      @param rec_ul_x   upper left hand x-coordinate of the rectangle of pixels
      @param rec_ul_y   upper left hand y-coordinate of the rectangle of pixels
      @param width      width of the pixel data to read from the PPM file
      @param height     height of the pixel data to read from the PPM file
      @param inputFile  must name a PPM image file with magic number P6
      @param fis        input stream to the PPM image file
      @throws IOException if there is a problem with {@code fis}
   */
   private void setPixels(final int rec_ul_x, final int rec_ul_y,
                          final int width,    final int height,
                          final String inputFile, final FileInputStream fis)
   throws IOException {
   // Read the pixel data in a PPM file.
   // http://stackoverflow.com/questions/2693631/read-ppm-file-and-store-it-in-an-array-coded-with-c
      // Read image rgb dimensions (which we don't use).
      char c = (char)fis.read();
      while (c != '\n') {
         c = (char)fis.read();
      }

      // Create a small data array.
      final byte[] pixelData = new byte[3];

      // Read pixel data, one pixel at a time, from the PPM file.
      for (int y = 0; y < height; ++y) {
         for (int x = 0; x < width; ++x) {
            if ( fis.read(pixelData, 0, 3) != 3 ) {
               System.err.printf("ERROR! Could not load %s\n", inputFile);
               System.exit(-1);
            }
            int r = pixelData[0];
            int g = pixelData[1];
            int b = pixelData[2];
            if (r < 0) r = 256+r;  // convert from signed byte to unsigned byte
            if (g < 0) g = 256+g;
            if (b < 0) b = 256+b;
            setPixelFB(rec_ul_x + x, rec_ul_y + y, new Color(r, g, b));
         }
      }
   }


   /**
      Get the width of this {@code FrameBuffer}.

      @return width of this {@code FrameBuffer}
   */
   public int getWidthFB() {
      return widthFB;
   }


   /**
      Get the height of this {@code FrameBuffer}.

      @return height of this {@code FrameBuffer}
   */
   public int getHeightFB() {
      return heightFB;
   }


   /**
      Get this {@code FrameBuffer}'s default {@code Viewport}.

      @return this {@code FrameBuffer}'s default {@code Viewport}
   */
   public Viewport getViewport() {
      return this.vp;
   }


   /**
      Set the default {@code Viewport} to be this whole {@code FrameBuffer}.
   */
   public void setViewport() {
      this.vp.setViewport(0, 0, widthFB, heightFB);
   }


   /**
      Set the default {@code Viewport} with the given upper-left-hand corner,
      width and height within this {@code FrameBuffer}.

      @param vp_ul_x  upper left hand x-coordinate of default {@code Viewport}
      @param vp_ul_y  upper left hand y-coordinate of default {@code Viewport}
      @param width    default {@code Viewport}'s width
      @param height   default {@code Viewport}'s height
   */
   public void setViewport(final int vp_ul_x, final int vp_ul_y,
                           final int width,   final int height) {
      this.vp.setViewport(vp_ul_x, vp_ul_y, width, height);
   }


   /**
      Get the background color of this {@code FrameBuffer}.

      @return the background {@link Color} of this {@code FrameBuffer}
   */
   public Color getBackgroundColorFB() {
      return bgColorFB;
   }


   /**
      Set the background color of this {@code FrameBuffer}.
      <p>
      NOTE: This method does not clear the pixels of the
      {@code FrameBuffer} to the given {@link Color}. To
      change all the pixels to the given {@link Color},
      use the {@link clearFB} method.

      @param c  new background {@link Color} for this {@code FrameBuffer}
   */
   public void setBackgroundColorFB(final Color c) {
      bgColorFB = c;
   }


   /**
      Clear (fill) the {@code FrameBuffer} using its background color.
   */
   public void clearFB() {
      clearFB(bgColorFB);
   }


   /**
      Clear (fill) the {@code FrameBuffer} using the given {@link Color}.
      <p>
      NOTE: This method does not change the background color of this
      {@code FrameBuffer}. To change the background color use the
      {@link setBackgroundColorFB} method.

      @param c  {@link Color} to clear {@code FrameBuffer} with
   */
   public void clearFB(final Color c) {
      final int rgb = c.getRGB();
      for (int y = 0; y < heightFB; ++y) {
         for (int x = 0; x < widthFB; ++x) {
            setPixelFB(x, y, rgb);
         }
      }
   }


   /**
      Get the {@link Color} of the pixel with coordinates
      {@code (x,y)} in the {@code FrameBuffer}.

      @param x  horizontal coordinate within the {@code FrameBuffer}
      @param y  vertical coordinate within the {@code FrameBuffer}
      @return the {@link Color} of the pixel at the given pixel coordinates
   */
   public Color getPixelFB(final int x, final int y) {
      final int index = (y * widthFB) + x;
      try {
         final int rgb = pixel_buffer[index];
         return new Color(rgb);
      }
      catch(ArrayIndexOutOfBoundsException e) {
         System.err.println("FrameBuffer: Bad pixel coordinate"
                                          + " (" + x + ", " + y +")"
                                          + " [w="+widthFB+", h="+heightFB+"]");
       //e.printStackTrace(System.err);
         return Color.black;
      }
   }


   /**
      Set the {@link Color} of the pixel with coordinates
      {@code (x,y)} in the {@code FrameBuffer}.

      @param x  horizontal coordinate within the {@code FrameBuffer}
      @param y  vertical coordinate within the {@code FrameBuffer}
      @param c  {@link Color} for the pixel at the given pixel coordinates
   */
   public void setPixelFB(final int x, final int y, final Color c) {
      setPixelFB(x, y, c.getRGB());
   }


   /**
      Set the combined RGB value of the pixel with coordinates
      {@code (x,y)} in the {@code FrameBuffer}.

      @param x  horizontal coordinate within the {@code FrameBuffer}
      @param y  vertical coordinate within the {@code FrameBuffer}
      @param c  combined RGB value for the pixel at the given pixel coordinates
   */
   public void setPixelFB(final int x, final int y, final int c) {
      final int index = (y * widthFB) + x;
      try {
         pixel_buffer[index] = c;
      }
      catch(ArrayIndexOutOfBoundsException e) {
         System.err.println("FrameBuffer: Bad pixel coordinate"
                                          + " (" + x + ", " + y +")"
                                          + " [w="+widthFB+", h="+heightFB+"]");
       //e.printStackTrace(System.err);
      }
   }


   /**
      Create a new {@code FrameBuffer} containing the pixel data
      from just the red plane of this {@code FrameBuffer}.

      @return {@code FrameBuffer} object holding just red pixel data from this {@code FrameBuffer}
   */
   public FrameBuffer convertRed2FB() {
      final FrameBuffer red_fb = new FrameBuffer(widthFB, heightFB);
      red_fb.bgColorFB = this.bgColorFB;

      // Copy the framebuffer's red values into the new framebuffer's pixel buffer.
      for (int y = 0; y < heightFB; ++y) {
         for (int x = 0; x < widthFB; ++x) {
            final Color c = new Color(this.getPixelFB(x, y).getRed(), 0, 0);
            red_fb.setPixelFB(x, y, c);
         }
      }
      return red_fb;
   }


   /**
      Create a new {@code FrameBuffer} containing the pixel data
      from just the green plane of this {@code FrameBuffer}.

      @return {@code FrameBuffer} object holding just green pixel data from this {@code FrameBuffer}
   */
   public FrameBuffer convertGreen2FB() {
      final FrameBuffer green_fb = new FrameBuffer(widthFB, heightFB);
      green_fb.bgColorFB = this.bgColorFB;

      // Copy the framebuffer's green values into the new framebuffer's pixel buffer.
      for (int y = 0; y < heightFB; ++y) {
         for (int x = 0; x < widthFB; ++x) {
            final Color c = new Color(0, this.getPixelFB(x, y).getGreen(), 0);
            green_fb.setPixelFB(x, y, c);
         }
      }
      return green_fb;
   }


   /**
      Create a new {@code FrameBuffer} containing the pixel data
      from just the blue plane of this {@code FrameBuffer}.

      @return {@code FrameBuffer} object holding just blue pixel data from this {@code FrameBuffer}
   */
   public FrameBuffer convertBlue2FB() {
      final FrameBuffer blue_fb = new FrameBuffer(widthFB, heightFB);
      blue_fb.bgColorFB = this.bgColorFB;

      // Copy the framebuffer's blue values into the new framebuffer's pixel buffer.
      for (int y = 0; y < heightFB; ++y) {
         for (int x = 0; x < widthFB; ++x) {
            final Color c = new Color(0, 0, this.getPixelFB(x, y).getBlue());
            blue_fb.setPixelFB(x, y, c);
         }
      }
      return blue_fb;
   }


   /**
      Write this {@code FrameBuffer} to the specified PPM file.
   <p>
      <a href="https://en.wikipedia.org/wiki/Netpbm_format" target="_top">
               https://en.wikipedia.org/wiki/Netpbm_format</a>

      @param filename  name of PPM image file to hold {@code FrameBuffer} data
   */
   public void dumpFB2File(final String filename) {
      dumpPixels2File(0, 0, widthFB - 1, heightFB - 1, filename);
   }


   /**
      Write a rectangular sub array of pixels from this {@code FrameBuffer}
      to the specified PPM file.
   <p>
      <a href="https://en.wikipedia.org/wiki/Netpbm_format#PPM_example" target="_top">
               https://en.wikipedia.org/wiki/Netpbm_format#PPM_example</a>
   <p>
<a href="http://stackoverflow.com/questions/2693631/read-ppm-file-and-store-it-in-an-array-coded-with-c" target="_top">
         http://stackoverflow.com/questions/2693631/read-ppm-file-and-store-it-in-an-array-coded-with-c</a>

      @param ul_x      upper left hand x-coordinate of pixel data rectangle
      @param ul_y      upper left hand y-coordinate of pixel data rectangle
      @param lr_x      lower right hand x-coordinate of pixel data rectangle
      @param lr_y      lower right hand y-coordinate of pixel data rectangle
      @param filename  name of PPM image file to hold pixel data
   */
   public void dumpPixels2File(final int ul_x, final int ul_y,
                               final int lr_x, final int lr_y,
                               final String filename) {
      final int p_width  = lr_x - ul_x + 1;
      final int p_height = lr_y - ul_y + 1;

      // Header (meta data) for the PPM file.
      final byte[] header = ("P6\n" + p_width + " " + p_height + "\n" + 255 + "\n").getBytes();

      // Open a memory-mapped file.
      try (final var fc = new RandomAccessFile(filename, "rw").getChannel()) { // try with resources
         final ByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE,
                                       0,
                                       header.length + 3 * pixel_buffer.length);

         // Write data to the memory-mapped file.
         // Write the PPM header information first.
         for (int i = 0; i < header.length; ++i) {
            mbb.put(i, header[i]);
         }
         // Copy all the pixel data to the memory-mapped file, one row at a time.
         int index = header.length;
         for (int n = 0; n < p_height; ++n) {
            // read data from the top row of the data buffer
            // down towards the bottom row
            for (int i = 0; i < p_width * 3; i += 3) {
               final int rgb = pixel_buffer[((ul_y+n)*widthFB + ul_x) + i/3];
               final Color c = new Color(rgb);
               mbb.put(index + 0, (byte)(c.getRed()));
               mbb.put(index + 1, (byte)(c.getGreen()));
               mbb.put(index + 2, (byte)(c.getBlue()));
               index += 3;
            }
         }
      }
      catch (IOException e) {
         System.err.printf("ERROR! Could not write to file %s\n", filename);
         e.printStackTrace(System.err);
         System.exit(-1);
      }
   }


   /**
      Write this {@code FrameBuffer} to the specified image file
      using the specified file format.

      @param filename    name of the image file to hold framebuffer data
      @param formatName  informal name of the image format
   */
   public void dumpFB2File(final String filename, final String formatName) {
      dumpPixels2File(0, 0, widthFB - 1, heightFB - 1, filename, formatName);
   }


   /**
      Write a rectangular sub array of pixels from this {@code FrameBuffer}
      to the specified image file using the specified file format.
   <p>
      Use the static method {@link ImageIO#getWriterFormatNames}
      to find out what informal image format names can be used
      (for example, png, gif, jpg, bmp).

      @param ul_x        upper left hand x-coordinate of pixel data rectangle
      @param ul_y        upper left hand y-coordinate of pixel data rectangle
      @param lr_x        lower right hand x-coordinate of pixel data rectangle
      @param lr_y        lower right hand y-coordinate of pixel data rectangle
      @param filename    name of the image file to hold pixel data
      @param formatName  informal name of the image format
   */
   public void dumpPixels2File(final int ul_x, final int ul_y,
                               final int lr_x, final int lr_y,
                               final String filename,
                               final String formatName) {
      final int p_width  = lr_x - ul_x + 1;
      final int p_height = lr_y - ul_y + 1;

      try (final var fos = new FileOutputStream(filename)) {  // try with resources
       //System.err.printf("Created file %s\n", filename);
         final BufferedImage bi = new BufferedImage(p_width, p_height, BufferedImage.TYPE_INT_RGB);
         for (int n = 0; n < p_height; ++n) {
            for (int i = 0; i < p_width; ++i) {
               final int rgb = pixel_buffer[((ul_y+n)*widthFB + ul_x) + i];
               bi.setRGB(i, n, rgb);
            }
         }
         ImageIO.write(bi, formatName, fos);
      }
      catch (FileNotFoundException e) {
         System.err.printf("ERROR! Could not open file %s\n", filename);
         e.printStackTrace(System.err);
         System.exit(-1);
      }
      catch (IOException e) {
         System.err.printf("ERROR! Could not write to file %s\n", filename);
         e.printStackTrace(System.err);
         System.exit(-1);
      }
   }


   /**
      For debugging very small {@code FrameBuffer} objects.
   <p>
      If this {@code FrameBuffer} is too large (more than
      16 columns or more than 100 rows), then the returned
      string is truncated.

      @return a {@link String} representation of this {@code FrameBuffer}
   */
   @Override
   public String toString() {
      final int w = (widthFB  >  16) ?  16 : widthFB;
      final int h = (heightFB > 100) ? 100 : heightFB;
      String result = "FrameBuffer [w=" + widthFB + ", h=" + heightFB + "]\n";
      for (int j = 0; j < w; ++j) {
         result += " r   g   b |";
      }
      result += "\n";
      for (int i = 0; i < h; ++i) {
         for (int j = 0; j < w; ++j) {
            final int c = pixel_buffer[(i*widthFB) + j];
            final Color color = new Color(c);
            result += String.format("%3d ", color.getRed())
                     +String.format("%3d ", color.getGreen())
                     +String.format("%3d|", color.getBlue());
         }
         result += "\n";
      }
      if (widthFB > 16 || heightFB > 100) result += "truncated...\n";
      return result;
   }


   /**
      A simple test of the {@code FrameBuffer} class.
   <p>
      It fills the framebuffer with a test pattern.
   */
   public void fbTestPattern() {
      for (int y = 0; y < heightFB; ++y) {
         for (int x = 0; x < widthFB; ++x) {
            final int gray = (x | y) % 255;
            setPixelFB(x, y, new Color(gray, gray, gray));
         }
      }
   }


/*******************************************************************
   The following code is an inner class of FrameBuffer.
********************************************************************/

   /**
      A {@code Viewport} is an inner (non-static nested) class of
      {@link FrameBuffer}. That means that a {@code Viewport} has
      access to the pixel data of its "parent" {@link FrameBuffer}.
   <p>
      A {@code Viewport} is a two-dimensional sub array of its
      "parent" {@link FrameBuffer}. A {@code Viewport} is
      represented by its upper-left-hand corner and its
      lower-right-hand corner in the {@link FrameBuffer}.
   <p>
      When you set a pixel in a {@code Viewport}, you are really
      setting a pixel in its parent {@link FrameBuffer}.
   <p>
      A {@link FrameBuffer} can have multiple {@code Viewport}s.
   <p>
      {@code Viewport} coordinates act like Java {@link java.awt.Graphics2D}
      coordinates; the positive {@code x} direction is to the right and the
      positive {@code y} direction is downward.
   */
   public class Viewport  // inner class (non-static nested class)
   {
      // Coordinates of the viewport within the framebuffer.
      public int vp_ul_x;     // upper-left-hand corner
      public int vp_ul_y;
      public int vp_lr_x;     // lower-right-hand corner
      public int vp_lr_y;
      public Color bgColorVP;   // the viewport's background color


      /**
         Create a {@code Viewport} that is the whole of its parent
         {@link FrameBuffer}. The default background {@link Color}
         is the {@link FrameBuffer}'s background color.
         <p>
         NOTE: This constructor does not set the pixels of this
         {@code Viewport}. If you want the pixels of this
         {@code Viewport} to be cleared to the background color,
         call the {@link clearVP} method.
      */
      public Viewport() {
         this(0, 0, widthFB, heightFB, bgColorFB);
      }


      /**
         Create a {@code Viewport} that is the whole of its
         parent {@link FrameBuffer} and with the given
         background color.
         <p>
         NOTE: This constructor does not use the background color to set
         the pixels of this {@code Viewport}. If you want the pixels of
         this {@code Viewport} to be cleared to the background color,
         call the {@link clearVP} method.

         @param c  background {@link Color} for the {@code Viewport}
      */
      public Viewport(final Color c) {
         this(0, 0, widthFB, heightFB, c);
      }


      /**
         Create a {@code Viewport} with the given upper-left-hand corner,
         width and height within its parent {@link FrameBuffer}.
         <p>
         NOTE: This constructor does not set the pixels of this {@code Viewport}.
         If you want the pixels of this {@code Viewport} to be cleared to the
         background color, call the {@link clearVP} method.

         @param vp_ul_x    upper left hand x-coordinate of new {@code Viewport} rectangle
         @param vp_ul_y    upper left hand y-coordinate of new {@code Viewport} rectangle
         @param widthVP    {@code Viewport}'s width
         @param heightVP   {@code Viewport}'s height
      */
      public Viewport(final int vp_ul_x, final int vp_ul_y,
                      final int widthVP,   final int heightVP) {
         this(vp_ul_x, vp_ul_y, widthVP, heightVP, bgColorFB);
      }


      /**
         Create a {@code Viewport} with the given upper-left-hand corner,
         width and height within its parent {@link FrameBuffer}, and with
         the given background color.
         <p>
         NOTE: This constructor does not use the background color to set the
         pixels of this {@code Viewport}. If you want the pixels of this
         {@code Viewport} to be cleared to the background color, call the
         {@link clearVP} method.
      <p>
         (Using upper-left-hand corner, width, and height is
         like Java's {@link java.awt.Rectangle} class and
         {@link java.awt.Graphics#drawRect} method.)

         @param vp_ul_x    upper left hand x-coordinate of new {@code Viewport} rectangle
         @param vp_ul_y    upper left hand y-coordinate of new {@code Viewport} rectangle
         @param widthVP    {@code Viewport}'s width
         @param heightVP   {@code Viewport}'s height
         @param c          background {@link Color} for the {@code Viewport}
      */
      public Viewport(final int vp_ul_x, final int vp_ul_y,
                      final int widthVP,   final int heightVP,
                      final Color c) {
         setViewport(vp_ul_x, vp_ul_y, widthVP, heightVP);
         this.bgColorVP = c;
      }


      /**
         Create a {@code Viewport}, within its parent {@link FrameBuffer},
         from the pixel data of another {@link FrameBuffer}.
         <p>
         The size of the {@code Viewport} will be the size of the
         source {@link FrameBuffer}.

         @param vp_ul_x   upper left hand x-coordinate of new {@code Viewport} rectangle
         @param vp_ul_y   upper left hand y-coordinate of new {@code Viewport} rectangle
         @param sourceFB  {@link FrameBuffer} to use as the source of the pixel data
      */
      public Viewport(final int vp_ul_x, final int vp_ul_y,
                      final FrameBuffer sourceFB) {
         this(vp_ul_x, vp_ul_y, sourceFB.getWidthFB(),
                                sourceFB.getHeightFB(),
                                sourceFB.getBackgroundColorFB());

         // Read pixel data, one pixel at a time, from the source FrameBuffer.
         for (int y = 0; y < sourceFB.getHeightFB(); ++y) {
            for (int x = 0; x < sourceFB.getWidthFB(); ++x) {
               this.setPixelVP(x, y, sourceFB.getPixelFB(x,y));
            }
         }
      }


      /**
         Create a {@code Viewport}, within its parent {@link FrameBuffer},
         from the pixel data of a {@code Viewport}.
      <p>
         The size of the new {@code Viewport} will be the size of the
         source {@code Viewport}.
      <p>
         This constructor makes the new {@code Viewport} into a copy of the
         source {@code Viewport}.

         @param vp_ul_x   upper left hand x-coordinate of new {@code Viewport} rectangle
         @param vp_ul_y   upper left hand y-coordinate of new {@code Viewport} rectangle
         @param sourceVP  {@link Viewport} to use as the source of the pixel data
      */
      public Viewport(final int vp_ul_x, final int vp_ul_y,
                      final Viewport sourceVP) {
         this(vp_ul_x, vp_ul_y, sourceVP.getWidthVP(),
                                sourceVP.getHeightVP(),
                                sourceVP.getBackgroundColorVP());

         // Read pixel data, one pixel at a time, from the source Viewport.
         for (int y = 0; y < sourceVP.getHeightVP(); ++y) {
            for (int x = 0; x < sourceVP.getWidthVP(); ++x) {
               this.setPixelVP(x, y, sourceVP.getPixelVP(x,y));
            }
         }
      }


      /**
         Create a {@code Viewport}, within its parent {@link FrameBuffer},
         from a PPM image file.
      <p>
         The size of the {@code Viewport} will be the size of the image.
      <p>
         This can be used to initialize a {@code Viewport} with a background image.

         @param vp_ul_x        upper left hand x-coordinate of new {@code Viewport} rectangle
         @param vp_ul_y        upper left hand y-coordinate of new {@code Viewport} rectangle
         @param inputFileName  must name a PPM image file with magic number P6.
      */
      public Viewport(final int vp_ul_x, final int vp_ul_y,
                      final String inputFileName) {
         try (final var fis = new FileInputStream(inputFileName)) {  // try with resources
            final Dimension vpDim = getPPMdimensions(inputFileName, fis);

            this.vp_ul_x = vp_ul_x;
            this.vp_ul_y = vp_ul_y;
            this.vp_lr_x = vp_ul_x + vpDim.width - 1;
            this.vp_lr_y = vp_ul_y + vpDim.height - 1;

            setPixels(vp_ul_x, vp_ul_y, vpDim.width, vpDim.height, inputFileName, fis);

            this.bgColorVP = bgColorFB;
         }
         catch (IOException e) {
            System.err.printf("ERROR! Could not read %s\n", inputFileName);
            e.printStackTrace(System.err);
            System.exit(-1);
         }
      }


      /**
         Mutate this {@code Viewport} into the given upper-left-hand corner,
         width and height within its parent {@link FrameBuffer}.
      <p>
         (Using upper-left-hand corner, width, and height is
         like Java's {@link java.awt.Rectangle} class and
         {@link java.awt.Graphics#drawRect} method.)

         @param vp_ul_x   new upper left hand x-coordinate of this {@code Viewport} rectangle
         @param vp_ul_y   new upper left hand y-coordinate of this {@code Viewport} rectangle
         @param widthVP   {@code Viewport}'s new width
         @param heightVP  {@code Viewport}'s new height
      */
      public void setViewport(final int vp_ul_x, final int vp_ul_y,
                              final int widthVP,   final int heightVP) {
         this.vp_ul_x = vp_ul_x;
         this.vp_ul_y = vp_ul_y;
         this.vp_lr_x = vp_ul_x + widthVP - 1;
         this.vp_lr_y = vp_ul_y + heightVP - 1;
      }


      /**
         Return a reference to the {@link FrameBuffer} object that
         this {@code Viewport} object is nested in.

         @return a reference to the {@link FrameBuffer} object that this {@code Viewport} is part of
      */
      public FrameBuffer getFrameBuffer()
      {
         return FrameBuffer.this;
      }


      /**
         Get the width of this {@code Viewport}.

         @return width of this {@code Viewport} rectangle
      */
      public int getWidthVP() {
         return this.vp_lr_x - this.vp_ul_x + 1;
      }


      /**
         Get the height of this {@code Viewport}.

         @return height of this {@code Viewport} rectangle
      */
      public int getHeightVP() {
         return this.vp_lr_y - this.vp_ul_y + 1;
      }


      /**
         Get the background color of this {@code Viewport}.

         @return the background {@link Color} of this {@code Viewport}
      */
      public Color getBackgroundColorVP() {
         return this.bgColorVP;
      }


      /**
         Set background color of this {@code Viewport}.
         <p>
         NOTE: This method does not clear the pixels of this
         {@code Viewport} to the given {@link Color}. To change
         all the pixels to the given {@link Color}, use the
         {@link clearVP} method.

         @param c  new background {@link Color} for this {@code Viewport}
      */
      public void setBackgroundColorVP(final Color c) {
         this.bgColorVP = c;
      }


      /**
         Clear (fill) this {@code Viewport} using its background color.
      */
      public void clearVP() {
         this.clearVP(this.bgColorVP);
      }


      /**
         Clear (fill) this {@code Viewport} using the given {@link Color}.
         <p>
         NOTE: This method does not change the background color of this
         {@code Viewport}. To change the background color use the
         {@link setBackgroundColorVP} method.

         @param c  {@link Color} to clear this {@code Viewport} with
      */
      public void clearVP(final Color c) {
         final int wVP = this.getWidthVP();
         final int hVP = this.getHeightVP();
         final int rgb = c.getRGB();
         for (int y = 0; y < hVP; ++y) {
            for (int x = 0; x < wVP; ++x) {
               this.setPixelVP(x, y, rgb);
            }
         }
      }


      /**
         Get the {@link Color} of the pixel with coordinates
         {@code (x,y)} relative to this {@code Viewport}.

         @param x  horizontal coordinate within this {@code Viewport}
         @param y  vertical coordinate within this {@code Viewport}
         @return the {@link Color} of the current pixel at the given {@code Viewport} coordinates
      */
      public Color getPixelVP(final int x, final int y) {
         return getPixelFB(this.vp_ul_x + x,
                           this.vp_ul_y + y);
      }


      /**
         Set the {@link Color} of the pixel with coordinates
         {@code (x,y)} relative to this {@code Viewport}.

         @param x  horizontal coordinate within this {@code Viewport}
         @param y  vertical coordinate within this {@code Viewport}
         @param c  {@link Color} for the pixel at the given {@code Viewport} coordinates
      */
      public void setPixelVP(final int x, final int y, final Color c) {
         setPixelFB(this.vp_ul_x + x,
                    this.vp_ul_y + y,
                    c);
      }


      /**
         Set the combined RGB value of the pixel with coordinates
         {@code (x,y)} relative to this {@code Viewport}.

         @param x  horizontal coordinate within this {@code Viewport}
         @param y  vertical coordinate within this {@code Viewport}
         @param c  combined RGB value for the pixel at the given {@code Viewport} coordinates
      */
      public void setPixelVP(final int x, final int y, final int c) {
         setPixelFB(this.vp_ul_x + x,
                    this.vp_ul_y + y,
                    c);
      }


      /**
         Create a new {@link FrameBuffer} containing the pixel data
         from this {@code Viewport} rectangle.

         @return {@code FrameBuffer} object holding pixel data from this {@code Viewport}
      */
      public FrameBuffer convertVP2FB() {
         final int wVP = this.getWidthVP();
         final int hVP = this.getHeightVP();

         final FrameBuffer vp_fb = new FrameBuffer(wVP, hVP);
         vp_fb.bgColorFB = this.bgColorVP;

         // Copy the current viewport into the new framebuffer's pixel buffer.
         for (int y = 0; y < hVP; ++y) {
            for (int x = 0; x < wVP; ++x) {
               vp_fb.setPixelFB( x, y, this.getPixelVP(x, y) );
            }
         }

         return vp_fb;
      }


      /**
         Write this {@code Viewport} to the specified PPM file.
      <p>
         <a href="https://en.wikipedia.org/wiki/Netpbm_format" target="_top">
                  https://en.wikipedia.org/wiki/Netpbm_format</a>

         @param filename  name of PPM image file to hold {@code Viewport} data
      */
      public void dumpVP2File(final String filename) {
         dumpPixels2File(this.vp_ul_x,
                         this.vp_ul_y,
                         this.vp_lr_x,
                         this.vp_lr_y,
                         filename);
      }


      /**
         Write this {@code Viewport} to the specified image file
         using the specified file format.

         @param filename    name of the image file to hold {@code Viewport} data
         @param formatName  informal name of the image format
      */
      public void dumpVP2File(final String filename,
                              final String formatName) {
         dumpPixels2File(this.vp_ul_x,
                         this.vp_ul_y,
                         this.vp_lr_x,
                         this.vp_lr_y,
                         filename,
                         formatName);
      }


      /**
         For debugging very small {@code Viewport} objects.
      <p>
         If this {@code Viewport} is too large (more than
         16 columns or more than 100 rows), then the returned
         string is truncated.

         @return a {@link String} representation of this {@code Viewport}
      */
      @Override
      public String toString() {
         final int wVP = this.getWidthVP();
         final int hVP = this.getHeightVP();
         final int w = (wVP >  16) ?  16 : wVP;
         final int h = (hVP > 100) ? 100 : hVP;
         String result = "Viewport [w=" + wVP + ", h=" + hVP + "]\n";
         for (int j = 0; j < w; ++j) {
            result += " r   g   b |";
         }
         result += "\n";
         for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
               final Color color = this.getPixelVP(x, y);
               result += String.format("%3d ", color.getRed())
                        +String.format("%3d ", color.getGreen())
                        +String.format("%3d|", color.getBlue());
            }
            result += "\n";
         }
         if (wVP > 16 || hVP > 100) result += "truncated...\n";
         return result;
      }


      /**
         A simple test of the {@code Viewport}.
      <p>
         It fills the viewport with a test pattern.
      */
      public void vpTestPattern() {
         for (int y = 0; y < this.getHeightVP(); ++y) {
            for (int x = 0; x < this.getWidthVP(); ++x) {
               final int gray = (x | y) % 255;
               this.setPixelVP(x, y, new Color(gray, gray, gray));
            }
         }
      }
   }// Viewport


/*******************************************************************
   The following is a main() method for testing, demonstration,
   and documentation purposes.
********************************************************************/

   /**
      A {@code main()} method for testing the {@code FrameBuffer} class.

      @param args  array of command-line arguments
   */
   public static void main(String[] args) {
      final int w = 512;
      final int h = 512;
      final FrameBuffer fb = new FrameBuffer(w, h);
      fb.fbTestPattern();  // fill the framebuffer with a test pattern
      fb.dumpFB2File("test01.ppm");

      // Notice the unusual notation for instantiating a new Viewport.
      final Viewport vp = fb.new Viewport(64, 64, 192, 320);  // 192 by 320
      vp.clearVP( Color.red );
      for (int i = 0; i < 512; ++i)
         fb.setPixelFB(128, i, Color.blue); // a blue vertical line
      for (int i = 0; i < 192; ++i)
         vp.setPixelVP(i, i, Color.green);  // a green diagonal line

      fb.dumpFB2File("test02.ppm");
      vp.dumpVP2File("test03.ppm");
      fb.dumpPixels2File(32, 256-64, 511-64, 255+64, "test04.ppm"); // 416 by 128

      final Viewport vp2 = fb.new Viewport(80, 80, 160, 160); // 160 by 160
      vp2.vpTestPattern();  // fill the viewport with a test pattern
      fb.dumpFB2File("test05.ppm");

      final FrameBuffer fb2a = new FrameBuffer(fb);
      final FrameBuffer fb2b = new FrameBuffer("test05.ppm");
      fb2a.dumpFB2File("test06a.ppm"); // test06a.ppm and test06b.ppm
      fb2b.dumpFB2File("test06b.ppm"); // should be exaactly the same

      fb.convertRed2FB().dumpFB2File("test07.ppm");
      fb.convertGreen2FB().dumpFB2File("test08.ppm");
      fb.convertBlue2FB().dumpFB2File("test09.ppm");

      final FrameBuffer fb3 = new FrameBuffer(600, 600);
      fb3.clearFB(Color.orange);
      final Viewport vp3 = fb3.new Viewport(44, 44, "test05.ppm");
      fb3.dumpFB2File("test10.ppm");
      fb3.new Viewport(86, 86, vp.convertVP2FB());
      fb3.dumpFB2File("test11.ppm");
      fb3.dumpFB2File("test11.png", "png");
      fb3.dumpFB2File("test11.gif", "gif");
      fb3.dumpFB2File("test11.jpg", "jpg");
      fb3.dumpFB2File("test11.bmp", "bmp");

      final FrameBuffer fb4 = new FrameBuffer(1200, 600);
      // Create two viewports in one frameBuffer.
      final Viewport vp4 = fb4.new Viewport(  0, 0, "test10.ppm");
      final Viewport vp5 = fb4.new Viewport(600, 0, fb3);
      // Copy a viewport into a viewport.
      final Viewport vp6 = fb4.new Viewport(0, 0, 200, 200); // source
      final Viewport vp7 = fb4.new Viewport(1000, 400, vp6);
      fb4.dumpFB2File("test12.ppm");

      // list the image file formats supported by the runtime
      for (final String s : ImageIO.getWriterFormatNames()) System.out.println(s);
   }//main()
}//FrameBuffer
