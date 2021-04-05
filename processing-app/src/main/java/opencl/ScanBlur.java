package opencl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class ScanBlur {

   private static final String scan_blur = "scan_blur";

   private final CLContext context;
   private final CLCommandQueue queue;
   private final Map<String, CLKernel> kernels;

   private Scan scan;

   ScanBlur(CLContext context) {
      this.context = context;
      CLDevice device = getDevice(context);
      this.queue = device.createCommandQueue();

      this.kernels = loadKernels(device);
      this.scan = new Scan(context, device, queue, 256);
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

         // Ensure the row scans have completed before using the result to perform a blur
//         this.queue.finish();

         //debug
//         this.queue.putReadBuffer(scanData, false);
//         System.out.println("Scan Result");
//         print(scanData.getBuffer(), width, height);
//         System.out.println("---");


         CLKernel kernel = this.kernels.get(scan_blur);
         kernel
             .putArg(scanData)
             .putArg(out)
             .putArg(radius)
             .putArg(width)
             .putArg(height);

         this.queue.put2DRangeKernel(
             kernel,
             0, 0,
             width, height,
             0, 0
         );

         // TODO: Remove this read
         this.queue.putReadBuffer(out, false);
         this.queue.finish();
      } finally {
         scanData.release();
      }
   }

   private void scanRows(CLBuffer<FloatBuffer> in, CLBuffer<FloatBuffer> out, int width, int height) {
      // We first copy the data, then perform the scan in-place
      this.queue.putCopyBuffer(in, out);

      // Create sub-buffer for each row, and perform the scan operation on each row
      // using the sub-buffers
      for (int y = 0; y < height; y++) {
         int offset = y * width;
         int size = width;
         CLSubBuffer<?> row = out.createSubBuffer(offset, size);
         this.scan.scan(row);

         // For some reason, if I don't put a read here the writes into
         // the sub-buffer are not visible to the parent buffer.
         this.queue.putReadBuffer(out, false);
      }
//      this.queue.finish();
//      System.out.println("Scan Result");
//      print(out.getBuffer(), width, height);
   }

   public static void main(String[] args) {
      int width = 20;
      int height = 20;
      float[] input = new float[20*20];
      input[9*width + 10] = 1;
      input[10*width + 10] = 1;
      input[11*width + 10] = 1;
      input[10*width + 9] = 1;
      input[10*width + 11] = 1;

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
