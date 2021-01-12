package turingpatterns;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScaleConfigs {

   private PApplet applet;

   ScaleConfigs(PApplet applet) {
      this.applet = applet;
   }

   //    Scale(int w, int h, int activatorRadius, int inhibitorRadius, float smallAmount, int colour) {
   public List<Scale> pastelPalette(int factor) {
      int w = applet.width;
      int h = applet.height;

      List<Scale> scales = new ArrayList<>();

      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(180 * factor)
              .inhibitorRadius(350 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(245, 59, 70))
              .build()
      );
      scales.add(
          Scale.newBuilder()
            .size(w, h)
            .activatorRadius(128 * factor)
            .inhibitorRadius(250 * factor)
            .bumpAmount(0.05f / 2)
            .colour(applet.color(246, 117, 29))
            .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(128 * factor)
              .inhibitorRadius(200 * factor)
              .bumpAmount(0.02f / 2)
              .colour(applet.color(243, 206, 25))
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(64 * factor)
              .inhibitorRadius(128 * factor)
              .bumpAmount(0.04f / 2)
              .colour(applet.color(22, 166, 174))
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(255))
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(0))
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(10 * factor)
              .inhibitorRadius(25 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(80, 151, 72))
              .build()
      );

      return scales;
   }

   public List<Scale> pastelPaletteWithSymmetry(float factor) {
      int w = applet.width;
      int h = applet.height;

      List<Scale> scales = new ArrayList<>();

      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(180 * factor)
              .inhibitorRadius(350 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(245, 59, 70))
              .symmetry(13)
              .blur(Scale.BlurType.Circular)
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(128 * factor)
              .inhibitorRadius(250 * factor)
              .bumpAmount(0.05f / 2)
              .colour(applet.color(246, 117, 29))
              .symmetry(12)
              .blur(Scale.BlurType.Circular)
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(128 * factor)
              .inhibitorRadius(200 * factor)
              .bumpAmount(0.02f / 2)
              .colour(applet.color(243, 206, 25))
              .symmetry(11)
              .blur(Scale.BlurType.Circular)
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(64 * factor)
              .inhibitorRadius(128 * factor)
              .bumpAmount(0.04f / 2)
              .colour(applet.color(22, 166, 174))
              .symmetry(3)
              .blur(Scale.BlurType.Circular)
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(255))
              .symmetry(3)
              .blur(Scale.BlurType.Circular)
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(50 * factor)
              .inhibitorRadius(100 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(0))
              .symmetry(3)
              .blur(Scale.BlurType.Circular)
              .build()
      );
      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(10 * factor)
              .inhibitorRadius(25 * factor)
              .bumpAmount(0.03f / 2)
              .colour(applet.color(80, 151, 72))
              .symmetry(10)
              .blur(Scale.BlurType.Circular)
              .build()
      );

      return scales;
   }

   List<Scale> symmetryExperiment() {
      int w = applet.width;
      int h = applet.height;

      List<Scale> scales = new ArrayList<>();

      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(100)
              .inhibitorRadius(250)
              .blur(Scale.BlurType.Circular)
              .bumpAmount(0.05f)
              .colour(applet.color(255, 0, 255))
              .symmetry(11)
              .build()
      );

      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(25)
              .inhibitorRadius(50)
              .blur(Scale.BlurType.Circular)
              .bumpAmount(0.05f)
              .colour(applet.color(255, 230, 0))
              .symmetry(9)
              .build()
      );

      scales.add(
          Scale.newBuilder()
              .size(w, h)
              .activatorRadius(6)
              .inhibitorRadius(15)
              .blur(Scale.BlurType.Circular)
              .bumpAmount(0.05f)
              .colour(applet.color(100, 0, 255))
              .symmetry(4)
              .build()
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
