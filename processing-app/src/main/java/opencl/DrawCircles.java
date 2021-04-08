package opencl;

import processing.core.PApplet;

public class DrawCircles extends PApplet {

   @Override
   public void settings() {
      size(600, 600);
   }

   @Override
   public void setup() {
      background(0);

      int radius = 200;



      int[][] data = new int[height][width];

      int batchSize = 16;

      for(int y = height/2; y < 16 + height/2; y++) {
         for(int x = width/2; x < 16 + width/2 ; x++) {


            int numberOfBatches = (2*radius) / batchSize;
            for(int batch = 0; batch < numberOfBatches; batch++) {
               int batchStartOffset = batch * batchSize;
               int batchEndOffset = batchStartOffset + batchSize;
               for (int iy = -radius + batchStartOffset; iy <= -radius + batchEndOffset; iy++) {
                  float xBound = sqrt((float)(radius*radius - iy*iy));

                  // x coordinates at the left and right edges of the circle
                  int leftX = constrain((int)(x - xBound), 0, width - 1);
                  int rightX = constrain((int)(x + xBound), 0, width - 1);

                  int currentY = constrain(y + iy, 0, height - 1);

                  data[currentY][leftX]++;
                  data[currentY][rightX]++;
               }
            }
         }
      }

      loadPixels();
      int max = 0;
      for(int y = 0; y < height; y++) {
         for(int x = 0; x < width; x++) {
            int idx = y*width + x;
            int v = data[y][x];
//            pixels[idx] = color(map(v, 0, 10, 40, 255));

            if(v == 0) {
               pixels[idx] = color(40);
            } else if(v == 1) {
               pixels[idx] = color(255, 0, 0);
            } else if(v == 2) {
               pixels[idx] = color(0, 255, 0);
            } else if(v == 3) {
               pixels[idx] = color(0, 0, 255);
            } else if(v == 4) {
               pixels[idx] = color(255, 255, 0);
            } else if(v == 5) {
               pixels[idx] = color(0, 255, 255);
            } else if(v == 6) {
               pixels[idx] = color(255, 0, 255);
            } else {
               if(v > max) max = v;
               pixels[idx] = color(255);
               println(v);
            }
         }
      }

      println("Max Value: " + max);

      updatePixels();
   }

   public static void main(String[] args) {
      PApplet.main(DrawCircles.class);
   }
}
