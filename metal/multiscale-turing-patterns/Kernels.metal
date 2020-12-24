#include <metal_stdlib>
using namespace metal;

constant float PI = 180; //3.14159265358979323846264338327950288419716939937510;

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
   int symmetry;
   float4 colour;
};

struct Colour_HSV {
   float h;
   float s;
   float v;
};

Colour_HSV rgb_to_hsv(float3 colour)
{
   Colour_HSV result;
   
   float minV = min(colour.r, min(colour.g, colour.b));
   float maxV = max(colour.r, min(colour.g, colour.b));
   result.v = maxV;
   float delta = maxV - minV;
   
   if(maxV != 0) {
      result.s = delta / maxV;
   } else {
      result.s = 0;
      result.h = -1;
      result.h = -1;
      return result;
   }
   
   if(colour.r == maxV) {
      result.h = (colour.g - colour.b) / delta;
   } else if(colour.g == maxV) {
      result.h = 2 + (colour.b - colour.r) / delta;
   } else {
      result.h = 4 + (colour.r - colour.g ) / delta;
   }
      
   result.h *= 60;
   if(result.h < 0) {
      result.h += 360;
   }
   
   return result;
}

float3 hsv_to_rgb(Colour_HSV colour) {
   
   if(colour.s == 0) {
      return float3(colour.v, colour.v, colour.v);
   }
   
   float h = colour.h / 60;
   int i = floor(h);
   float f = h - i;
   float p = colour.v * (1 - colour.s);
   float q = colour.v * (1 - colour.s * f);
   float t = colour.v * ( 1 - colour.s * (1 - f));
   switch(i) {
      case 0: return float3(colour.v, t, p);
      case 1: return float3(q, colour.v, p);
      case 2: return float3(p, colour.v, t);
      case 3: return float3(p, q, colour.v);
      case 4: return float3(t, p, colour.v);
      // case 5:
      default: return float3(colour.v, p, q);
   }
}

// Clears the display
kernel void clear_pass(texture2d<float, access::write> tex [[ texture(0) ]],
                       uint2 id [[ thread_position_in_grid ]]) {
    tex.write(float4(0, 0, 0, 1), id);
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

   float4 new_colour = mix(current_colour, target_colour, amount);

   Colour_HSV c = rgb_to_hsv(new_colour.rgb);
   float value = map(grid[idx], -1, 1, 0, 1);
   c.v = value;
   new_colour.rgb = hsv_to_rgb(c);

   // Gamma filter
   new_colour.rgb = pow(new_colour.rgb, float3(1.1));
   new_colour.a = 1;
   
   tex_write.write(new_colour, id);
   
//   float value = map(grid[idx], -1, 1, 0, 1);
//   tex_write.write(float4(value, value, value, 1), id);
}
