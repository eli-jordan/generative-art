package curlnoise;

import processing.core.PApplet;
import processing.core.PVector;

class Agent {

   private final PApplet applet;

   PVector prevPos;
   PVector pos;

   float size;


   Agent(PApplet applet, PVector position, float size) {
      this.applet = applet;
      this.pos = position;
      this.size = size;
      this.prevPos = this.pos;
   }

   void update(VectorField flow, float resolution) {
      prevPos = pos.copy();
      Advection.advect(this, flow, resolution);
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

   void updatePreviousPos() {
      this.prevPos.x = pos.x;
      this.prevPos.y = pos.y;
   }

   void draw() {
      applet.line(prevPos.x, prevPos.y, pos.x, pos.y);
   }
}
