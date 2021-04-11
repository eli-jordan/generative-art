package turingpatterns_cl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLImage2d;
import com.jogamp.opencl.CLImageFormat;
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
      CLImageFormat format = new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
      this.activator = context.createImage2d(Buffers.newDirectFloatBuffer(config.width*config.height), config.width, config.height, format);
      this.inhibitor = context.createImage2d(Buffers.newDirectFloatBuffer(config.width*config.height), config.width, config.height, format);
   }

   public void update(CLImage2d<?> scanData) {
      blur.runBlurKernel(scanData, activator, this.config.activatorRadius);
      blur.runBlurKernel(scanData, inhibitor, this.config.inhibitorRadius);
   }
}
