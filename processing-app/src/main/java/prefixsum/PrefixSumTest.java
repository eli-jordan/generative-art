package prefixsum;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import glslfft.AppletTest;
import processing.core.PApplet;

import java.util.Arrays;
import java.util.List;

public class PrefixSumTest extends AppletTest {

   private PrefixSum sum;

   @Override
   public void setup() {
      this.sum = new PrefixSum(new DwPixelFlow(this));
      super.setup();

   }


   public void test4x1() {
      float[][] data = new float[][]{
          {1, 2, 3, 4},
      };
      runTest(data);
   }

   public void test4x2() {
      float[][] data = new float[][]{
          {1, 2, 3, 4},
          {5, 6, 7, 8}
      };
      runTest(data);
   }

   public void test4x4() {
      float[][] data = new float[][]{
          {1, 2, 3, 4},
          {5, 6, 7, 8},
          {9, 10, 11, 12},
          {13, 14, 15, 16},
      };
      runTest(data);
   }

   public void test2x4() {
      float[][] data = new float[][]{
          {1, 2},
          {5, 6},
          {9, 10},
          {13, 14},
      };
      runTest(data);
   }

   public void test2000_RandomInputs() {
      for (int i = 0; i < 2000; i++) {
         int width = (int) pow(2, (int) random(1, 8));
         int height = (int) pow(2, (int) random(1, 8));
         println("width=" + width + ", height=" + height);
         float[][] data = randomMatrix(width, height);
         runTest(data);
      }
   }

   public void _testTimingsOnLargeInput() {
      int w = 8192;
      int h = 8192;
      float[][] data = randomMatrix(w, h);


      {
         for(int i = 0; i < 20; i++) {
            gpuPrefixSum(data, false);
         }

         gpuPrefixSum(data, true);

      }

      {
         // Warm up
         for(int i = 0; i < 20; i++) {
            cpuPrefixSum(data);
         }

         long start = System.nanoTime();
         cpuPrefixSum(data);
         long end = System.nanoTime();
         println("Java");
         println("------");
         println("  Java Version: " + (end - start) / 1000 + " micros");

      }
   }

   private float[][] randomMatrix(int w, int h) {
      float[][] result = new float[h][w];
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            result[y][x] = random(-1, 1);
         }
      }
      return result;
   }

   private void runTest(float[][] data) {

      int w = data[0].length;
      int h = data.length;

      Buffer input = sum.newBuffer(w, h, sum.prepare(data));
      Buffer ping = sum.newBuffer(w, h);
      Buffer pong = sum.newBuffer(w, h);

      List<PrefixSum.Pass<Buffer>> passes = sum.prefixSumPasses(input, ping, pong, w, h);
      passes.forEach(PApplet::println);

      Buffer output = sum.runPasses(passes);

      float[][] expected = cpuPrefixSum(data);
      float[][] actual = sum.read(output);

      assertEquals(expected, actual);
   }

   private void assertEquals(float[][] expected, float[][] actual) {
      StringBuilder builder = new StringBuilder();
      boolean eq = true;
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            float delta = Math.abs(expected[y][x] - actual[y][x]);
            if (delta > 0.05) {
               eq = false;
               builder.append("Mismatch at: (" + x + ", " + y + "): Expected: " + expected[y][x] + ", Actual: " + actual[y][x]);
            }
         }
      }

      if (!eq) {
         throw new AssertionError(
             builder + "\nExpected: \n" + Arrays.deepToString(expected) +
                 "\n Actual:\n" + Arrays.deepToString(actual));
      }
   }

   private float[][] gpuPrefixSum(float[][] data, boolean printTimings) {
      int w = data[0].length;
      int h = data.length;
      long start = System.nanoTime();
      Buffer input = sum.newBuffer(w, h, sum.prepare(data));
      Buffer ping = sum.newBuffer(w, h);
      Buffer pong = sum.newBuffer(w, h);
      long allocate = System.nanoTime();

      List<PrefixSum.Pass<Buffer>> passes = sum.prefixSumPasses(input, ping, pong, w, h);
      Buffer output = sum.runPasses(passes);
      println(passes);
      long passExec = System.nanoTime();

      float[][] read = sum.read(output);

      long end = System.nanoTime();

      if(printTimings) {
         println("Shader");
         println("------");
         println("Buffer Allocation: " + (allocate - start) / 1000 + " micros");
         println("   Pass Execution: " + (passExec - allocate) / 1000 + " micros");
         println("     Read Texture: " + (end - passExec) / 1000 + " micros");
         println("            Total: " + (end - start) / 1000 + " micros");
         println();
      }

      return read;
   }

   private float[][] cpuPrefixSum(float[][] data) {
      float[][] result = new float[data.length][data[0].length];
      for (int i = 0; i < data.length; i++) {
         float[] input = data[i];

         float sum = input[0];
         result[i][0] = input[0];
         for (int j = 1; j < input.length; j++) {
            sum += input[j];
            result[i][j] = sum;
         }
      }
      return result;
   }

   public static void main(String[] args) {
      PApplet.main(PrefixSumTest.class);
   }
}
