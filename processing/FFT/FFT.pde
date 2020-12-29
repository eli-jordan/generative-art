
// Eulers formula
//
// e^ix = cos(x) + i sin(x)


// omega(k, n) = exp( (-2*PI*k / n) i )
//             = cos( (-2*PI*k / n) ) + i sin( (-2*PI*k / n) )


class Complex {
  final float re;
  final float im;

  Complex(float re, float im) {
    this.re = re;
    this.im = im;
  }

  Complex add(Complex that) {
    return new Complex(this.re + that.re, this.im + that.im);
  }

  Complex minus(Complex that) {
    return new Complex(this.re - that.re, this.im - that.im);
  }

  Complex mult(Complex that) {
    float real = this.re * that.re - this.im * that.im;
    float imaginary = this.re * that.im + this.im * that.re;
    return new Complex(real, imaginary);
  }

  float mod() {
    return sqrt(pow(this.re, 2) + pow(this.im, 2));
  }

  float mag() {
    return sqrt(re*re + im*im);
  }

  Complex div(Complex that) {
    Complex output = this.mult(that.conjugate());
    float div = pow(that.mod(), 2);
    return new Complex(output.re / div, output.im / div);
  }

  Complex conjugate() {
    return new Complex(this.re, -this.im);
  }

  public String toString() {
    return re + " + " + im + "i";
  }
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

Complex[][] convolve2d_fft(Complex[][] x, Complex[][] y) {
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


enum Mode {
  fft, brute
}


////////////
//
// TUNABLES
//
////////////

int GridSize = 16;
int BlurRadius = 8;
Mode mode = Mode.fft;


// https://matlabgeeks.com/tips-tutorials/how-to-blur-an-image-with-a-fourier-transform-in-matlab-part-i/
int PaddingSize = 24;



void setup() {
  size(512, 512);


  Complex[][] data = centeredSquare();

  Complex[][] kernel = createKernel();
  Complex[][] result;
  if (mode == Mode.fft) {
    result = convolve2d_fft(data, kernel);
  } else if (mode == Mode.brute) {
    result = convolve2d_brute_force(data);
  } else {
    throw new RuntimeException("Unrecognised mode: " + mode);
  }

  int offset = 0;
  if (mode == Mode.fft) {
    offset = BlurRadius;
  }
  int scale = 512 / GridSize;
  float[][] values = new float[GridSize][GridSize];
  for (int i = PaddingSize + offset; i < (GridSize + PaddingSize + offset); i++) {
    for (int j = PaddingSize + offset; j < (GridSize + PaddingSize + offset); j++) {
      int ix = i - PaddingSize - offset;
      int jx = j - PaddingSize - offset;
      float value = 
        //kernel[ix][jx].mag();
        //data[i][j].mag(); 
        result[i][j].mag();
      values[ix][jx] = map(value, 0, 1, 0, 255);
    }
  }
  render(values, scale);
}

Complex[][] centeredSquare() {
  int dim = GridSize + 2*PaddingSize;
  Complex[][] data = new Complex[dim][dim];

  for (int i = 0; i < dim; i++) {
    for (int j = 0; j < dim; j++) {
      data[i][j] = new Complex(0, 0);
    }
  }

  for (int i = PaddingSize; i < (16 + PaddingSize); i++) {
    for (int j = PaddingSize; j < (16 + PaddingSize); j++) {
      if (
        i > (PaddingSize + 3) && 
        i < (PaddingSize + 12) && 
        j > (PaddingSize + 3) && 
        j < (PaddingSize + 12)) {
        data[i][j] = new Complex(1, 0);
      }
    }
  }
  return data;
}


Complex[][] createKernel() {
  int cx = BlurRadius;
  int cy = BlurRadius;
  int r = BlurRadius;

  int dim = GridSize + 2*PaddingSize;
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

      int ix = x + cx;
      int iy = y + cy;

      Complex c = new Complex(1.0f / area, 0);
      kernel[iy][ix] = c;
    }
  }

  return kernel;
}

Complex[][] convolve2d_brute_force(Complex[][] values) {
  int dim = GridSize + 2*PaddingSize;
  Complex[][] result = new Complex[dim][dim];

  for (int i = 0; i < dim; i++) {
    for (int j = 0; j < dim; j++) {
      result[i][j] = new Complex(0, 0);
    }
  }

  for (int x = 0; x < dim; x++) {
    for (int y = 0; y < dim; y++) {
      float avg = average(values, x, y);
      result[y][x] = new Complex(avg, 0);
    }
  }
  return result;
}

float average(Complex[][] grid, int cx, int cy) {
  int size = GridSize + 2*PaddingSize;
  float area = PI * BlurRadius * BlurRadius;
  float total = 0;
  //int count = 0;
  for (int x = -BlurRadius; x <= BlurRadius; x++) {
    int yBound = floor(sqrt(BlurRadius*BlurRadius - x*x));
    for (int y = -yBound; y <= yBound; y++) {

      int ix = x + cx;
      int iy = y + cy;

      if (ix >= 0 && ix < size && iy >= 0 && iy < size) {
        //count++;
        total += grid[iy][ix].re * (1.0 / area);
      }
    }
  }
  return total;
}

void render(float[][] values, int scale) {
  int ydim = values.length;
  int xdim = values[0].length;

  for (int x = 0; x < xdim; x++) {
    for (int y = 0; y < ydim; y++) {
      fill(values[y][x]);
      square(x*scale, y*scale, scale);
    }
  }
}




/////









































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
