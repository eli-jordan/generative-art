package painting;

import processing.core.PApplet;
import processing.core.PImage;

public class Painting extends PApplet {

   final float initialNumberOfStrokes = 200;
   float initialBrushSize;

   PImage image;

   @Override
   public void settings() {
      this.image = loadImage("painting/mountain-large.jpg");
      size(image.width, image.height);
      initialBrushSize = image.width / 10f;
   }


   @Override
   public void setup() {
      imageMode(CENTER);

      for(int i = 1; i <= 18; i++) {
         run(i);
         println("Finished run for stroke: " + i);
      }
   }

   // Number of strokes are proportional to the area covered
   private int newStrokeCount(int brushSize) {
      return (int) ((initialBrushSize * initialBrushSize * initialNumberOfStrokes) / (brushSize * brushSize));
   }

   private void run(int stroke) {
      background(color(163, 67, 170));
      int brushSize = (int) initialBrushSize;
      int strokeCount = newStrokeCount(brushSize);

      while(image.width / brushSize < 30) {
         PImage brush = loadImage("painting/brush-strokes/stroke" + stroke + ".png");

         PImage sizedBrush = brush.copy();
         sizedBrush.resize(brushSize, 0);

         int x = (int) random(0, image.width);
         int y = (int) random(0, image.height);
         float angle = random(0, TWO_PI);

         int idx = x + y * image.width;
         int colour = image.pixels[idx];

         int gradientMap = lerpColor(color(155, 57, 0), color(163, 67, 170), brightness(colour) / 255);
         int newColour = lerpColor(colour, gradientMap, 0.1f);


         pushMatrix();

         translate(x, y);
         rotate(angle);
         tint(newColour, 100);
         image(sizedBrush, 0, 0);

         popMatrix();

         strokeCount--;
         if (strokeCount <= 0) {
            brushSize -= 10;
            strokeCount = newStrokeCount(brushSize);
         }
      }

      String filePath = "/Users/elias.jordan/creative-code/renders/painting/mountain-stroke-" + stroke + ".png";
      saveFrame(filePath);
      println("Finished");

   }

//   @Override
//   public void draw() {
//      strokeCount--;
//      if (strokeCount <= 0) {
//         brushSize -= 10;
//         if (image.width / brushSize > 40) {
//            noLoop();
//            saveFrame("/Users/elias.jordan/creative-code/renders/painting/wave7.png");
//            println("Finished");
//         }
//         strokeCount = newStrokeCount();
//      }
//
//      int strokeNumber = 15; //floor(random(1, 17))
//      PImage brush = loadImage("painting/brush-strokes/stroke" + strokeNumber + ".png");
//
//
//      if (frameCount % 500 == 0) println("Frame Count: " + frameCount + ", Brush Size: " + brushSize);
//
//      PImage sizedBrush = brush.copy();
//      sizedBrush.resize(brushSize, 0);
//
//      int x = (int) random(0, image.width);
//      int y = (int) random(0, image.height);
//      float angle = random(0, TWO_PI);
//
//      int idx = x + y * image.width;
//      int colour = image.pixels[idx];
//
//      int gradientMap = lerpColor(color(155, 57, 0), color(163, 67, 170), brightness(colour) / 255);
//      int newColour = lerpColor(colour, gradientMap, 0.1f);
//
//
//      pushMatrix();
//
//      translate(x, y);
//      rotate(angle);
//      tint(newColour, 100);
//      image(sizedBrush, 0, 0);
//
//      popMatrix();
//   }

   public static void main(String[] args) {
      PApplet.main(Painting.class);
   }
}
