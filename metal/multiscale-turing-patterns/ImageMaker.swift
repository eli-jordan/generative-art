
import Accelerate
import Foundation
import MetalKit

func writeTexture(_ texture: MTLTexture, url: URL) {
    guard let image = makeImage(for: texture) else { return }

    if let imageDestination = CGImageDestinationCreateWithURL(url as CFURL, kUTTypePNG, 1, nil) {
        CGImageDestinationAddImage(imageDestination, image, nil)
        CGImageDestinationFinalize(imageDestination)
    }
}

func makeImage(for texture: MTLTexture) -> CGImage? {
   assert(texture.pixelFormat == .bgra8Unorm)

   let width = texture.width
   let height = texture.height
   let pixelByteCount = 4 * MemoryLayout<UInt8>.size
   let imageBytesPerRow = width * pixelByteCount
   let imageByteCount = imageBytesPerRow * height
   let imageBytes = UnsafeMutableRawPointer.allocate(byteCount: imageByteCount, alignment: pixelByteCount)
   defer {
      imageBytes.deallocate()
   }

   texture.getBytes(imageBytes,
                    bytesPerRow: imageBytesPerRow,
                    from: MTLRegionMake2D(0, 0, width, height),
                    mipmapLevel: 0)

   swizzleBGRA8toRGBA8(imageBytes, width: width, height: height)

   guard let colorSpace = CGColorSpace(name: CGColorSpace.linearSRGB) else { return nil }
   let bitmapInfo = CGImageAlphaInfo.premultipliedLast.rawValue
   guard let bitmapContext = CGContext(data: nil,
                                       width: width,
                                       height: height,
                                       bitsPerComponent: 8,
                                       bytesPerRow: imageBytesPerRow,
                                       space: colorSpace,
                                       bitmapInfo: bitmapInfo) else { return nil }
   bitmapContext.data?.copyMemory(from: imageBytes, byteCount: imageByteCount)
   let image = bitmapContext.makeImage()
   return image
}

func swizzleBGRA8toRGBA8(_ bytes: UnsafeMutableRawPointer, width: Int, height: Int) {
   var sourceBuffer = vImage_Buffer(data: bytes,
                                    height: vImagePixelCount(height),
                                    width: vImagePixelCount(width),
                                    rowBytes: width * 4)
   var destBuffer = vImage_Buffer(data: bytes,
                                  height: vImagePixelCount(height),
                                  width: vImagePixelCount(width),
                                  rowBytes: width * 4)
   var swizzleMask: [UInt8] = [2, 1, 0, 3] // BGRA -> RGBA
   vImagePermuteChannels_ARGB8888(&sourceBuffer, &destBuffer, &swizzleMask, vImage_Flags(kvImageNoFlags))
}
