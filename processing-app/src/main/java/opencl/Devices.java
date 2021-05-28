package opencl;

import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;

public class Devices {

   public static CLDevice getAMDGPU(CLContext context) {

      for(CLDevice device : context.getDevices()) {
         if(device.getVendor().contains("AMD") && device.getType() == CLDevice.Type.GPU) {
            return device;
         }
      }

      return null;
   }

   public static CLDevice getIntelGPU(CLContext context) {

      for(CLDevice device : context.getDevices()) {
         if(device.getVendor().contains("Intel") && device.getType() == CLDevice.Type.GPU) {
            return device;
         }
      }

      return null;
   }

   public static CLDevice getCPU(CLContext context) {
      return context.getMaxFlopsDevice(CLDevice.Type.CPU);
   }
}
