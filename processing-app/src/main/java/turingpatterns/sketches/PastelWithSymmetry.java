package turingpatterns.sketches;

import processing.core.PApplet;
import turingpatterns.ScaleConfiguarions;
import turingpatterns.TuringPatternApplet;
import turingpatterns.config.RunConfig;

public class PastelWithSymmetry extends TuringPatternApplet {

   public PastelWithSymmetry() {
      ScaleConfiguarions configs = new ScaleConfiguarions(this);
      runConfig = RunConfig.newBuilder()
          .size(512, 512)
          .scaleCoupling(RunConfig.ScaleCoupling.MultiScale)
          .renderer(RunConfig.RenderType.Colour)
          .addScales(configs.pastelPaletteWithSymmetry(1.0f))
          .build();
   }


   public static void main(String[] args) {
      PApplet.main(PastelWithSymmetry.class);
   }
}
