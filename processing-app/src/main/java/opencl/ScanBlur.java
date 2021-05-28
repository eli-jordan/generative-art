package opencl;

import com.jogamp.opencl.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Map;

public class ScanBlur {

   private static final String scan_blur_image = "scan_blur_image";

   private final CLContext context;
   final CLCommandQueue queue;
   private final Map<String, CLKernel> kernels;

   private final ScanImage2d sum;

   public ScanBlur(CLContext context, CLCommandQueue queue) {
      this.context = context;
      this.queue = queue;

      this.kernels = loadKernels(queue.getDevice());
      this.sum = new ScanImage2d(context, queue);
   }

   private Map<String, CLKernel> loadKernels(CLDevice device) {
      try {
         String path = "/cl-kernels/scan_blur.cl";
         CLProgram program = this.context.createProgram(getClass().getResourceAsStream(path));
         return program.build(CLProgram.CompilerOptions.FAST_RELAXED_MATH, device).createCLKernels();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public CLImage2d<?> blur(CLImage2d<?> in, CLImage2d<?> ping, CLImage2d<?> pong, int radius) {

      this.queue.finish();
      long startScan = System.currentTimeMillis();
      CLImage2d<?> scanData = this.sum.run(in, ping, pong);
      CLImage2d<?> blurOut = scanData.ID == ping.ID ? pong : ping;

      this.queue.finish();
      long endScan = System.currentTimeMillis();
      System.out.println("Scan Took: " + (endScan - startScan) + " ms");

//      this.queue.putReadImage(pong, true);
//      System.out.println("Scan Result");
//      print((FloatBuffer) pong.getBuffer(), in.width, in.height);
//      System.out.println("---");

      runBlurKernel(scanData, blurOut, radius, null);
      return blurOut;
   }

   public void runBlurKernel(CLImage2d<?> scanData, CLImage2d<?> out, int radius, CLEventList events) {
//      this.queue.finish();
//      long blurStart = System.currentTimeMillis();
      CLKernel kernel = this.kernels.get(scan_blur_image);
      kernel.rewind();
      kernel
          .putArg(scanData)
          .putArg(out)
          .putArg(radius)
          .putArg(out.width)
          .putArg(out.height);

      // TODO: 16x16 seems to work well here, but not when the global size < 16x16
      int localSizeX = 0;
      int localSizeY = 0;
      this.queue.put2DRangeKernel(
          kernel,
          0, 0,
          out.width, out.height,
          localSizeX, localSizeY,
          events
      );

//      this.queue.finish();
//      long blurEnd = System.currentTimeMillis();
//      System.out.println("ScanBlur.runBlurKernel(radius=" + radius + "): " + (blurEnd - blurStart) + " ms");
   }


   static void print(String pre, FloatBuffer buf) {
      int capacity = buf.capacity();
      float[] res = new float[capacity];
      buf.get(res);
      buf.rewind();
      System.out.println(pre + ": " + Arrays.toString(res));
   }

   static void print(FloatBuffer buf, int width, int height) {
      float[] result = new float[width * height];
      buf.get(result);
      buf.rewind();

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            System.out.print(result[idx] + " ");
         }
         System.out.println();
      }
   }
}
