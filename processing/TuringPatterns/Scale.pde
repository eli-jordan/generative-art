

Scale createScale(int w, int h, int activatorRadius, int inhibitorRadius, float smallAmount, color c) {
  return new Scale(w, h, activatorRadius, inhibitorRadius, smallAmount, c, 0);
}

class Scale {
  int w;
  int h;

  int inhibitorRadius;
  int activatorRadius;
  float smallAmount;
  
  float weight;

  float[][] inhibitor;
  float[][] activator;
  float[][] variation;
  
  Complex[][] kernelFFT;

  color c;

  Scale(int w, int h, int activatorRadius, int inhibitorRadius, float smallAmount, color c, float weight) {
    this.w = w;
    this.h = h;
    this.inhibitorRadius = inhibitorRadius;
    this.activatorRadius = activatorRadius;
    this.smallAmount = smallAmount;
    this.c = c;
    this.weight = weight;

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
