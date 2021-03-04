package physarum;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

public class PhysarumSketch extends PApplet {

   private Physarum physarum;

   @Override
   public void settings() {
      size(512, 512);
   }

   @Override
   public void mouseDragged() {
      this.physarum.trail[mouseY][mouseX] += 300f;
   }

   @Override
   public void setup() {
//      frameRate(5);
      List<PhysarumAgent> agents = new ArrayList<>();
      for(int i = 0; i < 20000; i++) {
         float x = width/2f + random(100) * cos(radians(random(360)));
         float y = height/2f + random(100) * sin(radians(random(360)));

//         float x = random(0, width);
//         float y = random(0, height);

         PhysarumAgent agent = new PhysarumAgent(this, x, y);

         agents.add(agent);
      }

      this.physarum = new Physarum(this.width, this.height, agents);
   }

   @Override
   public void draw() {
      this.physarum.update();

      double min = -1;
      for(int y = 0; y < width; y++) {
         for(int x = 0; x < width; x++) {
            min = Math.min(min, this.physarum.trail[y][x]);
         }
      }

      double max = -1;
      for(int y = 0; y < width; y++) {
         for(int x = 0; x < width; x++) {
            max = Math.max(max, this.physarum.trail[y][x]);
         }
      }

      loadPixels();
      for(int y = 0; y < width; y++) {
         for(int x = 0; x < width; x++) {
            int idx = y*width + x;
            float scaledValue = map((float) this.physarum.trail[y][x], (float) min, (float) max, 0, 255);
            pixels[idx] = color(scaledValue);
         }
      }
      updatePixels();

      if(frameCount % 30 == 0) {
         println("frameRate=" + frameRate);
      }

//      drawAgents();
   }

   private void drawAgents() {
      for(PhysarumAgent agent : this.physarum.agents) {
         fill(255, 0, 0);
         int x = floor(agent.pos.x);
         int y = floor(agent.pos.y);
//         PVector heading = PVector.fromAngle(radians(agent.headingDeg));
//         heading.setMag(10);

         pushMatrix();
         translate(x, y);

         noStroke();
         ellipse(0, 0, 10, 10);

//         stroke(0, 255, 0);
//         line(0, 0, heading.x, heading.y);

         popMatrix();
      }
   }

   public static void main(String[] args) {
      PApplet.main(PhysarumSketch.class);
   }
}
