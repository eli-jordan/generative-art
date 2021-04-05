package turingpatterns_gpu;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.Copy;
import com.thomasdiewald.pixelflow.java.imageprocessing.filter.DwFilter;
import prefixsum.Buffer;
import prefixsum.PrefixSumBlur;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import turingpatterns.config.ScaleConfig;

import java.util.ArrayList;
import java.util.List;

public class GpuSketch extends PApplet {
   GrayscaleRender render;
       PGraphicsOpenGL target;

   DwPixelFlow context;
   GpuGrid grid;

   @Override
   public void settings() {
      size(512, 512, P2D);
   }

   @Override
   public void setup() {
      context = new DwPixelFlow(this);
      target = (PGraphicsOpenGL) createGraphics(width, height, P2D);
      PrefixSumBlur blur = new PrefixSumBlur(context);
      render = new GrayscaleRender(context);
      grid = new GpuGrid(
          width,
          height,
          this,
          context
      );

      List<GpuScale> scales = new ArrayList<>();
      ScaleConfig config = ScaleConfig.newBuilder()
          .size(width, height)
          .smallAmount(0.05f)
          .inhibitorRadius(50)
          .activatorRadius(25)
          .build();
      scales.add(new GpuScale(
          config,
          blur
      ));

      grid.setScales(scales);

      frameRate(1);

   }

   private void sleep(long m) {
      try {
         Thread.sleep(m);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }

   void step() {
      this.grid.update();
      GpuScale scale = this.grid.scales.get(0);
//      this.render.render(this.grid.grid, this.target);
      this.render.render(scale.activator, this.target);

      image(target, 0, 0);

   }

   @Override
   public void draw() {
      step();

   }

   public static void main(String[] args) {
      PApplet.main(GpuSketch.class);
   }
}
