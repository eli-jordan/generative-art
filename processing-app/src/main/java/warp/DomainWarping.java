package warp;

import processing.core.PApplet;
import processing.core.PImage;
import turingpatterns.Colours;

public class DomainWarping extends PApplet {

   WorleyNoise cellularNoise;
   float[][] grid;

   Colours colours = new Colours(this);

   @Override
   public void settings() {
      size(512, 512);
   }

   @Override
   public void setup() {
      this.cellularNoise = WorleyNoise.getInstance();
//      this.grid = new float[height][width];
//      PImage image = loadImage("/Users/elias.jordan/Desktop/lena/a1.png");
//      image(image, 0, 0);
   }

   private void update() {
      float z0 = frameCount * 0.008f;
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {

            // pattern 2
//            float x1 = fbm(x, y, octaves);
//            float y1 = fbm(x + 5.2f, y + 1.3f, octaves);
//
//            float x2 = fbm(x + 4.0f * x1 + 1.7f, y + 4.0f * y1 + 9.2f, octaves);
//            float y2 = fbm(x + 4.0f * x1 + 8.3f, y + 4.0f * y1 + 2.8f, octaves);
//
//            return fbm(x + x2 * 4.0f, y + y2 * 4.0f, octaves);


            float x0 = x * 0.005f;
            float y0 = y * 0.005f;

            float x1 = worley(x0, y0, z0);
            float y1 = worley(x0 + 1.212f, y0 + 2.381f, z0);

//            float x2 = worley(x0 + 4.0f * x1 + 1.7f, y0 + 4.0f * y1 + 9.2f);
//            float y2 = worley(x0 + 4.0f * x1 + 8.3f, y0 + 4.0f * y1 + 8.2f);

            float v = worley(x0 + 4.0f * x1, y0 + 4.0f * y1);


            grid[y][x] = map(v, 0, 1, 1, 0);
         }
      }
   }

   private float worley(float x, float y, float z) {
      return (float) cellularNoise.noise(x, y, z);
   }

   private float worley(float x, float y) {
      return (float) cellularNoise.noise(x, y, 0);
   }

   private void normalize() {
      float max = 0;
      float min = Float.MAX_VALUE;
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            max = Math.max(max, grid[y][x]);
            min = Math.min(min, grid[y][x]);
         }
      }

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            grid[y][x] = map(grid[y][x], min, max, 0, 1);
         }
      }
   }

   @Override
   public void draw() {
//      update();
//      normalize();
//
//      loadPixels();
//      for (int x = 0; x < width; x++) {
//         for (int y = 0; y < height; y++) {
//            int index = x + y * width;
//            float v = grid[y][x];
//            pixels[index] = color(v * 255);
//         }
//      }
//      updatePixels();

      PImage image = loadImage("/Users/elias.jordan/Desktop/lena/jl1.jpg");
      image.loadPixels();

      loadPixels();
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {

            int index = x + y * width;

            float x0 = x * 0.005f;
            float y0 = y * 0.005f;

            int weight = 50;

            float x1 = worley(x0, y0, frameCount * 0.006f);
            float y1 = worley(x0 + 1.212f, y0 + 2.381f, frameCount * 0.006f);

            float x2 = worley(x0 + 2.0f * x1 + 1.7f, y0 + 4.0f * y1 + 9.2f);
            float y2 = worley(x0 + 2.0f * x1 + 8.3f, y0 + 4.0f * y1 + 8.2f);

            int xf = x + (int) map(x2, 0, 1, -weight, weight);
            int yf = y + (int) map(y2, 0, 1, -weight, weight);

            if(xf >= width) xf = width - 1;
            if(xf < 0) xf = 0;


            if(yf >= height) yf = height - 1;
            if(yf < 0) yf = 0;

            int index1 = xf + yf * width;

            int col = image.pixels[index1];
            Colours.RGBValue rgb = colours.createRGB(red(col), green(col), blue(col), 255);
            Colours.HSVValue hsv = rgb.toHSV();
            hsv.v = .4f * hsv.v + .6f * worley(x0 + 2.0f * x1, y0 + 2.0f * y1);

            pixels[index] = hsv.toRGB().toColor();

         }
      }
      updatePixels();
//      noLoop();

//      saveFrame("/Users/elias.jordan/Desktop/warping/frame-####.png");
   }

   static int wrapIndex(int i, int size) {
      return (i % size + size) % size;
   }

   float pattern0(float x, float y) {
      return fbm(x, y, 2);
   }

   float pattern1(float x, float y) {
      int octaves = 2;
      float x1 = fbm(x, y, octaves);
      float y1 = fbm(x + 5.2f, y + 1.3f, octaves);

      return fbm(x + 4.0f * x1, y + 4.0f * y1, octaves);
   }

   float pattern2(float x, float y) {
      int octaves = 2;
      float x1 = fbm(x, y, octaves);
      float y1 = fbm(x + 5.2f, y + 1.3f, octaves);

      float x2 = fbm(x + 4.0f * x1 + 1.7f, y + 4.0f * y1 + 9.2f, octaves);
      float y2 = fbm(x + 4.0f * x1 + 8.3f, y + 4.0f * y1 + 2.8f, octaves);

      return fbm(x + x2 * 4.0f, y + y2 * 4.0f, octaves);
   }


   float noiseOffset(float x) {
      return x * 0.008f;
   }

   /**
    * Fractional brownian motion
    * Used this article as a reference: https://www.iquilezles.org/www/articles/fbm/fbm.htm
    */
   private float fbm(float x, float y, int octaves) {
      float G = 0.707f;
      float f = 1.0f;
      float a = 1.0f;
      float t = 0.0f;
      for (int i = 0; i < octaves; i++) {
         t += a * noise(x * f, y * f);
         f *= 2.0;
         a *= G;
      }
      return t > 1 ? 1.0f : t;
   }

   public static void main(String[] args) {
      PApplet.main(DomainWarping.class);
   }
}
