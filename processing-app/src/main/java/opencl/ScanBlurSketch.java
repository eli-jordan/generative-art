package opencl;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;
import processing.core.PApplet;

import java.nio.FloatBuffer;

public class ScanBlurSketch extends PApplet {


   @Override
   public void settings() {
      size(8192, 8192, P2D);
   }

   /**
    * This is a hacky test of the blur logic, that shuttles the data to and from the
    * CPU way too many times to be efficient, but its a useful test to ensure the blur
    * looks visually correct.
    */
   @Override
   public void setup() {
//      DwPixelFlow flow = new DwPixelFlow(this);
      background(0);
      noStroke();
      fill(255);
      ellipse(width / 2f, height / 2f, 200, 200);

      CLContext context = CLContext.create();
      CLBuffer<FloatBuffer> input = context.createFloatBuffer(width * height);
      CLBuffer<FloatBuffer> output = context.createFloatBuffer(width * height);

      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            float v = red(pixels[idx]);
            input.getBuffer().put(v);
         }
      }

      ScanBlur blur = new ScanBlur(context);

      for (int i = 0; i < 10; i++) {
         println("Run " + i);
         input.getBuffer().rewind();
         output.getBuffer().rewind();
         blur.queue.putWriteBuffer(input, false);
         long start = System.currentTimeMillis();
         blur.blur(input, output, width, height, 300);
         long end = System.currentTimeMillis();

         blur.queue.putReadBuffer(output, false);
         blur.queue.finish();

         System.out.println("Time: " + (end - start) + " millis");

         float[] data = new float[width * height];
         output.getBuffer().get(data);

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int idx = y * width + x;
               pixels[idx] = color(data[idx]);
            }
         }

         updatePixels();
      }
   }

   public static void main(String[] args) {
      PApplet.main(ScanBlurSketch.class);
   }
}
