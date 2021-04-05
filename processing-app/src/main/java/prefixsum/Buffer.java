package prefixsum;

import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;

import java.nio.FloatBuffer;

public class Buffer {
      // Single channel texture of 32-bit float values
      public DwGLTexture buf = new DwGLTexture();

      public Buffer(DwPixelFlow context, int width, int height, FloatBuffer data) {
         this.buf.resize(
             context,
             GL2.GL_R32F,
             width,
             height,
             GL2.GL_RED,
             GL2.GL_FLOAT,
             GL2.GL_NEAREST,
             GL2.GL_CLAMP_TO_BORDER,
             1,
             4,
             data
         );
         if (data == null) {
            this.buf.clear(0.0f);
         }
      }
   }
