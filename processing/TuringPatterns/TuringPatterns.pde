
import java.util.concurrent.*;


ExecutorService exec = Executors.newWorkStealingPool();

Scale[] working(int w, int h, int factor) {
  return new Scale[] {
    createScale(w, h, 180 * factor, 350 * factor, 0.03 / 2, color(255, 255, 1)), 
    createScale(w, h, 128 * factor, 250 * factor, 0.05 / 2, color(100, 250, 255)), 
    createScale(w, h, 128 * factor, 200 * factor, 0.02 / 2, color(175, 101, 250)), 

    createScale(w, h, 64 * factor, 128 * factor, 0.04 / 2, color(100, 20, 255)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(255)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(0)), 

    createScale(w, h, 10 * factor, 25 * factor, 0.03 / 2, color(20, 20, 255)), 
    createScale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(100, 250, 255))
  };
}

Scale[] green_palette(int w, int h, int factor) {
  return new Scale[] {
    createScale(w, h, 180 * factor, 350 * factor, 0.03 / 2, color(82, 156, 25)), 
    createScale(w, h, 128 * factor, 250 * factor, 0.05 / 2, color(186, 214, 129)), 
    createScale(w, h, 128 * factor, 200 * factor, 0.02 / 2, color(162, 203, 177)), 

    createScale(w, h, 64 * factor, 128 * factor, 0.04 / 2, color(73.141, 80)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(255)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(0)), 

    createScale(w, h, 10 * factor, 25 * factor, 0.03 / 2, color(22, 66, 34)), 
    createScale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(22, 66, 34))
  };
}

//Scale[] willows(int w, int h, int factor) {
//  return new Scale[] {
//    new Scale(w, h, 180 * factor, 350 * factor, 0.03 / 2, color(5, 255, 249)), 
//    new Scale(w, h, 128 * factor, 250 * factor, 0.05 / 2, color(1, 255, 87)), 
//    new Scale(w, h, 128 * factor, 200 * factor, 0.02 / 2, color(252, 170, 5)), 

//    new Scale(w, h, 64 * factor, 128 * factor, 0.04 / 2, color(155, 64, 64)), 
//    new Scale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(255)), 
//    new Scale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(0)), 

//    new Scale(w, h, 10 * factor, 25 * factor, 0.03 / 2, color(242, 185, 245)), 
//    new Scale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(242, 185, 245))
//  };
//}

Scale[] pastel_palette(int w, int h, int factor) {
  return new Scale[] {
    createScale(w, h, 180 * factor, 350 * factor, 0.03 / 2, color(245, 59, 70)), 
    createScale(w, h, 128 * factor, 250 * factor, 0.05 / 2, color(246, 117, 29)), 
    createScale(w, h, 128 * factor, 200 * factor, 0.02 / 2, color(243, 206, 25)), 

    createScale(w, h, 64 * factor, 128 * factor, 0.04 / 2, color(22, 166, 174)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(255)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(0)), 

    createScale(w, h, 10 * factor, 25 * factor, 0.03 / 2, color(80, 151, 72)), 
    //new Scale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(242, 185, 245))
  };
}

Scale[] pastel_reverse(int w, int h, int factor) {
  return new Scale[] {
    createScale(w, h, 180 * factor, 350 * factor, 0.03 / 2, color(80, 151, 72)), 
    createScale(w, h, 128 * factor, 250 * factor, 0.05 / 2, color(22, 166, 174)), 
    createScale(w, h, 128 * factor, 200 * factor, 0.02 / 2, color(246, 117, 29)), 

    createScale(w, h, 64 * factor, 128 * factor, 0.04 / 2, color(243, 206, 25)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(255)), 
    createScale(w, h, 50 * factor, 100 * factor, 0.03 / 2, color(0)), 

    createScale(w, h, 10 * factor, 25 * factor, 0.03 / 2, color(245, 59, 70)), 
    //new Scale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(242, 185, 245))
  };
}

