package opencl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import sun.plugin2.os.windows.FLASHWINFO;

import java.io.IOException;
import java.nio.FloatBuffer;

public class Transpose {

   private static final int BLOCK_DIM = 16;
   
   private final CLContext context;
   private final CLCommandQueue queue;
   private final CLKernel transposeKernel;

   public Transpose(CLContext context, CLCommandQueue queue) {
      this.context = context;
      this.queue = queue;
      this.transposeKernel = loadKernel(queue.getDevice());
   }

   private CLKernel loadKernel(CLDevice device) {
      try {
         String path = "/cl-kernels/transpose.cl";
         CLProgram program = this.context.createProgram(getClass().getResourceAsStream(path));
         return program.build(device).createCLKernel("transpose");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public void transpose(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, int width, int height) {
      int localMemoryBytes = Buffers.SIZEOF_FLOAT * (BLOCK_DIM + 1) * BLOCK_DIM;
      this.transposeKernel.rewind();
      this.transposeKernel
          .putArg(out)
          .putArg(in)
          .putArg(0)
          .putArg(width)
          .putArg(height)
          .putNullArg(localMemoryBytes);

      this.queue.put2DRangeKernel(
          transposeKernel,
          0, 0,
          width, height,
          16, 16);
   }
}
