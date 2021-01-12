
void blurLenaImage() {
  final int radius = 5;
  PImage image = loadImage("lena.png");
  
  Complex[][] data = new Complex[image.height][image.width];
  for(int y = 0; y < image.height; y++) {
    for(int x = 0; x < image.width; x++) {
      int index = x + y * image.width;
      float value = 0.30 * red(image.pixels[index]) + 
                    0.59 * green(image.pixels[index]) + 
                    0.11 * blue(image.pixels[index]);
                    
      data[y][x] = new Complex(value, 0);
    }
  }
  
  Complex[][] kernel = createCircularKernel(radius, 512);
  
  Complex[][] result = convolve2d_fft(data, kernel);
  
  loadPixels();
  for(int y = 0; y < height; y++) {
    for(int x = 0; x < width; x++) {
      int index = x + y * image.width;
      pixels[index] = color(result[y][x].mag());
    }
  }
  
  updatePixels();
}
