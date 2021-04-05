package turingpatterns_gpu;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import prefixsum.Buffer;
import processing.opengl.PGraphicsOpenGL;

public class GrayscaleRender {
   private DwPixelFlow context;
   DwGLSLProgram shader;

   public GrayscaleRender(DwPixelFlow context) {
      this.context = context;
      shader = this.context.createShader("prefixsum/render-greyscale.frag");
   }

   public void render(Buffer grid, PGraphicsOpenGL g) {
      this.context.begin();
      this.context.beginDraw(g);

      this.shader.begin();
      this.shader.uniformTexture("grid", grid.buf);
      this.shader.uniform2f("resolution", 1.0f / g.width, 1.0f / g.height);

      this.shader.drawFullScreenQuad();
      this.shader.end();

      this.context.endDraw();
      this.context.end();
   }
}
