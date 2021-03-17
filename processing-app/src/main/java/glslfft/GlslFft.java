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

public class GlslFft {

   private final PApplet applet;
   private final DwPixelFlow context;
   private final DwGLSLProgram fft;

   public GlslFft(PApplet applet) {
      this.applet = applet;
      this.context = new DwPixelFlow(applet);
      this.fft = context.createShader("glslfft/fft.frag");
   }

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

      // TODO: Check width and height are power of 2

      List<FftPass<B>> passes = new ArrayList<>();
      float xIterations = Math.round(Math.log(width) / Math.log(2));
      float yIterations = Math.round(Math.log(height) / Math.log(2));
      int iterations = (int) (xIterations + yIterations);

      for (int i = 0; i < iterations; i++) {
         FftPass<B> pass = new FftPass<>();
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

   public void runPasses(List<FftPass<FftBuffer>> passes) {
      int count = 0;
      for (FftPass<FftBuffer> pass : passes) {
         int w = pass.output.buf.w;
         int h = pass.output.buf.h;
         this.context.begin();
         this.context.beginDraw(pass.output.buf);

         this.fft.begin();
         this.fft.uniform2f("resolution", 1.0f / w, 1.0f / h);

         this.fft.uniformTexture("src", pass.input.buf);
         this.fft.uniform1f("subtransformSize", pass.subtransformSize);
         this.fft.uniform1i("horizontal", pass.horizontal ? 1 : 0);
         this.fft.uniform1i("forward", pass.forward ? 1 : 0);
         this.fft.uniform1f("normalization", pass.normalization);
         this.fft.drawFullScreenQuad();

         this.fft.end();

         context.endDraw();
         context.end();

//         println("------------- Pass " + count + ": Inputs -------------");
//         printComplex(pass.input);
//
//         println("------------- Pass " + count + ": Outputs -------------");
//         printComplex(pass.output);
//         println();

         count++;
      }
   }

   // Loads the result that is in the R and G channels
   public Complex[][] loadLayer0(GlslFft.FftBuffer fft) {
      return loadResult(fft, 0);
   }

   // Loads the result that is in the B and A channels
   public Complex[][] loadLayer1(GlslFft.FftBuffer fft) {
      return loadResult(fft, 2);
   }

   private Complex[][] loadResult(GlslFft.FftBuffer fft, int offset) {
      Complex[][] result = new Complex[fft.buf.h][fft.buf.w];
      float[] data = fft.buf.getFloatTextureData(new float[fft.buf.w * fft.buf.h * 4]);
      for(int y = 0; y < fft.buf.h; y++) {
         for(int x = 0; x < fft.buf.w; x++) {
            int idx = 4 * (y * fft.buf.w + x);
            float re = data[idx + offset];
            float im = data[idx + offset + 1];
            result[y][x] = new Complex(re, im);
         }
      }
      return result;
   }


   void printComplex(GlslFft.FftBuffer fft) {
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

   public FftBuffer newBuffer(int width, int height) {
      return new FftBuffer(this.context, width, height, null);
   }

   public FftBuffer newBuffer(int width, int height, float[] data) {
      return new FftBuffer(this.context, width, height, data);
   }

   public static class FftBuffer {
//      private final DwPixelFlow context;
//      private final int width;
//      private final int height;

      // RGBA texture, with 32-bit floats in each channel
      DwGLTexture buf = new DwGLTexture();

      private FftBuffer(DwPixelFlow context, int width, int height, float[] data) {
//         this.context = context;
//         this.width = width;
//         this.height = height;
         this.buf.resize(
             context,
             GL2.GL_RGBA32F,
             width, height,
             GL2.GL_RGBA,
             GL2.GL_FLOAT,
             GL2.GL_LINEAR,
             GL2.GL_CLAMP_TO_EDGE,
             4,
             4,
             data != null ? FloatBuffer.wrap(data) : null
         );
         if(data == null) {
            this.buf.clear(0.0f);
         }
      }

//      public void setData(float[] data) {
//         if (data.length != width * height * 4) {
//            throw new IllegalArgumentException("Data is not of the correct length");
//         }
//
//         this.buf.resize(
//             context,
//             GL2.GL_RGBA32F,
//             width, height,
//             GL2.GL_RGBA,
//             GL2.GL_FLOAT,
//             GL2.GL_LINEAR,
//             GL2.GL_CLAMP_TO_EDGE,
//             4,
//             4,
//             FloatBuffer.wrap(data)
//         );
//      }
   }
}
