package turingpatterns;

import processing.core.PApplet;

public class ScaleConfigs {

    private PApplet applet;

    ScaleConfigs(PApplet applet) {
        this.applet = applet;
    }


    public Scale[] pastelPalette(int factor) {
        int w = applet.width;
        int h = applet.height;
        return new Scale[]{
                new Scale(w, h, 180 * factor, 350 * factor, 0.03f / 2, applet.color(245, 59, 70)),
                new Scale(w, h, 128 * factor, 250 * factor, 0.05f / 2, applet.color(246, 117, 29)),
                new Scale(w, h, 128 * factor, 200 * factor, 0.02f / 2, applet.color(243, 206, 25)),
                new Scale(w, h, 64 * factor, 128 * factor, 0.04f / 2, applet.color(22, 166, 174)),
                new Scale(w, h, 50 * factor, 100 * factor, 0.03f / 2, applet.color(255)),
                new Scale(w, h, 50 * factor, 100 * factor, 0.03f / 2, applet.color(0)),
                new Scale(w, h, 10 * factor, 25 * factor, 0.03f / 2, applet.color(80, 151, 72)),
                //new Scale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(242, 185, 245))
        };
    }

    Scale[] pastelReversePalette(int factor) {
        int w = applet.width;
        int h = applet.height;
        return new Scale[]{
                new Scale(w, h, 180 * factor, 350 * factor, 0.03f / 2, applet.color(80, 151, 72)),
                new Scale(w, h, 128 * factor, 250 * factor, 0.05f / 2, applet.color(22, 166, 174)),
                new Scale(w, h, 128 * factor, 200 * factor, 0.02f / 2, applet.color(246, 117, 29)),
                new Scale(w, h, 64 * factor, 128 * factor, 0.04f / 2, applet.color(243, 206, 25)),
                new Scale(w, h, 50 * factor, 100 * factor, 0.03f / 2, applet.color(255)),
                new Scale(w, h, 50 * factor, 100 * factor, 0.03f / 2, applet.color(0)),
                new Scale(w, h, 10 * factor, 25 * factor, 0.03f / 2, applet.color(245, 59, 70)),
                //new Scale(w, h, 5 * factor, 12 * factor, 0.02 / 2, color(242, 185, 245))
        };
    }
}
