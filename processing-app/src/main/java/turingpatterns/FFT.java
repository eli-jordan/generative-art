package turingpatterns;

public class FFT {

   public static Complex[] fft(Complex[] x) {
      // check that length is a power of 2
      int n = x.length;
      if (Integer.highestOneBit(n) != n) {
         throw new RuntimeException("n is not a power of 2");
      }

      Complex[] out = new Complex[n];
      System.arraycopy(x, 0, out, 0, n);


      // bit reversal permutation
      int shift = 1 + Integer.numberOfLeadingZeros(n);
      for (int k = 0; k < n; k++) {
         int j = Integer.reverse(k) >>> shift;
         if (j > k) {
            Complex temp = x[j];
            out[j] = x[k];
            out[k] = temp;
         }
      }

      // butterfly updates
      for (int L = 2; L <= n; L = L + L) {
         for (int k = 0; k < L / 2; k++) {
            float kth = (float) (-2 * k * Math.PI / L);
            Complex w = new Complex((float) Math.cos(kth), (float) Math.sin(kth));
            for (int j = 0; j < n / L; j++) {
               Complex tao = w.mult(out[j * L + k + L / 2]);
               out[j * L + k + L / 2] = out[j * L + k].minus(tao);
               out[j * L + k] = out[j * L + k].add(tao);
            }
         }
      }
      return out;
   }

   public static Complex[] ifft(Complex[] x) {
      int n = x.length;
      Complex[] y = new Complex[n];

      // take conjugate
      for (int i = 0; i < n; i++) {
         y[i] = x[i].conjugate();
      }

      // compute forward FFT
      y = fft(y);

      // take conjugate again
      for (int i = 0; i < n; i++) {
         y[i] = y[i].conjugate();
      }

      // divide by n
      for (int i = 0; i < n; i++) {
         y[i] = y[i].div(new Complex(n, 0));
      }

      return y;
   }


   public static Complex[][] fft2d(Complex[][] values) {
      int ydim = values.length;
      int xdim = values[0].length;
      Complex[][] result = new Complex[ydim][xdim];

      // First take the fft of the rows
      for (int y = 0; y < ydim; y++) {
         result[y] = fft(values[y]);
      }

      result = transpose(result);

      // Second take the fft of the columns in the result
      for (int x = 0; x < xdim; x++) {
         result[x] = fft(result[x]);
      }

      result = transpose(result);

      return result;
   }


   public static Complex[][] wrapReals(float[][] re) {
      int ydim = re.length;
      int xdim = re[0].length;

      Complex[][] complex = new Complex[ydim][xdim];
      for (int y = 0; y < ydim; y++) {
         for (int x = 0; x < xdim; x++) {
            complex[y][x] = new Complex(re[y][x], 0);
         }
      }

      return complex;
   }

   public static Complex[][] ifft2d(Complex[][] values) {
      int ydim = values.length;
      int xdim = values[0].length;
      Complex[][] result = new Complex[ydim][xdim];

      // First take the fft of the rows
      for (int y = 0; y < ydim; y++) {
         result[y] = ifft(values[y]);
      }

      result = transpose(result);

      // Second take the fft of the columns in the result
      for (int x = 0; x < xdim; x++) {
         result[x] = ifft(result[x]);
      }

      result = transpose(result);

      return result;
   }

   public static Complex[][] transpose(Complex[][] values) {
      int m = values.length;
      int n = values[0].length;

      Complex[][] result = new Complex[n][m];

      for (int x = 0; x < n; x++) {
         for (int y = 0; y < m; y++) {
            result[x][y] = values[y][x];
         }
      }

      return result;
   }

//

   /*
   To run fft in python repl using numpy:

   >>> numpy.fft.fft([1, 0, 0])
   */
   void testRunFFT1d() {
      Complex[] data1 = new Complex[]{new Complex(1, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0)};
      System.out.println(java.util.Arrays.asList(fft(data1)));

      Complex[] data2 = new Complex[]{new Complex(0, 0), new Complex(1, 0), new Complex(0, 0), new Complex(0, 0)};
      System.out.println(java.util.Arrays.asList(fft(data2)));

      Complex[] data3 = new Complex[]{new Complex(0, 0), new Complex(0, 0), new Complex(1, 0), new Complex(0, 0)};
      System.out.println(java.util.Arrays.asList(fft(data3)));

      Complex[] data4 = new Complex[]{new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(1, 0)};
      System.out.println(java.util.Arrays.asList(fft(data4)));
   }

    /*
    To run fft in python repl using numpy:

    >>> x = numpy.zeros((4,4))
    >>> x[0, 2] = 1
    >>> numpy.fft.fft2(x)
    */
//    void testRunFFT2d() {
//        Complex[][] data = new Complex[][]{
//                new Complex[]{new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0)},
//                new Complex[]{new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0)},
//                new Complex[]{new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0)},
//                new Complex[]{new Complex(1, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0)}
//        };
//
//        printMatrix(fft2d(data));
//    }
//
//    void printMatrix(Complex[][] data) {
//        int ydim = data.length;
//        int xdim = data[0].length;
//        for (int y = 0; y < ydim; y++) {
//            for (int x = 0; x < xdim; x++) {
//                System.out.print(data[y][x] + ", ");
//            }
//            System.out.println();
//        }
//    }

   Complex[] dft(float[] values) {
      int N = values.length;
      Complex[] X = new Complex[N];
      for (int k = 0; k < N; k++) {
         float re = 0;
         float im = 0;
         for (int n = 0; n < N; n++) {
            float phi = (float) (Math.PI * 2 * k * n) / N;
            re += values[n] * Math.cos(phi);
            im -= values[n] * Math.sin(phi);
         }
         X[k] = new Complex(re, im);
      }

      return X;
   }
}
