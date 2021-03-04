package turingpatterns.sketches;

import processing.core.PApplet;
import turingpatterns.ScaleConfiguarions;
import turingpatterns.TuringPatternApplet;
import turingpatterns.config.RunConfig;
import turingpatterns.config.ScaleConfig;

import java.util.List;

public class Warping extends TuringPatternApplet {

   @Override
   protected RunConfig createRunConfig() {
      ScaleConfiguarions configs = new ScaleConfiguarions(this);
      List<ScaleConfig.Builder> scales = configs.pastelPaletteWithSymmetry(0.5f);

      return RunConfig.newBuilder()
          .size(1024, 1024)
          .scaleCoupling(RunConfig.ScaleCoupling.MultiScale)
          .renderer(RunConfig.RenderType.Colour)
          .addScales(scales)
          .build();
   }

   public static void main(String[] args) {
      PApplet.main(Warping.class);
   }
}
