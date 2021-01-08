package turingpatterns;
import java.util.concurrent.CountDownLatch;

class Scale {
    int w;
    int h;

    int inhibitorRadius;
    int activatorRadius;
    int symmetry;
    float smallAmount;

    float[][] inhibitor;
    float[][] activator;
    float[][] variation;

    Complex[][] kernelFFT;

    int colour;

    private Scale(Builder builder) {
        this.w = builder.w;
        this.h = builder.h;
        this.inhibitorRadius = builder.inhibitorRadius;
        this.activatorRadius = builder.activatorRadius;
        this.symmetry = builder.symmetry;
        this.smallAmount = builder.smallAmount;
        this.colour = builder.colour;

        inhibitor = new float[h][w];
        activator = new float[h][w];
        variation = new float[h][w];

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
        Complex[][] activatorKernel = Convolution.createKernel(activatorRadius, w, h);
        Complex[][] inhibitorKernel = Convolution.createKernel(inhibitorRadius, w, h);
        Complex[][] kernel = new Complex[h][w];

        Complex factor = new Complex(0, 1);
        for(int y = 0; y < h; y++) {
            for(int x = 0; x < w; x++) {
                kernel[y][x] = activatorKernel[y][x].add(inhibitorKernel[y][x].mult(factor));
            }
        }

        kernelFFT = FFT.fft2d(kernel);
    }

    void update(Grid g, CountDownLatch latch) {
        try {
            // Convolve the merged kernels
            Complex[][] convolution = Convolution.convolve2d_kernel(this.kernelFFT, g.getGridFFT());

            for (int x = 0; x < w; x ++) {
                for (int y = 0; y < h; y++) {
                    // Extract the separable components from the convolution.
                    float activatorAvg = convolution[y][x].re;
                    float inhibitorAvg = convolution[y][x].im;
                    this.activator[y][x] = activatorAvg;
                    this.inhibitor[y][x] = inhibitorAvg;
                    this.variation[y][x] = Math.abs(activatorAvg - inhibitorAvg);
                }
            }

            applySymmetry();
        } finally {
            latch.countDown();
        }
    }

    void applySymmetry() {
        if(symmetry > 0) {
            for(int y = 0; y < h; y++) {
                for(int x = 0; x < w; x++) {
                    int cx = w / 2;
                    int cy = h / 2;
                    int dx = x - cx;
                    int dy = y - cy;
                    float activatorSum = this.activator[y][x];
                    float inhibitorSum = this.inhibitor[y][x];
                    for(int i = 1; i < symmetry; i++) {
                        float angle = ((float) i / (float) symmetry) * 2.0f * (float) Math.PI;
                        float s = (float) Math.sin(angle);
                        float c = (float) Math.cos(angle);
                        int symX = (int)((dx * c - dy * s) + cx);
                        int symY = (int)((dy * c + dx * s) + cy);


                        if(symX >= w) symX = symX - w;
                        if(symX < 0) symX = w - (-1 * symX);
                        if(symY >= h) symY = symY - h;
                        if(symY < 0) symY = h - (-1 * symY);

                        activatorSum += this.activator[symY][symX];
                        inhibitorSum += this.inhibitor[symY][symX];
                    }

                    float activatorAvg = activatorSum / symmetry;
                    float inhibitorAvg = inhibitorSum / symmetry;

                    this.activator[y][x] = activatorAvg;
                    this.inhibitor[y][x] = inhibitorAvg;
                    this.variation[y][x] = Math.abs(activatorAvg - inhibitorAvg);
                }
            }
        }
    }


    /*
    kernel void apply_symmetry_to_scale(texture2d<float, access::read> tex [[ texture(0) ]],
                                device ScaleCell *scale_state [[ buffer(0) ]],
                                constant float *grid [[ buffer(1) ]],
                                constant ScaleConfig *scale_config [[ buffer(2) ]],
                                uint2 id [[ thread_position_in_grid ]]) {

   int w = tex.get_width();
   int h = tex.get_height();
   int idx = linear_index(id.x, id.y, w);

   int2 center = int2(w / 2, h / 2);
   int dx = id.x - center.x;
   int dy = id.y - center.y;

//   float r = sqrt(float(dx * dx) + float(dy * dy));

   float activator = scale_state[idx].activator;
   float inhibitor = scale_state[idx].inhibitor;
   for(int i = 1; i < scale_config->symmetry; i++) {
      float angle = ((float) i / (float) scale_config->symmetry) * 2.0f * PI;
      float s = sin(angle);
      float c = cos(angle);
      int x = (dx * c - dy * s) + center.x;
      int y = (dy * c + dx * s) + center.y;


      if(x > w) x = x - w;
      if(x < 0) x = w - (-1 * x);
      if(y > h) y = y - h;
      if(y < 0) y = h - (-1 * y);

      activator += scale_state[linear_index(x, y, w)].activator;
      inhibitor += scale_state[linear_index(x, y, w)].inhibitor;
   }

   ScaleCell cell;
   cell.activator = activator / scale_config->symmetry;
   cell.inhibitor = inhibitor / scale_config->symmetry;
   cell.variation = abs(cell.activator - cell.inhibitor);

   scale_state[idx] = cell;
}
     */

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        int w;
        int h;

        int inhibitorRadius;
        int activatorRadius;
        int symmetry = -1;
        float smallAmount;

        int colour;

        public Builder size(int w, int h) {
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder inhibitorRadius(int r) {
            this.inhibitorRadius = r;
            return this;
        }

        public Builder activatorRadius(int r) {
            this.activatorRadius = r;
            return this;
        }

        public Builder symmetry(int symmetry) {
            this.symmetry = symmetry;
            return this;
        }

        public Builder bumpAmount(float amount) {
            this.smallAmount = amount;
            return this;
        }

        public Builder colour(int colour) {
            this.colour = colour;
            return this;
        }

        public Scale build() {
            return new Scale(this);
        }
    }

}
