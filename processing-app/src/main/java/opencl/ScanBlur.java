package opencl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Map;

public class ScanBlur {

   private static final String scan_blur = "scan_blur";
   private static final String scan_blur_image = "scan_blur_image";

   private final CLContext context;
   final CLCommandQueue queue;
   private final Map<String, CLKernel> kernels;

   private final ScanBuffer scan;

   ScanBlur(CLContext context) {
      this.context = context;
      CLDevice device = getDevice(context);
      System.out.println("Device: " + device);
      this.queue = device.createCommandQueue();

      this.kernels = loadKernels(device);
      this.scan = new ScanBuffer(context, device, queue, 256);
   }

   private CLDevice getDevice(CLContext context) {
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

   private Map<String, CLKernel> loadKernels(CLDevice device) {
      try {
         String path = "/cl-kernels/scan_blur.cl";
         CLProgram program = this.context.createProgram(getClass().getResourceAsStream(path));
         return program.build(device).createCLKernels();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public void blur(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, int width, int height, int radius) {

      CLBuffer<FloatBuffer> scanData = this.context.createFloatBuffer(width * height);
      try {
         // Create sub-buffer for each row, and perform the scan operation on each row
         // using the sub-buffers
         scanRows(in, scanData, width, height);

         this.queue.putBarrier();

         boolean useImage = true;
         if (useImage) {
            blurWithImage(scanData, out, width, height, radius);
         } else {
            blurWithBuffer(scanData, out, width, height, radius);
         }


      } finally {
         scanData.release();
      }
   }

   private void blurWithBuffer(CLBuffer<FloatBuffer> scanData, CLBuffer<FloatBuffer> out, int width, int height, int radius) {
      long start = System.currentTimeMillis();
      CLKernel kernel = this.kernels.get(scan_blur);
      kernel.rewind();
      kernel
          .putArg(scanData)
          .putArg(out)
          .putArg(radius)
          .putArg(width)
          .putArg(height);

      // TODO: 16x16 seems to work well here, but not when the global size < 16x16
      int localSizeX = 16;
      int localSizeY = 16;
      this.queue.put2DRangeKernel(
          kernel,
          0, 0,
          width, height,
          localSizeX, localSizeY
      );

      this.queue.finish();
      long end = System.currentTimeMillis();
      System.out.println("kernel(scan_blur:buffer): " + (end - start) + " millis");
   }

   private void blurWithImage(CLBuffer<FloatBuffer> scanData, CLBuffer<FloatBuffer> out, int width, int height, int radius) {
      //TODO: Even with the copies necessary to move the prefix sum buffer
      // into an image, this is still faster than performing the blur
      // on the buffer.
      // Notes:
      //  - Its tricky to change the scan kernel to use images, since it relies on in-place
      //    updates.
      //  - There is no way (AFAIK) to view an image as a buffer or a buffer as an image, without copies.
      //    This makes sense, because images have an opaque storage format that allows spatially
      //    near looks to be cached.
      // Is there something else we can do here to avoid the copies?
      this.queue.finish();
      long startCopy = System.currentTimeMillis();
      CLImageFormat imageFormat = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
      CLImage2d<?> imageIn = context.createImage2d(width, height, imageFormat);
      CLImage2d<?> imageOut = context.createImage2d(width, height, imageFormat);
      this.queue.putCopyBufferToImage(scanData, imageIn);

      this.queue.finish();
      long start = System.currentTimeMillis();
      CLKernel kernel = this.kernels.get(scan_blur_image);
      kernel.rewind();
      kernel
          .putArg(imageIn)
          .putArg(imageOut)
          .putArg(radius)
          .putArg(width)
          .putArg(height);

      // TODO: 16x16 seems to work well here, but not when the global size < 16x16
      int localSizeX = 16;
      int localSizeY = 16;
      this.queue.put2DRangeKernel(
          kernel,
          0, 0,
          width, height,
          localSizeX, localSizeY
      );

      this.queue.finish();
      long end = System.currentTimeMillis();
      System.out.println("kernel(scan_blur:image:no-copies): " + (end - start) + " millis");

      this.queue.putCopyImageToBuffer(imageOut, out);
      this.queue.finish();
      long endCopy = System.currentTimeMillis();
      System.out.println("kernel(scan_blur:image:with-copies): " + (endCopy - startCopy) + " millis");
   }

   private void scanRows(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, int width, int height) {
      // We first copy the data, then perform the scan in-place
      this.queue.putCopyBuffer(in, out);

      this.queue.finish();
      long start = System.currentTimeMillis();
      // Create sub-buffer for each row, and perform the scan operation on each row
      // using the sub-buffers
      for (int y = 0; y < height; y++) {
         int offset = y * width;
         int size = width;
         CLSubBuffer<?> row = out.createSubBuffer(offset, size);
         this.scan.scan(row);

         // For some reason, if I don't put a read here the writes into
         // the sub-buffer are not visible to the parent buffer.
//         this.queue.putReadBuffer(out, false);
      }
      this.queue.finish();
      long end = System.currentTimeMillis();
      System.out.println("scanRows: " + (end - start) + " millis");
   }

   public static void main(String[] args) {
//      int width = 20;
//      int height = 20;
//      float[] input = new float[20 * 20];
//      input[9 * width + 10] = 1;
//      input[10 * width + 10] = 1;
//      input[11 * width + 10] = 1;
//      input[10 * width + 9] = 1;
//      input[10 * width + 11] = 1;

      int width = 4;
      int height = 4;
      float[] input = new float[] {
          1, 1, 1, 1,
          2, 2, 2, 2,
          3, 3, 3, 3,
          4, 4, 4, 4
      };

      CLContext context = CLContext.create();

//      CLDevice d = context.getMaxFlopsDevice(CLDevice.Type.GPU);
//      for(CLDevice device : context.getDevices()) {
//         final int maxComputeUnits     = device.getMaxComputeUnits();
//         final int maxClockFrequency   = device.getMaxClockFrequency();
//         final int flops = maxComputeUnits*maxClockFrequency;
//         System.out.println("Device: " + device);
//         System.out.println("     Max CUs: " + maxComputeUnits);
//         System.out.println("    Max Freq: " + maxClockFrequency);
//         System.out.println("       FLOPs: " + flops);
//         System.out.println("--------");
//      }


      CLBuffer<FloatBuffer> inBuf = context.createBuffer(Buffers.newDirectFloatBuffer(input));
      CLBuffer<FloatBuffer> outBuf = context.createFloatBuffer(width * height);
      ScanBlur blur = new ScanBlur(context);
      blur.queue.putWriteBuffer(inBuf, false);
//      blur.queue.putWriteBuffer(outBuf, false);
      blur.blur(inBuf, outBuf, width, height, 3);
//      blur.scanRows(inBuf, outBuf, width, height);
      blur.queue.putReadBuffer(outBuf, false);
      blur.queue.finish();

//      for (int sub = 0; sub < 10; sub++) {
//         FloatBuffer buf = (FloatBuffer) outBuf.getSubBuffers().get(sub).getBuffer();
//         float[] res = new float[10];
//         buf.get(res);
//         System.out.println("SubBuf(" + sub + "): " + Arrays.toString(res));
//      }

      System.out.println("\nResult");
      print(outBuf.getBuffer(), width, height);
      context.release();
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
