package turingpatterns;

import org.jtransforms.fft.DoubleFFT_2D;
import processing.core.PApplet;

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
   static float compoundScaleDelta(Grid g, int x, int y) {
      float value = 0;
      for (Scale scale : g.scales) {
         if (scale.activator[y][x] > scale.inhibitor[y][x]) {
            value += scale.config.smallAmount;
         } else {
            value -= scale.config.smallAmount;
         }
      }
      value /= g.scales.size();
      return value;
   }

   private final static ExecutorService exec = Executors.newWorkStealingPool();

   private final PApplet applet;

   private final ScaleCouplingAtCell deltas;

   private final List<Scale> scales;

   final float[][] grid;
   private double[][] gridFFT;

   final int[][] colors;

   int w, h;

   private Grid(Builder builder) {
      this.applet = builder.applet;
      this.deltas = builder.deltas;
      this.scales = builder.scales;
      w = builder.applet.width;
      h = builder.applet.height;

      this.gridFFT = new double[h][w*2];


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

   private void updateGridFFT() {
//      this.gridFFT = FFT.fft2d(FFT.wrapReals(grid));
      for(int y = 0; y < h; y++) {
         for(int x = 0; x < w; x++) {
            this.gridFFT[y][2*x] = this.grid[y][x];
            this.gridFFT[y][2*x + 1] = 0;
         }
      }

      DoubleFFT_2D fft = new DoubleFFT_2D(h, w);
      fft.complexForward(this.gridFFT);
   }

   private void updateScales() {
      updateGridFFT();

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

   double[][] getGridFFT() {
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
