package turingpatterns_gpu;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.dwgl.DwGLSLProgram;
import prefixsum.Buffer;
import prefixsum.PrefixSum;
import processing.core.PApplet;

import java.nio.FloatBuffer;
import java.util.List;

public class GpuGrid {

   private int width, height;

   private PApplet applet;
   private DwPixelFlow context;
   private PrefixSum prefixSum;

   private DwGLSLProgram turingStep;

   Buffer grid;

   Buffer bufA;
   Buffer bufB;

   List<GpuScale> scales;

   GpuGrid(int width, int height, PApplet applet, DwPixelFlow context) {
      this.width = width;
      this.height = height;
      this.applet = applet;
      this.context = context;

      this.prefixSum = new PrefixSum(this.context);
      this.grid = initGrid(width, height);
      this.bufA = this.prefixSum.newBuffer(width, height);
      this.bufB = this.prefixSum.newBuffer(width, height);

      this.turingStep = this.context.createShader("prefixsum/turing-pattern-step.frag");
   }

   void setScales(List<GpuScale> scales) {
      this.scales = scales;
   }

   private Buffer initGrid(int width, int height) {
      int total = width*height;
      float[] random = new float[total];
      for(int i = 0; i < total; i++) {
         random[i] = applet.random(-1, 1);
      }
      return this.prefixSum.newBuffer(width, height, FloatBuffer.wrap(random));
   }

   Buffer update() {
      Buffer prefixSum = this.prefixSum.run(this.grid, this.bufA, this.bufB, width, height);
      for(GpuScale scale : this.scales) {
         scale.applyBlur(prefixSum);
      }

      runTuringStep();

      return prefixSum;
   }

   void runTuringStep() {
      this.context.begin();
      this.context.beginDraw(this.bufA.buf);

      this.turingStep.begin();
      this.turingStep.uniformTexture("grid", this.grid.buf);
      this.turingStep.uniform1i("scaleCount", this.scales.size());
      this.turingStep.uniform2f("resolution", 1.0f / width, 1.0f / height);

      for(int i = 0; i < this.scales.size(); i++) {
         GpuScale scale = this.scales.get(i);
         this.turingStep.uniformTexture("activator[" + i + "]", scale.activator.buf);
         this.turingStep.uniformTexture("inhibitor[" + i + "]", scale.inhibitor.buf);
         this.turingStep.uniform1f("bumpAmount[" + i + "]", scale.config.smallAmount);
      }

      this.turingStep.drawFullScreenQuad();

      this.turingStep.end();

      this.context.endDraw();
      this.context.end();

      // Swap bufA and grid
      Buffer tmp = grid;
      this.grid = this.bufA;
      this.bufA = tmp;

//      this.turingStep.clearUniformTextures();
   }

}
