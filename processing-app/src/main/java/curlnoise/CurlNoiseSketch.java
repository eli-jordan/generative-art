package curlnoise;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;


public class CurlNoiseSketch extends PApplet {

   int resolution = 5;
   PVector[][] flow;

   int agentCount = 100;
   List<Agent> agents = new ArrayList<>();

   private boolean drawFlowField = false;

   @Override
   public void settings() {
      size(800, 800);
   }

   @Override
   public void setup() {
//      noiseDetail(2, 0.1f);
      background(0);
      initFlowField();
      initAgents();
   }

   @Override
   public void draw() {
//      background(0);
      if(drawFlowField) {
         drawFlowField();
      }

      drawAgents();
//      println("Frame Rate: " + frameRate);
//      println("Frame Count: " + frameCount);
//      saveFrame("/Users/elias.jordan/creative-code/renders/curlnoise/explode/frame-####.png");
   }

   PVector gravity = new PVector(0, 0.08f);


   private void drawAgents() {
      for (Agent agent : agents) {

//         agent.applyForce(gravity);


         int ix = 0, iy = 0;
         try {
            ix = floor(agent.pos.x / resolution);
            iy = floor(agent.pos.y / resolution);
            PVector force = flow[iy][ix];

//            agent.applyForce(force.copy().setMag(1));
            agent.applyForce(force.copy().mult(3));

            strokeWeight(1);
            stroke(255, 10);
            agent.draw();
         } catch (Exception e) {
            e.printStackTrace();
            println(agent.pos + ", ix=" + ix + ", iy=" + iy);
         }
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
            PVector vec = curl(x / 0.01f, y / 0.01f);
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
   }

   private void initAgents() {
      for (int i = 0; i < agentCount/2; i++) {
//         agents.add(new Agent(new PVector(random(width), random(height))));
         PVector pos = new PVector(
             width / 2f + random(-width/2f, width/2f),
             height / 2f
         );

         PVector vel = PVector.fromAngle(random(-PI, PI)).setMag(random(0.5f,1));

         float mass = random(1, 5);
         Agent agent = new Agent(this, pos, mass);
//         agent.vel = vel;
         agents.add(agent);
      }

      for (int i = 0; i < agentCount/2; i++) {
//         agents.add(new Agent(new PVector(random(width), random(height))));
         PVector pos = new PVector(
             width / 2f ,
             height / 2f + random(-height/2f, height/2f)
         );

         PVector vel = PVector.fromAngle(random(-PI, PI)).setMag(random(0.5f,1));

         float mass = random(1, 5);
         Agent agent = new Agent(this, pos, mass);
//         agent.vel = vel;
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
            line(0, 0, vec.x * 30, vec.y * 30);
            popMatrix();
         }
      }

   }

   /**
    * Uses the centered finite difference method to compute the curl
    * of perlin noise at the provided location.
    */
   PVector curl(float x, float y) {
      float eps = 0.01f;
      float n1, n2, a, b;

      n1 = noise(x, y + eps);
      n2 = noise(x, y - eps);
      a = (n1 - n2) / (2 * eps);

      n1 = noise(x + eps, y);
      n2 = noise(x - eps, y);
      b = (n1 - n2) / (2 * eps);

      return new PVector(b, -a);


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
