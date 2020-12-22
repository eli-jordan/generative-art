
import MetalKit

/**
 This file defines the structs that are used to pass data to the compute kernel(s)
 These MUST reflect EXACTLY the same memory layout as the structs declared in the kernel itself.
 */

struct ScaleCell {
   var activator: Float
   var inhibitor: Float
   var variation: Float
}


struct ScaleConfig {
   var activator_radius: Int32
   var inhibitor_radius: Int32
   var small_amount: Float
   var colour: SIMD4<Float>
}

struct GridCell {
   var value: Float
   var colour: SIMD4<Float>
}
