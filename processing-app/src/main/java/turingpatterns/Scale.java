package turingpatterns;

import java.util.concurrent.CountDownLatch;

class Scale {
   enum BlurType {
      Circular,
      Gaussian
   }

   int w;
   int h;

   int inhibitorRadius;
   int activatorRadius;
   float smallAmount;

   int symmetry;
   int[][][] symmetricalX;
   int[][][] symmetricalY;

   float[][] inhibitor;
   float[][] activator;
   float[][] variation;

   float[][] nextInhibitor;
   float[][] nextActivator;

   Complex[][] kernelFFT;

   int colour;

   private Scale(Builder builder) {
      this.w = builder.w;
      this.h = builder.h;
      this.inhibitorRadius = builder.inhibitorRadius;
      this.activatorRadius = builder.activatorRadius;
      this.symmetry = builder.symmetry;
      this.smallAmount = builder.smallAmount;
      this.colour = builder.colour;

      this.inhibitor = new float[h][w];
      this.activator = new float[h][w];

      this.nextActivator = new float[h][w];
      this.nextInhibitor = new float[h][w];
      this.variation = new float[h][w];

      // Since our kernels use only the real component of the complex number that
      // is being convolved. We can convolve both kernels at the same time by
      // putting one kernels values in the real component and the other in the
      // imaginary component.
      //
      // Here we put the activator kernel in the real component and the
      // inhibitor kernel imaginary component.
      //
      // After convolution we can extract the relevant values out of each component
      // of the result. In this case activator from the real component and inhibitor from
      // the imaginary component.

      Complex[][] activatorKernel;
      Complex[][] inhibitorKernel;

      if (builder.type == BlurType.Circular) {
         activatorKernel = Convolution.createCircularKernel(activatorRadius, w, h);
         inhibitorKernel = Convolution.createCircularKernel(inhibitorRadius, w, h);
      } else if (builder.type == BlurType.Gaussian) {
         activatorKernel = Convolution.createGaussianKernel(activatorRadius, w, h);
         inhibitorKernel = Convolution.createGaussianKernel(inhibitorRadius, w, h);
      } else {
         throw new IllegalArgumentException("Unknown BlurType " + builder.type);
      }


      Complex[][] kernel = new Complex[h][w];

      Complex factor = new Complex(0, 1);
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            kernel[y][x] = activatorKernel[y][x].add(inhibitorKernel[y][x].mult(factor));
         }
      }

      this.kernelFFT = FFT.fft2d(kernel);
      precalculateSymmetryIndices();
   }

   /**
    * We precalculate the symmetrical indices once, then simply look them up for every frame.
    * Avoiding all those repeated trig functions is a significant performance boost.
    */
   private void precalculateSymmetryIndices() {

      this.symmetricalX = new int[h][w][symmetry];
      this.symmetricalY = new int[h][w][symmetry];

      int cx = w / 2;
      int cy = h / 2;
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int dx = x - cx;
            int dy = y - cy;
            for (int i = 1; i < symmetry; i++) {
               double angle = (i * 2.0d * Math.PI) / symmetry;
               double sinA = Math.sin(angle);
               double cosA = Math.cos(angle);
               int symX = (int) Math.floor(((dx * cosA - dy * sinA) + cx));
               int symY = (int) Math.floor(((dy * cosA + dx * sinA) + cy));

               symX = Convolution.wrapIndex(symX, w);
               symY = Convolution.wrapIndex(symY, h);

               this.symmetricalX[y][x][i] = symX;
               this.symmetricalY[y][x][i] = symY;
            }
         }
      }
   }

   void update(Grid g, CountDownLatch latch) {
      try {
         applyBlur(g);
         applySymmetry();
         updateVariation();
      } finally {
         latch.countDown();
      }
   }

   /**
    * Runs the blur/averaging algorithm using FFT
    */
   void applyBlur(Grid g) {
      // Convolve the merged kernels
      Complex[][] convolution = Convolution.convolve2d_kernel(this.kernelFFT, g.getGridFFT());

      for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {
            // Extract the separable components from the convolution.
            float activatorAvg = convolution[y][x].re;
            float inhibitorAvg = convolution[y][x].im;
            this.activator[y][x] = activatorAvg;
            this.inhibitor[y][x] = inhibitorAvg;
         }
      }
   }


   /**
    * Apply the configured symmetries.
    *
    * To apply symmetries the activator and inhibitor values are averaged at several counter-points
    * that are rotated around the center of the canvas.
    *
    * Note here we use a 'current' and 'next' array for the activator and inhibitor
    * to avoid averaging based on results that have already been averaged. This could be optimised by
    * reflecting the average to the symmetrical counter-point.
    */
   void applySymmetry() {
      if (symmetry <= 0) return;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            float activatorSum = this.activator[y][x];
            float inhibitorSum = this.inhibitor[y][x];
            for (int i = 1; i < symmetry; i++) {
//               double angle = (i * 2.0d * Math.PI) / symmetry;
//               double sinA = Math.sin(angle);
//               double cosA = Math.cos(angle);
//               int symX = (int) Math.floor(((dx * cosA - dy * sinA) + cx));
//               int symY = (int) Math.floor(((dy * cosA + dx * sinA) + cy));
//
//               symX = Convolution.wrapIndex(symX, w);
//               symY = Convolution.wrapIndex(symY, h);

               int symY = this.symmetricalY[y][x][i];
               int symX = this.symmetricalX[y][x][i];

               activatorSum += this.activator[symY][symX];
               inhibitorSum += this.inhibitor[symY][symX];
            }


            float activatorAvg = activatorSum / symmetry;
            float inhibitorAvg = inhibitorSum / symmetry;

            this.nextActivator[y][x] = activatorAvg;
            this.nextInhibitor[y][x] = inhibitorAvg;
         }
      }

      // Swap the current and next activator arrays
      float[][] aTemp = this.activator;
      this.activator = this.nextActivator;
      this.nextActivator = aTemp;

      // Swap the current and next inhibitor arrays
      float[][] iTemp = this.inhibitor;
      this.inhibitor = this.nextInhibitor;
      this.nextInhibitor = iTemp;
   }

   /**
    * Updates the variation for each cell based on the latest activator and inhibitor values.
    */
   void updateVariation() {
      for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {
            this.variation[y][x] = Math.abs(this.activator[y][x] - this.inhibitor[y][x]);
         }
      }
   }

   public static Builder newBuilder() {
      return new Builder();
   }

   public static class Builder {
      private int w;
      private int h;

      private BlurType type = BlurType.Gaussian;

      private int inhibitorRadius;
      private int activatorRadius;
      private int symmetry = -1;
      private float smallAmount;

      private int colour;

      public Builder size(int w, int h) {
         this.w = w;
         this.h = h;
         return this;
      }

      public Builder blur(BlurType type) {
         this.type = type;
         return this;
      }

      public Builder inhibitorRadius(float r) {
         this.inhibitorRadius = (int) r;
         return this;
      }

      public Builder activatorRadius(float r) {
         this.activatorRadius = (int) r;
         return this;
      }

      public Builder symmetry(int symmetry) {
         this.symmetry = symmetry;
         return this;
      }

      public Builder bumpAmount(float amount) {
         this.smallAmount = amount;
         return this;
      }

      public Builder colour(int colour) {
         this.colour = colour;
         return this;
      }

      public Scale build() {
         return new Scale(this);
      }
   }
}
