
int count = 1;
long copy_nanos = 0;
long bit_reversal_nanos = 0;
long butterfly_updates = 0;

Complex[] fft(Complex[] x) {
  // check that length is a power of 2
  int n = x.length;
  if (Integer.highestOneBit(n) != n) {
    throw new RuntimeException("n is not a power of 2");
  }
  
  long start, end;
  
  start = System.nanoTime();
  Complex[] out = new Complex[n];
  System.arraycopy(x, 0, out, 0, n);
  end = System.nanoTime();
  
  copy_nanos += end - start;

  // bit reversal permutation
  start = System.nanoTime();
  int shift = 1 + Integer.numberOfLeadingZeros(n);
  for (int k = 0; k < n; k++) {
    int j = Integer.reverse(k) >>> shift;
    if (j > k) {
      Complex temp = x[j];
      out[j] = x[k];
      out[k] = temp;
    }
  }
  end = System.nanoTime();
  bit_reversal_nanos += end - start;

  // butterfly updates
  start = System.nanoTime();
  for (int L = 2; L <= n; L = L+L) {
    for (int k = 0; k < L/2; k++) {
      float kth = -2 * k * PI / L;
      Complex w = new Complex(cos(kth), sin(kth));
      for (int j = 0; j < n/L; j++) {
        Complex tao = w.mult(out[j*L + k + L/2]);
        out[j*L + k + L/2] = out[j*L + k].minus(tao); 
        out[j*L + k]       = out[j*L + k].add(tao);
      }
    }
  }
  end = System.nanoTime();
  butterfly_updates += end - start;
  
  count++;
  
  //if(count % 10000 == 0) {
  //  println("================== FFT Counters ==============================");
  //  println("       Copy Avg: " + (copy_nanos / (double) count) * 1e-6 + " ms");
  //  println("Bit-reverse Avg: " + (bit_reversal_nanos / (double) count) * 1e-6 + " ms");
  //  println("  Butterfly Avg: " + (butterfly_updates / (double) count) * 1e-6 + " ms");
  //  println("==============================================================");
    
  //  copy_nanos = 0;
  //  bit_reversal_nanos = 0;
  //  butterfly_updates = 0;
  //  count = 1;
  //}
  
  return out;
}

Complex[] ifft(Complex[] x) {
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


Complex[][] fft2d(Complex[][] values) {
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



Complex[][] wrapReals(float[][] re) {
  int ydim = re.length;
  int xdim = re[0].length;
  
  Complex[][] complex = new Complex[ydim][xdim];
  for(int y = 0; y < ydim; y++) {
    for(int x = 0; x < xdim; x++) {
      complex[y][x] = new Complex(re[y][x], 0);
    }
  }
  
  return complex;
}

Complex[][] ifft2d(Complex[][] values) {
  int ydim = values.length;
  int xdim = values[0].length;
  Complex[][] result = new Complex[ydim][xdim];

  // First take the fft of the rows
  for (int y = 0; y < ydim; y++) {
    result[y] = ifft(values[y]);
  }
  //println("ifft2d: inverse-fft-rows took: " + (end - start) + " ms");

  result = transpose(result);

  // Second take the fft of the columns in the result
  for (int x = 0; x < xdim; x++) {
    result[x] = ifft(result[x]);
  }

  result = transpose(result);

  return result;
}

Complex[][] transpose(Complex[][] values) {
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
  Complex[] data1 = new Complex[] { new Complex(1, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0) };
  println(java.util.Arrays.asList(fft(data1)));
  
  Complex[] data2 = new Complex[] { new Complex(0, 0), new Complex(1, 0), new Complex(0, 0), new Complex(0, 0) };
  println(java.util.Arrays.asList(fft(data2)));
  
  Complex[] data3 = new Complex[] { new Complex(0, 0), new Complex(0, 0), new Complex(1, 0), new Complex(0, 0) };
  println(java.util.Arrays.asList(fft(data3)));
  
  Complex[] data4 = new Complex[] { new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(1, 0) };
  println(java.util.Arrays.asList(fft(data4)));
}

/*
To run fft in python repl using numpy:

>>> x = numpy.zeros((4,4))
>>> x[0, 2] = 1
>>> numpy.fft.fft2(x)
*/
void testRunFFT2d() {
  Complex[][] data = new Complex[][] {
    new Complex[] { new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0) }, 
    new Complex[] { new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0) }, 
    new Complex[] { new Complex(0, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0) }, 
    new Complex[] { new Complex(1, 0), new Complex(0, 0), new Complex(0, 0), new Complex(0, 0) }
  };

  printMatrix(fft2d(data));
}

void printMatrix(Complex[][] data) {
  int ydim = data.length;
  int xdim = data[0].length;
  for (int y = 0; y < ydim; y++) {
    for (int x = 0; x < xdim; x++) {
      print(data[y][x] + ", ");
    }
    println();
  }
}














/////////////////

Complex[] dft(float[] values) {
  int N = values.length;
  Complex[] X = new Complex[N];
  for (int k = 0; k < N; k++) {
    float re = 0;
    float im = 0;
    for (int n = 0; n < N; n++) {
      float phi = (TWO_PI * k * n) / N;
      re += values[n] * cos(phi);
      im -= values[n] * sin(phi);
    }
    X[k] = new Complex(re, im);
  }

  return X;
}
