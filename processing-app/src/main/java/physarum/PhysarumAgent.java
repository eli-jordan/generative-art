package physarum;

import static processing.core.PApplet.*;

import processing.core.PApplet;
import processing.core.PVector;

public class PhysarumAgent {

   private final PApplet applet;

   // The range of the sensor
   private final float senseAngleDeg;
   private final float senseDistance;

   // How far the agent can turn and move in one time step
   private final float turnAngleDeg;
   private final float moveDistance;

   // The amount of trail that is added to the trail map
   private final float depositAmount;

   // Current position and heading
   private PVector pos;
   private float headingDeg;

   private float heading;
   private PVector dir = new PVector(0, 0);

   public static Builder newBuilder() {
      return new Builder();
   }


   private PhysarumAgent(Builder builder) {
      this.applet = builder.applet;
      this.senseAngleDeg = builder.senseAngle;
      this.senseDistance = builder.senseDistance;

      this.turnAngleDeg = builder.turnAngle;
      this.moveDistance = builder.moveDistance;

      this.depositAmount = builder.depositAmount;
      this.pos = builder.pos;

      this.headingDeg = floor(this.applet.random(360));

      this.heading = this.headingDeg / senseAngleDeg;
   }


   void update(double[][] trail) {
//      turnToMax(trail);
      turnJones(trail);
      move();
      deposit(trail);
   }

   private void turnToMax(double[][] trail) {
      double[] sensed = new double[] {
          senseAt(headingDeg - senseAngleDeg, trail),
          senseAt(headingDeg, trail),
          senseAt(headingDeg + senseAngleDeg, trail)
      };

      double maxValue = 0;
      int maxIndex = 1;
      for(int i = 0; i < sensed.length; i++) {
         if(sensed[i] > maxValue) {
            maxValue = sensed[i];
            maxIndex = i;
         }
      }

//      println("turn1: Data: " + Arrays.toString(sensed) + ", Direction: " + (maxIndex - 1));
      this.headingDeg += (turnAngleDeg * (maxIndex - 1));
   }

   /**
    * Replicated the algorithm defined in https://uwe-repository.worktribe.com/output/980579
    */
   private void turnJones(double[][] trail) {
      double fl = senseAt(headingDeg - senseAngleDeg, trail);
      double f = senseAt(headingDeg, trail);
      double fr = senseAt(headingDeg + senseAngleDeg, trail);

      boolean debug = false;
      if (f > fl && f > fr) {
         // stay facing the same direction
         if(debug) println("Direction: 0");
      } else if (f < fl && f < fr) {
         if(debug) println("Direction: rand");
         // rotate randomly left or right
         if (this.applet.random(1) > 0.5) {
            this.headingDeg += turnAngleDeg;
         } else {
            this.headingDeg -= turnAngleDeg;
         }
      } else if (fl < fr) {
         if(debug) println("Direction: +1");
         this.headingDeg += turnAngleDeg;
      } else if (fr < fl) {
         if(debug) println("Direction: -1");
         this.headingDeg -= turnAngleDeg;
      }
   }

   private void move() {
      PVector moveVec = PVector.fromAngle(radians(this.headingDeg)).setMag(this.moveDistance);
      this.pos.add(moveVec);
      wrap();
   }

   private double senseAt(float degrees, double[][] trail) {
      PVector offset = PVector.fromAngle(radians(degrees)).setMag(senseDistance);
      int senseX = wrapIndex((int)(pos.x + offset.x), this.applet.width - 1);
      int senseY = wrapIndex((int)(pos.y + offset.y), this.applet.height - 1);
      return trail[senseY][senseX];
   }

   private int wrapIndex(int value, int max) {
      if (value > max) {
         return 0;
      } else if (value < 0) {
         return max;
      } else {
         return value;
      }
   }

//   private void turn0(double[][] trail) {
//      double[] sensed = new double[3];
//      double maxIntensity = 0;
//      float maxHeading = 0;
//      for (int i = -1; i < 2; i++) {
//         //look in directions relative to heading
//         float look = heading + i;
//         //get radians angle from heading direction
//         float angle = radians(look * senseAngleDeg);
//
//         PVector offset = PVector.fromAngle(angle).mult(senseDistance);
//
//
//         int currentX, currentY;
//         currentX = (int) (pos.x + offset.x);
//         currentY = (int) (pos.y + offset.y);
//
//         if (currentX > this.applet.width - 1) {
//            currentX = 0;
//         } else if (currentX < 0) {
//            currentX = this.applet.width - 1;
//         }
//
//         if (currentY > this.applet.height - 1) {
//            currentY = 0;
//         } else if (currentY < 0) {
//            currentY = this.applet.height - 1;
//         }
//
//         double nextIntensity = trail[currentY][currentX];
//         sensed[i+1] = nextIntensity;
//         if (nextIntensity > maxIntensity) {
//            maxIntensity = nextIntensity;
//            dir.x = offset.x;
//            dir.y = offset.y;
//            dir.setMag(senseDistance);
//            maxHeading = i;
//         }
//      }
////      println("turn0: Data: " + Arrays.toString(sensed) + ", Direction=" + maxHeading);
//      //turn particle
//      heading += maxHeading;
//   }
//
//   private void move0() {
//      this.pos.add(dir);
//      wrap();
//   }



   private void deposit(double[][] trail) {
      int x = floor(this.pos.x);
      int y = floor(this.pos.y);
      trail[y][x] += this.depositAmount;
   }


   void wrap() {
      if (pos.x > applet.width - 1) {
         pos.x = 0;
      } else if (pos.x < 0) {
         pos.x = applet.width - 1;
      }

      if (pos.y > this.applet.height - 1) {
         pos.y = 0;
      } else if (pos.y < 0) {
         pos.y = this.applet.height - 1;
      }
   }

   public static class Builder {
      private PApplet applet;
      private PVector pos;
      private float senseAngle = 25f;
      private float senseDistance = 4f;

      private float turnAngle;
      private float moveDistance;

      private float depositAmount = 10f;


      public Builder applet(PApplet applet) {
         this.applet = applet;
         return this;
      }

      public Builder position(float x, float y) {
         this.pos = new PVector(x, y);
         return this;
      }

      public Builder depositAmount(float amt) {
         this.depositAmount = amt;
         return this;
      }


      public Builder senseAngle(float degrees) {
         this.senseAngle = degrees;
         return this;
      }

      public Builder turnAngle(float degrees) {
         this.turnAngle = degrees;
         return this;
      }

      public Builder senseDistance(float d) {
         this.senseDistance = d;
         return this;
      }

      public Builder moveDistance(float d) {
         this.moveDistance = d;
         return this;
      }

      public PhysarumAgent build() {
         return new PhysarumAgent(this);
      }
   }
}
