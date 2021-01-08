package turingpatterns;

import processing.core.PApplet;

public class MultiscaleMain extends PApplet {

    private Grid g;
    private Colours colours;
    private ScaleConfigs configs;

    @Override
    public void settings() {
        size(1024, 512, P2D);

    }

    @Override
    public void setup() {
        this.colours = new Colours(this);
        this.configs = new ScaleConfigs(this);

//        Scale[] scales = new Scale[]{
//                new Scale(width, height, 100, 200, 0.05f, color(255, 0, 0)),
//                new Scale(width, height, 20, 40, 0.04f, color(0, 255, 0)),
//                new Scale(width, height, 10, 20, 0.03f, color(0, 0, 255)),
//                new Scale(width, height, 5, 10, 0.02f, color(155, 0, 255)),
//                new Scale(width, height, 1, 2, 0.01f, color(0, 0, 0))
//        };

        g = new Grid(this, this.configs.pastelPalette(1));
    }

    @Override
    public void draw() {
        loadPixels();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x + y * width;

                pixels[index] = pixelColour(x, y);
//                pixels[index] = pixelBW(x, y);
            }
        }

        g.update();
        updatePixels();
        println("Frame Rate: " + frameRate);
//        saveFrame();
    }

    private int pixelBW(int x, int y) {
        float value = map(g.grid[y][x], -1, 1, 0, 255);
        return color(value);
    }

    private int pixelColour(int x, int y) {
        int c = g.colors[y][x];

        Colours.RGBValue rgb = colours.createRGB(red(c), green(c), blue(c), alpha(c));
        Colours.HSVValue hsv = rgb.toHSV();
        hsv.v = map(g.grid[y][x], -1, 1, 0, 1);

        Colours.RGBValue newColour = hsv.toRGB();

//        float contrast = 1.3;
//        newColour.r = (newColour.r - 0.5) * contrast + 0.5;
//        newColour.g = (newColour.g - 0.5) * contrast + 0.5;
//        newColour.b = (newColour.b - 0.5) * contrast + 0.5;

        float gamma = 1.1f;
        newColour.r = pow(newColour.r, gamma);
        newColour.g = pow(newColour.g, gamma);
        newColour.b = pow(newColour.b, gamma);

        return newColour.toColor();
    }

    public static void main(String[] args) {
        PApplet.main(MultiscaleMain.class);
    }
}
