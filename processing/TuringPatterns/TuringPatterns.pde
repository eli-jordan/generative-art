
import java.util.concurrent.*;

class Scale {
  int w;
  int h;

  int inhibitorRadius;
  int activatorRadius;
  float smallAmount;

  float[][] inhibitor;
  //Complex[][] inhibitorKernelFFT;

  float[][] activator;
  //Complex[][] activatorKernelFFT;
  
  Complex[][] kernelFFT;

  float[][] variation;


  color c;

  Scale(int w, int h, int activatorRadius, int inhibitorRadius, float smallAmount, color c) {
    this.w = w;
    this.h = h;
    this.inhibitorRadius = inhibitorRadius;
    this.activatorRadius = activatorRadius;
    this.smallAmount = smallAmount;
    this.c = c;

    inhibitor = new float[h][w];
    activator = new float[h][w];
    variation = new float[h][w];

    if (w != h) {
      // The kernel initialisation doesn't support it right now. Should be easy to add.
      throw new IllegalArgumentException("Non-square canvas is not supported");
    }
    
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
    Complex[][] activatorKernel = createKernel(activatorRadius, w);
    Complex[][] inhibitorKernel = createKernel(inhibitorRadius, w);
    Complex[][] kernel = new Complex[w][w];
    
    Complex factor = new Complex(0, 1);
    for(int y = 0; y < w; y++) {
      for(int x = 0; x < w; x++) {
        kernel[y][x] = activatorKernel[y][x].add(inhibitorKernel[y][x].mult(factor));
      }
    }
    
    kernelFFT = fft2d(kernel);
  }

  void update(Grid g, CountDownLatch latch) {

    // Convolve the merged kernels
    Complex[][] convolution = convolve2d_kernel(this.kernelFFT, g.gridFFT);
    
    for (int x = 0; x < w; x ++) {
      for (int y = 0; y < h; y++) {
        // Extract the separable components from the convolution.
        float activatorAvg = convolution[y][x].re;
        float inhibitorAvg = convolution[y][x].im;
        activator[y][x] = activatorAvg;
        inhibitor[y][x] = inhibitorAvg;
        variation[y][x] = abs(activatorAvg - inhibitorAvg);
      }
    }
    
    latch.countDown();
  }
}

ExecutorService exec = Executors.newWorkStealingPool();

class Grid {

  int w;
  int h;

  Scale[] scales;

  float[][] grid;
  Complex[][] gridFFT;
  
  color[][] colors;
  
  float maxBump = 0;

  Grid(int w, int h) {
    this.w = w;
    this.h = h;

    this.scales = new Scale[] { 
      new Scale(w, h, 100, 200, 0.05, color(255, 0,   0)),
      new Scale(w, h,  20,  40, 0.04, color(0,   255, 0)),
      new Scale(w, h,  10,  20, 0.03, color(0,   0,   255)),
      new Scale(w, h,   5,  10, 0.02, color(155, 0,   255)),
      new Scale(w, h,   1,   2, 0.01, color(0,   0,   0))
    };

    grid = new float[h][w];
    colors = new color[h][w];

    for (int x = 0; x < w; x ++) {
      for (int y = 0; y < h; y++) {
        colors[y][x] = color(0, 0, 0, 255);
      }
    }

    for (int x = 0; x < w; x ++) {
      for (int y = 0; y < h; y++) {
        grid[y][x] = random(-1, 1);
      }
    }
    
    for(Scale s : scales) {
      maxBump = max(maxBump, s.smallAmount);
    }
  }

  void update() {
    gridFFT = fft2d(wrapReals(grid));
    
    final CountDownLatch latch = new CountDownLatch(scales.length);
    for (final Scale scale : scales) {
      exec.execute(new Runnable() {
        public void run() {
          scale.update(Grid.this, latch);
        }
      });
    }
    
    try { latch.await(); } catch(Exception e) { e.printStackTrace(); }

    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        float minVariation = Float.MAX_VALUE;
        Scale bestScale = null;
        for (Scale s : scales) {
          if (s.variation[y][x] < minVariation) {
            minVariation = s.variation[y][x];
            bestScale = s;
          }
        }

        //colors[y][x] = lerpColor(colors[y][x], bestScale.c, 0.1);

        if (bestScale.activator[y][x] > bestScale.inhibitor[y][x]) {
          grid[y][x] += bestScale.smallAmount;
        } else {
          grid[y][x] -= bestScale.smallAmount;
        }
      }
    }

    float max = 0;
    float min = Float.MAX_VALUE;
    for (int x = 0; x < w; x ++) {
      for (int y = 0; y < h; y++) {
        max = max(max, grid[y][x]);
        min = min(min, grid[y][x]);
      }
    }

    for (int x = 0; x < w; x ++) {
      for (int y = 0; y < h; y++) {
        grid[y][x] = map(grid[y][x], min, max, -1, 1);
        //map(grid[y][x], -1 - maxBump, 1 + maxBump, -1, 1);
      }
    }
  }
}

Grid g;

void setup() {
  size(2048, 2048);

  g = new Grid(width, height);
}

void draw() {
  loadPixels();

  for (int x = 0; x < width; x++) {
    for (int y = 0; y < height; y++) {
      //color c = g.colors[y][x];
      int index = x + y * width;

      float value = map(g.grid[y][x], -1, 1, 0, 255);
      //float r = red(c) * value;
      //float g = green(c) * value;
      //float b = blue(c) * value;

      pixels[index] = color(value);
    }
  }

  g.update();
  updatePixels();
  println("Frame Rate: " + frameRate);
  saveFrame();
}
