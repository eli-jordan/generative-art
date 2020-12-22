
class Scale {
  int w;
  int h;

  int inhibitorRadius;
  int activatorRadius;
  float smallAmount;

  float[][] inhibitor;
  float[][] activator;
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
    
  }

  void update(Grid g) {
    for (int x = 0; x < w; x ++) {
      for (int y = 0; y < h; y++) {
        float activatorAvg = g.average(x, y, activatorRadius);
        float inhibitorAvg = g.average(x, y, inhibitorRadius);
        activator[y][x] = activatorAvg;
        inhibitor[y][x] = inhibitorAvg;
        variation[y][x] = abs(activatorAvg - inhibitorAvg);
      }
    }
  }
}

class Grid {

  int w;
  int h;

  Scale[] scales;

  float[][] grid;
  color[][] colors;

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
  }

  void update() {
    
    for(Scale scale : scales) {
      scale.update(this);
    }
    
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        float minVariation = Float.MAX_VALUE;
        Scale bestScale = null;
        for(Scale s : scales) {
          if(s.variation[y][x] < minVariation) {
            minVariation = s.variation[y][x];
            bestScale = s;
          }
        }
        
        colors[y][x] = lerpColor(colors[y][x], bestScale.c, 0.1);
        
        if(bestScale.activator[y][x] > bestScale.inhibitor[y][x]) {
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
      }
    }
  }

  float average(int cx, int cy, int r) {
    float total = 0;
    int count = 0;
    for (int x = -r; x < r; x++) {
      int yBound = floor(sqrt(r*r - x*x));
      for (int y = -yBound; y < yBound; y++) {

        int ix = x + cx;
        int iy = y + cy;

        if (ix >= 0 && ix < w && iy >= 0 && iy < h) {
          count++;
          total += grid[iy][ix];
        }
      }
    }
    return total / count;
  }
}

Grid g;

void setup() {
  size(800, 800);
  displayDensity(1);

  g = new Grid(width, height);
}

void draw() {
  loadPixels();

  for (int x = 0; x < width; x++) {
    for (int y = 0; y < height; y++) {
      color c = g.colors[y][x];
      int index = x + y * width;
      
      float value = map(g.grid[y][x], -1, 1, 0, 1);
      float r = red(c) * value;
      float g = green(c) * value;
      float b = blue(c) * value;
      
      pixels[index] = color(r, g, b);
    }
  }

  g.update();
  updatePixels();
  saveFrame();
}
