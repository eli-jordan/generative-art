package turingpatterns_gpu;

import prefixsum.Buffer;
import prefixsum.PrefixSumBlur;
import turingpatterns.config.ScaleConfig;

public class GpuScale {
   ScaleConfig config;
   PrefixSumBlur blur;

   Buffer activator;
   Buffer inhibitor;

   GpuScale(ScaleConfig config, PrefixSumBlur blur) {
      this.config = config;
      this.blur = blur;
      this.activator = blur.newBuffer(config.width, config.height);
      this.inhibitor = blur.newBuffer(config.width, config.height);
   }

   void applyBlur(Buffer inputPrefixSum) {
      // Activator blur
      blur.blur(inputPrefixSum, activator, config.width, config.height, config.activatorRadius);

      // Inhibitor blur
      blur.blur(inputPrefixSum, inhibitor, config.width, config.height, config.inhibitorRadius);
   }
}
