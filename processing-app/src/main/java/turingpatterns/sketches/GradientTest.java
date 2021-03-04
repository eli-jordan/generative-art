package turingpatterns.sketches;

import processing.core.PApplet;

public class GradientTest extends PApplet {

   @Override
   public void settings() {
      size(1000, 800);
   }

   @Override
   public void setup() {
      Gradient grad = new Gradient(
//          color(152, 49, 44),
//          color(219, 206, 182),
          color(152 - 30, 49 - 30, 44 - 30),
          color(219 - 30, 206 - 30, 182 - 30),
          color(176 - 30, 111 - 30, 74 - 30)

//          color(219, 206, 182),
//          color(212, 117, 99)
      );

//      grad = new Gradient(
//          color(179, 160, 145),
//          color(214,170,112),
//          color(140, 47, 48),
//          color(140, 47, 48),
//          color(50, 45, 50),
//          color(20, 20, 20)
//      );

//      grad  = new Gradient(
////          applet.color(160,191,204),
//          applet.color(214, 170, 112),
////          applet.color(139,156,123),
////          applet.color(225,128,129),
//          applet.color(140, 47, 48),
////          applet.color(178, 94, 65),
//          applet.color(0)
//      );

      PApplet applet = this;

      grad = new Gradient(
          applet.color(179, 160, 145),
          applet.color(214,170,112),
          applet.color(140, 47, 48),
          applet.color(50, 45, 50),
          applet.color(20, 20, 20)
      );
      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            int c = grad.at((float) x / width);
            pixels[idx] = c;

         }
      }
      updatePixels();
      noLoop();
   }

   public static void main(String[] args) {
      PApplet.main(GradientTest.class);
   }
}
