
Complex[] fft(Complex[] values) {
  if (values.length <= 1) {
    return values;
  }

  if (values.length % 2 != 0) {
    throw new IllegalArgumentException("n=" + values.length + " is not a power of 2");
  }

  Complex[] even = fft(evens(values));
  Complex[] odd = fft(odds(values));

  Complex[] temp = new Complex[values.length];
  for (int k = 0; k < values.length / 2; k++) {
    Complex w = omega(k, values.length);
    temp[k] = even[k].add(w.mult(odd[k]));
    temp[k + values.length / 2] = even[k].minus(w.mult(odd[k]));
  }

  return temp;
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

Complex[][] ifft2d(Complex[][] values) {
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

Complex[] evens(Complex[] values) {
  Complex[] result = new Complex[values.length / 2];
  for (int i = 0; i < values.length; i++) {
    if (i % 2 == 0) {
      result[i / 2] = values[i];
    }
  }

  return result;
}

Complex[] odds(Complex[] values) {
  Complex[] result = new Complex[values.length / 2];
  for (int i = 0; i < values.length; i++) {
    if (i % 2 != 0) {
      result[i / 2] = values[i];
    }
  }

  return result;
}

Complex omega(int k, int n) {
  float w = -TWO_PI * k / n;
  return new Complex(cos(w), sin(w));
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

void inplace_fft(Complex[] x) {
  // check that length is a power of 2
  int n = x.length;
  if (Integer.highestOneBit(n) != n) {
    throw new RuntimeException("n is not a power of 2");
  }

  // bit reversal permutation
  int shift = 1 + Integer.numberOfLeadingZeros(n);
  for (int k = 0; k < n; k++) {
    int j = Integer.reverse(k) >>> shift;
    if (j > k) {
      Complex temp = x[j];
      x[j] = x[k];
      x[k] = temp;
    }
  }

  // butterfly updates
  for (int L = 2; L <= n; L = L+L) {
    for (int k = 0; k < L/2; k++) {
      float kth = -2 * k * PI / L;
      Complex w = new Complex(cos(kth), sin(kth));
      for (int j = 0; j < n/L; j++) {
        Complex tao = w.mult(x[j*L + k + L/2]);
        x[j*L + k + L/2] = x[j*L + k].minus(tao); 
        x[j*L + k]       = x[j*L + k].add(tao);
      }
    }
  }
}
