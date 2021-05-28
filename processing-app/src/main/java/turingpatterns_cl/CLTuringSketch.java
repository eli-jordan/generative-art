package turingpatterns_cl;

import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLContext;
import com.jogamp.opencl.gl.CLGLTexture2d;
import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.Copy;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwFilter;
import opencl.Devices;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import turingpatterns.ScaleConfiguarions;
import turingpatterns.config.ScaleConfig;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class CLTuringSketch extends PApplet {
   private CLGLContext context;
   private CLCommandQueue queue;
   private DwPixelFlow pixelFlow;

   private long startTs;

   private CLGrid grid;

   @Override
   public void settings() {
//      pixelDensity(1);
      size(1024, 1024, P2D);
      this.startTs = System.currentTimeMillis();
   }

   @Override
   public void setup() {
      this.pixelFlow = new DwPixelFlow(this);

      this.context = CLGLContext.create(pixelFlow.pjogl.context);

      CLDevice amdGPU = Devices.getAMDGPU(context);
      assert amdGPU != null;
      this.queue = amdGPU.createCommandQueue();

      ScaleConfiguarions configs = new ScaleConfiguarions(this);
      List<ScaleConfig.Builder> builders = configs.pastelPaletteWithSymmetry(1.0f);

      List<CLScale> scales = new ArrayList<>();
      for (ScaleConfig.Builder config : builders) {
         scales.add(new CLScale(config.build(), this.context, this.queue));
      }

      this.grid = new CLGrid(scales, pixelWidth, pixelHeight, this.context, this.queue, this);
      this.grid.setRenderBuffers(createRenderBuffer(), createRenderBuffer());
      this.grid.initialise(this);
   }

   private CLGrid.RenderBuffer createRenderBuffer() {
      DwGLTexture buf = new DwGLTexture();
      buf.resize(
          pixelFlow,
          GL2.GL_RGBA32F,
          pixelWidth, pixelHeight,
          GL2.GL_RGBA,
          GL2.GL_FLOAT,
          GL2.GL_NEAREST,
          GL2.GL_CLAMP_TO_EDGE,
          4,
          4
      );
      buf.clear(0.0f);

      CLGLTexture2d<?> clRef = context.createFromGLTexture2d(buf.target, buf.HANDLE[0], 0);
      return new CLGrid.RenderBuffer(clRef, buf);
   }

   @Override
   public void draw() {
//      doTestGLDraw();
      doDraw();
   }

   private void doDraw() {

      if (frameCount % 10 == 0) {
         long runTime = System.currentTimeMillis() - this.startTs;
         println("Frame Rate: " + frameRate + ", Frame Count: " + frameCount + ", Running Time: " + runTime + " ms");
      }

      long drawStart = System.currentTimeMillis();

      boolean printMetrics = frameCount % 60 == 0;

      if (printMetrics) {
         System.out.println("FrameRate: " + frameRate);
      }

      this.grid.update(printMetrics);
      this.queue.finish();

      //TODO: CLGL interop to render the result

      long start = System.currentTimeMillis();
//      this.grid.grid.getBuffer().rewind();
//      this.queue.putReadImage(this.grid.grid, false);
//      this.queue.finish();
//
//      FloatBuffer outBuffer = (FloatBuffer) this.grid.grid.getBuffer();
//      outBuffer.rewind();
//      float[] data = new float[width * height];
//      outBuffer.get(data);
//
//      loadPixels();
//      for (int y = 0; y < height; y++) {
//         for (int x = 0; x < width; x++) {
//            int idx = y * width + x;
//            pixels[idx] = color(map(data[idx], -1.0f, 1.0f, 0, 255));
//         }
//      }
//
//      updatePixels();

//      this.grid.currentFrame.cl.getBuffer().rewind();
//      this.queue.putReadImage(this.grid.currentFrame.cl, false);
//      this.queue.finish();
//
//      FloatBuffer outBuffer = (FloatBuffer) this.grid.currentFrame.cl.getBuffer();
//      outBuffer.rewind();
//      float[] data = new float[pixelWidth * pixelHeight * 4]; // RGB channels are included
//      outBuffer.get(data);
//
//      println("Read data length: " + data.length);
//
//      loadPixels();
//      for (int y = 0; y < pixelHeight; y++) {
//         for (int x = 0; x < pixelWidth; x++) {
//            int pixelIdx = y * pixelWidth + x;
//            int dataIdx = 3*pixelIdx;
//
////            println(idx);
//            pixels[pixelIdx] = color(data[dataIdx + 0] * 255.0f, data[dataIdx + 1] * 255.0f, data[dataIdx + 2] * 255.0f);
//         }
//      }
//
//      updatePixels();

      Copy copy = DwFilter.get(pixelFlow).copy;
      copy.apply(grid.currentFrame.gl, (PGraphicsOpenGL) g);
      image(g, 0, 0);

      long end = System.currentTimeMillis();
      if (printMetrics) {
         System.out.println("Draw Took: " + (end - drawStart) + " ms");
         System.out.println("Blit Took: " + (end - start) + " ms");
      }
   }

   public static void main(String[] args) {
      PApplet.main(CLTuringSketch.class);
   }
}
