package prefixsum;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.Copy;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwFilter;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

public class PrefixSumBlurRender extends PApplet {

   private DwPixelFlow context;
   private PrefixSum sum;
   private PrefixSumBlur blur;

   @Override
   public void settings() {
      size(512, 512, P2D);

   }

   @Override
   public void setup() {
      this.context = new DwPixelFlow(this);
      this.sum = new PrefixSum(context);
      this.blur = new PrefixSumBlur(context);

      DwGLSLProgram shader = this.context.createShader("prefixsum/turing-pattern-step.frag");

      float[][] input1 = createInput(200);
      Buffer buffer1 = sum.newBuffer(width, height, sum.prepare(input1));

      float[][] input2 = createInput(80);
      Buffer buffer2 = sum.newBuffer(width, height, sum.prepare(input2));


      Buffer out = sum.newBuffer(width, height);

      this.context.begin();
      this.context.beginDraw(out.buf);

      shader.begin();
      shader.uniformTexture("test[0]", buffer1.buf);
      shader.uniformTexture("test[1]", buffer2.buf);
      shader.uniform2f("resolution", 1.0f / width, 1.0f / height);
      shader.drawFullScreenQuad();

      shader.end();

      this.context.endDraw();
      this.context.end();

      Copy copy = DwFilter.get(context).copy;
      copy.apply(out.buf, (PGraphicsOpenGL) g);
   }

   public static void main(String[] args) {
      PApplet.main(PrefixSumBlurRender.class);
   }

   private float[][] createInput(int radius) {
      float[][] data = new float[height][width];
      for(int y = 0; y < height; y++) {
         for(int x = 0; x < width; x++) {
            int cx = x - height/2;
            int cy = y - width/2;
            double d = Math.sqrt(cx * cx + cy * cy);
            if(d < radius) {
               data[y][x] = 1.0f;
            } else {
               data[y][x] = 0.0f;
            }
         }
      }
      return data;
   }
}
