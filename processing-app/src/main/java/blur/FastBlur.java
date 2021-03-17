package blur;

import org.jtransforms.fft.DoubleFFT_2D;

public class FastBlur {

   // JTransform class for performing 2d FFTs on double values.
   private final DoubleFFT_2D fft;

   // Dimensions of the data to be blurred.
   private final int width;
   private final int height;

   // The FFT of the kernel that will be used to blur
   private final double[][] kernelFFT;

   // A buffer that is re-used to store intermediate results.
   private final double[][] dataBuffer;

   FastBlur(Builder builder) {
      this.width = builder.width;
      this.height = builder.height;
      this.fft = new DoubleFFT_2D(builder.height, builder.width);

      this.dataBuffer = new double[builder.height][2 * builder.width];
      this.kernelFFT = initKernelFFT(builder.kernelA, builder.kernelB);
   }

   /**
    * Since the blur kernels are all real numbers and therefore fully separable, we can pack two separate
    * kernel convolutions into one pass by putting on in the real component and one in the imaginary component.
    *
    * @param kernelA must not be null
    * @param kernelB may be null
    */
   private double[][] initKernelFFT(double[][] kernelA, double[][] kernelB) {
      double[][] kernel = new double[this.height][2 * this.width];
      for (int y = 0; y < this.height; y++) {
         for (int x = 0; x < width; x++) {
            kernel[y][2 * x] = kernelA[y][x];
            kernel[y][2 * x + 1] = kernelB != null ? kernelB[y][x] : 0;
         }
      }
      this.fft.complexForward(kernel);
      return kernel;
   }

   public static Builder newBuilder() {
      return new Builder();
   }

   /**
    * Equivalent to applyInto(data, data, null);
    */
   public void applyInplace(double[][] data) {
      applyInto(data, data, null);
   }

   /**
    * Applys the kernels to 'data' and
    * writes the result of applying 'kernelA' in 'resultA', and also
    * writes the result of applying 'kernelB' in 'resultB'
    *
    * @param data the input data
    * @param resultA where the result of applying kernelA is written
    * @param resultB where the result of applying kernelB is written
    */
   public void applyInto(double[][] data, double[][] resultA, double[][] resultB) {
      computeBlur(data);
      writeData(resultA, resultB);
   }

   /**
    * Apply the blur and leave the result in `this.dataBuffer`
    */
   private void computeBlur(double[][] data) {
      // Layout the data in the format needed by JTransforms
      layoutData(data);

      // FFT the data
      this.fft.complexForward(dataBuffer);

      // Pointwise multiple the FFT'ed data with the FFT'ed kernel
      pointwiseMultiply();

      // Reverse the FFT
      this.fft.complexInverse(this.dataBuffer, true);
   }


   private void layoutData(double[][] data) {
      for (int y = 0; y < this.height; y++) {
         for (int x = 0; x < width; x++) {
            this.dataBuffer[y][2 * x] = data[y][x];
            this.dataBuffer[y][2 * x + 1] = 0;
         }
      }
   }

   private void pointwiseMultiply() {
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {

            // Perform the complex number multiplication

            double thisRe = this.dataBuffer[y][2 * x];
            double thisIm = this.dataBuffer[y][2 * x + 1];

            double thatRe = this.kernelFFT[y][2 * x];
            double thatIm = this.kernelFFT[y][2 * x + 1];

            double real = thisRe * thatRe - thisIm * thatIm;
            double imaginary = thisRe * thatIm + thisIm * thatRe;

            this.dataBuffer[y][2 * x] = real;
            this.dataBuffer[y][2 * x + 1] = imaginary;
         }
      }
   }

   private void writeData(double[][] re, double[][] im) {
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            if(re != null) {
               re[y][x] = this.dataBuffer[y][2 * x];
            }

            if(im != null) {
               im[y][x] = this.dataBuffer[y][2 * x + 1];
            }
         }
      }
   }


   public static class Builder {
      private int width;
      private int height;
      private double[][] kernelA;
      private double[][] kernelB;

      public Builder kernelA(double[][] kernel) {
         this.kernelA = kernel;
         return this;
      }

      public Builder kernelB(double[][] kernel) {
         this.kernelB = kernel;
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
