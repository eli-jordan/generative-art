package curlnoise;

import processing.core.PVector;
import static processing.core.PApplet.*;

/**
 * Implements advection of a particle in a 2d velocity field.
 * Based on the implementation here: https://www.openprocessing.org/sketch/779232
 */
public class Advection {

   /**
    * Bilinear interpolation.
    *
    * @param field the vector field that will be interpolated over
    * @param x the position to interpolate on the x-axis, in source coordinates (not field coordinates)
    * @param y the position to interpolate on the y-axis
    * @param resolution the number of positions available between each in the vector field.
    *                   e.g. if resolution = 10, there are 10 positions between field[0][0] and field[0][1]
    * @return the interpolated velocity at the specified point.
    */
   static PVector bilerp(PVector[][] field, float x, float y, float resolution) {

      int ix = constrain((int) Math.floor(x/resolution), 0, field[0].length - 2);
      int iy = constrain((int) Math.floor(y/resolution), 0, field.length - 2);

      PVector s0 = field[iy][ix];
      PVector s1 = field[iy][ix+1];
      PVector s2 = field[iy+1][ix];
      PVector s3 = field[iy+1][ix+1];

      float tx = x/resolution - ix;
      float ty = y/resolution - iy;

      PVector sx0 = new PVector(
          s0.x + (s1.x - s0.x) * tx,
          s0.y + (s1.y - s0.y) * ty
      );

      PVector sx1 = new PVector(
          s2.x + (s3.x - s2.x) * tx,
          s2.y + (s3.y - s2.y) * ty
      );

      PVector result = new PVector(
          sx0.x + (sx1.x - sx0.x) * tx,
          sx0.y + (sx1.y - sx0.y) * ty
      );

      return result;
   }

   /**
    * Advect an agent using 3rd order Runge-Kutta.
    */
   static void advect(Agent p, VectorField field, float resolution) {
      float h = 0.2f;

      float steps = 1 / h;

      PVector k1, k2, k3;
      float tx, ty;
      for (int i = 0; i < steps; i++) {
         k1 = bilerp(field.field, p.pos.x, p.pos.y, resolution);

         tx = p.pos.x + k1.x * 0.5f * h;
         ty = p.pos.y + k1.y * 0.5f * h;

         k2 = bilerp(field.field, tx, ty, resolution);
         tx = tx + k2.x * 2.0f * h;
         ty = ty + k2.y * 2.0f * h;

         k3 = bilerp(field.field, tx, ty, resolution);
         p.pos.x += k3.x * h;
         p.pos.y += k3.y * h;
      }
   }
}
