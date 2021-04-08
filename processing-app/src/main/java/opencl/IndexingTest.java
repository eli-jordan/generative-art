package opencl;

import processing.core.PApplet;

import java.util.Arrays;

public class IndexingTest extends PApplet {

   @Override
   public void settings() {
      size(12, 12);
   }

   @Override
   public void setup() {
      go();
      go_reference();
   }

   int buf_index(int x, int y, int width) {
      return y * width + x;
   }

   void go_reference() {
      float[] data = new float[]{
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
      };

      int radius = 4;
      int gx = 6;
      int gy = 6;

      float sum = 0f;

      for (int iy = -radius; iy < radius; iy++) {
         int xBound = (int) sqrt((float) (radius * radius - iy * iy));
         for (int ix = -xBound; ix < xBound; ix++) {
            int y = clamp(gy + iy, 0, height - 1);
            int x = clamp(gx + ix, 0, width - 1);

            int idx = y * width + x;
            sum += data[idx];
         }
      }

      println("Reference Sum: " + sum);
   }

   void go() {


      int BATCH_SIZE = 2;
      int WORKGROUP_Y_DIM = 2;
      int WORKGROUP_X_DIM = 2;

      int row_block_size = WORKGROUP_X_DIM * (BATCH_SIZE / WORKGROUP_Y_DIM);

      float[] scan_rows = new float[]{
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
          1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
      };

      float[] shared = new float[row_block_size * WORKGROUP_Y_DIM];
      println("Shared Memory Size: " + shared.length);
      println("row_block_size=" + row_block_size);

      int radius = 4;
      int groupX = 3;
      int groupY = 3;

//      int lx = 0;
//      int ly = 0;


      int number_of_batches = (2 * radius) / BATCH_SIZE;
      println("number_of_batches=" + number_of_batches);

      float sum = 0.0f;

      for (int batch = 0; batch < number_of_batches; batch++) {
         println("================> Batch " + batch + ": Start...");
         int start_offset = batch * BATCH_SIZE;
         int end_offset = start_offset + BATCH_SIZE;

         println("start_offset=" + start_offset + ", end_offset=" + end_offset);
         for (int ly = 0; ly < WORKGROUP_Y_DIM; ly++) {
            for (int lx = 0; lx < WORKGROUP_X_DIM; lx++) {

               int gx = groupX * WORKGROUP_X_DIM + lx;
               int gy = groupY * WORKGROUP_Y_DIM + ly;

               int global_start_y = -radius + start_offset + gy;
               int count = 0;
               for (int iy = -radius + start_offset; iy < -radius + end_offset; iy += WORKGROUP_Y_DIM, count++) {
                  int xBound = (int) sqrt((float) (radius * radius - iy * iy));
//                  println("xBound=" + xBound);

                  int leftX = clamp(gx - xBound, 0, width - 1);
                  int rightX = clamp(gx + xBound, 0, width - 1);

                  int currentY = clamp(gy + iy, 0, height - 1);

                  //println("sampling: left=(" + leftX + ", " + currentY + "), right=(" + rightX + ", " + currentY + ")");
                  float rightValue = scan_rows[buf_index(rightX, currentY, width)];
                  float leftValue = scan_rows[buf_index(leftX, currentY, width)];
                  float difference = rightValue - leftValue;


                  // We calculate the local memory indices by offsetting
//                  int local_left_index = 2 * ((count*row_block_size/(BATCH_SIZE / WORKGROUP_Y_DIM)) + (ly * WORKGROUP_X_DIM + lx));
//                  int local_right_index = local_left_index + 1;//(local_left_index + row_block_size);

                  //#define IX(x, y, z) ((x) + (y) * N + (z) * N * N)

//                  int batchSteps = (BATCH_SIZE / WORKGROUP_X_DIM);
//                  int index = lx + ly * WORKGROUP_X_DIM + count * batchSteps * WORKGROUP_X_DIM;

                  int yPos = end_offset - currentY;
                  int index = yPos + lx;
                  println("yPos=" + yPos + ", itemPos=" + index + ", index: " + index);

//                  int local_right_index = 0;
//                  println("local=(" + lx + ", " + ly + "), " +
//                      "sampling: left=(" + leftX + ", " + currentY + ")=" + leftValue +
//                      ", count=" + count +
//                      ", right=(" + rightX + ", " + currentY + ")=" + rightValue +
//                      ": local_left_index=" + local_left_index + ", local_right_index=" + local_right_index);

                  println("local=(" + lx + ", " + ly + "), " +
                      "sampling: left=(" + leftX + ", " + currentY + ")=" + leftValue +
                      ", right=(" + rightX + ", " + currentY + ")=" + rightValue +
                      ", x-dist=" + (rightX - leftX) +
                      ", count=" + count +
                      ": index=" + index + ", difference=" + difference);

//                  shared[index] = difference;
               }
            }
         }

         int lx = 1;
         int ly = 1;

         for (int iy = 0; iy < (end_offset - start_offset); iy++) {
            int index = lx + iy;
            sum += shared[index];
         }


         println("shared: " + Arrays.toString(shared));
         println("<================ Batch " + batch + ": End...");
      }

      System.out.println("Sum: " + sum);

   }



   static public final int clamp(int amt, int low, int high) {
      return constrain(amt, low, high);
   }

   public static void main(String[] args) {
      PApplet.main(IndexingTest.class);
   }
}
