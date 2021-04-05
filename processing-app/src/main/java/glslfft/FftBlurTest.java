package glslfft;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwFilter;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

import java.util.List;

public class FftBlurTest extends PApplet {
   private DwPixelFlow context;
   private GlslFft fft;

   @Override
   public void settings() {
      size(512, 256, P2D);
   }

   @Override
   public void setup() {
      context = new DwPixelFlow(this);
      fft = new GlslFft(context);

      DwGLSLProgram shader = context.createShader("glslfft/circle-kernel.frag");
      context.begin();
      context.beginDraw((PGraphicsOpenGL) g);

      shader.begin();
      shader.uniform2f("resolution", (float) width, (float) height);
      shader.drawFullScreenQuad();
      shader.end();

      context.endDraw();
      context.end();

//      PGraphics d = createGraphics(width, height, P2D);
//
//      d.beginDraw();
//      d.background(0);
//      d.fill(255);
//      d.ellipse(width/2f, height/2f, 200, 200);
//      d.endDraw();

//      GlslFft.FftBuffer in = fft.newBuffer(width, height);
//      GlslFft.FftBuffer ping = fft.newBuffer(width, height);
//      GlslFft.FftBuffer pong = fft.newBuffer(width, height);
//      GlslFft.FftBuffer out = fft.newBuffer(width, height);
//
//      List<FftPass<GlslFft.FftBuffer>> passes = fft.forward(in, ping, pong, out, width, height);
//      fft.runPasses(passes);

      // TODO: kernel, multiply, inverse

//      DwFilter.get(context).copy.apply(d, in.buf);
//      DwFilter.get(context).copy.apply(out.buf, (PGraphicsOpenGL) g);

   }

   public static void main(String[] args) {
      PApplet.main(FftBlurTest.class);
   }
}
