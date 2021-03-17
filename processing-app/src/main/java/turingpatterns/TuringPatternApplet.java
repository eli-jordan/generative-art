package turingpatterns;

import processing.core.PApplet;
import turingpatterns.config.ConfigPersistence;
import turingpatterns.config.RunConfig;
import turingpatterns.config.ScaleConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class that interprets the {{@link RunConfig}} renders the frames and saves both the configuration
 * and the frames to disk.
 * <p>
 * To define the runConfig, extend this class and in the constructor set the `runConfig` attribute.
 * This can be either loaded from disk using {{@link ConfigPersistence#load(File)}} or dynamically generated
 * in any other way.
 */
public abstract class TuringPatternApplet extends PApplet {

   /* The configuration for this run. Should be set in the constructor of a subclass*/
   protected RunConfig runConfig;

   /* Used to render the grid to the screen */
   private Renderer renderer;

   /* Persistence helper used to load and store the RunConfig */
   private ConfigPersistence persistence;

   private long startTs;

   @Override
   public final void settings() {
      this.startTs = System.currentTimeMillis();

      // Save the configuration
      this.persistence = new ConfigPersistence(this.getClass().getSimpleName());
      this.persistence.save(this.runConfig);

      println("Run configuration and render frames will be saved in: " + this.persistence.saveDir());

      size(runConfig.width, runConfig.height);

      // Set the random and noise seeds, so we can recreate the randomness of a goods run
      randomSeed(runConfig.randomSeed);
      noiseSeed(runConfig.noiseSeed);

      // Initialise the grid and renderer
      Grid grid = createGrid();
      if (runConfig.renderer == RunConfig.RenderType.Colour) {
         this.renderer = new ColourRenderer(grid, new Colours(this));
      } else if (runConfig.renderer == RunConfig.RenderType.Grayscale) {
         this.renderer = new GrayscaleRenderer(grid);
      } else {
         throw new IllegalStateException("Unrecognised RenderType: " + runConfig.renderer);
      }
   }

   private Grid createGrid() {
      Grid.Builder builder = Grid.newBuilder(this);
      if (runConfig.coupling == RunConfig.ScaleCoupling.MultiScale) {
         builder.scaleCoupling(Grid::multiScaleDelta);
      } else if (runConfig.coupling == RunConfig.ScaleCoupling.Compound) {
         builder.scaleCoupling(Grid::compoundScaleDelta);
      } else {
         throw new IllegalStateException("Unrecognised ScaleCoupling: " + runConfig.coupling);
      }

      List<Scale> scales = new ArrayList<>();
      for (ScaleConfig c : runConfig.scales) {
         scales.add(new Scale(c));
      }

      builder.scales(scales);

      return builder.build();
   }

   @Override
   public void draw() {
      this.renderer.draw(this);

      if (frameCount % 10 == 0) {
         long runTime = System.currentTimeMillis() - this.startTs;
         println("Frame Rate: " + frameRate + ", Frame Count: " + frameCount + ", Running Time: " + runTime + " ms");
      }

      saveFrame(this.persistence.saveDir().getAbsolutePath() + "/frame-#####.png");
   }
}
