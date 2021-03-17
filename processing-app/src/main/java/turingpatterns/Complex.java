package turingpatterns;

import java.util.Objects;

public class Complex {

   public float re;
   public float im;

   public Complex(float re, float im) {
      this.re = re;
      this.im = im;
   }

   Complex add(Complex that) {
      return new Complex(this.re + that.re, this.im + that.im);
   }

   Complex minus(Complex that) {
      return new Complex(this.re - that.re, this.im - that.im);
   }

   Complex mult(Complex that) {
      float real = this.re * that.re - this.im * that.im;
      float imaginary = this.re * that.im + this.im * that.re;
      return new Complex(real, imaginary);
   }

   float mod() {
      return (float) Math.sqrt(Math.pow(this.re, 2) + Math.pow(this.im, 2));
   }

   Complex div(Complex that) {
      Complex output = this.mult(that.conjugate());
      float div = (float) Math.pow(that.mod(), 2);
      return new Complex(output.re / div, output.im / div);
   }

   Complex conjugate() {
      return new Complex(this.re, -this.im);
   }

   private String render(float f) {
      if (f > -1.0e-6 && f < 1.0e-6) {
         return " + 0.0";
      } else if (f > 0) {
         return " + " + f;
      } else if (f < 0) {
         return " - " + f * -1;
      } else {
         return "" + f;
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Complex complex = (Complex) o;
      boolean reEq = Math.abs(complex.re - re) < 1;
      boolean imEq = Math.abs(complex.im - im) < 1;
      return reEq && imEq;
   }

   @Override
   public int hashCode() {
      return Objects.hash(re, im);
   }

   public String toString() {
      return render(re) + render(im) + "i";
   }
}
