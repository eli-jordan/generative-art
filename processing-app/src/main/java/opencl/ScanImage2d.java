package opencl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

   static FloatBuffer prepare(float[] input) {
      FloatBuffer buf = Buffers.newDirectFloatBuffer(input.length);
      buf.put(input);
      buf.rewind();
      return buf;
   }

   public static void main(String[] args) {
      CLContext context = CLContext.create();
      CLDevice device = getDevice(context);
      CLCommandQueue queue = device.createCommandQueue();

      int width = 1024;
      int height = 1024;

      float[] input = new float[width*height];
//      Arrays.fill(input, 1);
      Random random = new Random();
      for(int i = 0; i < input.length; i++) {
         input[i] = random.nextFloat();
      }

      ScanImage2d sum = new ScanImage2d(context, queue);
      CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
      FloatBuffer inputBuffer = prepare(input);
      CLImage2d<?> in = context.createImage2d(inputBuffer, width, height, format);
      CLImage2d<?> ping = context.createImage2d(inputBuffer, width, height, format);
      CLImage2d<?> pong = context.createImage2d(inputBuffer, width, height, format);

      sum.queue.putWriteImage(in, false);
      long start = System.currentTimeMillis();
      CLImage2d<?> out = sum.run(in, ping, pong, width, height);
      sum.queue.finish();

      long end = System.currentTimeMillis();

      System.out.println("Took: " + (end - start) + " ms");

      sum.queue.putReadImage(out, false);
      sum.queue.finish();

//      FloatBuffer buf = (FloatBuffer) out.getBuffer();
//      float[] result = new float[width*height];
//      buf.get(result);
//
//      for(int y = 0; y < height; y++) {
//         for(int x = 0; x < width; x++) {
//            int idx = y*width + x;
//            System.out.print(result[idx] + " ");
//         }
//         System.out.println();
//      }
   }

   private static CLDevice getDevice(CLContext context) {
      CLDevice d = null;
      for (CLDevice device : context.getDevices()) {
         if (device.getName().contains("AMD")) {
            d = device;
         }
      }
      return d;

//      return context.getMaxFlopsDevice(CLDevice.Type.GPU);
//      return context.getMaxFlopsDevice(CLDevice.Type.GPU);
   }
//
//   public Buffer newBuffer(int width, int height) {
//      return new Buffer(this.context, width, height, null);
//   }
//
//   public Buffer newBuffer(int width, int height, FloatBuffer buffer) {
//      return new Buffer(this.context, width, height, buffer);
//   }
//
//   public float[][] read(Buffer sum) {
//      int width = sum.buf.w;
//      int height = sum.buf.h;
//      float[][] result = new float[height][width];
//      float[] data = sum.buf.getFloatTextureData(new float[width * height]);
//      for (int y = 0; y < height; y++) {
//         for (int x = 0; x < width; x++) {
//            int idx = y * width + x;
//            result[y][x] = data[idx];
//         }
//      }
//      return result;
//   }

}
