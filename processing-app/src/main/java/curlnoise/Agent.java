package curlnoise;

import processing.core.PApplet;
import processing.core.PVector;

class Agent {

   private PApplet applet;

   private float mass;

   PVector prevPos;
   PVector pos;
   PVector vel = new PVector();
   PVector acc = new PVector();

   float maxSpeed = 3;

   Agent(PApplet applet, PVector position, float mass) {
      this.applet = applet;
      this.pos = position;
      this.prevPos = this.pos;
      this.mass = mass;
   }

   void applyForce(PVector force) {
      acc.add(force.copy().div(mass));
      vel.limit(maxSpeed);
      vel.add(acc);
      prevPos = pos.copy();
      pos.add(vel);
      acc.mult(0);

      edgesWrap();
   }

   void edgesWrap() {
      if (pos.x >= applet.width) {
         pos.x = 0;
         updatePreviousPos();
      }
      if (pos.x < 0) {
         pos.x = applet.width - 1;
         updatePreviousPos();
      }
      if (pos.y >= applet.height) {
         pos.y = 0;
         updatePreviousPos();
      }
      if (pos.y < 0) {
         pos.y = applet.height - 1;
         updatePreviousPos();
      }
   }

   void edgesReflect() {
      if (pos.x >= applet.width) {
         pos.x = applet.width - 1;
         updatePreviousPos();
         vel.mult(-0.999f);
      }
      if (pos.x < 0) {
         pos.x = 0;
         updatePreviousPos();
         vel.mult(-0.999f);
      }
      if (pos.y >= applet.height) {
         pos.y = applet.height - 1;
         updatePreviousPos();
         vel.mult(-0.999f);
      }
      if (pos.y < 0) {
         pos.y = 0;
         updatePreviousPos();
         vel.mult(-0.999f);
      }
   }

   void updatePreviousPos() {
      this.prevPos.x = pos.x;
      this.prevPos.y = pos.y;
   }

   void draw() {
//         ellipse(pos.x, pos.y, 5, 5);
//      applet.strokeWeight(mass);
      applet.line(prevPos.x, prevPos.y, pos.x, pos.y);
   }
}
