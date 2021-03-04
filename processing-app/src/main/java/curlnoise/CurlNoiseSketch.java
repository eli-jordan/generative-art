package curlnoise;

import processing.core.PApplet;
import processing.core.PVector;
import warp.WorleyNoise;

import java.util.ArrayList;
import java.util.List;


public class CurlNoiseSketch extends PApplet {

   WorleyNoise worley = WorleyNoise.getInstance();

   int resolution = 5;
   VectorField field;

   final float perlinDetail = 0.02f;
   final float worleyDetail = 0.03f;

   int agentCount = 10000;
   List<Agent> agents = new ArrayList<>();

   private boolean drawFlowField = false;


   @Override
   public void settings() {
      size(800, 600);
   }

   @Override
   public void setup() {
      this.field = new VectorField(width / resolution, height/ resolution, this::worleyCurl);
//      frameRate(2);
//      noiseDetail(2, 0.1f);
//      background(0);
      background(250, 233, 200);
//      initFlowField();
      initAgents();

   }

   @Override
   public void draw() {
//      background(250, 233, 200);
      background(0);
      this.field.recalculate();

      if (drawFlowField) {
         this.field.draw(this, resolution);
      }

      drawAgents();

//      println("Frame Rate: " + frameRate);
//      println("Frame Count: " + frameCount);
//      saveFrame("/Users/elias.jordan/creative-code/renders/curlnoise/worley2/frame-####.png");
   }

   private void drawAgents() {
      for (Agent agent : agents) {
         agent.update(field, resolution);
         float r = 255 - min(abs((agent.pos.x - agent.prevPos.x) * 100), 255) -10;
         float g = 255 - min((abs(agent.pos.y - agent.prevPos.y) * 100), 255) -30;
         float b = min(agent.size * 50, 255) -10;
         stroke(r, g, b);
         strokeWeight(agent.size * 1.5f);
//         stroke(255);
         agent.draw();
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

   /**
    * Uses the centered finite difference method to compute the 2d curl
    * of perlin noise at the provided location.
    */
   PVector perlinCurl(int x, int y) {

      float nx = x * perlinDetail;
      float ny = y * perlinDetail;

      float distanceToCenter = sqrt(pow(width / 2f - x, 2) + pow(height / 2f - y, 2));
      float distanceScale = 1f / (distanceToCenter * perlinDetail);

      float eps = 0.01f;
      float n1, n2, a, b;

      float zoff = frameCount * 0.003f;

      n1 = noise(nx, ny + eps, zoff) * distanceScale;
      n2 = noise(nx, ny - eps, zoff) * distanceScale;
      a = (n1 - n2) / (2 * eps);

      n1 = noise(nx + eps, ny, zoff) * distanceScale;
      n2 = noise(nx - eps, ny, zoff) * distanceScale;
      b = (n1 - n2) / (2 * eps);

      return new PVector(-a, b);
   }

   /**
    * Uses the centered finite difference method to compute the 2d curl
    * of worley noise at the provided location.
    */
   PVector worleyCurl(int x, int y) {

      float nx = x * worleyDetail;
      float ny = y * worleyDetail;

      float distanceToCenter = sqrt(pow(width / 2f - x, 2) + pow(height / 2f - y, 2));
      float distanceScale = 1f / (distanceToCenter * worleyDetail);

      float eps = 0.01f;
      float n1, n2, a, b;

      float zoff = frameCount * 0.003f;

      n1 = worley.noise(nx, ny + eps, zoff) * distanceScale;
      n2 = worley.noise(nx, ny - eps, zoff) * distanceScale;
      a = (n1 - n2) / (2 * eps);

      n1 = worley.noise(nx + eps, ny, zoff) * distanceScale;
      n2 = worley.noise(nx - eps, ny, zoff) * distanceScale;
      b = (n1 - n2) / (2 * eps);

      return new PVector(-a, b);
   }

   @Override
   public void mouseReleased() {
      drawFlowField = !drawFlowField;
   }

   public static void main(String[] args) {
      PApplet.main(CurlNoiseSketch.class);
   }
}
