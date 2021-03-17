package glslfft;

import processing.core.PApplet;
import turingpatterns.Complex;
import turingpatterns.FFT;

import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * This class tests the GLSL based FFT implementation
 */
public class GlslFftTest extends PApplet {

   private GlslFft fft;

   @Override
   public void settings() {
      size(0, 0, P2D);
   }

   @Override
   public void setup() {
      fft = new GlslFft(this);

      StringBuilder result = new StringBuilder();
      try {
         for(Method m :this.getClass().getDeclaredMethods()) {
            if(m.getName().startsWith("test") && m.getParameterCount() == 0) {
               boolean success;
               try {
                  println("==> Starting: " + m.getName());
                  m.invoke(this);
                  success = true;
               } catch(Exception e) {
                  e.printStackTrace(System.out);
                  success = false;
               }
               println("<== Finished: " + m.getName());
               if(success) {
                  result.append(m.getName()).append(": ✅ \n");
               } else {
                  result.append(m.getName()).append(": ❌ \n");
               }
            }
         }
      } finally {
         println("\nTest Results Summary: ");
         println(result);
         exit();
      }
   }

   public void testForwardFFT_Reals() {
      Complex[][] data = new Complex[][]{
          {c(1, 0), c(2, 0), c(3, 0), c(4, 0)},
          {c(5, 0), c(6, 0), c(7, 0), c(8, 0)},
          {c(9, 0), c(10, 0), c(11, 0), c(12, 0)},
          {c(13, 0), c(14, 0), c(15, 0), c(16, 0)}
      };

      Complex[][] expected = FFT.fft2d(data);
      Complex[][] actual0 = forward(data, null,4, 4).layer0;
      Complex[][] actual1 = forward(null, data,4, 4).layer1;

      assertEqual(expected, actual0);
      assertEqual(expected, actual1);
   }

   public void testForwardFFT_RealAndImaginary() {
      Complex[][] data = new Complex[][]{
          {c(1, 16), c(2, 15), c(3, 14), c(4, 13)},
          {c(5, 12), c(6, 11), c(7, 10), c(8, 9)},
          {c(9, 8), c(10, 7), c(11, 6), c(12, 5)},
          {c(13, 4), c(14, 3), c(15, 2), c(16, 1)}
      };

      Complex[][] expected = FFT.fft2d(data);
      Complex[][] actual0 = forward(data, null,4, 4).layer0;
      Complex[][] actual1 = forward(null, data,4, 4).layer1;

      assertEqual(expected, actual0);
      assertEqual(expected, actual1);
   }

   // Non-square matrices don't work at the moment. Not sure why.
//   public void _testForwardFFT_Reals_8x4() {
//      List<FftPass<String>> forward = fft.forward("in", "ping", "pong", "out", 16, 8);
//      println("Passes: ");
//      for(FftPass<String> pass : forward) {
//         println(pass);
//      }
//
//
//      Complex[][] data = new Complex[][]{
//          {c(1, 0), c(2, 0), c(3, 0), c(4, 0), c(5, 0), c(6, 0), c(7, 0), c(8, 0) },
//          {c(9, 0), c(10, 0), c(11, 0), c(12, 0), c(13, 0), c(14, 0), c(15, 0), c(16, 0) },
//          {c(17, 0), c(18, 0), c(19, 0), c(20, 0), c(21, 0), c(22, 0), c(23, 0), c(24, 0) },
//          {c(25, 0), c(27, 0), c(27, 0), c(28, 0), c(29, 0), c(30, 0), c(31, 0), c(32, 0) },
//      };
//
//      int width = 8;
//      int height = 4;
//
//      Complex[][] expected = FFT.fft2d(data);
//      Complex[][] actual = forward(data, width, height);
//
//      assertEqual(expected, actual);
//   }

