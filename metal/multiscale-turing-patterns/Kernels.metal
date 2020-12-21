#include <metal_stdlib>
using namespace metal;

struct ScaleCell {
   float activator;
   float inhibitor;
   float variation;
};

// Clears the display
kernel void clear_pass(texture2d<half, access::write> tex [[ texture(0) ]],
                       uint2 id [[ thread_position_in_grid ]]) {
    tex.write(half4(1, 0, 0, 1), id);
}

int linear_index(int x, int y, int width) {
   return y*width + x;
}

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

float boxAverage(constant float *grid, uint2 center, int r, int w, int h) {
   int count = 4 * r * r;
   float total = 0;
   for(int x = -r; x < r; x++) {
      for (int y = -r; y < r; y++) {

        int ix = x + center.x;
        int iy = y + center.y;

        if (ix >= 0 && ix < w && iy >= 0 && iy < h) {
          total += grid[linear_index(ix, iy, w)];
        }
      }
   }
   
   return total / (float) count;
}

// `scale_state` holds the current state for one turing-scale for all cells
// `grid` holds the current simulation satte for all cells
// `scale_config` holds the parameters that are constant for the scale being simulated.
kernel void update_turing_scale(texture2d<half, access::read> tex [[ texture(0) ]],
                                device ScaleCell *scale_state [[ buffer(0) ]],
                                constant float *grid [[ buffer(1) ]],
                                constant int2 *scale_config [[ buffer(2) ]],
                                uint2 id [[ thread_position_in_grid ]]) {
   
   int w = tex.get_width();
   int h = tex.get_height();
   int activator_r = scale_config->x;
   int inhibitor_r = scale_config->y;

   float activator_avg = circleAverage(grid, id, activator_r, w, h);
   float inhibitor_avg = circleAverage(grid, id, inhibitor_r, w, h);

   ScaleCell cell;
   cell.activator = activator_avg;
   cell.inhibitor = inhibitor_avg;
   cell.variation = abs(activator_avg - inhibitor_avg);
   
   int idx = linear_index(id.x, id.y, w);
   scale_state[idx] = cell;
}


float map(float value,
          float start1, float stop1,
          float start2, float stop2) {
   return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

kernel void render_grid(texture2d<half, access::write> tex [[ texture(0) ]],
                        constant ScaleCell *scale_state1 [[ buffer(10) ]],
                        constant ScaleCell *scale_state2 [[ buffer(11) ]],
                        constant ScaleCell *scale_state3 [[ buffer(12) ]],
                        constant ScaleCell *scale_state4 [[ buffer(13) ]],
                        constant ScaleCell *scale_state5 [[ buffer(14) ]],
                        device float *grid [[ buffer(1) ]],
                        uint2 id [[ thread_position_in_grid ]]) {
   int idx = linear_index(id.x, id.y, tex.get_width());
   
   ScaleCell cells[] = {
      scale_state1[idx],
      scale_state2[idx],
      scale_state3[idx],
      scale_state4[idx],
      scale_state5[idx],
   };
   
   float inc[] = {
      0.06,
      0.05,
      0.04,
      0.03,
      0.02,
   };
   
   float maxInc = 0.05;
   
   ScaleCell best = cells[0];
   float amount = inc[0];
   for(int i = 1; i < 5; i++) {
      if(cells[i].variation < best.variation) {
         best = cells[i];
         amount = inc[i];
      }
   }
   
   float v = 0;
   if(best.activator > best.inhibitor) {
      v = grid[idx] + amount;
   } else {
      v = grid[idx] - amount;
   }
   grid[idx] = map(v, -1 - maxInc, 1 + maxInc, -1, 1);

   float value = map(grid[idx], -1, 1, 0, 1);
   tex.write(half4(value, value, value, 1), id);
}
