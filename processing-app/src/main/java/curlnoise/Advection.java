package curlnoise;

import processing.core.PVector;
import static processing.core.PApplet.*;

public class Advection {
/*
function sample_2d(arr, x, y, res_x, res_y) {
	x = min(max(x, 0), res_x-2);
	y = min(max(y, 0), res_y-2);
	var ix = floor(x);
	var iy = floor(y);
	var s0 = arr[IX(ix,   iy  )];
	var s1 = arr[IX(ix+1, iy  )];
	var s2 = arr[IX(ix,   iy+1)];
	var s3 = arr[IX(ix+1, iy+1)];
	var tx = x - ix;
	var ty = y - iy;
	var sx0 = {"x": s0.x + (s1.x - s0.x) * tx, "y": s0.y + (s1.y - s0.y) * tx};
	var sx1 = {"x": s2.x + (s3.x - s2.x) * tx, "y": s2.y + (s3.y - s2.y) * tx};
	return {"x": sx0.x + (sx1.x - sx0.x) * ty, "y": sx0.y + (sx1.y - sx0.y) * ty};
}
 */

   static PVector sample2d(PVector[][] field, float x, float y, float resolution) {

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

   static void advect(Agent p, PVector[][] field, float resolution) {
      float h = 0.2f;

      float steps = 1 / h;
      float ox = p.pos.x;
      float oy = p.pos.y;

      // RK3
      PVector k1, k2, k3;
      float tx, ty;
      for (int i = 0; i < steps; i++) {
         k1 = sample2d(field, p.pos.x, p.pos.y, resolution);

         tx = p.pos.x + k1.x * 0.5f * h;
         ty = p.pos.y + k1.y * 0.5f * h;

         k2 = sample2d(field, tx, ty, resolution);
         tx = tx + k2.x * 2.0f * h;
         ty = ty + k2.y * 2.0f * h;

         k3 = sample2d(field, tx, ty, resolution);
         p.pos.x += k3.x * h;
         p.pos.y += k3.y * h;
      }

//      System.out.println("dx=" + (ox -p.pos.x) + ", dy=" + (oy - p.pos.y));

//      p.len = sqrt(pow(p.x-ox,2) + pow(p.y-oy,2));

//      if (p.pos.x < 0) p.pos.x = 0;
//      if (p.pos.x > width-2) p.x = width-2;
//      if (p.y < 1) p.y = 1;
//      if (p.y > height-2) p.y = height-2;
   }

   /*
   function advect_particle(p, vel_arr, res_x, res_y) {
	var h = 0.2;
	var steps = 1 / h;
	var ox = p.x;
	var oy = p.y;
	// RK3
	var k1, k2, k3, tx, ty;
	for (var s = 0; s < steps; ++s) {
		k1 = sample_2d(vel_arr, p.x, p.y, res_x, res_y);
		tx = p.x + k1.x * 0.5 * h;
		ty = p.y + k1.y * 0.5 * h;
		k2 = sample_2d(vel_arr, tx, ty, res_x, res_y);
		tx = tx + k2.x * 2.0 * h;
		ty = ty + k2.y * 2.0 * h;
		k3 = sample_2d(vel_arr, tx, ty, res_x, res_y);
		p.x += k3.x * h;
		p.y += k3.y * h;
	}

	p.len = sqrt(pow(p.x-ox,2) + pow(p.y-oy,2));

	if (p.x < 1) p.x = 1;
	if (p.x > width-2) p.x = width-2;
	if (p.y < 1) p.y = 1;
	if (p.y > height-2) p.y = height-2;
}
    */
}