   public void testInverseFFT_Reals() {
      Complex[][] data = new Complex[][]{
          {c(1, 0), c(2, 0), c(3, 0), c(4, 0)},
          {c(5, 0), c(6, 0), c(7, 0), c(8, 0)},
          {c(9, 0), c(10, 0), c(11, 0), c(12, 0)},
          {c(13, 0), c(14, 0), c(15, 0), c(16, 0)}
      };

      Complex[][] expected = FFT.ifft2d(data);
      Complex[][] actual0 = inverse(data, null,4, 4).layer0;
      Complex[][] actual1 = inverse(null, data,4, 4).layer1;


      assertEqual(expected, actual0);
      assertEqual(expected, actual1);
   }

   public void testInverseFFT_RealAndImaginary() {
      Complex[][] data = new Complex[][]{
          {c(1, 16), c(2, 15), c(3, 14), c(4, 13)},
          {c(5, 12), c(6, 11), c(7, 10), c(8, 9)},
          {c(9, 8), c(10, 7), c(11, 6), c(12, 5)},
          {c(13, 4), c(14, 3), c(15, 2), c(16, 1)}
      };

      Complex[][] expected = FFT.ifft2d(data);
      Complex[][] actual0 = inverse(data, null, 4, 4).layer0;
      Complex[][] actual1 = inverse(null, data, 4, 4).layer1;

      assertEqual(expected, actual0);
      assertEqual(expected, actual1);
   }

   public void testForwardFFT_1000_Random_32x32_RealAndImaginary() {
      int width = 32;
      int height = 32;

      for(int i = 0; i < 1000; i++) {
         Complex[][] data0 = randomValues(width, height);
         Complex[][] data1 = randomValues(width, height);

         Complex[][] expected0 = FFT.fft2d(data0);
         Complex[][] expected1 = FFT.fft2d(data1);

         ResultLayers actual = forward(data0, data1, width, height);

         assertEqual(expected0, actual.layer0);
         assertEqual(expected1, actual.layer1);
      }
   }

   public void testInverseFFT_1000_Random_32x32_RealAndImaginary() {
      int width = 32;
      int height = 32;

      for(int i = 0; i < 1000; i++) {
         Complex[][] data0 = randomValues(width, height);
         Complex[][] data1 = randomValues(width, height);

         Complex[][] expected0 = FFT.ifft2d(data0);
         Complex[][] expected1 = FFT.ifft2d(data1);

         ResultLayers actual = inverse(data0, data1, width, height);

         assertEqual(expected0, actual.layer0);
         assertEqual(expected1, actual.layer1);
      }
   }

   public void testForwardThenInverseFFT_Reals() {
      Complex[][] expected = new Complex[][]{
          {c(1, 0), c(2, 0), c(3, 0), c(4, 0)},
          {c(5, 0), c(6, 0), c(7, 0), c(8, 0)},
          {c(9, 0), c(10, 0), c(11, 0), c(12, 0)},
          {c(13, 0), c(14, 0), c(15, 0), c(16, 0)}
      };

      Complex[][] actualGlsl = inverse(forward(expected, null,4, 4).layer0, null,4, 4).layer0;
      assertEqual(expected, actualGlsl);

      Complex[][] actualJava = FFT.ifft2d(FFT.fft2d(expected));
      assertEqual(expected, actualJava);

      assertEqual(actualJava, actualGlsl);
   }

   public void testForwardThenInverseFFT_RealAndImaginary() {
      Complex[][] expected = new Complex[][]{
          {c(1, 16), c(2, 15), c(3, 14), c(4, 13)},
          {c(5, 12), c(6, 11), c(7, 10), c(8, 9)},
          {c(9, 8), c(10, 7), c(11, 6), c(12, 5)},
          {c(13, 4), c(14, 3), c(15, 2), c(16, 1)}
      };

      Complex[][] actualGlsl = inverse(forward(expected, null, 4, 4).layer0, null,4, 4).layer0;
      assertEqual(expected, actualGlsl);

      Complex[][] actualJava = FFT.ifft2d(FFT.fft2d(expected));
      assertEqual(expected, actualJava);

      assertEqual(actualJava, actualGlsl);
   }

