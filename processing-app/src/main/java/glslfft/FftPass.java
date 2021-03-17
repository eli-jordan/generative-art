package glslfft;

/**
 * Holds all the data necessary to submit a render pass to the GPU
 */
public class FftPass<B> {
   // The input buffer holds the result of the previous pass
   B input;

   // The output buffer is being filled by this pass
   B output;

   float normalization;
   float subtransformSize;

   // 1/width
   float resolutionX;

   // 1/height
   float resolutionY;

   // Are we doing a horizontal pass?
   boolean horizontal;

   // Are we calculating a forward or inverse FFT?
   boolean forward;

   public String toString() {
      return "[" +
          "forward=" + forward +
          ", input=" + input +
          ", output=" + output +
          ", normalization=" + normalization +
          ", horizontal=" + horizontal +
          ", subtransformSize=" + subtransformSize +
          ", resolution=(" + resolutionX + ", " + resolutionY + ")" +
          "]";
   }
}
