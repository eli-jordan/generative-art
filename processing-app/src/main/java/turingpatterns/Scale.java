package turingpatterns;
import java.util.concurrent.CountDownLatch;

class Scale {
    int w;
    int h;

    int inhibitorRadius;
    int activatorRadius;
    float smallAmount;

    float[][] inhibitor;
    float[][] activator;
    float[][] variation;

    Complex[][] kernelFFT;

    int colour;

    Scale(int w, int h, int activatorRadius, int inhibitorRadius, float smallAmount, int colour) {
        this.w = w;
        this.h = h;
        this.inhibitorRadius = inhibitorRadius;
        this.activatorRadius = activatorRadius;
        this.smallAmount = smallAmount;
        this.colour = colour;

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

        // Convolve the merged kernels
        Complex[][] convolution = Convolution.convolve2d_kernel(this.kernelFFT, g.getGridFFT());

        for (int x = 0; x < w; x ++) {
            for (int y = 0; y < h; y++) {
                // Extract the separable components from the convolution.
                float activatorAvg = convolution[y][x].re;
                float inhibitorAvg = convolution[y][x].im;
                activator[y][x] = activatorAvg;
                inhibitor[y][x] = inhibitorAvg;
                variation[y][x] = Math.abs(activatorAvg - inhibitorAvg);
            }
        }

        latch.countDown();
    }
}
