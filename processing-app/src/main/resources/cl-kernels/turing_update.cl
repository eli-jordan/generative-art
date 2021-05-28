

inline float map(float value,
          float start1, float stop1,
          float start2, float stop2) {
   return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}


kernel void turing_update(
	read_only image2d_t activator1,
	read_only image2d_t activator2,
	read_only image2d_t inhibitor1,
	read_only image2d_t inhibitor2,
	float small_amount1,
	float small_amount2,
	read_only image2d_t grid_in,
	write_only image2d_t grid_out
) {
	int2 coord = (int2)(get_global_id(0), get_global_id(1));


	float activator1_value = read_imagef(activator1, coord).x;
	float activator2_value = read_imagef(activator2, coord).x;

	float inhibitor1_value = read_imagef(inhibitor1, coord).x;
	float inhibitor2_value = read_imagef(inhibitor2, coord).x;


	float variation1 = fabs(activator1_value - inhibitor1_value);
	float variation2 = fabs(activator2_value - inhibitor2_value);

	float multiplier1 = activator1_value > inhibitor1_value ? 1.0f : -1.0f;
	float multiplier2 = activator2_value > inhibitor2_value ? 1.0f : -1.0f;

	float min_variation = variation1;
	float small_amount = small_amount1;
	float multiplier = multiplier1;

	if(variation2 < min_variation) {
		min_variation = variation2;
		small_amount = small_amount2;
		multiplier = multiplier2;
	}
	
	const float maxInc = 0.05f;

	float new_value = read_imagef(grid_in, coord).x + small_amount * multiplier;
	new_value = map(new_value, -1 - maxInc, 1 + maxInc, -1, 1);

	write_imagef(grid_out, coord, (float4)(new_value, 0.0f, 0.0f, 0.0f));

}
