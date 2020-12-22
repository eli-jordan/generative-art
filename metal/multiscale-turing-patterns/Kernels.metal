#include <metal_stdlib>
using namespace metal;

struct ScaleCell {
   float activator;
   float inhibitor;
   float variation;
};

struct GridCell {
   float value;
   float4 colour;
};

struct ScaleConfig {
   int activator_radius;
   int inhibitor_radius;
   float small_amount;
   float4 colour;
};

// Clears the display
kernel void clear_pass(texture2d<float, access::write> tex [[ texture(0) ]],
                       uint2 id [[ thread_position_in_grid ]]) {
    tex.write(float4(0, 0, 0, 1), id);
}

int linear_index(int x, int y, int width) {
   return y*width + x;
}

// Lineraly interpolate a colour between `start` and `stop` by the
// provided amount.
float4 lerp_color(float4 start, float4 stop, float4 amount) {
   return start + (stop - start) * amount;
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
kernel void update_turing_scale(texture2d<float, access::read> tex [[ texture(0) ]],
                                device ScaleCell *scale_state [[ buffer(0) ]],
                                constant float *grid [[ buffer(1) ]],
                                constant ScaleConfig *scale_config [[ buffer(2) ]],
                                uint2 id [[ thread_position_in_grid ]]) {
   
   int w = tex.get_width();
   int h = tex.get_height();
   int activator_r = scale_config->activator_radius;
   int inhibitor_r = scale_config->inhibitor_radius;

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

kernel void render_grid(texture2d<float, access::read> tex_read [[ texture(0) ]],
                        texture2d<float, access::write> tex_write [[ texture(1) ]],
                        constant ScaleCell *scale_state1 [[ buffer(10) ]],
                        constant ScaleCell *scale_state2 [[ buffer(11) ]],
                        constant ScaleCell *scale_state3 [[ buffer(12) ]],
                        constant ScaleCell *scale_state4 [[ buffer(13) ]],
                        constant ScaleCell *scale_state5 [[ buffer(14) ]],
                        constant ScaleConfig *scale_configs [[ buffer(20) ]],
                        device float *grid [[ buffer(1) ]],
                        uint2 id [[ thread_position_in_grid ]]) {
   int idx = linear_index(id.x, id.y, tex_read.get_width());
   
   ScaleCell cells[] = {
      scale_state1[idx],
      scale_state2[idx],
      scale_state3[idx],
      scale_state4[idx],
      scale_state5[idx],
   };
   
   float maxInc = 0;
   
   
   ScaleCell best = cells[0];
   ScaleConfig config = scale_configs[0];
   for(int i = 1; i < 5; i++) {
      maxInc = max(maxInc, scale_configs[i].small_amount);
      if(cells[i].variation < best.variation) {
         best = cells[i];
         config = scale_configs[i];
      }
   }
   
   float v = 0;
   float amount = config.small_amount;
   if(best.activator > best.inhibitor) {
      v = grid[idx] + amount;
   } else {
      v = grid[idx] - amount;
   }
   grid[idx] = map(v, -1 - maxInc, 1 + maxInc, -1, 1);
   
   float4 current_colour = tex_read.read(id);
   float4 target_colour = config.colour;
   
   float4 new_colour = lerp_color(current_colour, target_colour, amount);
   
//   float value = map(grid[idx], -1, 1, 0.5, 1);
   float4 scaled_new_colour = new_colour; // * value; // + 0.5);
   scaled_new_colour.a = 1;
   
   tex_write.write(scaled_new_colour, id);
}
