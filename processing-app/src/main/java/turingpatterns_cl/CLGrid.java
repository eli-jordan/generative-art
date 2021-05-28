package turingpatterns_cl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLObject;
import com.jogamp.opencl.gl.CLGLTexture2d;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import opencl.ScanImage2d;
import processing.core.PApplet;

import java.nio.FloatBuffer;
import java.util.List;

public class CLGrid {

   public static class RenderBuffer {
      CLGLTexture2d<?> cl;
      DwGLTexture gl;

      public RenderBuffer(CLGLTexture2d<?> cl, DwGLTexture gl) {
         this.cl = cl;
         this.gl = gl;
      }
   }

   private PApplet applet;

   private final CLContext context;
   private final CLKernel kernel;
   private final CLCommandQueue queue;

   int width, height;

   CLImage2d<?> grid;
   CLImage2d<?> ping;
   CLImage2d<?> pong;

   RenderBuffer currentFrame;
   RenderBuffer nextFrame;

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

      this.kernel = loadKernel(scales.size());

      this.grid = newGridBuffer();
      this.ping = newGridBuffer();
      this.pong = newGridBuffer();

//      this.currentFrame = newRenderBuffer();
//      this.nextFrame = newRenderBuffer();
   }

   public void setRenderBuffers(RenderBuffer current, RenderBuffer next) {
      this.currentFrame = current;
      this.nextFrame = next;
   }

//   private CLImage2d<?> newRenderBuffer() {
//      CLImageFormat format = ImageFormat.forRenderBuffer();
//      return context.createImage2d(Buffers.newDirectFloatBuffer(width * height * 3), width, height, format);
//   }

   private CLImage2d<?> newGridBuffer() {
      CLImageFormat format = ImageFormat.forGridBuffer();
      return context.createImage2d(Buffers.newDirectFloatBuffer(width * height), width, height, format);
   }

   private CLKernel loadKernel(int scaleCount) {
      String source = TuringKernelRender.render(scaleCount);
      CLProgram program = this.context.createProgram(source);
      return program.build(CLProgram.CompilerOptions.FAST_RELAXED_MATH).createCLKernel("turing_update");
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

//      this.queue.finish();
//      long scaleUpdateStart = System.currentTimeMillis();

      CLEventList events = new CLEventList(this.scales.size());
      CLImage2d<?> scanData = scan.run(this.grid, ping, pong);
      for (CLScale scale : this.scales) {
         scale.update(scanData, events);
      }

//      this.queue.finish();
//      long scaleUpdateEnd = System.currentTimeMillis();
//      if(printTimers) System.out.println("Scale Update:  " + (scaleUpdateEnd - scaleUpdateStart) + " ms");


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

      runUpdateKernel(newGrid, events);
//      this.queue.finish();
//      long turingUpdateEnd = System.currentTimeMillis();
//      if(printTimers) System.out.println("Turing Update:  " + (turingUpdateEnd - scaleUpdateEnd) + " ms");

      this.grid = newGrid;
      this.ping = newPing;
      this.pong = newPong;

      // Swap the coloured render buffers
      RenderBuffer tmp = this.currentFrame;
      this.currentFrame = this.nextFrame;
      this.nextFrame = tmp;

      return null;
   }

   private void runUpdateKernel(CLImage2d<?> output, CLEventList condition) {
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
         //TODO: pre-allocate and reuse these buffers
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
          .putArg(this.currentFrame.cl)
          .putArg(this.nextFrame.cl);

      this.queue.putAcquireGLObject(this.currentFrame.cl);
      this.queue.putAcquireGLObject(this.nextFrame.cl);

      this.queue.put2DRangeKernel(
          this.kernel,
          0, 0,
          width, height,
          0, 0,
          condition,
          null
      );

      this.queue.putReleaseGLObject(this.currentFrame.cl);
      this.queue.putReleaseGLObject(this.nextFrame.cl);
   }
}
