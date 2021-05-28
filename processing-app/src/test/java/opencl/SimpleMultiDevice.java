package opencl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import turingpatterns_cl.ImageFormat;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Map;

public class SimpleMultiDevice {
   public static void main(String[] args) {
      CLContext context = CLContext.create();

      CLDevice intelGPU = Devices.getIntelGPU(context);
      CLCommandQueue intelQueue = intelGPU.createCommandQueue();

      CLDevice amdGPU = Devices.getAMDGPU(context);
      CLCommandQueue amdQueue = amdGPU.createCommandQueue();

      int width = 4096;
      int height = 4096;

      float[] input = new float[width*height];
      for(int i = 0; i < input.length; i++) {
         input[i] = 1;
      }

      Map<String, CLKernel> kernels = loadKernels(context);
      CLKernel multiply = kernels.get("multiply");
      CLKernel compute_sum = kernels.get("compute_sum");

      CLImage2d<FloatBuffer> in = context.createImage2d(Buffers.newDirectFloatBuffer(input), width, height, ImageFormat.forGridBuffer());
      CLImage2d<FloatBuffer> out0 = context.createImage2d(Buffers.newDirectFloatBuffer(width*height), width, height, ImageFormat.forGridBuffer());
      CLImage2d<FloatBuffer> out1 = context.createImage2d(Buffers.newDirectFloatBuffer(width*height), width, height, ImageFormat.forGridBuffer());
      CLImage2d<FloatBuffer> out2 = context.createImage2d(Buffers.newDirectFloatBuffer(width*height), width, height, ImageFormat.forGridBuffer());

      CLImage2d<FloatBuffer> result = context.createImage2d(Buffers.newDirectFloatBuffer(width*height), width, height, ImageFormat.forGridBuffer());

      CLEventList events = new CLEventList(3);

      intelQueue.putWriteImage(in, false);
      multiply.rewind();
      multiply.putArg(in).putArg(out0).putArg(2.0f);
      intelQueue.put2DRangeKernel(multiply,
          0, 0,
          width, height,
          0, 0,
          events
      );

      amdQueue.putWriteImage(in, false);
      multiply.rewind();
      multiply.putArg(in).putArg(out1).putArg(4.0f);
      amdQueue.put2DRangeKernel(multiply,
          0, 0,
          width, height,
          0, 0,
          events
      );

      amdQueue.putWriteImage(in, false);
      multiply.rewind();
      multiply.putArg(in).putArg(out2).putArg(8.0f);
      amdQueue.put2DRangeKernel(multiply,
          0, 0,
          width, height,
          0, 0,
          events
      );

      // Cross-device synchronisation
//      amdQueue.finish();
//      intelQueue.finish();

      System.out.println(events);

      compute_sum.rewind();
      compute_sum.putArg(out0).putArg(out1).putArg(out2).putArg(result);

      amdQueue.put2DRangeKernel(compute_sum,
          0, 0,
          width, height,
          0, 0,
          events,
          null
      );
      amdQueue.putReadImage(result, false);
      amdQueue.finish();

      float[] data = new float[width*height];
      result.getBuffer().get(data);

      for(int i = 0; i < 10; i++) {
         System.out.println(data[i]);
      }

      for(int i = 0; i < data.length; i++) {
         if(data[i] != 14) {
            System.out.println("Not 14 (" + data[i] + ") at " + i);
            break;
         }
      }
   }

   private static Map<String, CLKernel> loadKernels(CLContext context) {
      try {
         String path = "/cl-kernels/multi_device_test.cl";
         CLProgram program = context.createProgram(SimpleMultiDevice.class.getResourceAsStream(path));
         return program.build().createCLKernels();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
