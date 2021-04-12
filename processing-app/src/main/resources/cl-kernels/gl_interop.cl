
kernel void red(write_only image2d_t pixels) {

    int x = get_global_id(0);
    int y = get_global_id(1);
    
    // printf("x=%d y=%d\n", x, y);

    if(y < 5) {
        write_imagef(pixels, (int2)(x, y), (float4)(0.0, 0.0, 1.0, 1.0));
    } else {
        write_imagef(pixels, (int2)(x, y), (float4)(1.0, 0.0, 0.0, 1.0));
    }

}