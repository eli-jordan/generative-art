package curlnoise;

import processing.core.PApplet;
import processing.core.PVector;

public class VectorField {
   interface VectorProvider {
      PVector provide(int x, int y);
   }

   private final int xdim;
   private final int ydim;
   PVector[][] field;

   private final VectorProvider provider;

   public VectorField(int xdim, int ydim, VectorProvider provider) {
      this.xdim = xdim;
      this.ydim = ydim;
      this.provider = provider;
      initField();
   }

   private void initField() {
      field = new PVector[ydim][xdim];

      float maxMag = Float.MIN_VALUE;
      float minMag = Float.MAX_VALUE;
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = this.provider.provide(x, y);
            maxMag = Math.max(maxMag, vec.mag());
            minMag = Math.min(minMag, vec.mag());
            field[y][x] = vec;
         }
      }

      // Normalise the magnitude of the vectors in the range 0-1
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = field[y][x];
            float mag = PApplet.map(vec.mag(), minMag, maxMag, 0, 1);
            vec.setMag(mag);
         }
      }
   }

   public void draw(PApplet into, int resolution) {
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = field[y][x];
            into.pushMatrix();
            into.stroke(255, 0, 0);
            into.strokeWeight(1);
            into.translate(x * resolution, y * resolution);
            into.line(0, 0, vec.x, vec.y);
            into.popMatrix();
         }
      }
   }


   public void recalculate() {
      initField();
   }

   public void mult(float f) {
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = field[y][x];
            vec.mult(1);
         }
      }
   }
}
