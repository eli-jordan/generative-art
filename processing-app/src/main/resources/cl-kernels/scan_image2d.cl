
/*
 * This kernel is used to compute the prefix-sum of each row in the input image.
 * 
 * It implements one step in the Stone-Kogge adder algorithm, and relies on the driver
 * program to execute the kernel log2(in.width) times with the appropriate stride.
 * 
 * See opencl/ScanImage2d.java for the driver logic.
 */
kernel void scan_image2d(
  read_only image2d_t in,
  write_only image2d_t out,
  int stride
) {

  int gx = get_global_id(0);
  int gy = get_global_id(1);
  
  int2 p0 = (int2)(gx, gy);
  int2 p1 = (int2)(gx - stride, gy);

  float v0 = read_imagef(in, p0).x;
  float v1 = read_imagef(in, p1).x;

  write_imagef(out, p0, (float4)(v0 + v1, 0.0, 0.0, 0.0));

}
