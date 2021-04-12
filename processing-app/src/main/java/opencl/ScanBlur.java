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
         return program.build(device).createCLKernels();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public CLImage2d<?> blur(CLImage2d<?> in, CLImage2d<?> ping, CLImage2d<?> pong, int radius) {

      this.queue.finish();
      long startScan = System.currentTimeMillis();
      CLImage2d<?> scanData = this.sum.run(in, ping, pong, in.width, in.height);
      CLImage2d<?> blurOut = scanData.ID == ping.ID ? pong : ping;

      this.queue.finish();
      long endScan = System.currentTimeMillis();
      System.out.println("Scan Took: " + (endScan - startScan) + " ms");

//      this.queue.putReadImage(pong, true);
//      System.out.println("Scan Result");
//      print((FloatBuffer) pong.getBuffer(), in.width, in.height);
//      System.out.println("---");

      runBlurKernel(scanData, blurOut, radius);
      return blurOut;
   }

   public void runBlurKernel(CLImage2d<?> scanData, CLImage2d<?> out, int radius) {
      this.queue.finish();
      long blurStart = System.currentTimeMillis();
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
          localSizeX, localSizeY
      );

      this.queue.finish();
      long blurEnd = System.currentTimeMillis();
//      System.out.println("ScanBlur.runBlurKernel(radius=" + radius + "): " + (blurEnd - blurStart) + " ms");
   }

//
//   public void blur(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, int width, int height, int radius) {
//
//      CLBuffer<FloatBuffer> scanData = this.context.createFloatBuffer(width * height);
//      try {
//         // Create sub-buffer for each row, and perform the scan operation on each row
//         // using the sub-buffers
//         scanRows(in, scanData, width, height);
//
//         this.queue.putBarrier();
//
//         boolean useImage = true;
//         if (useImage) {
//            blurWithImage(scanData, out, width, height, radius);
//         } else {
//            blurWithBuffer(scanData, out, width, height, radius);
//         }
//
//
//      } finally {
//         scanData.release();
//      }
//   }
//
//   private void blurWithBuffer(CLBuffer<FloatBuffer> scanData, CLBuffer<FloatBuffer> out, int width, int height, int radius) {
//      long start = System.currentTimeMillis();
//      CLKernel kernel = this.kernels.get(scan_blur);
//      kernel.rewind();
//      kernel
//          .putArg(scanData)
//          .putArg(out)
//          .putArg(radius)
//          .putArg(width)
//          .putArg(height);
//
//      // TODO: 16x16 seems to work well here, but not when the global size < 16x16
//      int localSizeX = 16;
//      int localSizeY = 16;
//      this.queue.put2DRangeKernel(
//          kernel,
//          0, 0,
//          width, height,
//          localSizeX, localSizeY
//      );
//
//      this.queue.finish();
//      long end = System.currentTimeMillis();
//      System.out.println("kernel(scan_blur:buffer): " + (end - start) + " millis");
//   }
//
//   private void blurWithImage(CLBuffer<FloatBuffer> scanData, CLBuffer<FloatBuffer> out, int width, int height, int radius) {
//      //TODO: Even with the copies necessary to move the prefix sum buffer
//      // into an image, this is still faster than performing the blur
//      // on the buffer.
//      // Notes:
//      //  - Its tricky to change the scan kernel to use images, since it relies on in-place
//      //    updates.
//      //  - There is no way (AFAIK) to view an image as a buffer or a buffer as an image, without copies.
//      //    This makes sense, because images have an opaque storage format that allows spatially
//      //    near looks to be cached.
//      // Is there something else we can do here to avoid the copies?
//      this.queue.finish();
//      long startCopy = System.currentTimeMillis();
//      CLImageFormat imageFormat = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
//      CLImage2d<?> imageIn = context.createImage2d(width, height, imageFormat);
//      CLImage2d<?> imageOut = context.createImage2d(width, height, imageFormat);
//      this.queue.putCopyBufferToImage(scanData, imageIn);
//
//      this.queue.finish();
//      long start = System.currentTimeMillis();
//      CLKernel kernel = this.kernels.get(scan_blur_image);
//      kernel.rewind();
//      kernel
//          .putArg(imageIn)
//          .putArg(imageOut)
//          .putArg(radius)
//          .putArg(width)
//          .putArg(height);
//
//      // TODO: 16x16 seems to work well here, but not when the global size < 16x16
//      int localSizeX = 16;
//      int localSizeY = 16;
//      this.queue.put2DRangeKernel(
//          kernel,
//          0, 0,
//          width, height,
//          localSizeX, localSizeY
//      );
//
//      this.queue.finish();
//      long end = System.currentTimeMillis();
//      System.out.println("kernel(scan_blur:image:no-copies): " + (end - start) + " millis");
//
//      this.queue.putCopyImageToBuffer(imageOut, out);
//      this.queue.finish();
//      long endCopy = System.currentTimeMillis();
//      System.out.println("kernel(scan_blur:image:with-copies): " + (endCopy - startCopy) + " millis");
//   }
//
//   private void scanRows(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, int width, int height) {
//      // We first copy the data, then perform the scan in-place
//      this.queue.putCopyBuffer(in, out);
//
//      this.queue.finish();
//      long start = System.currentTimeMillis();
//      // Create sub-buffer for each row, and perform the scan operation on each row
//      // using the sub-buffers
//      for (int y = 0; y < height; y++) {
//         int offset = y * width;
//         int size = width;
//         CLSubBuffer<?> row = out.createSubBuffer(offset, size);
//         this.scan.scan(row);
//
//         // For some reason, if I don't put a read here the writes into
//         // the sub-buffer are not visible to the parent buffer.
////         this.queue.putReadBuffer(out, false);
//      }
//      this.queue.finish();
//      long end = System.currentTimeMillis();
//      System.out.println("scanRows: " + (end - start) + " millis");
//   }


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
