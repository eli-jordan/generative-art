package prefixsum;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import glslfft.AppletTest;
import processing.core.PApplet;

import java.util.Arrays;

public class PrefixSumBlurTest extends AppletTest {
   private PrefixSumBlur blur;

   @Override
   public void setup() {
      this.blur = new PrefixSumBlur(new DwPixelFlow(this));
      super.setup();
   }

   public void testIt() {
      float[][] input = createInput();
      println("Input");
      print(input);
      println();

      float[][] blurred = this.blur.blur(input, 1);
      println("Output");
      print(blurred);
   }

   private void print(float[][] data) {
      for(float[] row : data) {
         println(Arrays.toString(row));
      }
   }

   private float[][] createInput() {
      int w = 8;
      int h = 8;
      float[][] data = new float[h][w];
      for(int y = 0; y < h; y++) {
         for(int x = 0; x < w; x++) {
            int cx = x - h/2;
            int cy = y - w/2;
            double d = Math.sqrt(cx * cx + cy * cy);
            if(d < 2) {
               data[y][x] = 1.0f;
            } else {
               data[y][x] = 0.0f;
            }
         }
      }
      return data;
   }

   public static void main(String[] args) {
      PApplet.main(PrefixSumBlurTest.class);
   }
}
