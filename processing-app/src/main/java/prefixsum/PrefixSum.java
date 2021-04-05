package prefixsum;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class PrefixSum {

   private final DwPixelFlow context;
   private final DwGLSLProgram sum;


   public PrefixSum(DwPixelFlow context) {
      this.context = context;
      this.sum = this.context.createShader("prefixsum/prefixsum.frag");
   }

   public Buffer run(Buffer input, Buffer pingBuf, Buffer pingPong, int width, int height) {
      List<Pass<Buffer>> passes = prefixSumPasses(input, pingBuf, pingPong, width, height);
      return runPasses(passes);
   }

   public <B> List<Pass<B>> prefixSumPasses(B input, B pingBuf, B pingPong, int width, int height) {
      if (Integer.highestOneBit(width) != width) {
         throw new IllegalArgumentException("width (" + width + ") is not a power of 2");
      }

      if (Integer.highestOneBit(height) != height) {
         throw new IllegalArgumentException("height (" + height + ") is not a power of 2");
      }

      B ping = pingBuf;
      B pong = pingPong;
      List<Pass<B>> passes = new ArrayList<>();
      int passCount = (int) (Math.log(width) / Math.log(2));
      for (int i = 0; i < passCount; i++) {
         Pass<B> pass = new Pass<>();

         if(i == 0) {
            pass.input = input;
         } else {
            pass.input = ping;
         }
         pass.output = pong;

         pass.resolutionX = 1.0f / width;
         pass.resolutionY = 1.0f / height;
         pass.stride = (int) Math.pow(2, i);

         passes.add(pass);

         // Swap the buffers
         B tmp = ping;
         ping = pong;
         pong = tmp;
      }

      return passes;
   }

   public Buffer runPasses(List<Pass<Buffer>> passes) {
      Buffer output = null;
      for (Pass<Buffer> pass : passes) {
         this.context.begin();
         this.context.beginDraw(pass.output.buf);

         this.sum.begin();
         this.sum.uniformTexture("src", pass.input.buf);
         this.sum.uniform2f("resolution", pass.resolutionX, pass.resolutionY);
         this.sum.uniform1i("stride", pass.stride);
         this.sum.drawFullScreenQuad();

         this.sum.end();

         context.endDraw();
         context.end();
         output = pass.output;
      }

      return output;
   }

   public static class Pass<B> {
      B input;
      B output;
      float resolutionX;
      float resolutionY;
      int stride;

      @Override
      public String toString() {
         return "Pass{" +
             "input=" + input +
             ", output=" + output +
             ", resolutionX=" + resolutionX +
             ", resolutionY=" + resolutionY +
             ", stride=" + stride +
             '}';
      }
   }

   FloatBuffer prepare(float[][] input) {
      int height = input.length;
      int width = input[0].length;
      float[] buf = new float[width * height];
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            buf[idx] = input[y][x];
         }
      }
      return FloatBuffer.wrap(buf);
   }

   public Buffer newBuffer(int width, int height) {
      return new Buffer(this.context, width, height, null);
   }

   public Buffer newBuffer(int width, int height, FloatBuffer buffer) {
      return new Buffer(this.context, width, height, buffer);
   }

   public float[][] read(Buffer sum) {
      int width = sum.buf.w;
      int height = sum.buf.h;
      float[][] result = new float[height][width];
      float[] data = sum.buf.getFloatTextureData(new float[width * height]);
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            result[y][x] = data[idx];
         }
      }
      return result;
   }

}
