
int GridSize = 16;
int BlurRadius = 3;

void setup() {
  size(1024, 1024);
  runConvolutionOnCenteredSquare();
  
  //size(512, 512);
  //blurLenaImage();
}



void runConvolutionOnCenteredSquare() {
  Complex[][] data = centeredSquare();

  Complex[][] kernel = createKernel(BlurRadius, GridSize);
  Complex[][] resultFFT = convolve2d_fft(data, kernel);
  Complex[][] resultBruteForce = convolve2d_brute_force(data);

  int scale = 512 / GridSize;
  
  // Render the source image
  push();
  translate(0, 0);
  float[][] sourceValues = getRenderValues(data, 0, 1);
  render(sourceValues, scale);
  pop();
  
  // Render the brute-force blurred image
  push();
  translate(512, 0);
  float[][] bfValues = getRenderValues(resultBruteForce, 0, 1);
  render(bfValues, scale);
  pop();
  
  // Render the fft convolution kernel
  push();
  translate(0, 512);
  float[][] kernelValues = getRenderValues(kernel, 0, 0.2); 
  render(kernelValues, scale);
  pop();
  
  // Render the fft convolution blurred image
  push();
  translate(512, 512);
  float[][] fftValues = getRenderValues(resultFFT, 0, 1);
  render(fftValues, scale);
  pop();
}

private float[][] getRenderValues(Complex[][] data, float rngStart, float rngEnd) {
  float[][] values = new float[GridSize][GridSize];
  for (int i = 0; i < GridSize; i++) {
    for (int j = 0; j < GridSize; j++) {
      int ix = i;
      int jx = j;
      float value = data[i][j].mag();
      values[ix][jx] = map(value, rngStart, rngEnd, 0, 255);
    }
  }
  return values;
}

/*
 Creates an arry representing a white square (as the value 1) in the center
 of a black grid (value 0).
*/
Complex[][] centeredSquare() {
  int dim = GridSize;
  Complex[][] data = new Complex[dim][dim];

  for (int i = 0; i < dim; i++) {
    for (int j = 0; j < dim; j++) {
      data[i][j] = new Complex(0, 0);
    }
  }

  for (int i = 0; i < GridSize; i++) {
    for (int j = 0; j < GridSize; j++) {
      if (
        i > 3 && 
        i < 12 && 
        j > 3 && 
        j < 12) {
        data[i][j] = new Complex(1, 0);
      }
    }
  }
  return data;
}

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


Complex[][] convolve2d_brute_force(Complex[][] values) {
  int dim = GridSize;
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
  int size = GridSize;
  float area = PI * BlurRadius * BlurRadius;
  float total = 0;
  for (int x = -BlurRadius; x <= BlurRadius; x++) {
    int yBound = floor(sqrt(BlurRadius*BlurRadius - x*x));
    for (int y = -yBound; y <= yBound; y++) {

      int ix = x + cx;
      int iy = y + cy;

      if (ix >= 0 && ix < size && iy >= 0 && iy < size) {
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
