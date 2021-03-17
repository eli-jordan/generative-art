package glslfft;

import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import processing.core.PApplet;
import turingpatterns.Complex;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.print;
import static processing.core.PApplet.println;

/**
 * This class provides a simplified interface to run an FFT on the GPU
 * using OpenGL and a GLSL fragment shader.
 *
 * This is a port of the glsl-fft javascript library that can be found here https://github.com/rreusser/glsl-fft
 */
public class GlslFft {

   private final DwPixelFlow context;
   private final DwGLSLProgram fft;

   public GlslFft(PApplet applet) {
      this.context = new DwPixelFlow(applet);
      this.fft = this.context.createShader("glslfft/fft.frag");
   }

   /**
    * Generate the configuration for each render pass that is necessary to calculate a forwards FFT.
    * <p>
    * These passes can then be run using {@link #runPasses(List)}
    */
   public <B> List<FftPass<B>> forward(
       B inputBuffer,
       B pingBuffer,
       B pongBuffer,
       B outputBuffer,
       int width,
       int height
   ) {
      return fftPasses(inputBuffer, pingBuffer, pongBuffer, outputBuffer, width, height, true);
   }

   /**
    * Generate the configuration for each render pass that is necessary to calculate an inverse FFT.
    * <p>
    * These passes can then be run using {@link #runPasses(List)}
    */
   public <B> List<FftPass<B>> inverse(
       B inputBuffer,
       B pingBuffer,
       B pongBuffer,
       B outputBuffer,
       int width,
       int height
   ) {
      return fftPasses(inputBuffer, pingBuffer, pongBuffer, outputBuffer, width, height, false);
   }

   private <B> List<FftPass<B>> fftPasses(
       B inputBuffer,
       B pingBuffer,
       B pongBuffer,
       B outputBuffer,
       int width,
       int height,
       boolean forward) {
      B ping = pingBuffer;
      B pong = pongBuffer;

      if (Integer.highestOneBit(width) != width) {
         throw new IllegalArgumentException("width (" + width + ") is not a power of 2");
      }

      if (Integer.highestOneBit(height) != height) {
         throw new IllegalArgumentException("height (" + height + ") is not a power of 2");
      }

      List<FftPass<B>> passes = new ArrayList<>();
      float xIterations = Math.round(Math.log(width) / Math.log(2));
      float yIterations = Math.round(Math.log(height) / Math.log(2));
      int iterations = (int) (xIterations + yIterations);

      for (int i = 0; i < iterations; i++) {
         FftPass<B> pass = new FftPass<>();
         pass.resolutionX = 1.0f / width;
         pass.resolutionY = 1.0f / height;

         pass.horizontal = i < xIterations;
         pass.forward = forward;

         // Set the input buffer
         if (i == 0) {
            pass.input = inputBuffer;
         } else {
            pass.input = ping;
         }

         // Set the output buffer
         if (i == iterations - 1) {
            pass.output = outputBuffer;
         } else {
            pass.output = pong;
         }

         if (i == 0) {
            if (pass.forward) {
               pass.normalization = 1;
            } else {
               pass.normalization = 1.0f / width / height;
            }
         } else {
            pass.normalization = 1;
         }

         pass.subtransformSize = (float) Math.pow(2, (pass.horizontal ? i : (i - xIterations)) + 1);

         passes.add(pass);

         // Swap the image buffers
         B tmp = ping;
         ping = pong;
         pong = tmp;
      }

      return passes;
   }

   /**
    * Performs the required OpenGL draw calls to execute each of the specified passes.
    */
   public void runPasses(List<FftPass<FftBuffer>> passes) {
      int count = 0;
      boolean debug = false;
      for (FftPass<FftBuffer> pass : passes) {
         this.context.begin();
         this.context.beginDraw(pass.output.buf);

         this.fft.begin();
         this.fft.uniform2f("resolution", pass.resolutionX, pass.resolutionY);

         this.fft.uniformTexture("src", pass.input.buf);
         this.fft.uniform1f("subtransformSize", pass.subtransformSize);
         this.fft.uniform1i("horizontal", pass.horizontal ? 0 : 1);
         this.fft.uniform1i("forward", pass.forward ? 0 : 1);
         this.fft.uniform1f("normalization", pass.normalization);
         this.fft.drawFullScreenQuad();

         this.fft.end();

         context.endDraw();
         context.end();

         if(debug) {
            println("------------- Pass " + count + ": Inputs -------------");
            printComplex(pass.input);

            println("------------- Pass " + count + ": Outputs -------------");
            printComplex(pass.output);
            println();
         }
         count++;
      }
   }

