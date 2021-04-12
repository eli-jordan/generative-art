package opencl;

import com.jogamp.opencl.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScanImage2d {
   private final CLContext context;
   private final CLCommandQueue queue;
   private final CLKernel kernel;

   public ScanImage2d(CLContext context, CLCommandQueue queue) {
      this.context = context;
      this.queue = queue;
      this.kernel = loadKernel(queue.getDevice());
   }

   private CLKernel loadKernel(CLDevice device) {
      try {
         String path = "/cl-kernels/scan_image2d.cl";
         CLProgram program = this.context.createProgram(getClass().getResourceAsStream(path));
         return program.build(device).createCLKernel("scan_image2d");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public CLImage2d<?> run(CLImage2d<?> input, CLImage2d<?> pingBuf, CLImage2d<?> pingPong, int width, int height) {
      List<Pass<CLImage2d<?>>> passes = prefixSumPasses(input, pingBuf, pingPong, width, height);
      this.queue.finish();
      long start = System.currentTimeMillis();
      CLImage2d<?> result = runPasses(passes, width, height);

      this.queue.finish();
      long end = System.currentTimeMillis();
//      System.out.println("ScanImage2d.run: Took " + (end-start) + " ms");

      return result;
   }

   public <B> List<Pass<B>> prefixSumPasses(B input, B pingBuf, B pingPong, int width, int height) {
      if (Integer.highestOneBit(width) != width) {
         throw new IllegalArgumentException("width (" + width + ") is not a power of 2");
      }

      if (Integer.highestOneBit(height) != height) {
         throw new IllegalArgumentException("height (" + height + ") is not a power of 2");
      }

      B ping = pingBuf;
      B pong = pingPong;
      List<Pass<B>> passes = new ArrayList<>();
      int passCount = (int) (Math.log(width) / Math.log(2));
      for (int i = 0; i < passCount; i++) {
         Pass<B> pass = new Pass<>();

         if (i == 0) {
            pass.input = input;
         } else {
            pass.input = ping;
         }
         pass.output = pong;
         pass.stride = (int) Math.pow(2, i);

         passes.add(pass);

         // Swap the buffers
         B tmp = ping;
         ping = pong;
         pong = tmp;
      }

      return passes;
   }

   public CLImage2d<?> runPasses(List<Pass<CLImage2d<?>>> passes, int width, int height) {
      CLImage2d<?> output = null;
      for (Pass<CLImage2d<?>> pass : passes) {
         this.kernel.rewind();
         this.kernel
             .putArg(pass.input)
             .putArg(pass.output)
             .putArg(pass.stride);

         int localSizeX = 0;
         int localSizeY = 0;
         this.queue.put2DRangeKernel(
             kernel,
             0, 0,
             width, height,
             localSizeX, localSizeY
         );

         output = pass.output;
      }

      return output;
   }

   public static class Pass<B> {
      B input;
      B output;
      int stride;

      @Override
      public String toString() {
         return "Pass{" +
             "input=" + input +
             ", output=" + output +
             ", stride=" + stride +
             '}';
      }
   }
}
