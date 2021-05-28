
kernel void multiply(
	read_only image2d_t in,
	write_only image2d_t out,
	float by
) {
	int2 coord = (int2)(get_global_id(0), get_global_id(1));

	float v = read_imagef(in, coord).x;
	write_imagef(out, coord, (float4)(v*by, 0.0f, 0.0f, 0.0f));
}


kernel void compute_sum(
	read_only image2d_t in0,
	read_only image2d_t in1,
	read_only image2d_t in2,
	write_only image2d_t out
) {
	int2 coord = (int2)(get_global_id(0), get_global_id(1));

	float v0 = read_imagef(in0, coord).x;
	float v1 = read_imagef(in1, coord).x;
	float v2 = read_imagef(in2, coord).x;

	float sum = v0 + v1 + v2;

	write_imagef(out, coord, (float4)(sum, 0.0f, 0.0f, 0.0f));
}
