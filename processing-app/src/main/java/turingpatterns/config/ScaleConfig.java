package turingpatterns.config;


import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScaleConfig {

   public enum BlurType {
      Circular,
      Gaussian;

      static BlurType fromString(String s) {
         for(BlurType b : BlurType.values()) {
            if(b.toString().equals(s)) {
               return b;
            }
         }

         throw new IllegalArgumentException("Unrecognised blur type: " + s);
      }
   }

   public int inhibitorRadius;
   public int activatorRadius;
   public float smallAmount;
   public BlurType blurType = BlurType.Circular;
   public int colour;
   public int symmetry;
   public int width;
   public int height;

   JSONObject toJson() {
      JSONObject j = new JSONObject();
      j.put("inhibitorRadius", this.inhibitorRadius);
      j.put("activatorRadius", this.activatorRadius);
      j.put("symmetry", this.symmetry);
      j.put("smallAmount", this.smallAmount);
      j.put("colour", this.colour);
      j.put("blurType", this.blurType.toString());
      return j;
   }

   static ScaleConfig.Builder fromJson(JSONObject j) {
      return ScaleConfig.newBuilder()
          .inhibitorRadius(j.getFloat("inhibitorRadius"))
          .activatorRadius(j.getFloat("activatorRadius"))
          .symmetry(j.getInt("symmetry"))
          .smallAmount(j.getFloat("smallAmount"))
          .colour(j.getInt("colour"))
          .blur(BlurType.fromString(j.getString("blurType")));
   }

   static List<ScaleConfig.Builder> fromJson(JSONArray a) {
      List<ScaleConfig.Builder> result = new ArrayList<>();
      for(int i = 0; i < a.size(); i++) {
         result.add(fromJson(a.getJSONObject(i)));
      }
      return result;
   }

   static JSONArray toJson(List<ScaleConfig> scales) {
      JSONArray result = new JSONArray();
      for(ScaleConfig s : scales) {
         result.append(s.toJson());
      }
      return result;
   }

   public static Builder newBuilder() {
      return new Builder();
   }

   public static class Builder {
      private ScaleConfig config = new ScaleConfig();

      public Builder size(int w, int h) {
         this.config.width = w;
         this.config.height = h;
         return this;
      }

      public Builder blur(BlurType type) {
         this.config.blurType = type;
         return this;
      }

      public Builder inhibitorRadius(float r) {
         this.config.inhibitorRadius = (int) r;
         return this;
      }

      public Builder activatorRadius(float r) {
         this.config.activatorRadius = (int) r;
         return this;
      }

      public Builder symmetry(int symmetry) {
         this.config.symmetry = symmetry;
         return this;
      }

      public Builder smallAmount(float amount) {
         this.config.smallAmount = amount;
         return this;
      }

      public Builder colour(int colour) {
         this.config.colour = colour;
         return this;
      }

      public ScaleConfig build() {
         // TODO: Make a copy
         return this.config;
      }
   }
}
