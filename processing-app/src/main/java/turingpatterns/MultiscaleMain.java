package turingpatterns;

import processing.core.PApplet;

public class MultiscaleMain extends PApplet {

   private Grid g;
   private Colours colours;

   @Override
   public void settings() {
      size(2048, 2048);
   }

   @Override
   public void setup() {
      this.colours = new Colours(this);
      ScaleConfigs configs = new ScaleConfigs(this);

      g = Grid.newBuilder(this)
          .scales(configs.pastelPaletteWithSymmetry(3))
          .deltas(Grid::multiScaleDelta)
          .build();
   }

   @Override
   public void draw() {
      loadPixels();

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            int index = x + y * width;

            pixels[index] = pixelColour(x, y);
//                pixels[index] = pixelBW(x, y);
         }
      }

      g.update();
      updatePixels();
      println("Frame Rate: " + frameRate + ", Frame Count: " + frameCount);
      saveFrame("/Users/elias.jordan/creative-code/renders/pastel-sym-2048-2/frame-####.png");
   }

   private int pixelBW(int x, int y) {
      float value = map(g.grid[y][x], -1, 1, 0, 255);
      return color(value);
   }

   private int pixelColour(int x, int y) {
      int c = g.colors[y][x];

      Colours.RGBValue rgb = colours.createRGB(red(c), green(c), blue(c), alpha(c));
      Colours.HSVValue hsv = rgb.toHSV();
      hsv.v = map(g.grid[y][x], -1, 1, 0, 1);

      Colours.RGBValue newColour = hsv.toRGB();

//        float contrast = 1.3;
//        newColour.r = (newColour.r - 0.5) * contrast + 0.5;
//        newColour.g = (newColour.g - 0.5) * contrast + 0.5;
//        newColour.b = (newColour.b - 0.5) * contrast + 0.5;

      float gamma = 1.1f;
      newColour.r = pow(newColour.r, gamma);
      newColour.g = pow(newColour.g, gamma);
      newColour.b = pow(newColour.b, gamma);

      return newColour.toColor();
   }

   public static void main(String[] args) {
      PApplet.main(MultiscaleMain.class);
   }
}
