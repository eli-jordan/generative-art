package physarum;

import blur.FastBlur;
import turingpatterns.Convolution;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.*;

public class Physarum {

   private float evaporationRate;
   private int blurRadius;

   private FastBlur blur;

   List<PhysarumAgent> agents;

   int width;
   int height;

   double[][] trail;

   public static Physarum.Builder newBuilder() {
      return new Builder();
   }

   Physarum(Builder builder) {
      this.width = builder.width;
      this.height = builder.height;
      this.blurRadius = builder.blurRadius;
      this.evaporationRate = builder.evaporationRate;
      this.agents = builder.agents;

      this.blur = FastBlur.newBuilder()
          .size(width, height)
          .kernelA(Convolution.createRealCircularKernel(blurRadius, width, height))
          .build();

      this.trail = new double[height][width];
   }

   void update() {
      for (PhysarumAgent agent : agents) {
         agent.update(this.trail);
      }

      applyBlur();
      applyEvaporation();
   }

   void applyBlur() {
      this.blur.applyInplace(this.trail);
   }

   void applyEvaporation() {
      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            this.trail[y][x] *= evaporationRate;
         }
      }
   }

   public static class Builder {
      private float evaporationRate = 0.59f;
      private int blurRadius = 3;

      private List<PhysarumAgent> agents = new ArrayList<>();

      private int width;
      private int height;

      public Builder size(int width, int height) {
         this.width = width;
         this.height = height;
         return this;
      }

      public Builder evaporationRate(float rate) {
         this.evaporationRate = rate;
         return this;
      }

      public Builder blurRadius(int radius) {
         this.blurRadius = radius;
         return this;
      }

      public Builder addAgent(PhysarumAgent agent) {
         this.agents.add(agent);
         return this;
      }

      public Physarum build() {
         return new Physarum(this);
      }
   }
}
