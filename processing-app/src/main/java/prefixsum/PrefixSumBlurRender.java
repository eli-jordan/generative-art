package prefixsum;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
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
      size(1024, 1024, P2D);

   }

   @Override
   public void setup() {
      this.context = new DwPixelFlow(this);
      this.sum = new PrefixSum(context);
      this.blur = new PrefixSumBlur(context);

      long start = System.currentTimeMillis();
      float[][] input = createInput(100);
      float[][] blurResult = this.blur.blur(input, 300);
      Buffer buffer = sum.newBuffer(width, height, sum.prepare(blurResult));

      Copy copy = DwFilter.get(context).copy;
      copy.apply(buffer.buf, (PGraphicsOpenGL) g);
      long end = System.currentTimeMillis();
      System.out.println("Time: " + (end - start) + " millis");
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
