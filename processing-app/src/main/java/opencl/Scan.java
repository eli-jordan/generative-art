package opencl;

import com.jogamp.opencl.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class Scan {

   private static final String scan_inc_subarrays = "scan_inc_subarrays";
   private static final String scan_subarrays = "scan_subarrays";
   private static final String scan_pad_to_pow2 = "scan_pad_to_pow2";
   private static final String scan_pow2_wrapper = "scan_pow2_wrapper";

   private final CLContext context;
   final CLCommandQueue queue;
   private final int workgroupSize;
   private final int m;

   private final Map<String, CLKernel> kernels;


   public Scan(CLContext context, CLDevice device, CLCommandQueue queue, int workgroupSize) {
      this.context = context;
      this.queue = queue;
      this.workgroupSize = workgroupSize;
      this.m = 2 * workgroupSize;
      this.kernels = loadKernels(context, device);
   }

   public static Scan create(CLContext context, int workgroupSize) {
      CLDevice device = context.getMaxFlopsDevice(CLDevice.Type.GPU);
      CLCommandQueue queue = device.createCommandQueue();
      return new Scan(context, device, queue, workgroupSize);
   }

   private static Map<String, CLKernel> loadKernels(CLContext context, CLDevice device) {
      try {
         InputStream stream = Scan.class.getResourceAsStream("/cl-kernels/scan.cl");
         CLProgram program = context.createProgram(stream).build(device);
         return program.createCLKernels();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * Performs and out-of-place exclusive prefix-scan on `in` writing the result to `out`.
    *
    * Note: The input buffer is assumed to already be on the device
    */
   public void scan(CLBuffer<?> in, CLBuffer<?> out) {
      // Since the scan is in-place, and we want to leave the source unmodified
      // we need to first copy the input to the output, then perform
      // the in-place operation on the output.
      this.queue.putCopyBuffer(in, out);
      this.scan(out);
   }

   /**
    * Performs an in-place exclusive prefix-scan on the provided buffer.
    *
    * Note: The buffer is assumed to already be on the device
    */
   public void scan(CLBuffer<?> inout) {
      boolean debug = false;
      if(debug) {
         this.queue.putReadBuffer(inout, false);
         this.queue.finish();
         ScanBlur.print("scan(input)", (FloatBuffer) inout.getBuffer());
      }
      recursiveScan(inout, inout.getCLCapacity());

      if(debug) {
         this.queue.putReadBuffer(inout, false);
         this.queue.finish();
         ScanBlur.print("scan(output)", (FloatBuffer) inout.getBuffer());
      }
   }

   /**
    * Performs an out-of-place exclusive scan on `in` and writes the result to `out`.
    */
   public void scan(float[] in, float[] out) {
      if(in.length != out.length) {
         throw new RuntimeException("Input and output are not of equal size");
      }

      int n = in.length;
      CLBuffer<FloatBuffer> buffer = this.context.createFloatBuffer(n);
      try {
         for(int i = 0; i < n; i++) {
            buffer.getBuffer().put(in[i]);
         }
         buffer.getBuffer().rewind();

         this.queue.putWriteBuffer(buffer, false);
         recursiveScan(buffer, n);
         this.queue.putReadBuffer(buffer, false);
         this.queue.finish();

         for(int i = 0; i < n; i++) {
            out[i] = buffer.getBuffer().get();
         }
      } finally {
         buffer.release();
      }
   }

   private void recursiveScan(CLMemory<?> data, int n) {
      int k = (int) Math.ceil((float) n / (float) m);

      //size of each subarray stored in local memory
      int localMemoryBytes = 4 * m;

      if (k == 1) {
         CLKernel kernel = kernels.get(scan_pad_to_pow2);
         kernel.rewind();
         kernel.putArg(data)
             .putNullArg(localMemoryBytes)
             .putArg(n);

         this.queue.put1DRangeKernel(kernel, 0, workgroupSize, workgroupSize);
      } else {
         int globalSize = k * workgroupSize;
         CLBuffer<?> partial = this.context.createBuffer(4 * k);
         try {
            CLKernel subArrays = kernels.get(scan_subarrays);
            subArrays.rewind();
            subArrays.putArg(data)
                .putNullArg(localMemoryBytes)
                .putArg(partial)
                .putArg(n);
            this.queue.put1DRangeKernel(subArrays, 0, globalSize, workgroupSize);
            recursiveScan(partial, k);

            CLKernel incArrays = kernels.get(scan_inc_subarrays);
            incArrays.rewind();
            incArrays.putArg(data)
                .putNullArg(localMemoryBytes)
                .putArg(partial)
                .putArg(n);
            this.queue.put1DRangeKernel(incArrays, 0, globalSize, workgroupSize);
         } finally {
            partial.release();
         }
      }
   }

   static void exclusive_scan(float[] input, float[] output) {
      int n = input.length;
      output[0] = 0;
      for (int i=1; i<n; i++) {
         output[i] = output[i-1] + input[i-1];
      }
   }

   static void inclusive_scan(float[] input, float[] output) {
      float sum = input[0];
      output[0] = input[0];
      for (int j = 1; j < input.length; j++) {
         sum += input[j];
         output[j] = sum;
      }
   }

   public static void main(String[] args) {
      Scan scan = Scan.create(CLContext.create(), 256);
      float[] in = randomValues(10000);
          //new float[] { 1, 2, 3, 4, 5, 6, 7, 8};
      float[] outExclusiveRef = new float[in.length];
      float[] outInclusiveRef = new float[in.length];
      float[] outCL = new float[in.length];

      scan.scan(in, outCL);

      exclusive_scan(in, outExclusiveRef);
      inclusive_scan(in, outInclusiveRef);




      if(!approxEqual(outExclusiveRef, outCL, 1.0f)) {
         System.out.println("❌ Not equal");
         System.out.println("Ref: " + Arrays.toString(outExclusiveRef));
         System.out.println(" CL: " + Arrays.toString(outCL));
      } else {
         System.out.println("✅ Equal");
      }


//      System.out.println("Result Reference (exclusive): " + Arrays.toString(outExclusiveRef));
//      System.out.println("   Result OpenCL (exclusive): " + Arrays.toString(outCL));
//      System.out.println("Result Reference (inclusive): " + Arrays.toString(outInclusiveRef));
   }

   private static boolean approxEqual(float[] expected, float[] actual, float tolerance) {
      boolean equal = true;
      float maxDiff = 0.0f;
      for(int i = 0; i < expected.length; i++) {
         float diff = Math.abs(expected[i] - actual[i]);
         if(diff > tolerance) {
            equal = false;
            maxDiff = Math.max(maxDiff, diff);
         }
      }
      System.out.println("Max Diff: " + maxDiff);
      return equal;
   }

   private static float[] randomValues(int count) {
      Random random = new Random();
      float[] data = new float[count];
      for(int i = 0; i < count; i++) {
         data[i] = random.nextFloat() * 100;
      }
      return data;
   }
}
