package warp;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.Arrays;
import java.util.Random;

import static processing.core.PApplet.map;

public class SlowWorleyNoise {

   private final int width, height, depth;
   private final float[][][] values;

   SlowWorleyNoise(int width, int height, int depth, int numPoints, int n) {
      this.width = width;
      this.height = height;
      this.depth = depth;

      this.values = new float[height][width][depth];
      PVector[] points = new PVector[numPoints];

      Random random = new Random();

      for (int i = 0; i < numPoints; i++) {
         int x = (int) (random.nextFloat() * width);
         int y = (int) (random.nextFloat() * height);
         int z = (int) (random.nextFloat() * depth);
         points[i] = new PVector(x, y, z);
      }

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
               values[y][x][z] = distances(points, x, y, z)[n];
            }
         }
      }

      normalize();
   }

   float get(int x, int y) {
      return get(x, y, 0);
   }

   float get(int x, int y, int z) {
      int ix = wrapIndex(x, width);
      int iy = wrapIndex(y, height);
      int iz = wrapIndex(z, depth);
      return values[iy][ix][iz];
   }

   static int wrapIndex(int i, int size) {
      return (i % size + size) % size;
   }

   private void normalize() {
      float max = 0;
      float min = Float.MAX_VALUE;
      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
               max = Math.max(max, values[y][x][z]);
               min = Math.min(min, values[y][x][z]);
            }
         }
      }

      for (int x = 0; x < width; x++) {
         for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
               values[y][x][z] = map(values[y][x][z], min, max, 1, 0);
            }
         }
      }
   }

   private static float[] distances(PVector[] points, int x, int y, int z) {
      float[] distances = new float[points.length];
      for (int i = 0; i < points.length; i++) {
         float dist = PApplet.dist(points[i].x, points[i].y, points[i].z, x, y, z);
         distances[i] = dist;
      }
      Arrays.sort(distances);
      return distances;
   }
}
