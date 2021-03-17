package glslfft;

public class FftPass<B> {
   B input;
   B output;

   float normalization;
   float subtransformSize;

   boolean horizontal;
   boolean forward;

   public String toString() {
      return "[" +
          "forward=" + forward +
          ", input=" + input +
          ", output=" + output +
          ", normalization=" + normalization +
          ", horizontal=" + horizontal +
          ", subtransformSize=" + subtransformSize +
          "]";
   }
}
