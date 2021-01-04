package turingpatterns;

import processing.core.PApplet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static processing.core.PApplet.*;

class Grid {

    private static ExecutorService exec = Executors.newWorkStealingPool();

    private final PApplet applet;

    Scale[] scales;

    float[][] grid;
    Complex[][] gridFFT;

    int[][] colors;

    float maxBump = 0;

    Grid(PApplet applet, Scale[] scales) {
        this.applet = applet;
        int w = applet.width;
        int h = applet.height;

        this.scales = scales;

        grid = new float[h][w];
        colors = new int[h][w];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                colors[y][x] = applet.color(0, 0, 0, 255);
            }
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                grid[y][x] = applet.random(-1, 1);
            }
        }

        for (Scale s : scales) {
            maxBump = max(maxBump, s.smallAmount);
        }
    }

    void update() {
        gridFFT = FFT.fft2d(FFT.wrapReals(grid));

        final CountDownLatch latch = new CountDownLatch(scales.length);
        for (final Scale scale : scales) {
            exec.execute(() -> scale.update(this, latch));
        }

        try {
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int x = 0; x < applet.width; x++) {
            for (int y = 0; y < applet.height; y++) {
                float minVariation = Float.MAX_VALUE;
                Scale bestScale = null;
                for (Scale s : scales) {
                    if (s.variation[y][x] < minVariation) {
                        minVariation = s.variation[y][x];
                        bestScale = s;
                    }
                }

                float colourBump = max(bestScale.smallAmount * 5, 0.001f);
                colors[y][x] = applet.lerpColor(colors[y][x], bestScale.colour, colourBump);

                if (bestScale.activator[y][x] > bestScale.inhibitor[y][x]) {
                    grid[y][x] += bestScale.smallAmount;
                } else {
                    grid[y][x] -= bestScale.smallAmount;
                }
            }
        }

        float max = 0;
        float min = Float.MAX_VALUE;
        for (int x = 0; x < applet.width; x++) {
            for (int y = 0; y < applet.height; y++) {
                max = Math.max(max, grid[y][x]);
                min = Math.min(min, grid[y][x]);
            }
        }

        for (int x = 0; x < applet.width; x++) {
            for (int y = 0; y < applet.height; y++) {
                grid[y][x] = map(grid[y][x], min, max, -1, 1);
            }
        }
    }
}
