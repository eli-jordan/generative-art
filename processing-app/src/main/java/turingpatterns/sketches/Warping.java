package turingpatterns.sketches;

import processing.core.PApplet;
import turingpatterns.ScaleConfiguarions;
import turingpatterns.TuringPatternApplet;
import turingpatterns.config.RunConfig;
import turingpatterns.config.ScaleConfig;

import java.util.List;

public class Warping extends TuringPatternApplet {

   public Warping() {

//      ScaleConfig.Builder scale1 = ScaleConfig.newBuilder()
//          .activatorRadius(25)
//          .inhibitorRadius(50)
//          .symmetry(8)
//          .blur(ScaleConfig.BlurType.Circular)
//          .smallAmount(0.05f);
//
//      ScaleConfig.Builder scale2 = ScaleConfig.newBuilder()
//          .activatorRadius(7)
//          .inhibitorRadius(15)
//          .symmetry(3)
//          .blur(ScaleConfig.BlurType.Circular)
//          .smallAmount(0.05f);

      ScaleConfiguarions configs = new ScaleConfiguarions(this);
      List<ScaleConfig.Builder> scales = configs.pastelPaletteWithSymmetry(0.5f);

      runConfig = RunConfig.newBuilder()
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
