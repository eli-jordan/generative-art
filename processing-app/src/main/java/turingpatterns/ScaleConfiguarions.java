package turingpatterns;

import processing.core.PApplet;
import turingpatterns.config.ScaleConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScaleConfiguarions {

   private PApplet applet;

   public ScaleConfiguarions(PApplet applet) {
      this.applet = applet;
   }

   public List<ScaleConfig.Builder> pastelPalette(float factor) {
      int w = applet.width;
      int h = applet.height;

      List<ScaleConfig.Builder> scales = new ArrayList<>();

      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(180 * factor)
              .inhibitorRadius(350 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(245, 59, 70))
      );
      scales.add(
          ScaleConfig.newBuilder()
            .size(w, h)
            .activatorRadius(128 * factor)
            .inhibitorRadius(250 * factor)
            .smallAmount(0.05f / 2)
            .colour(applet.color(246, 117, 29))
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(128 * factor)
              .inhibitorRadius(200 * factor)
              .smallAmount(0.02f / 2)
              .colour(applet.color(243, 206, 25))
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(64 * factor)
              .inhibitorRadius(128 * factor)
              .smallAmount(0.04f / 2)
              .colour(applet.color(22, 166, 174))
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(255))
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(0))
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(10 * factor)
              .inhibitorRadius(25 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(80, 151, 72))
      );

      return scales;
   }

   public List<ScaleConfig.Builder> pastelPaletteWithSymmetry(float factor) {
      int w = applet.width;
      int h = applet.height;

      List<ScaleConfig.Builder> scales = new ArrayList<>();

      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(180 * factor)
              .inhibitorRadius(350 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(245, 59, 70))
              .symmetry(13)
              .blur(ScaleConfig.BlurType.Circular)
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(128 * factor)
              .inhibitorRadius(250 * factor)
              .smallAmount(0.05f / 2)
              .colour(applet.color(246, 117, 29))
              .symmetry(12)
              .blur(ScaleConfig.BlurType.Circular)
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(128 * factor)
              .inhibitorRadius(200 * factor)
              .smallAmount(0.02f / 2)
              .colour(applet.color(243, 206, 25))
              .symmetry(11)
              .blur(ScaleConfig.BlurType.Circular)
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(64 * factor)
              .inhibitorRadius(128 * factor)
              .smallAmount(0.04f / 2)
              .colour(applet.color(22, 166, 174))
              .symmetry(3)
              .blur(ScaleConfig.BlurType.Circular)
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(255))
              .symmetry(3)
              .blur(ScaleConfig.BlurType.Circular)
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(0))
              .symmetry(3)
              .blur(ScaleConfig.BlurType.Circular)
      );
      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(10 * factor)
              .inhibitorRadius(25 * factor)
              .smallAmount(0.03f / 2)
              .colour(applet.color(80, 151, 72))
              .symmetry(10)
              .blur(ScaleConfig.BlurType.Circular)
      );

      return scales;
   }

   public List<ScaleConfig.Builder> symmetryExperiment() {
      int w = applet.width;
      int h = applet.height;

      List<ScaleConfig.Builder> scales = new ArrayList<>();

      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(100)
              .inhibitorRadius(250)
              .blur(ScaleConfig.BlurType.Circular)
              .smallAmount(0.05f)
              .colour(applet.color(255, 0, 255))
              .symmetry(11)
      );

      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(25)
              .inhibitorRadius(50)
              .blur(ScaleConfig.BlurType.Circular)
              .smallAmount(0.05f)
              .colour(applet.color(255, 230, 0))
              .symmetry(9)
      );

      scales.add(
          ScaleConfig.newBuilder()
              .size(w, h)
              .activatorRadius(6)
              .inhibitorRadius(15)
              .blur(ScaleConfig.BlurType.Circular)
              .smallAmount(0.05f)
              .colour(applet.color(100, 0, 255))
              .symmetry(4)
      );




      return scales;
   }

//   Scale[] pastelReversePalette(int factor) {
//      int w = applet.width;
//      int h = applet.height;
//      return new Scale[]{
//          new Scale(w, h, 180 * factor, 350 * factor, 0.03f / 2, applet.color(80, 151, 72)),
//          new Scale(w, h, 128 * factor, 250 * factor, 0.05f / 2, applet.color(22, 166, 174)),
//          new Scale(w, h, 128 * factor, 200 * factor, 0.02f / 2, applet.color(246, 117, 29)),
//          new Scale(w, h, 64 * factor, 128 * factor, 0.04f / 2, applet.color(243, 206, 25)),
//          new Scale(w, h, 50 * factor, 100 * factor, 0.03f / 2, applet.color(255)),
//          new Scale(w, h, 50 * factor, 100 * factor, 0.03f / 2, applet.color(0)),
//          new Scale(w, h, 10 * factor, 25 * factor, 0.03f / 2, applet.color(245, 59, 70)),
//          //new Scale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(242, 185, 245))
//      };
//   }
}
