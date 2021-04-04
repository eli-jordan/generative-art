package prefixsum;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

public class PrefixSumBlur {
   private final DwPixelFlow context;
   private final PrefixSum sum;
   private final DwGLSLProgram blur;

   public PrefixSumBlur(DwPixelFlow context) {
      this.context = context;
      this.sum = new PrefixSum(context);
      this.blur = this.context.createShader("prefixsum/striped-prefixsum-blur.frag");
   }

   public Buffer newBuffer(int width, int height) {
      return sum.newBuffer(width, height);
   }

   public Buffer newBuffer(int width, int height, FloatBuffer buffer) {
      return sum.newBuffer(width, height, buffer);
   }

   public float[][] blur(float[][] data, int radius) {
      int h = data.length;
      int w = data[0].length;
      Buffer input = sum.newBuffer(w, h, sum.prepare(data));
      Buffer ping = sum.newBuffer(w, h);
      Buffer pong = sum.newBuffer(w, h);

      List<PrefixSum.Pass<Buffer>> passes = sum.prefixSumPasses(input, ping, pong, w, h);
      Buffer out = sum.runPasses(passes);

//      System.out.println("PrefixSum");
//      print(sum.read(out));

      Buffer blurOut = out == ping ? pong : ping;

      blur(out, blurOut, w, h, radius);
      return this.sum.read(blurOut);
   }

   private void print(float[][] data) {
      for(float[] row : data) {
         System.out.println(Arrays.toString(row));
      }
   }

   public void blur(
       Buffer prefixSum,
       Buffer output,
       int width, int height,
       int radius) {
      this.context.begin();
      this.context.beginDraw(output.buf);

      this.blur.begin();
      this.blur.uniformTexture("prefixSum", prefixSum.buf);
      this.blur.uniform2f("resolution", 1.0f / width, 1.0f / height);
      this.blur.uniform1i("radius", radius);
      this.blur.uniform1i("width", width);
      this.blur.drawFullScreenQuad();

      this.blur.end();

      this.context.endDraw();
      this.context.end();

   }
}
