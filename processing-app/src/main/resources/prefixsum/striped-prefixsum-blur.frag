#version 150

#ifdef GL_ES
precision highp float;
precision highp int;
#endif

const float PI = 3.14159265358979323846;

uniform sampler2D prefixSum;
uniform vec2 resolution;
uniform int width;
uniform int radius;

out float glFragColor;

/*
float circleAverage(constant float *grid, uint2 center, int r, int w, int h) {
   int count = 0;
   float total = 0;
   for(int x = -r; x < r; x++) {
      int yBound = floor(sqrt((float) r*r - x*x));
      for (int y = -yBound; y < yBound; y++) {

        int ix = x + center.x;
        int iy = y + center.y;

        if (ix >= 0 && ix < w && iy >= 0 && iy < h) {
          count++;
          total += grid[linear_index(ix, iy, w)];
        }
      }
   }

   return total / (float) count;
}
*/

void main() {
    float sum = 0.0;
    for (int y = -radius; y <= radius; y++) {
        float xBound = sqrt(radius*radius - y*y);
        vec2 left = vec2(clamp(gl_FragCoord.x - xBound, 0, width - 1), gl_FragCoord.y + y);
        vec2 right = vec2(clamp(gl_FragCoord.x + xBound, 0, width - 1), gl_FragCoord.y + y);
        sum += texture(prefixSum, right * resolution).x - texture(prefixSum, left * resolution).x;
    }
    glFragColor = sum / (PI*radius*radius);
}
