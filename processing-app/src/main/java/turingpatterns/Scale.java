package turingpatterns;

import org.jtransforms.fft.DoubleFFT_2D;
import turingpatterns.config.ScaleConfig;
import warp.WorleyNoise;
import static processing.core.PApplet.*;
import java.util.concurrent.CountDownLatch;

class Scale {

   private static WorleyNoise worley = WorleyNoise.getInstance();

   private DoubleFFT_2D fft;

   ScaleConfig config;

   int w, h;

   int[][][] symmetricalX;
   int[][][] symmetricalY;

   float[][] inhibitor;
   float[][] activator;
   float[][] variation;

   float[][] nextInhibitor;
   float[][] nextActivator;

   double[][] kernelFFT;
   double[][] multiplyBuffer;

   int frame = 0;

   Scale(ScaleConfig config) {
      this.config = config;
      h = config.height;
      w = config.width;

      this.fft = new DoubleFFT_2D(h, w);

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

      double[][] activatorKernel;
      double[][] inhibitorKernel;

      if (config.blurType == ScaleConfig.BlurType.Circular) {
         activatorKernel = Convolution.createRealCircularKernel(this.config.activatorRadius, w, h);
         inhibitorKernel = Convolution.createRealCircularKernel(this.config.inhibitorRadius, w, h);
      }
//      else if (config.blurType == ScaleConfig.BlurType.Gaussian) {
//         activatorKernel = Convolution.createGaussianKernel(this.config.activatorRadius, w, h);
//         inhibitorKernel = Convolution.createGaussianKernel(this.config.inhibitorRadius, w, h);
//      }
      else {
         throw new IllegalArgumentException("Unknown BlurType " + config.blurType);
      }


      this.multiplyBuffer = new double[h][2*w];
      double[][] kernel = new double[h][2*w];

//      Complex factor = new Complex(0, 1);
      for (int y = 0; y < h; y++) {
         for (int x = 0; x < w; x++) {
            // Put the activator kernel in the real component
            kernel[y][2*x] = activatorKernel[y][x];

            // Put the inhibitor kernel in the imaginary component
            kernel[y][2*x + 1] = inhibitorKernel[y][x];
//            kernel[y][x] = activatorKernel[y][x].add(inhibitorKernel[y][x].mult(factor));
         }
      }
      this.fft.complexForward(kernel);

      this.kernelFFT = kernel;
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
         frame++;
         applyBlur(g);
         applySymmetry();
//         applyWarp();
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
//      Complex[][] convolution = Convolution.convolve2d_kernel(this.kernelFFT, g.getGridFFT());

      for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {

            // Perform the complex number multiplication

            double thisRe = this.kernelFFT[y][2*x];
            double thatRe = g.getGridFFT()[y][2*x];

            double thisIm = this.kernelFFT[y][2*x + 1];
            double thatIm = g.getGridFFT()[y][2*x + 1];

            double real = thisRe * thatRe - thisIm * thatIm;
            double imaginary = thisRe * thatIm + thisIm * thatRe;

            this.multiplyBuffer[y][2*x] = real;
            this.multiplyBuffer[y][2*x + 1] = imaginary;
         }
      }

      this.fft.complexInverse(this.multiplyBuffer, true);

      for (int x = 0; x < w; x++) {
         for (int y = 0; y < h; y++) {

            // Extract the separable components from the convolution.
            double activatorAvg = this.multiplyBuffer[y][2*x];
            double inhibitorAvg = this.multiplyBuffer[y][2*x + 1];

            this.activator[y][x] = (float) activatorAvg;
            this.inhibitor[y][x] = (float) inhibitorAvg;
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

      swap();
   }

   private void swap() {
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

   void applyWarp() {
      applyActivatorWarp();
      applyInhibitorWarp();
      swap();
   }


   void applyActivatorWarp() {
      float weight = this.config.activatorRadius / 2.0f;
      float z0 = frame * 0.005f;
      for(int x = 0; x < w; x++) {
         for(int y = 0; y < h; y++) {
            float x0 = x * 0.005f;
            float y0 = y * 0.005f;

            float x1 = worley.noise(x0, y0, z0);
            float y1 = worley.noise(x0 + 1.212f, y0 + 2.381f, z0);

//            float x2 = worley.noise(x0 + 2.0f * x1 + 1.7f, y0 + 2.0f * y1 + 9.2f);
//            float y2 = worley.noise(x0 + 2.0f * x1 + 8.3f, y0 + 2.0f * y1 + 8.2f);

            int xf = x + (int) map(x1, 0, 1, -weight, weight);
            int yf = y + (int) map(y1, 0, 1, -weight, weight);

            if(xf >= w) xf = w - 1;
            if(xf < 0) xf = 0;


            if(yf >= h) yf = h - 1;
            if(yf < 0) yf = 0;

            this.nextActivator[y][x] = this.activator[yf][xf];
//            this.nextInhibitor[y][x] = this.inhibitor[yf][xf];
         }
      }
   }

   void applyInhibitorWarp() {
      float weight = this.config.inhibitorRadius / 2.0f;
      float z0 = frame * 0.005f;
      for(int x = 0; x < w; x++) {
         for(int y = 0; y < h; y++) {
            float x0 = x * 0.005f;
            float y0 = y * 0.005f;

            float x1 = worley.noise(x0, y0, z0);
            float y1 = worley.noise(x0 + 1.212f, y0 + 2.381f, z0);

//            float x2 = worley.noise(x0 + 2.0f * x1 + 1.7f, y0 + 2.0f * y1 + 9.2f);
//            float y2 = worley.noise(x0 + 2.0f * x1 + 8.3f, y0 + 2.0f * y1 + 8.2f);

            int xf = x + (int) map(x1, 0, 1, -weight, weight);
            int yf = y + (int) map(y1, 0, 1, -weight, weight);

            if(xf >= w) xf = w - 1;
            if(xf < 0) xf = 0;


            if(yf >= h) yf = h - 1;
            if(yf < 0) yf = 0;

//            this.nextActivator[y][x] = this.activator[yf][xf];
            this.nextInhibitor[y][x] = this.inhibitor[yf][xf];
         }
      }
   }
}
