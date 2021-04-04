package glslfft;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import processing.core.PApplet;

import java.lang.reflect.Method;

public class AppletTest extends PApplet {

   @Override
   public void settings() {
      size(0, 0, P2D);
   }

   @Override
   public void setup() {

      StringBuilder result = new StringBuilder();
      try {
         for(Method m : this.getClass().getDeclaredMethods()) {
            if(m.getName().startsWith("test") && m.getParameterCount() == 0) {
               boolean success;
               try {
                  println("==> Starting: " + m.getName());
                  m.invoke(this);
                  success = true;
               } catch(Exception e) {
                  e.printStackTrace(System.out);
                  success = false;
               }
               println("<== Finished: " + m.getName());
               if(success) {
                  result.append(m.getName()).append(": ✅ \n");
               } else {
                  result.append(m.getName()).append(": ❌ \n");
               }
            }
         }
      } finally {
         println("\nTest Results Summary: ");
         println(result);
         exit();
      }
   }
}
