package physarum;

import convolution.FastBlur;
import turingpatterns.Convolution;

import java.util.List;

import static processing.core.PApplet.*;

public class Physarum {

   private final float depositAmount = 20f;
   private final float evaporationRate = 0.59f;
   private final int blurRadius = 3;

   private FastBlur blur;

   List<PhysarumAgent> agents;

   int width;
   int height;

   double[][] trail;

   Physarum(int width, int height, List<PhysarumAgent> agents) {
      this.width = width;
      this.height = height;
      this.agents = agents;

      this.blur = FastBlur.newBuilder()
          .size(width, height)
          .kernel(Convolution.createRealCircularKernel(blurRadius, width, height))
          .build();

      this.trail = new double[height][width];
//      this.trailFFTBuffer = new double[height][width*2];
//      this.trailFFTConvBuffer = new double[height][width*2];
   }

   void update() {
      for (PhysarumAgent agent : agents) {
         agent.update(this.trail);
         int x = floor(agent.pos.x);
         int y = floor(agent.pos.y);

         this.trail[y][x] += depositAmount;
      }

      applyBlurFFT();
      applyEvaporation();
   }



   void applyBlurFFT() {
      this.blur.applyInplace(this.trail);
//
//      DoubleFFT_2D fft = new DoubleFFT_2D(height, width);
//
//      for (int y = 0; y < height; y++) {
//         for (int x = 0; x < width; x++) {
//            this.trailFFTBuffer[y][2*x] = this.trail[y][x];
//            this.trailFFTBuffer[y][2*x + 1] = 0;
//         }
//      }
//
//      fft.complexForward(this.trailFFTBuffer);
//
//      // Point-wise multiply the kernel FFT and the trail FFT
//      for (int y = 0; y < height; y++) {
//         for (int x = 0; x < width; x++) {
//            double trail = this.trailFFTBuffer[y][2*x];
//            double kernel = this.blurKernelFFT[y][2*x];
//            this.trailFFTConvBuffer[y][2*x] = trail * kernel;
//            this.trailFFTConvBuffer[y][2*x + 1] = 0;
//         }
//      }
//
//      // Perform the inverse FFT on the point-wise multiplied values
//      fft.complexInverse(this.trailFFTConvBuffer, true);
//
//      // Copy the result back into the trail array
//      for (int y = 0; y < height; y++) {
//         for (int x = 0; x < width; x++) {
//            this.trail[y][x] = this.trailFFTConvBuffer[y][2*x];
//         }
//      }
   }

   void applyEvaporation() {
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            this.trail[y][x] *= evaporationRate;
         }
      }
   }
}
