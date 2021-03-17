package physarum;

import processing.core.PApplet;
import turingpatterns.config.ConfigPersistence;

import java.util.ArrayList;
import java.util.List;

public class PhysarumSketch extends PApplet {

   private Physarum physarum;

   private ConfigPersistence config = new ConfigPersistence(this.getClass().getSimpleName());

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
//      frameRate(2);
      Physarum.Builder builder = Physarum.newBuilder()
          .size(width, height)
          .blurRadius(2)
          .evaporationRate(0.75f);

      float agentCount = (width * height) * 0.5f;
//      float agentCount = 20000;
      for (int i = 0; i < agentCount; i++) {

         float x = (width / 2f) + random((width/2f)*0.1f, (width/2f)*0.8f) * cos(radians(i/5f + random(-1, 1)));
         float y = (height / 2f) + random((width/2f)*0.1f, (width/2f)*0.8f) * sin(radians(i/5f + random(-1, 1)));

//         float x = random(0, width);
//         float y = random(0, height);

         PhysarumAgent agent = PhysarumAgent.newBuilder()
             .applet(this)
             .senseAngle(45 + random(-5, 5))
             .turnAngle(22.5f)// + random(-10, 10))
             .senseDistance(9)// + random(-3, 3))
             .moveDistance(2)// + random(-1, 1))
             .depositAmount(10)
             .position(x, y)
             .build();

         builder.addAgent(agent);
      }

      this.physarum = builder.build();

      println("Rendered frames saved at: " + config.saveDir().getAbsolutePath());
   }

   @Override
   public void draw() {
      this.physarum.update();

      double min = -1;
      for (int y = 0; y < width; y++) {
         for (int x = 0; x < width; x++) {
            min = Math.min(min, this.physarum.trail[y][x]);
         }
      }

      double max = -1;
      for (int y = 0; y < width; y++) {
         for (int x = 0; x < width; x++) {
            max = Math.max(max, this.physarum.trail[y][x]);
         }
      }

      loadPixels();
      for (int y = 0; y < width; y++) {
         for (int x = 0; x < width; x++) {
            int idx = y * width + x;
            float scaledValue = map((float) this.physarum.trail[y][x], (float) min, (float) max, 0, 255);
            pixels[idx] = color(scaledValue);
         }
      }
      updatePixels();

      if (frameCount % 30 == 0) {
         println("Frame Rate:" + frameRate + ", Frame Count: " + frameCount);
      }

//      saveFrame(config.saveDir().getAbsolutePath() + "/frame-#####.png");
   }

   public static void main(String[] args) {
      PApplet.main(PhysarumSketch.class);
   }
}
