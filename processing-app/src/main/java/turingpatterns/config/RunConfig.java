package turingpatterns.config;

import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RunConfig {
   public enum RenderType {
      Colour,
      Grayscale;

      static RenderType fromString(String s) {
         for (RenderType r : RenderType.values()) {
            if (r.toString().equals(s)) {
               return r;
            }
         }

         throw new IllegalArgumentException("Unrecognised RenderType: " + s);
      }
   }

   public enum ScaleCoupling {
      MultiScale,
      Compound;

      static ScaleCoupling fromString(String s) {
         for (ScaleCoupling r : ScaleCoupling.values()) {
            if (r.toString().equals(s)) {
               return r;
            }
         }

         throw new IllegalArgumentException("Unrecognised ScaleCoupling: " + s);
      }
   }

   public int width;
   public int height;

   public long randomSeed;
   public long noiseSeed;
   public RenderType renderer;
   public ScaleCoupling coupling;

   public List<ScaleConfig> scales;

   private RunConfig(Builder builder) {
      this.width = builder.width;
      this.height = builder.height;
      this.randomSeed = builder.randomSeed;
      this.noiseSeed = builder.randomSeed;
      this.renderer = builder.renderer;
      this.coupling = builder.coupling;
      this.scales = builder.scales();
   }

   JSONObject toJson() {
      JSONObject j = new JSONObject();
      j.put("width", this.width);
      j.put("height", this.height);
      j.put("randomSeed", this.randomSeed);
      j.put("noiseSeed", this.noiseSeed);
      j.put("renderer", this.renderer.toString());
      j.put("coupling", this.coupling.toString());
      j.put("scales", ScaleConfig.toJson(this.scales));

      return j;
   }

   static RunConfig.Builder fromJson(JSONObject j) {
      return RunConfig.newBuilder()
          .size(j.getInt("width"), j.getInt("height"))
          .randomSeed(j.getLong("randomSeed"))
          .noiseSeed(j.getLong("noiseSeed"))
          .renderer(RenderType.fromString(j.getString("renderer")))
          .scaleCoupling(ScaleCoupling.fromString(j.getString("coupling")))
          .addScales(ScaleConfig.fromJson(j.getJSONArray("scales")));
   }

   public static Builder newBuilder() {
      return new Builder();
   }

   public static class Builder {
      private int width;
      private int height;

      private long randomSeed = new Random().nextLong();
      private long noiseSeed = new Random().nextLong();
      private RenderType renderer = RenderType.Colour;
      private ScaleCoupling coupling;

      List<ScaleConfig.Builder> scales = new ArrayList<>();

      public Builder size(int width, int height) {
         this.width = width;
         this.height = height;
         return this;
      }

      public Builder randomSeed(long seed) {
         this.randomSeed = seed;
         return this;
      }

      public Builder noiseSeed(long seed) {
         this.noiseSeed = seed;
         return this;
      }

      public Builder renderer(RenderType renderer) {
         this.renderer = renderer;
         return this;
      }

      public Builder scaleCoupling(ScaleCoupling coupling) {
         this.coupling = coupling;
         return this;
      }

      public Builder addScale(ScaleConfig.Builder config) {
         this.scales.add(config);
         return this;
      }

      public Builder addScales(List<ScaleConfig.Builder> configs) {
         this.scales.addAll(configs);
         return this;
      }

      private List<ScaleConfig> scales() {
         List<ScaleConfig> configs = new ArrayList<>();
         for (ScaleConfig.Builder b : this.scales) {
            configs.add(b.build());
         }
         return configs;
      }

      public RunConfig build() {
         for (ScaleConfig.Builder c : this.scales) {
            c.size(this.width, this.height);
         }

         return new RunConfig(this);
      }
   }
}
