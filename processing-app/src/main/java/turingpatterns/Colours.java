package turingpatterns;

import processing.core.PApplet;

import static processing.core.PApplet.*;

public class Colours {

   private final PApplet applet;

   public Colours(PApplet applet) {
      this.applet = applet;
   }


   public RGBValue createRGB(float r, float g, float b, float a) {
      RGBValue result = new RGBValue();
      result.r = map(r, 0, 255, 0, 1);
      result.g = map(g, 0, 255, 0, 1);
      result.b = map(b, 0, 255, 0, 1);
      result.a = map(a, 0, 255, 0, 1);
      return result;
   }

   public HSVValue createHSV(float h, float s, float v, float a) {
      return new HSVValue(h, s, v, a);
   }

   public class RGBValue {
      float r;
      float g;
      float b;
      float a;

      public HSVValue toHSV() {
         float h, s, v;

         float minV = Math.min(r, Math.min(g, b));
         float maxV = Math.max(r, Math.max(g, b));
         v = maxV;
         float delta = maxV - minV;

         if (maxV != 0) {
            s = delta / maxV;
         } else {
            h = -1;
            s = 0;
            v = -1;
            return new HSVValue(h, s, v, a);
         }

         if (delta == 0) {
            h = 0;
         } else if (r == maxV) {
            h = (g - b) / delta;
         } else if (g == maxV) {
            h = 2 + (b - r) / delta;
         } else {
            h = 4 + (r - g) / delta;
         }

         h *= 60;
         if (h < 0) {
            h += 360;
         }

         return new HSVValue(h, s, v, a);
      }

      public int toColor() {
         return applet.color(
             map(r, 0, 1, 0, 255),
             map(g, 0, 1, 0, 255),
             map(b, 0, 1, 0, 255),
             255
         );
      }

      public String toString() {
         return "RGB[" + r + ", " + g + ", " + b + "]";
      }
   }

   public class HSVValue {
      public float hu;
      public float s;
      public float v;
      public float a;

      HSVValue(float h, float s, float v, float a) {
         this.hu = h;
         this.s = s;
         this.v = v;
         this.a = a;
      }

      public RGBValue toRGB() {
         RGBValue result = new RGBValue();
         if (s == 0) {
            result.r = v;
            result.g = v;
            result.b = v;
            return result;
         }

         float h = hu / 60;
         int i = (int) Math.floor(h);
         float f = h - i;
         float p = v * (1 - s);
         float q = v * (1 - s * f);
         float t = v * (1 - s * (1 - f));
         switch (i) {
            case 0:
               result.r = v;
               result.g = t;
               result.b = p;
               break;
            case 1:
               result.r = q;
               result.g = v;
               result.b = p;
               break;
            case 2:
               result.r = p;
               result.g = v;
               result.b = t;
               break;
            case 3:
               result.r = p;
               result.g = q;
               result.b = v;
               break;
            case 4:
               result.r = t;
               result.g = p;
               result.b = v;
               break;
            // case 5:
            default:
               result.r = v;
               result.g = p;
               result.b = q;
               break;
         }

         return result;
      }

      public String toString() {
         return "HSV[" + hu + ", " + s + ", " + v + "]";
      }
   }
}
