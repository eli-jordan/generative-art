package turingpatterns;

import turingpatterns.config.ScaleConfig;

import java.util.concurrent.CountDownLatch;

class Scale {

   ScaleConfig config;

   int w, h;

   int[][][] symmetricalX;
   int[][][] symmetricalY;

   float[][] inhibitor;
   float[][] activator;
   float[][] variation;

   float[][] nextInhibitor;
   float[][] nextActivator;

   Complex[][] kernelFFT;

   Scale(ScaleConfig config) {
      this.config = config;
      h = config.height;
      w = config.width;

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

      if (config.blurType == ScaleConfig.BlurType.Circular) {
         activatorKernel = Convolution.createCircularKernel(this.config.activatorRadius, w, h);
         inhibitorKernel = Convolution.createCircularKernel(this.config.inhibitorRadius, w, h);
      } else if (config.blurType == ScaleConfig.BlurType.Gaussian) {
         activatorKernel = Convolution.createGaussianKernel(this.config.activatorRadius, w, h);
         inhibitorKernel = Convolution.createGaussianKernel(this.config.inhibitorRadius, w, h);
      } else {
         throw new IllegalArgumentException("Unknown BlurType " + config.blurType);
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

      this.symmetricalX = new int[h][w][this.config.symmetry];
      this.symmetricalY = new int[h][w][this.config.symmetry];

      int cx = w / 2;
      int cy = h / 2;
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            int dx = x - cx;
            int dy = y - cy;
            for (int i = 1; i < this.config.symmetry; i++) {
               double angle = (i * 2.0d * Math.PI) / this.config.symmetry;
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
      if (this.config.symmetry <= 0) return;

      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            float activatorSum = this.activator[y][x];
            float inhibitorSum = this.inhibitor[y][x];
            for (int i = 1; i < this.config.symmetry; i++) {
               int symY = this.symmetricalY[y][x][i];
               int symX = this.symmetricalX[y][x][i];

               activatorSum += this.activator[symY][symX];
               inhibitorSum += this.inhibitor[symY][symX];
            }


            float activatorAvg = activatorSum / this.config.symmetry;
            float inhibitorAvg = inhibitorSum / this.config.symmetry;

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
}
