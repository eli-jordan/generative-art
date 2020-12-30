/*
 Creates a convolution kernel with weights representing an average in a circular region.
 
 Note: The weights need to be centered at (0, 0) and wrap around to the opposite edges to avoid 
       edge effects when used with FFT to perform convolution.
*/
Complex[][] createKernel(int radius, int dim) {
  // Center the kernel at (0,0)
  int cx = 0;
  int cy = 0;
  int r = radius;

  Complex[][] kernel = new Complex[dim][dim];

  for (int i = 0; i < dim; i++) {
    for (int j = 0; j < dim; j++) {
      kernel[i][j] = new Complex(0, 0);
    }
  }

  float area = PI * r * r;
  for (int x = -r; x <= r; x++) {
    int yBound = floor(sqrt(r*r - x*x));
    for (int y = -yBound; y <= yBound; y++) {

      // We wrap indices to avoid edge effects.
      int ix = wrapIndex(x + cx, dim);
      int iy = wrapIndex(y + cy, dim);

      Complex c = new Complex(1.0f / area, 0);
      kernel[iy][ix] = c;
    }
  }

  return kernel;
}

int wrapIndex(int i, int size) {
  return (i % size + size) % size;
}

Complex[][] convolve2d(Complex[][] x, Complex[][] y) {
  // compute FFT of each sequence
  Complex[][] a = fft2d(x);
  Complex[][] b = fft2d(y);

  int m = x.length;
  int n = x[0].length;

  // point-wise multiply
  Complex[][] c = new Complex[n][m];
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
      c[i][j] = a[i][j].mult(b[i][j]);
    }
  }

  // compute inverse FFT
  return ifft2d(c);
}

Complex[][] convolve2d_kernel(Complex[][] fftedKernel, Complex[][] inputFFT) {
  long start, end;
  Complex[][] a = fftedKernel;
  Complex[][] b = inputFFT;

  int m = a.length;
  int n = a[0].length;

  // point-wise multiply
  start = System.currentTimeMillis();
  Complex[][] c = new Complex[n][m];
  for (int i = 0; i < n; i++) {
    for (int j = 0; j < m; j++) {
      c[i][j] = a[i][j].mult(b[i][j]);
    }
  }
  end = System.currentTimeMillis();
  
  //println("convolve2d_kernel: point-wise multiply took: " + (end - start) + " ms");

  // compute inverse FFT
  start = System.currentTimeMillis();
  Complex[][] result = ifft2d(c);
  end = System.currentTimeMillis();
  
  // println("convolve2d_kernel: inverse-fft took: " + (end - start) + " ms");
  
  return result;
}
