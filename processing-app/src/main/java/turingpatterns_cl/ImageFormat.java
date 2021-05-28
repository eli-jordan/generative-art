package turingpatterns_cl;

import com.jogamp.opencl.CLImageFormat;

public class ImageFormat {

   public static CLImageFormat forGridBuffer() {
      return new CLImageFormat(CLImageFormat.ChannelOrder.R, CLImageFormat.ChannelType.FLOAT);
   }

   public static CLImageFormat forRenderBuffer() {
      return new CLImageFormat(CLImageFormat.ChannelOrder.RGBx, CLImageFormat.ChannelType.FLOAT);
   }
}
