package turingpatterns.sketches;

import processing.core.PApplet;
import turingpatterns.Colours;

public class ColourTest extends PApplet {

   Colours colours;

   @Override
   public void settings() {
      size(400, 200);
   }

   @Override
   public void setup() {
      noStroke();
      this.colours = new Colours(this);
      int c1 = hsv(0, 0, 100);
      int c2 = hsv(357, 41, 53);
      int c3 = hsv(51, 60, 82);
      int c4 = hsv(0, 0, 0);

      fill(c1);
      rect(0, 0, 100, 200);

      fill(c2);
      rect(100, 0, 100, 200);

      fill(c3);
      rect(200, 0, 100, 200);

      fill(c4);
      rect(300, 0, 100, 200);
   }

   private int hsv(float h, float s, float v) {
      return colours.createHSV(h, s/100f, v/100f, 1.0f).toRGB().toColor();
   }

   public static void main(String[] args) {
      PApplet.main(ColourTest.class);
   }
}
