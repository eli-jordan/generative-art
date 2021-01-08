package turingpatterns;

public class Convolution {
    /**
     * Creates a convolution kernel with weights representing an average in a circular region.
     * <p>
     * Note: The weights need to be centered at (0, 0) and wrap around to the opposite edges to avoid
     * edge effects when used with FFT to perform convolution.
     **/
    public static Complex[][] createKernel(int radius, int w, int h) {
        // Center the kernel at (0,0)
        int cx = 0;
        int cy = 0;
        int r = radius;

        Complex[][] kernel = new Complex[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                kernel[y][x] = new Complex(0, 0);
            }
        }

        float area = (float) Math.PI * r * r;
        for (int x = -r; x <= r; x++) {
            int yBound = (int) Math.floor(Math.sqrt(r * r - x * x));
            for (int y = -yBound; y <= yBound; y++) {

                // We wrap indices to avoid edge effects.
                int ix = wrapIndex(x + cx, w);
                int iy = wrapIndex(y + cy, h);

                Complex c = new Complex(1.0f / area, 0);
                kernel[iy][ix] = c;
            }
        }

        return kernel;
    }

    static int wrapIndex(int i, int size) {
        return (i % size + size) % size;
    }

    public static Complex[][] convolve2d(Complex[][] x, Complex[][] y) {
        // compute FFT of each sequence
        Complex[][] a = FFT.fft2d(x);
        Complex[][] b = FFT.fft2d(y);

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
        return FFT.ifft2d(c);
    }

    public static Complex[][] convolve2d_kernel(Complex[][] fftedKernel, Complex[][] inputFFT) {

        int h = fftedKernel.length;
        int w = fftedKernel[0].length;

        // point-wise multiply
        Complex[][] c = new Complex[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                c[y][x] = fftedKernel[y][x].mult(inputFFT[y][x]);
            }
        }

        // compute inverse FFT
        return FFT.ifft2d(c);
    }
}
