package physarum;

import static processing.core.PApplet.*;

import processing.core.PApplet;
import processing.core.PVector;

public class PhysarumAgent {

   private final float SenseAngle = 25f;
   private final float SenseDistance = 4f;

   int maxX;
   int maxY;

   PVector pos;
   PVector dir;

   float heading;


   PhysarumAgent(PApplet applet, float x, float y) {
      this.pos = new PVector(x, y);
      this.dir = new PVector(0, 0);
      this.heading = floor(applet.random(360/SenseAngle));

      this.maxX = applet.width - 1;
      this.maxY = applet.height - 1;
   }


   void update(double[][] trail) {
      //
      double nextIntensity = 0;
      double maxIntensity = 0;
      float maxHeading = 0;
      for (int i = -1; i < 2; i++) {
         //look in directions relative to heading
         float look = heading + i;
         //get radians angle from heading direction
         float angle = radians(look * SenseAngle);

         PVector offset = PVector.fromAngle(angle).mult(SenseDistance);


         int currentX, currentY;
         currentX = (int) (pos.x + offset.x);
         currentY = (int) (pos.y + offset.y);

         if (currentX > this.maxX) {
            currentX = 0;
         } else if (currentX < 0) {
            currentX = this.maxX;
         }

         if (currentY > this.maxY) {
            currentY = 0;
         } else if (currentY < 0) {
            currentY = this.maxY;
         }

         nextIntensity = trail[currentY][currentX];
         if (maxIntensity < nextIntensity) {
            maxIntensity = nextIntensity;
            dir.x = offset.x;
            dir.y = offset.y;
            dir.setMag(SenseDistance);
            maxHeading = i;
         }
      }
      //turn particle
      heading += maxHeading;
      this.pos.add(this.dir);
      wrap();
   }

   void wrap() {
      if (pos.x > maxX) {
         pos.x = 0;
      } else if (pos.x < 0) {
         pos.x = maxX;
      }

      if (pos.y > maxY) {
         pos.y = 0;
      } else if (pos.y < 0) {
         pos.y = maxY;
      }
   }
}
