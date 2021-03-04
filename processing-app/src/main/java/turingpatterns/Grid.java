package turingpatterns;

import processing.core.PApplet;
import turingpatterns.sketches.Gradient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static processing.core.PApplet.*;

interface ScaleCouplingAtCell {
   float delta(Grid g, int x, int y);
}

class Grid {

   /**
    * The {@link ScaleCouplingAtCell} function for multi-scale turing patterns.
    * <p>
    * It selects the scale with the smallest variation and bumps the
    * current value based on its config.
    */
   static float multiScaleDelta(Grid g, int x, int y) {
      float minVariation = Float.MAX_VALUE;
      Scale bestScale = null;
      for (Scale s : g.scales) {
         if (s.variation[y][x] <= minVariation) {
            minVariation = s.variation[y][x];
            bestScale = s;
         }
      }

      float colourBump = max(bestScale.config.smallAmount * 5, 0.001f);
      g.colors[y][x] = g.applet.lerpColor(g.colors[y][x], bestScale.config.colour, colourBump);

      if (bestScale.activator[y][x] > bestScale.inhibitor[y][x]) {
         return bestScale.config.smallAmount;
      } else {
         return -bestScale.config.smallAmount;
      }
   }

   /**
    * The {@link ScaleCouplingAtCell} function for compound turing patterns.
    * <p>
    * It sums the increment for all scales.
    */
   static ScaleCouplingAtCell compoundScaleDelta(PApplet applet) {
//      Gradient grad = Gradient.earthy2(applet);

//      Gradient grad = new Gradient(
//          applet.color(0),
//          applet.color(140, 47, 48),
//          applet.color(214,170,112),
//          applet.color(160,191,204),
//          applet.color(160,191,204),
//          applet.color(139,156,123),
//          applet.color(255)
//      );

//      Gradient grad = new Gradient(
////          applet.color(160,191,204),
//          applet.color(214,170,112),
//          applet.color(139,156,123),
////          applet.color(225,128,129),
//          applet.color(140, 47, 48),
////          applet.color(178, 94, 65),
//          applet.color(0)
//      );

//      Gradient grad = new Gradient(
//          applet.color(152, 49, 44),
//          applet.color(219, 206, 182),
//          applet.color(176, 111, 74),
//          applet.color(219, 206, 182),
//          applet.color(212, 117, 99)
//      );

//      Gradient grad = new Gradient(
//          applet.color(179, 160, 145),
//          applet.color(214,170,112),
//          applet.color(140, 47, 48),
//          applet.color(50, 45, 50),
//          applet.color(20, 20, 20)
//      );

//      Gradient grad = new Gradient(
//          applet.color(179, 160, 145),
//          applet.color(214,170,112),
//          applet.color(140, 47, 48),
//          applet.color(140, 47, 48),
//          applet.color(50, 45, 50),
//          applet.color(20, 20, 20)
//      );

      Gradient grad = new Gradient(
          applet.color(179, 160, 145),
          applet.color(214,170,112),
          applet.color(140, 47, 48),
          applet.color(50, 45, 50),
          applet.color(20, 20, 20)
      );

      return (Grid g, int x, int y) -> {
         float value = 0;
//      int colour = g.colors[y][x];
         for (Scale scale : g.scales) {
            if (scale.activator[y][x] > scale.inhibitor[y][x]) {
               value += scale.config.smallAmount;
//            colour = lerpColor(colour, scale.config.colour, scale.config.smallAmount * 5, RGB);
            } else {
               value -= scale.config.smallAmount;
//            colour = lerpColor(colour, scale.config.colour, -scale.config.smallAmount * 5, RGB);
            }
         }

         float amt = map(g.grid[y][x], -1, 1, 0, 0.9999f);
         g.colors[y][x] = grad.at(amt);

         return value;
      };
   }

   private final static ExecutorService exec = Executors.newWorkStealingPool();

   private final PApplet applet;

   private final ScaleCouplingAtCell deltas;

   private final List<Scale> scales;

   final float[][] grid;
   private Complex[][] gridFFT;

   final int[][] colors;

   private Grid(Builder builder) {
      this.applet = builder.applet;
      this.deltas = builder.deltas;
      this.scales = builder.scales;
      int w = builder.applet.width;
      int h = builder.applet.height;


      this.grid = new float[h][w];
      this.colors = new int[h][w];

      // initialise the colour for each location to black
      for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {
            colors[y][x] = applet.color(0, 0, 0, 255);
         }
      }

      // initialise each cell in the grid to a random number between -1 and 1
      for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {
            grid[y][x] = applet.random(-1, 1);
         }
      }

      // initialise each cell in the grid to a random number between -1 and 1
      for (int x = 0; x < applet.width; x++) {
         for (int y = 0; y < applet.height; y++) {
            grid[y][x] = applet.random(-1, 1);
         }
      }
   }

   public void update() {
      updateScales();
      updateGridValues();
      normaliseGridValues();
   }

   private void updateScales() {
      this.gridFFT = FFT.fft2d(FFT.wrapReals(grid));

      final CountDownLatch latch = new CountDownLatch(scales.size());
      for (final Scale scale : scales) {
         exec.execute(() -> scale.update(this, latch));
      }

      try {
         latch.await();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void updateGridValues() {
      for (int x = 0; x < applet.width; x++) {
         for (int y = 0; y < applet.height; y++) {
            float delta = this.deltas.delta(this, x, y);
            grid[y][x] += delta;
         }
      }
   }

   private void normaliseGridValues() {
      float max = 0;
      float min = Float.MAX_VALUE;
      for (int x = 0; x < applet.width; x++) {
         for (int y = 0; y < applet.height; y++) {
            max = Math.max(max, grid[y][x]);
            min = Math.min(min, grid[y][x]);
         }
      }

      for (int x = 0; x < applet.width; x++) {
         for (int y = 0; y < applet.height; y++) {
            grid[y][x] = map(grid[y][x], min, max, -1, 1);
         }
      }
   }

   Complex[][] getGridFFT() {
      return this.gridFFT;
   }

   public static Builder newBuilder(PApplet applet) {
      return new Builder(applet);
   }

   static class Builder {
      private final PApplet applet;
      private ScaleCouplingAtCell deltas;
      private List<Scale> scales;

      private Builder(PApplet applet) {
         this.applet = applet;
      }

      public Builder scaleCoupling(ScaleCouplingAtCell deltas) {
         this.deltas = deltas;
         return this;
      }

      public Builder scales(List<Scale> scales) {
         this.scales = new ArrayList<>(scales);
         return this;
      }

      public Grid build() {
         return new Grid(this);
      }
   }
}
