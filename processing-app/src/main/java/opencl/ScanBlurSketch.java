package opencl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLImage2d;
import com.jogamp.opencl.CLImageFormat;
import com.jogamp.opencl.util.CLInfo;
import processing.core.PApplet;

import java.nio.FloatBuffer;

public class ScanBlurSketch extends PApplet {


   @Override
   public void settings() {
      size(1024, 1024, P2D);
   }

   /**
    * This is a hacky test of the blur logic, that shuttles the data to and from the
    * CPU way too many times to be efficient, but its a useful test to ensure the blur
    * looks visually correct.
    */
   @Override
   public void setup() {
//      DwPixelFlow flow = new DwPixelFlow(this);
      background(0);
      noStroke();
      fill(255);
      ellipse(width / 2f, height / 2f, 200, 200);

//      StringBuilder b = new StringBuilder();
//      CLInfo.print(b);
//      println(b);

      CLContext context = CLContext.create();

      FloatBuffer buffer = Buffers.newDirectFloatBuffer(width * height);
      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            float v = red(pixels[idx]);
            buffer.put(v);
         }
      }
      buffer.rewind();

      ScanBlur blur = new ScanBlur(context);

      CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
      CLImage2d<FloatBuffer> in = context.createImage2d(buffer, width, height, format);
      CLImage2d<FloatBuffer> ping = context.createImage2d(Buffers.newDirectFloatBuffer(width*height), width, height, format);
      CLImage2d<FloatBuffer> pong = context.createImage2d(Buffers.newDirectFloatBuffer(width*height), width, height, format);

      for(int i = 0; i < 10; i++) {
         System.out.println("Run " + i);
         in.getBuffer().rewind();
         ping.getBuffer().rewind();
         pong.getBuffer().rewind();

         blur.queue.putWriteImage(in, false);

         long start = System.currentTimeMillis();
         CLImage2d<?> output = blur.blur(in, ping, pong, 300);
         blur.queue.finish();

         long end = System.currentTimeMillis();
         System.out.println("Time: " + (end - start) + " millis");

         blur.queue.putReadImage(output, false);
         blur.queue.finish();

         FloatBuffer outBuffer = (FloatBuffer) output.getBuffer();
         outBuffer.rewind();
         float[] data = new float[width * height];
         outBuffer.get(data);

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int idx = y * width + x;
               pixels[idx] = color(data[idx]);
            }
         }

         updatePixels();
      }



   }

   private void runWithBuffers(CLContext context, FloatBuffer buffer) {

      CLBuffer<FloatBuffer> input = context.createBuffer(buffer);
      CLBuffer<FloatBuffer> output = context.createFloatBuffer(width * height);
      ScanBlur blur = new ScanBlur(context);


      for (int i = 0; i < 10; i++) {
         println("Run " + i);
         long start = System.currentTimeMillis();
         input.getBuffer().rewind();
         output.getBuffer().rewind();
         blur.queue.putWriteBuffer(input, false);
         blur.blur(input, output, width, height, 300);

         long end = System.currentTimeMillis();
         System.out.println("Time: " + (end - start) + " millis");


         blur.queue.putReadBuffer(output, false);
         blur.queue.finish();
         float[] data = new float[width * height];
         output.getBuffer().get(data);

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int idx = y * width + x;
               pixels[idx] = color(data[idx]);
            }
         }

         updatePixels();
      }
   }

   public static void main(String[] args) {
      PApplet.main(ScanBlurSketch.class);
   }
}