   /**
    * Flatten the complex number data so that it can be loaded into an OpenGL texture buffer.
    * We can pack two complex number matrices into the four channels of one texture.
    */
   public FloatBuffer prepare(Complex[][] data0, Complex[][] data1, int width, int height) {
      if (width != height) {
         throw new IllegalArgumentException("For some reason the glsl based FFT doesn't work on non-square data and I haven't debugged it yet");
      }
      float[] fdata = new float[width * height * 4];
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = 4 * (y * width + x);
            if (data0 != null) {
               fdata[idx] = data0[y][x].re;
               fdata[idx + 1] = data0[y][x].im;
            }

            if (data1 != null) {
               fdata[idx + 2] = data1[y][x].re;
               fdata[idx + 3] = data1[y][x].im;
            }
         }
      }
      return FloatBuffer.wrap(fdata);
   }


   /**
    * Loads the result that is in the R and G channels
    */
   public Complex[][] loadLayer0(GlslFft.FftBuffer fft) {
      return loadResult(fft, 0);
   }

   /**
    * Loads the result that is in the B and A channels
    */
   public Complex[][] loadLayer1(GlslFft.FftBuffer fft) {
      return loadResult(fft, 2);
   }

   private Complex[][] loadResult(GlslFft.FftBuffer fft, int offset) {
      int width = fft.buf.w;
      int height = fft.buf.h;
      Complex[][] result = new Complex[height][width];
      float[] data = fft.buf.getFloatTextureData(new float[width * height * 4]);
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = 4 * (y * fft.buf.w + x);
            float re = data[idx + offset];
            float im = data[idx + offset + 1];
            result[y][x] = new Complex(re, im);
         }
      }
      return result;
   }

   /**
    * Create a new buffer the the provided dimensions that is configured to hold 2 complex number matrices.
    */
   public FftBuffer newBuffer(int width, int height) {
      return new FftBuffer(this.context, width, height, null);
   }

   /**
    * Create a new buffer the the provided dimensions that is configured to hold 2 complex number matrices.
    * Initialised with the provided data.
    * <p>
    * The initalization data can be flattened into the required format using
    * {@link #prepare(Complex[][], Complex[][], int, int)}
    */
   public FftBuffer newBuffer(int width, int height, FloatBuffer data) {
      if (width != height) {
         throw new IllegalArgumentException("For some reason the glsl based FFT doesn't work on non-square data and I haven't debugged it yet");
      }
      return new FftBuffer(this.context, width, height, data);
   }

   private void printComplex(GlslFft.FftBuffer fft) {
      float[] data = fft.buf.getFloatTextureData(new float[4 * 4 * 4]);
      for (int y = 0; y < 4; y++) {
         for (int x = 0; x < 4; x++) {
            int idx = 4 * (y * fft.buf.w + x);
            float real = data[idx];
            float img = data[idx + 1];

            print(real + "+" + img + "j, ");
         }
         println();
      }
   }

   public static class FftBuffer {
      // RGBA texture, with 32-bit floats in each channel
      DwGLTexture buf = new DwGLTexture();

      private FftBuffer(DwPixelFlow context, int width, int height, FloatBuffer data) {
         this.buf.resize(
             context,
             GL2.GL_RGBA32F,
             width, height,
             GL2.GL_RGBA,
             GL2.GL_FLOAT,
             GL2.GL_NEAREST,
             GL2.GL_CLAMP_TO_EDGE,
             4,
             4,
             data
         );
         if (data == null) {
            this.buf.clear(0.0f);
         }
      }
   }
}
