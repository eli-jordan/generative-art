package turingpatterns_cl;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TuringKernelRender {

   /**
    * Uses a handlebars template to render the turing_update kernel for a given number of scales.
    */
   public static String render(int scaleCount) {
      try {
         List<Integer> indices = new ArrayList<>();
         for(int i = 0; i < scaleCount; i++) {
            indices.add(i);
         }

         String templateSource = readStream(TuringKernelRender.class.getResourceAsStream("/cl-kernels/turing_update.cl.hbs"));

         Handlebars hbs = new Handlebars();
         Template template = hbs.compileInline(templateSource);
         Map<String, List<Integer>> context = new HashMap<>();
         context.put("indices", indices);
         return template.apply(context);
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static String readStream(InputStream s) throws IOException {
      int bufferSize = 1024;
      char[] buffer = new char[bufferSize];
      StringBuilder out = new StringBuilder();
      Reader in = new InputStreamReader(s, StandardCharsets.UTF_8);
      for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
         out.append(buffer, 0, numRead);
      }
      return out.toString();
   }
}
