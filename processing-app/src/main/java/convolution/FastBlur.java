package convolution;

import org.jtransforms.fft.DoubleFFT_2D;

public class FastBlur {

   private final DoubleFFT_2D fft;
   private final int width;
   private final int height;

   private final double[][] kernelFFT;

   private final double[][] dataBuffer;

   FastBlur(Builder builder) {
      this.width = builder.width;
      this.height = builder.height;
      this.fft = new DoubleFFT_2D(builder.height, builder.width);

      this.dataBuffer = new double[builder.height][2 * builder.width];

      double[][] kernel = new double[builder.height][2 * builder.width];
      for (int y = 0; y < this.height; y++) {
         for (int x = 0; x < width; x++) {
            kernel[y][2 * x] = builder.kernel[y][x];
            kernel[y][2 * x + 1] = 0;
         }
      }
      this.fft.complexForward(kernel);
      this.kernelFFT = kernel;
   }

   public static Builder newBuilder() {
      return new Builder();
   }

   public void applyInplace(double[][] data) {
      // Layout the data in the format needed by JTransforms
      for (int y = 0; y < this.height; y++) {
         for (int x = 0; x < width; x++) {
            this.dataBuffer[y][2 * x] = data[y][x];
            this.dataBuffer[y][2 * x + 1] = 0;
         }
      }

      // FFT the data
      this.fft.complexForward(dataBuffer);

      // Pointwise multiple the FFT'ed data with the FFT'ed kernel
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            double d = this.dataBuffer[y][2*x];
            double k = this.kernelFFT[y][2*x];
            this.dataBuffer[y][2*x] = d * k;
            this.dataBuffer[y][2*x + 1] = 0;
         }
      }

      // Reverse the FFT
      this.fft.complexInverse(this.dataBuffer, true);

      // Write the result back into the data array
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            data[y][x] = this.dataBuffer[y][2*x];
         }
      }
   }


   public static class Builder {
      private int width;
      private int height;
      private double[][] kernel;

      public Builder kernel(double[][] kernel) {
         this.kernel = kernel;
         return this;
      }

      public Builder size(int width, int height) {
         this.width = width;
         this.height = height;
         return this;
      }

      public FastBlur build() {
         return new FastBlur(this);
      }
   }
}
