
// OpenCL Kernel Function for element by element vector addition
kernel void VectorAdd(
          global const float* a,
          global const float* b,
          global float* c,
          int numElements) {

    // get index into global data array
    int gid = get_global_id(0);

    // bound check (equivalent to the limit on a 'for' loop for standard/serial C code
    if (gid >= numElements)  {
        return;
    }

    // add the vector elements
    c[gid] = a[gid] + b[gid];
}
