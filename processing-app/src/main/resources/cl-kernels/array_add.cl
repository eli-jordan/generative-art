
kernel void array_add_buffer(
	global float *a,
	global float *b,
	global float *c
) {
	int id = get_global_id(0);
	c[id] = a[id] + b[id];
}





























kernel void array_add_image(
	 read_only image2d_t a,
	 read_only image2d_t b,
	write_only image2d_t c
) {
	//printf("(%d, %d): \n", get_global_id(0), get_global_id(1));

	int2 coord = (int2)(get_global_id(0), get_global_id(1));
	float v0 = read_imagef(a, coord).x;
	float v1 = read_imagef(b, coord).x;

  //printf("(%d, %d): v0=%f, v1=%f \n", get_global_id(0), get_global_id(1), v0, v1);

	write_imagef(c, coord, (float4)(v0 + v1, 0.0f, 0.0f, 0.0f));
}
