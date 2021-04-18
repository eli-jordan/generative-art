package turingpatterns_cl;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import processing.core.PApplet;
import turingpatterns.ScaleConfiguarions;
import turingpatterns.config.ScaleConfig;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class CLTuringSketch extends PApplet {
   private CLContext context;
   private CLDevice device;
   private CLCommandQueue queue;

   private long startTs;

   private CLGrid grid;

   @Override
   public void settings() {
      size(1024, 1024);
      this.startTs = System.currentTimeMillis();
   }

   @Override
   public void setup() {
      this.context = CLContext.create();
      this.device = getDevice(this.context);
      this.queue = this.device.createCommandQueue();

      ScaleConfiguarions configs = new ScaleConfiguarions(this);
      List<ScaleConfig.Builder> builders = configs.pastelPaletteWithSymmetry(1);

//      List<ScaleConfig> configs = Arrays.asList(
//          ScaleConfig.newBuilder()
//              .size(width, height)
//              .activatorRadius(10)
//              .inhibitorRadius(20)
//              .smallAmount(0.05f)
//              .colour(color(255, 0, 0))
//              .build(),
//
//          ScaleConfig.newBuilder()
//              .size(width, height)
//              .activatorRadius(40)
//              .inhibitorRadius(100)
//              .smallAmount(0.05f)
//              .colour(color(0, 255, 0))
//              .build(),
//
//          ScaleConfig.newBuilder()
//              .size(width, height)
//              .activatorRadius(125)
//              .inhibitorRadius(250)
//              .smallAmount(0.05f)
//              .colour(color(0, 0, 255))
//              .build()
//      );

      List<CLScale> scales = new ArrayList<>();
      for (ScaleConfig.Builder config : builders) {
         scales.add(new CLScale(config.build(), this.context, this.queue));
      }

      this.grid = new CLGrid(scales, width, height, this.context, this.queue, this);
      timed("initialise", () -> this.grid.initialise(this));

//      frameRate(1);
   }

   @Override
   public void draw() {

      if (frameCount % 10 == 0) {
         long runTime = System.currentTimeMillis() - this.startTs;
         println("Frame Rate: " + frameRate + ", Frame Count: " + frameCount + ", Running Time: " + runTime + " ms");
      }

      long drawStart = System.currentTimeMillis();

      boolean printMetrics = frameCount % 60 == 0;

      if (printMetrics) {
         System.out.println("FrameRate: " + frameRate);
      }

      this.grid.update(printMetrics);

      //TODO: CLGL interop to render the result

      long start = System.currentTimeMillis();
//      this.grid.grid.getBuffer().rewind();
//      this.queue.putReadImage(this.grid.grid, false);
//      this.queue.finish();
//
//      FloatBuffer outBuffer = (FloatBuffer) this.grid.grid.getBuffer();
//      outBuffer.rewind();
//      float[] data = new float[width * height];
//      outBuffer.get(data);
//
//      loadPixels();
//      for (int y = 0; y < height; y++) {
//         for (int x = 0; x < width; x++) {
//            int idx = y * width + x;
//            pixels[idx] = color(map(data[idx], -1.0f, 1.0f, 0, 255));
//         }
//      }
//
//      updatePixels();

      this.grid.currentFrame.getBuffer().rewind();
      this.queue.putReadImage(this.grid.currentFrame, false);
      this.queue.finish();

      FloatBuffer outBuffer = (FloatBuffer) this.grid.currentFrame.getBuffer();
      outBuffer.rewind();
      float[] data = new float[width * height * 3]; // RGB channels are included
      outBuffer.get(data);

//      println("Read data length: " + data.length);

      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int pixelIdx = y * width + x;
            int dataIdx = 3*pixelIdx;

//            println(idx);
            pixels[pixelIdx] = color(data[dataIdx + 0] * 255.0f, data[dataIdx + 1] * 255.0f, data[dataIdx + 2] * 255.0f);
         }
      }

      updatePixels();

      long end = System.currentTimeMillis();
      if (printMetrics) {
         System.out.println("Draw Took: " + (end - drawStart) + " ms");
         System.out.println("Blit Took: " + (end - start) + " ms");
      }
   }

   private void timed(String name, Supplier<Void> thunk) {
      this.queue.finish();
      long start = System.currentTimeMillis();
      thunk.get();
      this.queue.finish();
      long end = System.currentTimeMillis();
      println("Action(" + name + "): Took " + (end - start) + " ms");
   }

   private CLDevice getDevice(CLContext context) {
      CLDevice d = null;
      for (CLDevice device : context.getDevices()) {
         if (device.getName().contains("AMD")) {
            d = device;
         }
      }
      return d;
   }

   public static void main(String[] args) {
      PApplet.main(CLTuringSketch.class);
   }
}
