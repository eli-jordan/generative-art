package turingpatterns.sketches;

import processing.core.PApplet;
import turingpatterns.ScaleConfiguarions;
import turingpatterns.TuringPatternApplet;
import turingpatterns.config.RunConfig;

public class PastelWithSymmetry extends TuringPatternApplet {

   @Override
   protected RunConfig createRunConfig() {
      ScaleConfiguarions configs = new ScaleConfiguarions(this);
      return RunConfig.newBuilder()
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
