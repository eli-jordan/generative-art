package wavelets;

import jwave.tools.MathToolKit;
import processing.core.PApplet;
import processing.core.PImage;

public class ImageFusion extends PApplet {

   @Override
   public void settings() {
      size(512, 512);

   }

   @Override
   public void setup() {
      swapCoeffsTest();
      println(g.getClass());
   }

   private void swapCoeffsTest() {
      double[][] image = getImageDataGreyscale("/Users/elias.jordan/Desktop/lena/a1.png");
      println(MathToolKit.getExponent(512.0));

//      Coefficients2d imageC = Coefficients2d.from(image);



      // 1 -> 2x2
      // 2 -> 4x4
      // 3 -> 8x8
      // n -> 2^n x 2^n

      double[][] data = Coefficients2d.transform.forward(image, 4, 4);

      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int index = x + y * width;
            pixels[index] = color(
                (float) data[y][x]
            );
         }
      }

      updatePixels();
   }

   private void repairTest() {
      double[][] lena1 = getImageDataGreyscale("/Users/elias.jordan/Desktop/lena/lena1.png");
      double[][] lena2 = getImageDataGreyscale("/Users/elias.jordan/Desktop/lena/lena2.png");

      Coefficients2d lena1C = Coefficients2d.from(lena1);
      Coefficients2d lena2C = Coefficients2d.from(lena2);

      // Good for repair
      lena1C.applyA(lena2C, Math::max);
      lena1C.applyH(lena2C, Math::max);
      lena1C.applyV(lena2C, Math::max);
      lena1C.applyD(lena2C, Math::max);

      double[][] data = lena1C.inverse();

      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int index = x + y * width;
            pixels[index] = color(
                (float) data[y][x]
            );
         }
      }

      updatePixels();
   }

   private void facesTest() {
      double[][][] merge1 = getImageDataColour("/Users/elias.jordan/Desktop/lena/a1.png");
      Coefficients2d merge1R = Coefficients2d.from(merge1[0]);
      Coefficients2d merge1G = Coefficients2d.from(merge1[1]);
      Coefficients2d merge1B = Coefficients2d.from(merge1[2]);

      double[][][] merge2 = getImageDataColour("/Users/elias.jordan/Desktop/lena/a2.jpg");
      Coefficients2d merge2R = Coefficients2d.from(merge2[0]);
      Coefficients2d merge2G = Coefficients2d.from(merge2[1]);
      Coefficients2d merge2B = Coefficients2d.from(merge2[2]);

      merge1R.minMean(merge2R);
      merge1G.minMean(merge2G);
      merge1B.minMean(merge2B);

      double[][] dataR = merge1R.inverse();
      double[][] dataG = merge1G.inverse();
      double[][] dataB = merge1B.inverse();

      loadPixels();
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int index = x + y * width;
            pixels[index] = color(
                (float) dataR[y][x],
                (float) dataG[y][x],
                (float) dataB[y][x]
            );
         }
      }

      updatePixels();
   }



   private double[][][] getImageDataColour(String path) {
      PImage image = loadImage(path);

      double[][][] data = new double[3][image.height][image.width];
      for (int y = 0; y < image.height; y++) {
         for (int x = 0; x < image.width; x++) {
            int index = x + y * image.width;

            data[0][y][x] = red(image.pixels[index]);
            data[1][y][x] = green(image.pixels[index]);
            data[2][y][x] = blue(image.pixels[index]);
         }
      }
      return data;
   }

   private double[][] getImageDataGreyscale(String path) {
      PImage image = loadImage(path);

      double[][] data = new double[image.height][image.width];
      for (int y = 0; y < image.height; y++) {
         for (int x = 0; x < image.width; x++) {
            int index = x + y * image.width;
            float value = 0.30f * red(image.pixels[index]) +
                0.59f * green(image.pixels[index]) +
                0.11f * blue(image.pixels[index]);

            data[y][x] = value;
         }
      }
      return data;
   }

   @Override
   public void draw() {


   }

   public static void main(String[] args) {
      PApplet.main(ImageFusion.class);
   }
}
