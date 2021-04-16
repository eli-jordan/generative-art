package turingpatterns_cl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import opencl.ScanImage2d;
import processing.core.PApplet;

import java.nio.FloatBuffer;
import java.util.List;

public class CLGrid {

   private PApplet applet;

   private final CLContext context;
   private final CLKernel kernel;
   private final CLCommandQueue queue;

   int width, height;

   CLImage2d<?> grid;
   CLImage2d<?> ping;
   CLImage2d<?> pong;

   CLImage2d<?> currentFrame;
   CLImage2d<?> nextFrame;

   List<CLScale> scales;

   ScanImage2d scan;

   private float maxBump;


   public CLGrid(List<CLScale> scales, int width, int height, CLContext context, CLCommandQueue queue, PApplet applet) {
      this.scales = scales;
      this.width = width;
      this.height = height;

      this.context = context;
      this.queue = queue;
      this.applet = applet;

      this.maxBump = 0f;
      for (CLScale scale : scales) {
         if (scale.config.smallAmount > this.maxBump) {
            this.maxBump = scale.config.smallAmount;
         }
      }

      this.scan = new ScanImage2d(context, queue);

      this.kernel = loadKernel(queue.getDevice(), scales.size());

      this.grid = newGridBuffer();
      this.ping = newGridBuffer();
      this.pong = newGridBuffer();

      this.currentFrame = newRenderBuffer();
      this.nextFrame = newRenderBuffer();
   }

   private CLImage2d<?> newRenderBuffer() {
      CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.RGB, CLImageFormat.ChannelType.FLOAT);
      return context.createImage2d(Buffers.newDirectFloatBuffer(width * height * 3), width, height, format);
   }

   private CLImage2d<?> newGridBuffer() {
      CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
      return context.createImage2d(Buffers.newDirectFloatBuffer(width * height), width, height, format);
   }

   private CLKernel loadKernel(CLDevice device, int scaleCount) {
      String source = TuringKernelRender.render(scaleCount);
      CLProgram program = this.context.createProgram(source);
      return program.build(device).createCLKernel("turing_update");
   }

   /**
    * Initialise the grid to random values in the range [-1, 1]
    */
   Void initialise(PApplet applet) {
      FloatBuffer data = (FloatBuffer) this.grid.getBuffer();
      for (int i = 0; i < width * height; i++) {
         data.put(applet.random(-1.0f, 1.0f));
      }
      data.rewind();
      this.queue.putWriteImage(this.grid, false);
      return null;
   }

   interface Action {
      void apply();
   }

   Void update(boolean printTimers) {

      this.queue.finish();
      long scaleUpdateStart = System.currentTimeMillis();

      CLImage2d<?> scanData = scan.run(this.grid, ping, pong, this.width, this.height);
      for (CLScale scale : this.scales) {
         scale.update(scanData);
      }

      this.queue.finish();
      long scaleUpdateEnd = System.currentTimeMillis();
      if(printTimers) System.out.println("Scale Update:  " + (scaleUpdateEnd - scaleUpdateStart) + " ms");


      CLImage2d<?> newGrid;
      CLImage2d<?> newPing;
      CLImage2d<?> newPong;

      if (scanData.ID == ping.ID) {
         newGrid = this.pong;
         newPing = this.grid;
         newPong = this.ping;
      } else {
         newGrid = this.ping;
         newPing = this.grid;
         newPong = this.pong;
      }

//      System.out.println("Current: ping=" + this.ping.ID + ", pong=" + this.pong.ID + ", grid=" + this.grid.ID);
//      System.out.println("   Next: ping=" + newPing.ID + ", pong=" + newPong.ID + ", grid=" + newGrid.ID);


      runUpdateKernel(newGrid);
      this.queue.finish();
      long turingUpdateEnd = System.currentTimeMillis();
      if(printTimers) System.out.println("Turing Update:  " + (turingUpdateEnd - scaleUpdateEnd) + " ms");

      this.grid = newGrid;
      this.ping = newPing;
      this.pong = newPong;

      // Swap the coloured render buffers
      CLImage2d<?> tmp = this.currentFrame;
      this.currentFrame = this.nextFrame;
      this.nextFrame = tmp;

      return null;
   }

   private void timed(String name, Action thunk) {
      this.queue.finish();
      long start = System.currentTimeMillis();
      thunk.apply();
      this.queue.finish();
      long end = System.currentTimeMillis();
      System.out.println("Action(" + name + "): Took " + (end - start) + " ms");
   }

   private void runUpdateKernel(CLImage2d<?> output) {
      this.kernel.rewind();

      for (CLScale scale : this.scales) {
         this.kernel.putArg(scale.activator);
      }

      for (CLScale scale : this.scales) {
         this.kernel.putArg(scale.inhibitor);
      }

      for (CLScale scale : this.scales) {
         this.kernel.putArg(scale.config.smallAmount);
      }

      for (CLScale scale : this.scales) {
         float red = applet.red(scale.config.colour) / 255.0f;
         float green = applet.green(scale.config.colour) / 255.0f;
         float blue = applet.blue(scale.config.colour) / 255.0f;
         CLBuffer<FloatBuffer> colourBuffer = context.createBuffer(Buffers.newDirectFloatBuffer(new float[]{red, green, blue}));
         this.queue.putWriteBuffer(colourBuffer, false);
         this.kernel.putArg(colourBuffer);
      }

      this.kernel.putArg(this.maxBump);

      this.kernel
          .putArg(this.grid)
          .putArg(output);

      this.kernel
          .putArg(this.currentFrame)
          .putArg(this.nextFrame);

      this.queue.put2DRangeKernel(
          this.kernel,
          0, 0,
          width, height,
          0, 0
      );
   }
}
