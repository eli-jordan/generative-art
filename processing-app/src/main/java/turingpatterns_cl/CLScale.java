package turingpatterns_cl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.*;
import opencl.ScanBlur;
import turingpatterns.config.ScaleConfig;

public class CLScale {

   ScaleConfig config;
   ScanBlur blur;

   CLImage2d<?> activator;
   CLImage2d<?> inhibitor;

   public CLScale(ScaleConfig config, CLContext context, CLCommandQueue queue) {
      this.config = config;
      this.blur = new ScanBlur(context, queue);
      CLImageFormat format = ImageFormat.forGridBuffer();
      this.activator = context.createImage2d(Buffers.newDirectFloatBuffer(config.width*config.height), config.width, config.height, format);
      this.inhibitor = context.createImage2d(Buffers.newDirectFloatBuffer(config.width*config.height), config.width, config.height, format);
   }

   public void update(CLImage2d<?> scanData, CLEventList events) {
      blur.runBlurKernel(scanData, activator, this.config.activatorRadius, null);
      blur.runBlurKernel(scanData, inhibitor, this.config.inhibitorRadius, events);
   }
}
