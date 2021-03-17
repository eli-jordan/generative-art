package glslfft;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;

import java.util.List;

public class SketchTest extends PApplet {

   GlslFft fft;

   @Override
   public void settings() {
      size(4, 4, P2D);
   }

   @Override
   public void setup() {
      fft = new GlslFft(this);

      testFftPasses();

   }

   private void testFftPasses() {

      float[] data = new float[4*4*4];
      {
         int x = 2;
         int y = 0;
         int idx = 4 * (y * width + x);
         data[idx] = 1;
      }

      GlslFft.FftBuffer input = fft.newBuffer(4, 4, data);

      GlslFft.FftBuffer ping = fft.newBuffer(4, 4);
      GlslFft.FftBuffer pong = fft.newBuffer(4, 4);
      GlslFft.FftBuffer output = fft.newBuffer(4, 4);

      List<FftPass<GlslFft.FftBuffer>> forwardPasses = fft.forward(input, ping, pong, output, 4, 4);
      fft.runPasses(forwardPasses);

      fft.printComplex(output);


   }

//   private void testFftPasses() {
//      PGraphics input = newBuffer(4, 4);
//      PGraphics ping = newBuffer(4, 4);
//      PGraphics pong = newBuffer(4, 4);
//      PGraphics output = newBuffer(4, 4);
//
//      input.beginDraw();
//      input.background(0);
//      int x = 2;
//      int y = 0;
//      input.set(x, y, color(1.3f, 0, 0, 255));
//      input.endDraw();
//
//      List<FftPass<PGraphics>> forwardPasses = fft.forward(input, ping, pong, output, 4, 4);
//      for(FftPass<?> pass : forwardPasses) {
//         println(pass);
//      }
//
//      fft.runPasses(forwardPasses);
//
////      PJOGL jogl = (PJOGL) input.beginPGL();
////      GL2ES3 gl2ES3 = jogl.gl.getGL2ES3();
////      print(gl2ES3);
////      input.endPGL();
//
//
//
//      output.loadPixels();
//      fft.printComplex(output);
//   }

   private void testBasicPixelCopying() {
      PShader shader = loadShader("glslfft/fft.frag");

      PGraphics a = newBuffer(8, 8);
      PGraphics b = newBuffer(8, 8);

      a.loadPixels();
      b.loadPixels();

      a.beginDraw();
      a.background(color(0));
      int x = 2;
      int y = 0;
      a.set(x, y, color(255, 0, 0, 255));
      a.endDraw();

      b.beginDraw();
      b.shader(shader);
      b.image(a, 0, 0);
      b.endDraw();

      a.beginDraw();
      a.shader(shader);
      a.image(b, 0, 0);
      a.endDraw();

      b.beginDraw();
      b.shader(shader);
      b.image(a, 0, 0);
      b.endDraw();

      image(b, 0, 0);

      //      a.loadPixels();
//      b.loadPixels();
//
//      fft.printComplex(a);
//      println("---");
//      fft.printComplex(b);
   }

   private PGraphics newBuffer(int width, int height) {
      PGraphics buffer = createGraphics(width, height, P2D);
      buffer.noSmooth();
      buffer.beginDraw();
      buffer.background(color(0));
      buffer.endDraw();
      return buffer;
   }

   public static void main(String[] args) {
      PApplet.main(SketchTest.class);
   }
}
