import MetalKit

class MultiscaleView: MTKView {
   var commandQueue: MTLCommandQueue!
   
   // Pipeline state for the `clear_pass` compute kernel
   var clearPass: MTLComputePipelineState!
   
   // Pipeline state for the `update_turing_scale` compute kernel
   var updateTuringScalePass: MTLComputePipelineState!
   
   // Pipeline state for the `render_grid` compute kernel
   var renderGridPass: MTLComputePipelineState!
   
   var gridBuffer: MTLBuffer!
   var scaleStateBuffers: [MTLBuffer] = []
   var scaleConfigBuffers: [MTLBuffer] = []
   
   var textureForFrameCapture: MTLTexture!
   
   var scaleConfigs = [
      SIMD2<Int32>(100, 200),
      SIMD2<Int32>(20, 40),
      SIMD2<Int32>(10, 20),
      SIMD2<Int32>(5, 10),
      SIMD2<Int32>(1, 2)
   ]
   
   var textureWidth = 0
   var textureHeight = 0
   
   required init(coder: NSCoder) {
      super.init(coder: coder)
      
      // Setting this to false allows us to draw directly to the view's drawable within
      // our compute kernel.
      self.framebufferOnly = false
      
      // Lookup the default GPU device. Enable the discreet GPU if it is not already enabled.
      self.device = MTLCreateSystemDefaultDevice()
      
      // Initialise the queue that is used to send commands to the GPU device.
      self.commandQueue = device?.makeCommandQueue()
      
      let drawable = currentDrawable!
      self.textureWidth = drawable.texture.width * 2
      self.textureHeight = drawable.texture.height * 2
      
      let descriptor = MTLTextureDescriptor.texture2DDescriptor(
         pixelFormat: .bgra8Unorm,
         width: textureWidth,
         height: textureHeight,
         mipmapped: false
      )
      descriptor.resourceOptions = [.storageModeManaged]
      descriptor.usage = [.shaderRead, .shaderWrite]
      self.textureForFrameCapture = device?.makeTexture(descriptor: descriptor)
      
      lookupKernels()
      allocateBuffers()
      
      print("Texture: width =", textureWidth, " height =", textureHeight)
   }
   
   func lookupKernels() {
      // Lookup our compute kernel functions
      let library = device?.makeDefaultLibrary()
      let clearKernel = library?.makeFunction(name: "clear_pass")
      let updateTuringScaleKernel = library?.makeFunction(name: "update_turing_scale")
      let renderGridKernel = library?.makeFunction(name: "render_grid")
   
      do {
         clearPass = try device?.makeComputePipelineState(function: clearKernel!)
         updateTuringScalePass = try device?.makeComputePipelineState(function: updateTuringScaleKernel!)
         renderGridPass = try device?.makeComputePipelineState(function: renderGridKernel!)
      } catch let error as NSError {
         print(error)
      }
   }
   
   func allocateBuffers() {
      let pixels = textureWidth * textureHeight
      print("Total Pixels: ", pixels)
      
      // Allocate a state buffer for each cell
      for i in 0..<scaleConfigs.count {
         let stateBuffer = device?.makeBuffer(
            length: MemoryLayout<ScaleCell>.stride * pixels,
            options: .storageModeManaged
         )
         scaleStateBuffers.append(stateBuffer!)
         
         let configBuffer = device?.makeBuffer(
            bytes: &scaleConfigs[i],
            length: MemoryLayout<SIMD2<Int32>>.stride,
            options: .storageModeManaged
         )
         scaleConfigBuffers.append(configBuffer!)
      }
      
      // Generate the initial values for the grid using random numbers between -1 and 1
      var initial: [Float] = []
      for _ in 0..<pixels {
         let value = Float.random(in: -1..<1)
         initial.append(value)
      }
      
      gridBuffer = device?.makeBuffer(
         bytes: initial,
         length: MemoryLayout<Float>.stride * pixels,
         options: .storageModeManaged
      )
   }
   
   var frameCount = 0
   override func draw(_ dirtyRect: NSRect) {
      let commandBuffer = commandQueue.makeCommandBuffer()
      let encoder = commandBuffer?.makeComputeCommandEncoder()
      
      let drawable = currentDrawable!
      encoder!.setTexture(drawable.texture, index: 0)
      encoder!.setBuffer(gridBuffer, offset: 0, index: 1)

      for i in 0..<scaleConfigs.count {
         encoder!.setBuffer(scaleStateBuffers[i], offset: 0, index: 0)
         encoder!.setBuffer(scaleConfigBuffers[i], offset: 0, index: 2)
         enqueuePass(encoder: encoder!, pass: updateTuringScalePass)
      }
      
      encoder!.setBuffer(scaleStateBuffers[0], offset: 0, index: 10)
      encoder!.setBuffer(scaleStateBuffers[1], offset: 0, index: 11)
      encoder!.setBuffer(scaleStateBuffers[2], offset: 0, index: 12)
      encoder!.setBuffer(scaleStateBuffers[3], offset: 0, index: 13)
      encoder!.setBuffer(scaleStateBuffers[4], offset: 0, index: 14)
      enqueuePass(encoder: encoder!, pass: renderGridPass)
      
      encoder!.endEncoding()
      commandBuffer?.present(drawable)
      commandBuffer?.commit()
      commandBuffer?.waitUntilCompleted()
      
      // "/Users/elias.jordan/creative-code/turing-patterns/metal/frame-" +
      let filePath = "frame-" + String(format: "%04d", frameCount) + ".png"
      let url = URL.init(fileURLWithPath: filePath)
      print(url)
      let tex = currentDrawable!.texture
      writeTexture(tex, url: url)
      
      frameCount += 1
      print("Completed Frames: ", frameCount)
   }
   
   private func enqueuePass(encoder: MTLComputeCommandEncoder, pass: MTLComputePipelineState) {
      // Tell the encoder what kernel to run
      encoder.setComputePipelineState(pass)
      
      let tgWidth = pass.threadExecutionWidth
      let tgHeight = pass.maxTotalThreadsPerThreadgroup / tgWidth
      
      // Configure the size of each thread group. This controls how
      // many pixels are processed by the GPU at one time.
      let threadsPerThreadGroup = MTLSize(
         width: tgWidth,
         height: tgHeight,
         depth: 1
      )
      
      // Configure the total number of threads that will need to be run
      // to complete this unit of work. Since there is one thread per pixel
      // we use the drawables texture dimensions.
      let threadsPerGrid = MTLSize(
         width: textureWidth,
         height: textureHeight,
         depth: 1
      )
      
      // Enqueues the compute function along with its parameters
      encoder.dispatchThreads(threadsPerGrid, threadsPerThreadgroup: threadsPerThreadGroup)
   }
}
