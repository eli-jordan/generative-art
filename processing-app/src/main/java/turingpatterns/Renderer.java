package turingpatterns;

import processing.core.PApplet;

import static processing.core.PApplet.map;

interface Renderer {
   void draw(PApplet into);
}

class GrayscaleRenderer implements Renderer {

   private final Grid grid;

   GrayscaleRenderer(Grid grid) {
      this.grid = grid;
   }

   @Override
   public void draw(PApplet into) {
      this.grid.update();

      into.loadPixels();

      for (int x = 0; x < into.width; x++) {
         for (int y = 0; y < into.height; y++) {
            int index = x + y * into.width;

            into.pixels[index] = pixelBW(into, x, y);
         }
      }

      into.updatePixels();
   }

   private int pixelBW(PApplet applet, int x, int y) {
      float value = map(this.grid.grid[y][x], -1, 1, 0, 255);
      return applet.color(value);
   }
}

class ColourRenderer implements Renderer {
   private final Grid grid;
   private final Colours colours;

   ColourRenderer(Grid grid, Colours colours) {
      this.grid = grid;
      this.colours = colours;
   }

   @Override
   public void draw(PApplet into) {
      this.grid.update();

      into.loadPixels();

      for (int x = 0; x < into.width; x++) {
         for (int y = 0; y < into.height; y++) {
            int index = x + y * into.width;

            into.pixels[index] = pixelColour(into, x, y);
         }
      }

      into.updatePixels();
   }

   private int pixelColour(PApplet applet, int x, int y) {
      int c = this.grid.colors[y][x];

      Colours.RGBValue rgb = colours.createRGB(applet.red(c), applet.green(c), applet.blue(c), applet.alpha(c));
      Colours.HSVValue hsv = rgb.toHSV();
      hsv.v = map(this.grid.grid[y][x], -1, 1, 0, 1);

      Colours.RGBValue newColour = hsv.toRGB();

      float gamma = 1.1f;
      newColour.r = (float) Math.pow(newColour.r, gamma);
      newColour.g = (float) Math.pow(newColour.g, gamma);
      newColour.b = (float) Math.pow(newColour.b, gamma);

      return newColour.toColor();
   }
}
