package turingpatterns_cl;

import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import processing.core.PApplet;
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

   private CLGrid grid;

   @Override
   public void settings() {
      size(2048, 1024);
   }

   @Override
   public void setup() {
      this.context = CLContext.create();
      this.device = getDevice(this.context);
      this.queue = this.device.createCommandQueue();

      List<ScaleConfig> configs = Arrays.asList(
          ScaleConfig.newBuilder()
              .size(width, height)
              .activatorRadius(10)
              .inhibitorRadius(20)
              .smallAmount(0.05f)
              .build(),

          ScaleConfig.newBuilder()
              .size(width, height)
              .activatorRadius(40)
              .inhibitorRadius(100)
              .smallAmount(0.05f)
              .build(),

          ScaleConfig.newBuilder()
              .size(width, height)
              .activatorRadius(125)
              .inhibitorRadius(250)
              .smallAmount(0.05f)
              .build()
      );

      List<CLScale> scales = new ArrayList<>();
      for(ScaleConfig config : configs) {
         scales.add(new CLScale(config, this.context, this.queue));
      }

      this.grid = new CLGrid(scales, width, height, this.context, this.queue);
      timed("initialise", () -> this.grid.initialise(this));

//      frameRate(1);
   }

   @Override
   public void draw() {
      long drawStart = System.currentTimeMillis();

      if(frameCount % 60 == 0) {
         System.out.println("FrameRate: " + frameRate);
      }

//      timed("Grid.update", () -> this.grid.update());

      this.grid.update();

      long start = System.currentTimeMillis();
      this.queue.putReadImage(this.grid.grid, false);
      this.queue.finish();

      FloatBuffer outBuffer = (FloatBuffer) this.grid.grid.getBuffer();
      outBuffer.rewind();
      float[] data = new float[width * height];
      outBuffer.get(data);

      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            pixels[idx] = color(map(data[idx], -1.0f, 1.0f, 0, 255));
         }
      }

      updatePixels();

      long end = System.currentTimeMillis();
      if(frameCount % 60 == 0) {
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
