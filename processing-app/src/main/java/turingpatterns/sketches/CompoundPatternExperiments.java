package turingpatterns.sketches;

import processing.core.PApplet;
import turingpatterns.TuringPatternApplet;
import turingpatterns.config.RunConfig;
import turingpatterns.config.ScaleConfig;

import java.util.ArrayList;
import java.util.List;

public class CompoundPatternExperiments extends TuringPatternApplet {

   public CompoundPatternExperiments() {
      this.runConfig = RunConfig.newBuilder()
          .size(1024, 1024)
          .scaleCoupling(RunConfig.ScaleCoupling.Compound)
          .renderer(RunConfig.RenderType.Grayscale)
          .addScales(fig4_experiment())
          .build();
   }

   private List<ScaleConfig.Builder> fig4_experiment() {

      List<ScaleConfig.Builder> configs = new ArrayList<>();

//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(100)
//              .inhibitorRadius(300)
//              .smallAmount(0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(150)
              .inhibitorRadius(480)
              .smallAmount(-0.08f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );


      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(150)
              .inhibitorRadius(480)
              .smallAmount(-0.05f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(18)
              .inhibitorRadius(36)
              .smallAmount(0.085f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(6)
              .inhibitorRadius(12)
              .smallAmount(-0.1f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );

//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(12.5f)
//              .inhibitorRadius(25)
//              .smallAmount(0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );

//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(10)
//              .inhibitorRadius(20)
//              .smallAmount(-0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );
//
//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(15)
//              .inhibitorRadius(53)
//              .smallAmount(-0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );

//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(150/f/f)
//              .inhibitorRadius(300/f/f)
//              .smallAmount(-0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );

//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(150/f/f/f)
//              .inhibitorRadius(300/f/f/f)
//              .smallAmount(0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );


      return configs;
   }

   private List<ScaleConfig.Builder> nestedDotsAndStripes2() {

      float scale = 1.0f;

      List<ScaleConfig.Builder> configs = new ArrayList<>();
      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(60 * scale)
              .inhibitorRadius(240)
              .smallAmount(0.05f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(30 * scale)
              .inhibitorRadius(60 * scale)
              .smallAmount(0.05f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(10 * scale)
              .inhibitorRadius(30 * scale)
              .smallAmount(-0.05f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );

      return configs;
   }

   private List<ScaleConfig.Builder> nestedDotsAndStripes() {

      List<ScaleConfig.Builder> configs = new ArrayList<>();
      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(60)
              .inhibitorRadius(180)
              .smallAmount(0.05f)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(30)
              .inhibitorRadius(90)
              .smallAmount(-0.05f)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(10)
              .inhibitorRadius(30)
              .smallAmount(0.05f)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(3)
              .inhibitorRadius(10)
              .smallAmount(-0.05f)
              .colour(color(255))
      );

      return configs;
   }

   private List<ScaleConfig.Builder> experiment() {
      List<ScaleConfig.Builder> configs = new ArrayList<>();
      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(36)
              .inhibitorRadius(36 * 2)
              .smallAmount(0.05f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );
//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(6)
//              .inhibitorRadius(6 * 2)
//              .smallAmount(-0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );


//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(120)
//              .inhibitorRadius(120 * 2)
//              .smallAmount(-0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );
//
//      configs.add(
//          ScaleConfig.newBuilder()
//              .activatorRadius(60)
//              .inhibitorRadius(60 * 2)
//              .smallAmount(-0.05f)
//              .blur(ScaleConfig.BlurType.Circular)
//              .colour(color(255))
//      );

      return configs;
   }

   private List<ScaleConfig.Builder> dotsAndStripes() {
      List<ScaleConfig.Builder> configs = new ArrayList<>();
      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(16)
              .inhibitorRadius(32)
              .smallAmount(0.08f)
              .blur(ScaleConfig.BlurType.Circular)
              .symmetry(12)
              .colour(color(255))
      );

      configs.add(
          ScaleConfig.newBuilder()
              .activatorRadius(8)
              .inhibitorRadius(16)
              .smallAmount(-0.08f)
              .blur(ScaleConfig.BlurType.Circular)
              .colour(color(255))
      );
      return configs;
   }


   public static void main(String[] args) {
      PApplet.main(CompoundPatternExperiments.class);
   }
}
