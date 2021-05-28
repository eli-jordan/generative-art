package opencl;

public class PrefixScanReference {
   public static void exclusiveScan(float[] input, float[] output) {
      int n = input.length;
      output[0] = 0;
      for (int i=1; i<n; i++) {
         output[i] = output[i-1] + input[i-1];
      }
   }

   public static void inclusiveScan(float[] input, float[] output) {
      float sum = input[0];
      output[0] = input[0];
      for (int j = 1; j < input.length; j++) {
         sum += input[j];
         output[j] = sum;
      }
   }
}
