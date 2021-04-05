package opencl;
import static com.jogamp.opencl.CLMemory.Mem.*;
/*
package com.jogamp.opencl.demos.hellojocl;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Random;

import static java.lang.System.*;
import static com.jogamp.opencl.CLMemory.Mem.*;
import static java.lang.Math.*;

public class HelloJOCL {

   public static void main(String[] args) throws IOException {

      // set up (uses default CLPlatform and creates context for all devices)
      CLContext context = CLContext.create();
      out.println("created "+context);

      // always make sure to release the context under all circumstances
      // not needed for this particular sample but recommented
      try{

         // select fastest device
         CLDevice device = context.getMaxFlopsDevice();
         out.println("using "+device);

         // create command queue on device.
         CLCommandQueue queue = device.createCommandQueue();

         int elementCount = 1444477;                                  // Length of arrays to process
         int localWorkSize = min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
         int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize

         // load sources, create and build program
         CLProgram program = context.createProgram(HelloJOCL.class.getResourceAsStream("VectorAdd.cl")).build();

         // A, B are input buffers, C is for the result
         CLBuffer<FloatBuffer> clBufferA = context.createFloatBuffer(globalWorkSize, READ_ONLY);
         CLBuffer<FloatBuffer> clBufferB = context.createFloatBuffer(globalWorkSize, READ_ONLY);
         CLBuffer<FloatBuffer> clBufferC = context.createFloatBuffer(globalWorkSize, WRITE_ONLY);

         out.println("used device memory: "
             + (clBufferA.getCLSize()+clBufferB.getCLSize()+clBufferC.getCLSize())/1000000 +"MB");

         // fill input buffers with random numbers
         // (just to have test data; seed is fixed -> results will not change between runs).
         fillBuffer(clBufferA.getBuffer(), 12345);
         fillBuffer(clBufferB.getBuffer(), 67890);

         // get a reference to the kernel function with the name 'VectorAdd'
         // and map the buffers to its input parameters.
         CLKernel kernel = program.createCLKernel("VectorAdd");
         kernel.putArgs(clBufferA, clBufferB, clBufferC).putArg(elementCount);

         // asynchronous write of data to GPU device,
         // followed by blocking read to get the computed results back.
         long time = nanoTime();
         queue.putWriteBuffer(clBufferA, false)
             .putWriteBuffer(clBufferB, false)
             .put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
             .putReadBuffer(clBufferC, true);
         time = nanoTime() - time;

         // print first few elements of the resulting buffer to the console.
         out.println("a+b=c results snapshot: ");
         for(int i = 0; i < 10; i++)
            out.print(clBufferC.getBuffer().get() + ", ");
         out.println("...; " + clBufferC.getBuffer().remaining() + " more");

         out.println("computation took: "+(time/1000000)+"ms");

      }finally{
         // cleanup all resources associated with this context.
         context.release();
      }

   }

   private static void fillBuffer(FloatBuffer buffer, int seed) {
      Random rnd = new Random(seed);
      while(buffer.remaining() != 0)
         buffer.put(rnd.nextFloat()*100);
      buffer.rewind();
   }

   private static int roundUp(int groupSize, int globalSize) {
      int r = globalSize % groupSize;
      if (r == 0) {
         return globalSize;
      } else {
         return globalSize + groupSize - r;
      }
   }

}
 */

import com.jogamp.opencl.*;

import java.io.FileInputStream;
import java.nio.FloatBuffer;
import java.util.Random;

import static java.lang.System.*;

public class VectorAdd {
   public static void main(String[] args) throws Exception {
      CLContext context = CLContext.create();
      for (CLDevice device : context.getDevices()) {
         out.println("Device: " + device);
         out.println("Extensions: " + device.getExtensions());
         int maxH = device.getMaxImage2dHeight();
         int maxW = device.getMaxImage2dWidth();
         out.println("Max 2d Size: " + maxW + "x" + maxH);
         out.println("Maxx Compute Units: " + device.getMaxComputeUnits());
         out.println("Local Mem Type: " + device.getLocalMemType());
         out.println("Local Mem Size: " + device.getLocalMemSize());
         out.println("Global Mem Cache Type: " + device.getGlobalMemCacheType());
         out.println("Global Mem Cache Size: " + device.getGlobalMemCacheSize());

//         out.println("Max Samplers: " + device.getMaxSamplers());
//         out.println("Is double precision available? " + device.isDoubleFPAvailable());
//         out.println("Is GL memory sharing supported? " + device.isGLMemorySharingSupported());

         out.println("OpenCL Version: " + device.getVersion());

         String path = "/cl-kernels/VectorAdd.cl";
         CLProgram program = context.createProgram(VectorAdd.class.getResourceAsStream(path)).build(device);

         CLCommandQueue queue = device.createCommandQueue();


         int elementCount = 10000 * 10000; //1444477;                                  // Length of arrays to process
         int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
         int globalWorkSize = roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize


         if(device.getType() == CLDevice.Type.CPU) {
            localWorkSize = 1;
         }
//         out.println("localWorkSize: " + localWorkSize);
//         out.println("globalWorkSize: " + globalWorkSize);

         CLBuffer<FloatBuffer> clBufferA = context.createFloatBuffer(globalWorkSize, READ_ONLY);
         CLBuffer<FloatBuffer> clBufferB = context.createFloatBuffer(globalWorkSize, READ_ONLY);
         CLBuffer<FloatBuffer> clBufferC = context.createFloatBuffer(globalWorkSize, WRITE_ONLY);

         out.println("used device memory: "
             + (clBufferA.getCLSize() + clBufferB.getCLSize() + clBufferC.getCLSize()) / 1000000 + "MB");

         fillBuffer(clBufferA.getBuffer());
         fillBuffer(clBufferB.getBuffer());

         // get a reference to the kernel function with the name 'VectorAdd'
         // and map the buffers to its input parameters.
         CLKernel kernel = program.createCLKernel("VectorAdd");
         kernel.putArgs(clBufferA, clBufferB, clBufferC)
             .putArg(elementCount);


         queue.putWriteBuffer(clBufferA, false);
         queue.putWriteBuffer(clBufferB, false);
         queue.finish();

         long start = nanoTime();
         queue.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize);
         queue.finish();
         long end = nanoTime();
//         queue.putReadBuffer(clBufferC, false);

         out.println("computation took: " + ((end - start) / 1000000) + "ms");

         clBufferA.release();
         clBufferB.release();
         clBufferC.release();
         out.println("-------------------------------------------------------\n");
      }
   }

   private static void fillBuffer(FloatBuffer buffer) {
      Random rnd = new Random();
      while(buffer.remaining() != 0) {
         buffer.put(rnd.nextFloat()*100);
      }
      buffer.rewind();
   }

   private static int roundUp(int groupSize, int globalSize) {
      int r = globalSize % groupSize;
      if (r == 0) {
         return globalSize;
      } else {
         return globalSize + groupSize - r;
      }
   }
}
