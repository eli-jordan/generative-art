package curlnoise;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

// https://www.openprocessing.org/sketch/779232
public class CurlNoiseSketch extends PApplet {

   int resolution = 5;
   PVector[][] flow;

   int agentCount = 20000;
   List<Agent> agents = new ArrayList<>();

   private boolean drawFlowField = false;


   @Override
   public void settings() {
      size(800, 800);
   }

   @Override
   public void setup() {
//      frameRate(2);
//      noiseDetail(2, 0.1f);
      background(0);
//      background(250, 233, 200);
      initFlowField();
      initAgents();

   }

   @Override
   public void draw() {
//      background(250, 233, 200);
      background(0);
      initFlowField();
      if (drawFlowField) {
         drawFlowField();
      }

      drawAgents();
//      println("Frame Rate: " + frameRate);
//      println("Frame Count: " + frameCount);
//      saveFrame("/Users/elias.jordan/creative-code/renders/curlnoise/explode/frame-####.png");
   }

   private void drawAgents() {
      for (Agent agent : agents) {
         agent.update(flow, resolution);
         float r = 255 - min(abs((agent.pos.x - agent.prevPos.x) * 100), 255);
         float g = 255 - min((abs(agent.pos.y - agent.prevPos.y) * 100), 255);
         float b = min(agent.size * 50, 255);
         stroke(r, g, b, 120);
         strokeWeight(2);
//            stroke(255);
         agent.draw();
      }
   }

   private void initFlowField() {
      int xdim = width / resolution;
      int ydim = height / resolution;
      flow = new PVector[ydim][xdim];

      float maxMag = Float.MIN_VALUE;
      float minMag = Float.MAX_VALUE;
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = curl(x, y, 0.01f);
            maxMag = max(maxMag, vec.mag());
            minMag = min(minMag, vec.mag());
            flow[y][x] = vec;
         }
      }

      // Normalise the magnitude of the vectors in the range 0-1
      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = flow[y][x];
            float mag = map(vec.mag(), minMag, maxMag, 0, 1);
            vec.setMag(mag);
         }
      }

      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = flow[y][x];
            vec.mult(5);
         }
      }
   }

   private void initAgents() {
      for (int i = 0; i < agentCount; i++) {
         PVector pos = new PVector(
             random(0, width),
             random(0, height)
         );

         float size = random(1, 5);
         Agent agent = new Agent(this, pos, size);
         agents.add(agent);
      }
   }


   private void drawFlowField() {
      int xdim = width / resolution;
      int ydim = height / resolution;

      for (int x = 0; x < xdim; x++) {
         for (int y = 0; y < ydim; y++) {
            PVector vec = flow[y][x];
            pushMatrix();
            stroke(255, 0, 0);
            strokeWeight(1);
            translate(x * resolution, y * resolution);
            line(0, 0, vec.x, vec.y);
            popMatrix();
         }
      }

   }

   /**
    * Uses the centered finite difference method to compute the curl
    * of perlin noise at the provided location.
    */
   PVector curl(int x, int y, float detail) {

      float nx = x * detail;
      float ny = y * detail;

      float distanceToCenter = sqrt(pow(width / 2f - x, 2) + pow(height / 2f - y, 2));
      float distanceScale = 1f / (distanceToCenter * detail);

      float eps = 0.01f;
      float n1, n2, a, b;

      float zoff = frameCount * 0.007f;

      n1 = noise(nx, ny + eps, zoff) * distanceScale;
      n2 = noise(nx, ny - eps, zoff) * distanceScale;
      a = (n1 - n2) / (2 * eps);

      n1 = noise(nx + eps, ny, zoff) * distanceScale;
      n2 = noise(nx - eps, ny, zoff) * distanceScale;
      b = (n1 - n2) / (2 * eps);

      return new PVector(-a, b);


//      float noise = noise(x, y, zoff);
//      PVector vec = PVector.fromAngle(noise * TWO_PI);
//      vec.setMag(1);
//      return vec;
   }

   @Override
   public void mouseReleased() {
      drawFlowField = !drawFlowField;
   }

   public static void main(String[] args) {
      PApplet.main(CurlNoiseSketch.class);
   }


}