   public void testForwardThenInverseFFT_1000_Random_32x32_RealAndImaginary() {
      for(int i = 0; i < 1000; i++) {
         int width = 32;
         int height = 32;

         Complex[][] expected = randomValues(width, height);

         Complex[][] actualGlsl = inverse(forward(expected, null, width, height).layer0, null, width, height).layer0;
         assertEqual(expected, actualGlsl);

         Complex[][] actualJava = FFT.ifft2d(FFT.fft2d(expected));
         assertEqual(expected, actualJava);

         assertEqual(actualJava, actualGlsl);
      }
   }

   private Complex[][] randomValues(int width, int height) {
      Complex[][] data = new Complex[height][width];

      for(int y = 0; y < height; y++) {
         for(int x = 0; x < width; x++) {
            data[y][x] = new Complex(random(-100, 100), random(-100, 100));
         }
      }
      return data;
   }

   private void assertEqual(Complex[][] expected, Complex[][] actual) {
      int ydim = expected.length;
      int xdim = expected[0].length;
      for(int y = 0; y < ydim; y++) {
         for(int x = 0; x < xdim; x++) {
            if(!expected[y][x].equals(actual[y][x])) {
               println("Mismatch at: (" + x + ", " + y + "): Expected: " + expected[y][x] + ", Actual: " + actual[y][x]);
            }
         }
      }
      boolean eq = Arrays.deepEquals(expected, actual);
      if (!eq) {
         throw new AssertionError("\nExpected: \n" + format(expected) + "\n Actual:\n" + format(actual));
      }
   }

   String format(Complex[][] data) {
      StringBuilder buffer = new StringBuilder();
      int ydim = data.length;
      int xdim = data[0].length;
      for (int y = 0; y < ydim; y++) {
         for (int x = 0; x < xdim; x++) {
            buffer.append(data[y][x]).append(", ");
         }
         buffer.append("\n");
      }
      return buffer.toString();
   }

   static class ResultLayers {
      Complex[][] layer0;
      Complex[][] layer1;
   }

   private ResultLayers forward(Complex[][] data0, Complex[][] data1, int w, int h) {
      FloatBuffer texData = fft.prepare(data0, data1, w, h);
      GlslFft.FftBuffer input = fft.newBuffer(w, h, texData);
      GlslFft.FftBuffer ping = fft.newBuffer(w, h);
      GlslFft.FftBuffer pong = fft.newBuffer(w, h);
      GlslFft.FftBuffer output = fft.newBuffer(w, h);

      List<FftPass<GlslFft.FftBuffer>> forward = fft.forward(input, ping, pong, output, w, h);
//      println("Forward Passes: ");
//      for(FftPass<GlslFft.FftBuffer> pass : forward) {
//         println(pass);
//      }

      fft.runPasses(forward);

      ResultLayers layers = new ResultLayers();
      layers.layer0 = fft.loadLayer0(output);
      layers.layer1 = fft.loadLayer1(output);

      return layers;
   }

   private ResultLayers inverse(Complex[][] data0, Complex[][] data1, int w, int h) {
      FloatBuffer texData = fft.prepare(data0, data1, w, h);
      GlslFft.FftBuffer input = fft.newBuffer(w, h, texData);
      GlslFft.FftBuffer ping = fft.newBuffer(w, h);
      GlslFft.FftBuffer pong = fft.newBuffer(w, h);
      GlslFft.FftBuffer output = fft.newBuffer(w, h);

      List<FftPass<GlslFft.FftBuffer>> inverse = fft.inverse(input, ping, pong, output, w, h);
//      println("Inverse Passes: ");
//      for(FftPass<GlslFft.FftBuffer> pass : inverse) {
//         println(pass);
//      }
      fft.runPasses(inverse);

      ResultLayers layers = new ResultLayers();
      layers.layer0 = fft.loadLayer0(output);
      layers.layer1 = fft.loadLayer1(output);

      return layers;
   }

   private Complex c(float re, float im) {
      return new Complex(re, im);
   }

   public static void main(String[] args) {
      PApplet.main(GlslFftTest.class);
   }
}
