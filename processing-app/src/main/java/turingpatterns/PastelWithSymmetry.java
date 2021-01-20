package turingpatterns;

import processing.core.PApplet;
import turingpatterns.config.RunConfig;

public class PastelWithSymmetry extends TuringPatternApplet {

   public PastelWithSymmetry() {
      ScaleConfiguarions configs = new ScaleConfiguarions(this);
      runConfig = RunConfig.newBuilder()
          .size(1024, 1024)
          .scaleCoupling(RunConfig.ScaleCoupling.MultiScale)
          .renderer(RunConfig.RenderType.Colour)
          .addScales(configs.pastelPaletteWithSymmetry(0.2f))
          .build();
   }


   public static void main(String[] args) {
      PApplet.main(PastelWithSymmetry.class);
   }
}
