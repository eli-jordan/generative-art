package opencl;

import com.jogamp.opencl.*;
import com.jogamp.opencl.gl.CLGLContext;
import com.jogamp.opencl.gl.CLGLTexture2d;
import com.jogamp.opengl.GL2;
import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLTexture;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.Copy;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwFilter;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

import java.io.IOException;

public class OpenGLInterop extends PApplet {

   private DwPixelFlow pixelFlow;

   @Override
   public void settings() {
      size(10, 10, P2D);
   }

   @Override
   public void setup() {
      this.pixelFlow = new DwPixelFlow(this);

      // 1. Create an OpenCL context that is associated with the current OpenGL
      //    context. This allows memory to be shared between the two systems without
      //    being copied.
      CLGLContext clContext = CLGLContext.create(pixelFlow.pjogl.context);

      // 2. Initialise an OpenGL texture that will be used as a buffer in OpenCL.
      //    We need to initialise it in OpenGL then acquire it for use in OpenCL.
      DwGLTexture buf = new DwGLTexture();
      buf.resize(
          pixelFlow,
          GL2.GL_RGBA32F,
          width, height,
          GL2.GL_RGBA,
          GL2.GL_FLOAT,
          GL2.GL_NEAREST,
          GL2.GL_CLAMP_TO_EDGE,
          4,
          4
      );
      buf.clear(0.0f);



      // 3. Create an OpenCL object that references the OpenGL buffer that was just initialised.
      CLGLTexture2d<?> clglBuf = clContext.createFromGLTexture2d(buf.target, buf.HANDLE[0], 0, CLMemory.Mem.WRITE_ONLY);


      // 4. Initalise the OpenCL device and kernel
      CLDevice device = clContext.getMaxFlopsDevice(CLDevice.Type.GPU);
      if (!device.isGLMemorySharingSupported()) {
         throw new RuntimeException("GL mem sharing not supported");
      }
      CLKernel kernel = getKernel(clContext);
      kernel.putArg(clglBuf);


      // 5. When submitting the kernel to the device, we need to ensure that
      //    we also submit the commands to acquire our OpenGL buffer as well
      //    otherwise the object will not be shared correctly.
      CLCommandQueue queue = device.createCommandQueue();
      queue.putAcquireGLObject(clglBuf);
      queue.put2DRangeKernel(kernel,
          0, 0,
          width, height,
          0, 0
          );
      queue.putReleaseGLObject(clglBuf);
      queue.finish();

      // 6. Copy the texture buffer to the display
      Copy copy = DwFilter.get(pixelFlow).copy;
      copy.apply(buf, (PGraphicsOpenGL) g);

   }

   private CLKernel getKernel(CLContext context) {
      try {
         String path = "/cl-kernels/gl_interop.cl";
         CLProgram program = context.createProgram(getClass().getResourceAsStream(path)).build();
         return program.createCLKernel("red");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static void main(String[] args) {
      PApplet.main(OpenGLInterop.class);
   }
}
