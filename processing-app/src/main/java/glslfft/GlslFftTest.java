package glslfft;

import processing.core.PApplet;
import turingpatterns.Complex;
import turingpatterns.FFT;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class GlslFftTest extends PApplet {

   private GlslFft fft;

   @Override
   public void settings() {
      size(8, 8, P2D);
   }

   @Override
   public void setup() {
      fft = new GlslFft(this);

      try {
         for(Method m :this.getClass().getDeclaredMethods()) {
            if(m.getName().startsWith("test") && m.getParameterCount() == 0) {
               boolean success;
               try {
                  m.invoke(this);
                  success = true;
               } catch(Exception e) {
                  e.printStackTrace();
                  success = false;
               }
               if(success) {
                  println(m.getName() + ": ✅");
               } else {
                  println(m.getName() + ": ❌");
               }

            }
         }
      } finally {
         exit();
      }
   }

   public void testForwardFFT() {
      Complex[][] data = new Complex[][]{
          {c(0, 0), c(0, 0), c(1, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)}
      };

      Complex[][] expected = FFT.fft2d(data);
      Complex[][] actual = forward(data, 4, 4);

      assertEqual(expected, actual);
   }

   public void _testForwardThenInverseFFT() {
      Complex[][] data = new Complex[][]{
          {c(0, 0), c(0, 0), c(1, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)}
      };

      Complex[][] expected = FFT.ifft2d(FFT.fft2d(data));
      Complex[][] actual = inverse(forward(data, 4, 4), 4, 4);

      assertEqual(expected, actual);
   }

   public void _testForwardFFT_TopLeft() {
      Complex[][] data = new Complex[][]{
          {c(1, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)}
      };

      Complex[][] expected = FFT.fft2d(data);
      Complex[][] actual = forward(data, 4, 4);

      assertEqual(expected, actual);
   }

   public void _testForwardFFT_BottomLeft() {
      Complex[][] data = new Complex[][]{
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(1, 0), c(0, 0), c(0, 0), c(0, 0)}
      };

      Complex[][] expected = FFT.fft2d(data);
      Complex[][] actual = forward(data, 4, 4);

      assertEqual(expected, actual);
   }

   public void testForwardFFT_TopRight() {
      Complex[][] data = new Complex[][]{
          {c(0, 0), c(0, 0), c(0, 0), c(1, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)}
      };

      Complex[][] expected = FFT.fft2d(data);
      Complex[][] actual = forward(data, 4, 4);

      assertEqual(expected, actual);
   }

   public void _testForwardFFT_BottomRight() {
      Complex[][] data = new Complex[][]{
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(0, 0)},
          {c(0, 0), c(0, 0), c(0, 0), c(1, 0)}
      };

      Complex[][] expected = FFT.fft2d(data);
      Complex[][] actual = forward(data, 4, 4);

      assertEqual(expected, actual);
   }

   private void assertEqual(Complex[][] expected, Complex[][] actual) {
      boolean eq = Arrays.deepEquals(expected, actual);
      if (!eq) {
         throw new AssertionError("\nExpected: \n" + format(expected) + "\n Actual:\n" + format(actual));
      }
   }

   String format(Complex[][] data) {
      StringBuffer buffer = new StringBuffer();
      int ydim = data.length;
      int xdim = data[0].length;
      for (int y = 0; y < ydim; y++) {
         for (int x = 0; x < xdim; x++) {
            buffer.append(data[y][x] + ", ");
         }
         buffer.append("\n");
      }
      return buffer.toString();
   }


   private Complex[][] forward(Complex[][] data, int w, int h) {
      float[] fdata = new float[w * h * 4];
      for (int y = 0; y < w; y++) {
         for (int x = 0; x < w; x++) {
            int idx = 4 * (y * w + x);
            fdata[idx] = data[y][x].re;
            fdata[idx + 1] = data[y][x].im;
         }
      }
      GlslFft.FftBuffer input = fft.newBuffer(w, h, fdata);
      GlslFft.FftBuffer ping = fft.newBuffer(w, h);
      GlslFft.FftBuffer pong = fft.newBuffer(w, h);
      GlslFft.FftBuffer output = fft.newBuffer(w, h);

      List<FftPass<GlslFft.FftBuffer>> forward = fft.forward(input, ping, pong, output, w, h);
      println("Forward Passes: ");
      for(FftPass<GlslFft.FftBuffer> pass : forward) {
         println(pass);
      }

      fft.runPasses(forward);
      return fft.loadLayer0(output);
   }

   private Complex[][] inverse(Complex[][] data, int w, int h) {
      float[] fdata = new float[w * h * 4];
      for (int y = 0; y < w; y++) {
         for (int x = 0; x < w; x++) {
            int idx = 4 * (y * w + x);
            fdata[idx] = data[y][x].re;
            fdata[idx + 1] = data[y][x].im;
         }
      }
      GlslFft.FftBuffer input = fft.newBuffer(w, h, fdata);
      GlslFft.FftBuffer ping = fft.newBuffer(w, h);
      GlslFft.FftBuffer pong = fft.newBuffer(w, h);
      GlslFft.FftBuffer output = fft.newBuffer(w, h);

      List<FftPass<GlslFft.FftBuffer>> inverse = fft.inverse(input, ping, pong, output, w, h);
      println("Inverse Passes: ");
      for(FftPass<GlslFft.FftBuffer> pass : inverse) {
         println(pass);
      }
      fft.runPasses(inverse);
      return fft.loadLayer0(output);
   }

   private Complex c(float re, float im) {
      return new Complex(re, im);
   }

   public static void main(String[] args) {
      PApplet.main(GlslFftTest.class);
   }
}
