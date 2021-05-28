package turingpatterns_cl;

import com.jogamp.opencl.*;
import opencl.Devices;

import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * This example uses the java binding for OpenCL called jocl
 */
public class ArrayAddExample {

   /**
    * The below program is logically equivalent to the following.
    */
   private static void reference(float[] a, float[] b, float[] c) {
      for(int i = 0; i < a.length; i++) {
         c[i] = a[i] + b[i];
      }
   }

   public static void main(String[] args) {
      // First we create an OpenCL context, which is the entry point to the API
      CLContext context = CLContext.create();

      // The we use the meta-data API to find the device (or devices) we want to use.
      // In this case, I want to use the discreet GPU on my MacBook Pro.
      CLDevice amdGPU = Devices.getAMDGPU(context);
      assert amdGPU != null;

      // Then we create a command queue for that device
      // This is used to send commands to the device.
      // Commands are things like: Allocate memory, copy data to/from the CPU or run a kernel.
      CLCommandQueue queue = amdGPU.createCommandQueue();

      int globalSize = 1000000;

      // Here we load the source code for the kernel
      CLKernel arrayAddKernel = loadKernel(context, amdGPU);

      // Here we allocate data on the device
      CLBuffer<FloatBuffer> bufferA = context.createFloatBuffer(globalSize, CLMemory.Mem.READ_ONLY);
      CLBuffer<FloatBuffer> bufferB = context.createFloatBuffer(globalSize, CLMemory.Mem.READ_ONLY);
      CLBuffer<FloatBuffer> bufferC = context.createFloatBuffer(globalSize, CLMemory.Mem.WRITE_ONLY);

      fillBuffers(bufferA.getBuffer(), bufferB.getBuffer(), globalSize);

      long start = System.currentTimeMillis();

      // Copy input buffers to the GPU
      queue.putWriteBuffer(bufferA, false);
      queue.putWriteBuffer(bufferB, false);

      // Define the kernel's arguments
      arrayAddKernel.putArgs(bufferA, bufferB, bufferC);

      // Launch the kernel
      queue.put1DRangeKernel(arrayAddKernel, 0, globalSize, 10);

      // Copy the result back to the CPU
      queue.putReadBuffer(bufferC, true);

      long end = System.currentTimeMillis();

      // Verify the results
      float[] result = new float[globalSize];
      bufferC.getBuffer().get(result);

      for(int i = 0; i < globalSize; i++) {
         if(result[i] != 15.0f) {
            throw new RuntimeException("Incorrect result at index " + i);
         }
      }

      System.out.println("Successfully added " + globalSize + " values in " + (end - start) + "ms");
   }



   private static void fillBuffers(FloatBuffer bufferA, FloatBuffer bufferB, int size) {
      for (int i = 0; i < size; i++) {
         bufferA.put(5.0f);
         bufferB.put(10.0f);
      }
      bufferA.rewind();
      bufferB.rewind();
   }

   private static CLKernel loadKernel(CLContext context, CLDevice device) {
      try {
         String path = "/cl-kernels/array_add.cl";
         CLProgram program = context.createProgram(ArrayAddExample.class.getResourceAsStream(path));
         return program.build(device).createCLKernel("array_add_buffer");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
