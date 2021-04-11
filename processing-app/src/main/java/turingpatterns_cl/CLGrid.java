package turingpatterns_cl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import opencl.ScanImage2d;
import processing.core.PApplet;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;

public class CLGrid {

   private final CLContext context;
   private final CLKernel kernel;
   private final CLCommandQueue queue;

   int width, height;

   CLImage2d<?> grid;
//   CLImage2d<?> ping;
//   CLImage2d<?> pong;

   List<CLScale> scales;

   ScanImage2d scan;

   private float maxBump;


   public CLGrid(List<CLScale> scales, int width, int height, CLContext context, CLCommandQueue queue) {
      this.scales = scales;
      this.width = width;
      this.height = height;

      this.context = context;
      this.queue = queue;

      this.maxBump = 0f;
      for(CLScale scale :  scales) {
         if(scale.config.smallAmount > this.maxBump) {
            this.maxBump = scale.config.smallAmount;
         }
      }

      this.scan = new ScanImage2d(context, queue);

      this.kernel = loadKernel(queue.getDevice(), scales.size());

      this.grid = newImage();
   }

   private CLImage2d<?> newImage() {
      CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
      return context.createImage2d(Buffers.newDirectFloatBuffer(width * height), width, height, format);
   }

   private CLKernel loadKernel(CLDevice device, int scaleCount) {
      String source = KernelRender.render(scaleCount);
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

   Void update() {
      CLImage2d<?> ping = null;
      CLImage2d<?> pong = null;
      CLImage2d<?> newGrid = null;
      try {
         ping = newImage();
         pong = newImage();
         newGrid = newImage();

         CLImage2d<?> scanData = scan.run(this.grid, ping, pong, this.width, this.height);
         for (CLScale scale : this.scales) {
            scale.update(scanData);
         }
         runUpdateKernel(newGrid);

         this.grid.release();
         this.grid = newGrid;
      } finally {
         if (ping != null) ping.release();
         if (pong != null) pong.release();
      }


//      CLImage2d<?> newGrid;
//      CLImage2d<?> newPing;
//      CLImage2d<?> newPong;
//
//      if(scanData.ID == ping.ID) {
//         newGrid = this.pong;
//         newPing = this.grid;
//         newPong = this.ping;
//      } else {
//         newGrid = this.ping;
//         newPing = this.grid;
//         newPong = this.pong;
//      }
//
//      runUpdateKernel(newGrid);
//
//      this.grid = newGrid;
//      this.ping = newPing;
//      this.pong = newPong;

      return null;
   }

   private void runUpdateKernel(CLImage2d<?> output) {
      this.kernel.rewind();

      for(CLScale scale : this.scales) {
         this.kernel.putArg(scale.activator);
      }

      for(CLScale scale : this.scales) {
         this.kernel.putArg(scale.inhibitor);
      }

      for(CLScale scale : this.scales) {
         this.kernel.putArg(scale.config.smallAmount);
      }
      this.kernel
          .putArg(this.grid)
          .putArg(output)
          .putArg(this.maxBump);

      this.queue.put2DRangeKernel(
          this.kernel,
          0, 0,
          width, height,
          0, 0
      );
   }
}
