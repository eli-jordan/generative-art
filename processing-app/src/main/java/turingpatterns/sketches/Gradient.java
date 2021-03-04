package turingpatterns.sketches;

import processing.core.PApplet;
import processing.core.PConstants;

import java.util.Arrays;

public class Gradient {

   public static Gradient earthy(PApplet applet) {
      return new Gradient(
          applet.color(69, 90, 60),
          applet.color(134, 145, 97),
          applet.color(196, 187, 108),
          applet.color(239, 228, 139),
          applet.color(204, 162, 68),
          applet.color(136, 85, 32)
      );
   }

   public static Gradient earthy2(PApplet applet) {
      return new Gradient(
          applet.color(196, 187, 108),
          applet.color(134, 145, 97),
          applet.color(69, 90, 60),
          applet.color(136, 85, 32),
          applet.color(204, 162, 68),
          applet.color(239, 228, 139)
      );
   }


   private final int[] colours;

   public Gradient(int... colours) {
      this.colours = Arrays.copyOf(colours, colours.length);
   }

   public int at(float value) {
      int index = 0;
      float amt = 0;
      try {
         float partSize = 1.0f / (colours.length - 1);
         float part = value / partSize;
         index = PApplet.floor(part);
         amt = part - index;

         return PApplet.lerpColor(colours[index], colours[index + 1], amt, PConstants.RGB);
      } catch (Throwable t) {
         System.out.println("value=" + value + ", index=" + index + ", amt=" + amt);
         t.printStackTrace();
         return -1;
      }
   }
}
