package wavelets;

import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.Wavelet;
import jwave.transforms.wavelets.coiflet.Coiflet1;
import jwave.transforms.wavelets.coiflet.Coiflet5;
import jwave.transforms.wavelets.daubechies.*;

import jwave.transforms.wavelets.haar.Haar1;

import java.util.Arrays;
import java.util.Random;
import java.util.function.DoubleFunction;

interface DoubleFunction1 {
   double apply(double a);
}

interface DoubleFunction2 {
   double apply(double a, double b);
}

public class Coefficients2d {

   private static Wavelet wavelet = new Daubechies2();
   static Transform transform = new Transform(new FastWaveletTransform(wavelet));

   int xdim, ydim;
   double[][] cA;
   double[][] cH;
   double[][] cV;
   double[][] cD;

   Coefficients2d(double[][] cA, double[][] cH, double[][] cV, double[][] cD, int xdim, int ydim) {
      this.xdim = xdim;
      this.ydim = ydim;
      this.cA = cA;
      this.cH = cH;
      this.cV = cV;
      this.cD = cD;
   }

   static Random random = new Random();

   void random(Coefficients2d b) {
      this.applyA(b, (x, y) -> random.nextGaussian() <= 0.5 ? x : y);
      this.applyH(b, (x, y) -> random.nextGaussian() <= 0.5 ? x : y);
      this.applyV(b, (x, y) -> random.nextGaussian() <= 0.5 ? x : y);
      this.applyD(b, (x, y) -> random.nextGaussian() <= 0.5 ? x : y);
   }

   void maxMax(Coefficients2d b) {
      this.applyA(b, Math::max);
      this.applyH(b, Math::max);
      this.applyV(b, Math::max);
      this.applyD(b, Math::max);
   }

   void maxMin(Coefficients2d b) {
      this.applyA(b, Math::max);
      this.applyH(b, Math::min);
      this.applyV(b, Math::min);
      this.applyD(b, Math::min);
   }

   void maxMean(Coefficients2d b) {
      this.applyA(b, Math::max);
      this.applyH(b, (x, y) -> (x + y) / 2.0);
      this.applyV(b, (x, y) -> (x + y) / 2.0);
      this.applyD(b, (x, y) -> (x + y) / 2.0);
   }

   void minMean(Coefficients2d b) {
      this.applyA(b, Math::min);
      this.applyH(b, (x, y) -> (x + y) / 2.0);
      this.applyV(b, (x, y) -> (x + y) / 2.0);
      this.applyD(b, (x, y) -> (x + y) / 2.0);
   }

   void minMax(Coefficients2d b) {
      this.applyA(b, Math::min);
      this.applyH(b, Math::max);
      this.applyV(b, Math::max);
      this.applyD(b, Math::max);
   }

   void meanMax(Coefficients2d b) {
      this.applyA(b, (x, y) -> (x + y) / 2.0);
      this.applyH(b, Math::max);
      this.applyV(b, Math::max);
      this.applyD(b, Math::max);
   }

   void meanMean(Coefficients2d b) {
      this.applyA(b, (x, y) -> (x + y) / 2.0);
      this.applyH(b, (x, y) -> (x + y) / 2.0);
      this.applyV(b, (x, y) -> (x + y) / 2.0);
      this.applyD(b, (x, y) -> (x + y) / 2.0);
   }

   private void apply(double[][] it, DoubleFunction1 fn) {
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            it[y][x] = fn.apply(it[y][x]);
         }
      }
   }

   private void apply(double[][] it, double[][] that, DoubleFunction2 fn) {
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            it[y][x] = fn.apply(it[y][x], that[y][x]);
         }
      }
   }

   public void multA(double d) {
      apply(cA, a -> a * d);
   }

   public void multH(double d) {
      apply(cH, a -> a * d);
   }

   public void multV(double d) {
      apply(cV, a -> a * d);
   }

   public void multD(double d) {
      apply(cD, a -> a * d);
   }

   public void add(Coefficients2d that) {
      apply(cA, that.cA, Double::sum);
      apply(cH, that.cH, Double::sum);
      apply(cV, that.cV, Double::sum);
      apply(cD, that.cD, Double::sum);
   }

   public void applyA(Coefficients2d that, DoubleFunction2 fn) {
      apply(cA, that.cA, fn);
   }

   public void applyH(Coefficients2d that, DoubleFunction2 fn) {
      apply(cH, that.cH, fn);
   }

   public void applyV(Coefficients2d that, DoubleFunction2 fn) {
      apply(cV, that.cV, fn);
   }

   public void applyD(Coefficients2d that, DoubleFunction2 fn) {
      apply(cD, that.cD, fn);
   }

   public double[][] inverse() {
      return transform.reverse(flatten(), 1, 1);
   }

   public double[][] flatten() {
      double[][] result = new double[ydim * 2][xdim * 2];

      inject(0, 0, xdim, ydim, cA, result);
      inject(xdim, 0, xdim, ydim, cH, result);
      inject(0, ydim, xdim, ydim, cV, result);
      inject(xdim, ydim, xdim, ydim, cD, result);

      return result;
   }

   private static void inject(int xoff, int yoff, int xdim, int ydim, double[][] inject, double[][] into) {
      for (int x = xoff; x < xdim + xoff; x++) {
         for (int y = yoff; y < ydim + yoff; y++) {
            into[y][x] = inject[y - yoff][x - xoff];
         }
      }
   }


   public static Coefficients2d from(double[][] data) {
      Transform transform = new Transform(new FastWaveletTransform(new Haar1()));
      double[][] hilbert = transform.forward(data, 1, 1);

      int xdim = data[0].length / 2;
      int ydim = data.length / 2;

      double[][] cA = extract(0, 0, xdim, ydim, hilbert);
      double[][] cH = extract(xdim, 0, xdim, ydim, hilbert);
      double[][] cV = extract(0, ydim, xdim, ydim, hilbert);
      double[][] cD = extract(xdim, ydim, xdim, ydim, hilbert);

      return new Coefficients2d(cA, cH, cV, cD, xdim, ydim);
   }


   private static double[][] extract(int xoff, int yoff, int xdim, int ydim, double[][] data) {
      double[][] result = new double[ydim][xdim];
      for (int x = xoff; x < xoff + xdim; x++) {
         for (int y = yoff; y < yoff + ydim; y++) {
            result[y - yoff][x - xoff] = data[y][x];
         }
      }
      return result;
   }

   @Override
   public String toString() {
      return "Coefficients2d{" +
          ", cA=" + Arrays.toString(cA) + "\n" +
          ", cH=" + Arrays.toString(cH) + "\n" +
          ", cV=" + Arrays.toString(cV) + "\n" +
          ", cD=" + Arrays.toString(cD) + "\n" +
          '}';
   }
}