Scale[] dots_and_stripes() {
  return new Scale[] {
    createScale(width, height, 16, 32, 0.05, color(255)), 
    createScale(width, height, 8, 16, -0.05, color(255))
  };
}

class Grid {

  int w;
  int h;

  Scale[] scales;

  float[][] grid;
  Complex[][] gridFFT;

  color[][] colors;

  Grid(int w, int h) {
    this.w = w;
    this.h = h;

    this.scales = new Scale[] {
      //createScale(w, h, 60, 60 + 4*30,  0.05, color(255)),
      //createScale(w, h, 30, 30 + 2*30,  -0.05, color(255)),

      createScale(w, h, 36, 36*2, 0.05, color(255)), 
      createScale(w, h, 6, 6*2, -0.05, color(255)), 

      createScale(w, h, 120, 120*2, -0.05, color(255)), 
      createScale(w, h, 60, 60*2, -0.05, color(255)), 
      

      //createScale(w, h, 52, 88,  0.05, color(255)),
      //createScale(w, h, 12, 24,  -0.04, color(255)),

      //createScale(w, h, 32, 64,  -0.05, color(255)),
      //createScale(w, h, 4, 8,   0.05, color(255)), 
      //createScale(w, h, 10, 16,  -0.08, color(255)), 
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
  }

  void update() {
    gridFFT = fft2d(wrapReals(grid));

    final CountDownLatch latch = new CountDownLatch(scales.length);
    for (final Scale scale : scales) {
      exec.execute(new Runnable() {
        public void run() {
          scale.update(Grid.this, latch);
        }
      }
      );
    }

    try { 
      latch.await();
    } 
    catch(Exception e) { 
      e.printStackTrace();
    }

    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {

        float value = 0;
        for (Scale scale : scales) {
          if (scale.activator[y][x] > scale.inhibitor[y][x]) {
            value += scale.smallAmount;
          } else {
            value -= scale.smallAmount;
          }
        }
        value /= scales.length;
        grid[y][x] += value;

        //float minVariation = Float.MAX_VALUE;
        //Scale bestScale = null;
        //for (Scale s : scales) {
        //  if (s.variation[y][x] <= minVariation) {
        //    minVariation = s.variation[y][x];
        //    bestScale = s;
        //  }
        //}



        //float colourBump = max(bestScale.smallAmount * 5 , 0.001);
        //float valueBump = max(bestScale.smallAmount , 0.001);

        //colors[y][x] = lerpColor(colors[y][x], bestScale.c, colourBump);

        //if (bestScale.activator[y][x] > bestScale.inhibitor[y][x]) {
        //  grid[y][x] += valueBump;
        //} else {
        //  grid[y][x] -= valueBump;
        //}
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
      }
    }
  }
}

Grid g;

void setup() {
  size(512, 512);

  g = new Grid(width, height);
}

void draw() {
  loadPixels();

  for (int x = 0; x < width; x++) {
    for (int y = 0; y < height; y++) {
      //color c = g.colors[y][x];
      //int index = x + y * width;

      //RGB rgb = createRGB(c);
      //HSV hsv = rgb.toHSV();
      //hsv.v = map(g.grid[y][x], -1, 1, 0, 1);

      //RGB newColour = hsv.toRGB();

      //float contrast = 1.3;
      //newColour.r = (newColour.r - 0.5) * contrast + 0.5;
      //newColour.g = (newColour.g - 0.5) * contrast + 0.5;
      //newColour.b = (newColour.b - 0.5) * contrast + 0.5;

      //float gamma = 1.1;
      //newColour.r = pow(newColour.r, gamma);
      //newColour.g = pow(newColour.g, gamma);
      //newColour.b = pow(newColour.b, gamma);

      //pixels[index] = newColour.toColor();

      int index = x + y * width;
      pixels[index] = color(map(g.grid[y][x], -1, 1, 0, 255));
    }
  }

  g.update();
  updatePixels();
  println("Frame Rate: " + frameRate);
  println("Frame Count: " + frameCount);
  //if (frameCount > 100) {
  saveFrame();
  //}
}
