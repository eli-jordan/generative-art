package turingpatterns.config;

import processing.core.PApplet;
import processing.data.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ConfigPersistence {

   private File saveDir;

   public ConfigPersistence(String name) {
      this.saveDir = getSaveDir(name);
   }

   public File saveDir() {
      return this.saveDir;
   }

   public void save(RunConfig config) {
      File file = saveDir;
      if(!file.exists()) {
         file.mkdirs();
      }

      if(!file.isDirectory()) {
         throw new IllegalArgumentException("Expected a directory");
      }
      File configFile = new File(file, "config.json");
      if(configFile.exists()) {
         throw new IllegalArgumentException("There is already a config saved at " + file);
      }

      JSONObject configJson = config.toJson();
      if(!configJson.save(configFile, "indent=3")) {
         throw new IllegalStateException("Failed to save config to " + file);
      }
   }

   public static RunConfig.Builder load(File file) {
      JSONObject json = PApplet.loadJSONObject(file);
      return RunConfig.fromJson(json);
   }

   private static File getSaveDir(String name) {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

      File baseDir = getBaseDir();
      String prefix = name + "-" + format.format(new Date());
      int countWithPrefix = 0;

      for(File f : Objects.requireNonNull(baseDir.listFiles())) {
         if(f.getName().startsWith(prefix)) {
            countWithPrefix++;
         }
      }

      return new File(baseDir, String.format("%s-%03d", prefix, countWithPrefix));
   }

   private static File getBaseDir() {
      File home = new File(System.getProperty("user.home"));
      File base = new File(home, "turing-pattern-renderings");
      if(!base.exists()) {
         base.mkdirs();
      }
      return base;
   }
}
