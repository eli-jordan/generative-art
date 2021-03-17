package glslfft;

import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

import java.nio.FloatBuffer;

public class PixelFlowHelperTest extends PApplet {

   private DwPixelFlow context;
   private DwGLTexture in = new DwGLTexture();
   private DwGLTexture out = new DwGLTexture();

   @Override
   public void settings() {
      size(100, 100, P2D);
   }

   @Override
   public void setup() {
      this.context = new DwPixelFlow(this);
      context.print();
      context.printGL();

      DwGLSLProgram shader = this.context.createShader("glslfft/fft.frag");


      initInputTexture();
      initOutputTexture();

      context.begin();
      context.beginDraw((PGraphicsOpenGL) g);

      shader.begin();
      shader.uniform2f("resolution", (float) width, (float) height);
//      shader.uniform1f("subtransformSize", 1.0f);
//      shader.uniform1i("horizontal", 1);
//      shader.uniform1i("forward", 1);
//      shader.uniform1f("normalization", 1.0f);
      shader.uniformTexture("src", in);
      shader.drawFullScreenQuad();
      shader.end();

      context.endDraw();
      context.end();

//      float[] data = in.getFloatTextureData(new float[1]);
//      for(int i = 0; i < data.length; i++) {
//         println(i + ": " + data[i]);
//      }
//
//      exit();
   }

   void initInputTexture() {
//      FloatBuffer data = FloatBuffer.wrap(new float[] {
//           1.1f, 1.2f, 1.3f, 1.4f,
//           2.1f, 2.2f, 2.3f, 2.4f,
//           3.1f, 3.2f, 3.3f, 3.4f,
//           4.1f, 4.2f, 4.3f, 4.4f
//      });

      float[] data = new float[100*100*4];

      for(int y = 0; y < 100; y++) {
         for(int x = 0; x < 100; x++) {
            int idx = 4 * (y*width + x);

            if(y > 66) {
               data[idx + 0] = 0;
               data[idx + 1] = 0;
               data[idx + 2] = 1;
               data[idx + 3] = 1;
            } else if(y > 33) {
               data[idx + 0] = 0;
               data[idx + 1] = 1;
               data[idx + 2] = 0;
               data[idx + 3] = 1;
            } else {
               data[idx + 0] = 1;
               data[idx + 1] = 0;
               data[idx + 2] = 0;
               data[idx + 3] = 1;
            }
         }
      }
      in.resize(
          context,
          GL2.GL_RGBA32F,
          width, height,
          GL2.GL_RGBA,
          GL2.GL_FLOAT,
          GL2.GL_LINEAR,
          GL2.GL_CLAMP_TO_EDGE,
          4,
          4,
          FloatBuffer.wrap(data)
      );
//      in.clear(1, 0.5f, 0.6f, 1);
   }

   void initOutputTexture() {
      out.resize(context,
          GL2.GL_RGBA32F,
          width, height,
          GL2.GL_RGBA,
          GL2.GL_FLOAT,
          GL2.GL_LINEAR,
          GL2.GL_CLAMP_TO_EDGE,
          4,
          4);
      out.clear(0.0f);
   }

   public static void main(String[] args) {
      PApplet.main(PixelFlowHelperTest.class);
   }
}
