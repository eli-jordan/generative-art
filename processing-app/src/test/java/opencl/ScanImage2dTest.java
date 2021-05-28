package opencl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.nio.FloatBuffer;

public class ScanImage2dTest {
   private static CLDevice device;
   private static CLContext context;


   @BeforeAll
   static void beforeAll() {
      context = CLContext.create();
      device = getDevice(context);
   }

   @AfterAll
   static void afterAll() {
      context.release();
   }

   private static CLDevice getDevice(CLContext context) {
//      CLDevice d = null;
//      for (CLDevice device : context.getDevices()) {
//         if (device.getName().contains("AMD")) {
//            d = device;
//         }
//      }
//      return d;

      return context.getMaxFlopsDevice(CLDevice.Type.CPU);
   }

   @Test
   public void scan_8x8_full_of_ones() {
      CLCommandQueue queue = device.createCommandQueue();
      try {
         int width = 8;
         int height = 8;

         float[] input = new float[] {
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
             1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f
         };

         ScanImage2d scan = new ScanImage2d(context, queue);

         CLImage2d<FloatBuffer> in = newScanBuffer(width, height);
         CLImage2d<FloatBuffer> ping = newScanBuffer(width, height);
         CLImage2d<FloatBuffer> pong = newScanBuffer(width, height);

         in.getBuffer().put(input);
         in.getBuffer().rewind();
         queue.putWriteImage(in, false);


         CLImage2d<FloatBuffer> result = scan.run(in, ping, pong);

         queue.putReadImage(result, false);
         queue.finish();

         float[] actual = new float[width*height];
         result.getBuffer().rewind();
         result.getBuffer().get(actual);

         print(result.getBuffer(), width, height);

         float[] expected = new float[] {
             1,2,3,4,5,6,7,8,
             1,2,3,4,5,6,7,8,
             1,2,3,4,5,6,7,8,
             1,2,3,4,5,6,7,8,
             1,2,3,4,5,6,7,8,
             1,2,3,4,5,6,7,8,
             1,2,3,4,5,6,7,8,
             1,2,3,4,5,6,7,8
         };

//         assertArrayEquals(expected, actual);


      } finally {
         queue.release();
      }
   }

   private CLImage2d<FloatBuffer> newScanBuffer(int width, int height) {
      CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
      return context.createImage2d(Buffers.newDirectFloatBuffer(width*height), width, height, format);
   }

   static void print(FloatBuffer buf, int width, int height) {
      buf.rewind();
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
