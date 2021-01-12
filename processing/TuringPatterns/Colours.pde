

RGB createRGB(color c) {
  RGB result = new RGB();
  result.r = map(red(c), 0, 255, 0, 1);
  result.g = map(green(c), 0, 255, 0, 1);
  result.b = map(blue(c), 0, 255, 0, 1);
  result.a = map(alpha(c), 0, 255, 0, 1);
  return result;
}

class RGB {
  float r;
  float g;
  float b;
  float a;

  HSV toHSV() { 
    HSV result = new HSV();
    result.a = a;

    float minV = min(r, min(g, b));
    float maxV = max(r, max(g, b));
    result.v = maxV;
    float delta = maxV - minV;

    if (maxV != 0) {
      result.s = delta / maxV;
    } else {
      result.s = 0;
      result.hu = -1;
      result.hu = -1;
      return result;
    }

    if (delta == 0) {
      result.hu = 0;
    } else if (r == maxV) {
      result.hu = (g - b) / delta;
    } else if (g == maxV) {
      result.hu = 2 + (b - r) / delta;
    } else {
      result.hu = 4 + (r - g) / delta;
    }

    result.hu *= 60;
    if (result.hu < 0) {
      result.hu += 360;
    }

    return result;
  }

  color toColor() {
    return color(
      map(r, 0, 1, 0, 255), 
      map(g, 0, 1, 0, 255), 
      map(b, 0, 1, 0, 255), 
      255
    );
  }

  public String toString() {
    return "RGB[" + r + ", " + g + ", " + b + "]";
  }
}

class HSV {
  float hu;
  float s;
  float v;
  float a;

  RGB toRGB() {
    RGB result = new RGB();
    if (s == 0) {
      result.r = v;
      result.g = v;
      result.b = v;
      return result;
    }

    float h = hu / 60;
    int i = floor(h);
    float f = h - i;
    float p = v * (1 - s);
    float q = v * (1 - s * f);
    float t = v * ( 1 - s * (1 - f));
    switch(i) {
    case 0: 
      result.r = v;
      result.g = t;
      result.b = p;
      break;
    case 1: 
      result.r = q;
      result.g = v;
      result.b = p;
      break;
    case 2:
      result.r = p;
      result.g = v;
      result.b = t;
      break;
    case 3:
      result.r = p;
      result.g = q;
      result.b = v;
      break;
    case 4:
      result.r = t;
      result.g = p;
      result.b = v;
      break;
      // case 5:
    default: 
      result.r = v;
      result.g = p;
      result.b = q;
      break;
    }

    return result;
  }

  public String toString() {
    return "HSV[" + hu + ", " + s + ", " + v + "]";
  }
}


void testColourConversion() {

  color red = color(255, 0, 0);
  println("red: " + createRGB(red).toHSV().toRGB());

  color green = color(0, 255, 0);
  println("green: " + createRGB(green).toHSV().toRGB());

  color blue = color(0, 0, 255);
  println("blue: " + createRGB(blue).toHSV().toRGB());

  color white = color(255, 255, 255);
  println("white: " + createRGB(white).toHSV().toRGB());

  color black = color(0, 0, 0);
  println("black: " + createRGB(black).toHSV().toRGB());

  color yellow = color(255, 255, 0);
  println("yellow: " + createRGB(yellow).toHSV().toRGB());

  color teal = color(0, 255, 255);
  println("teal: " + createRGB(teal).toHSV().toRGB());

  color pink = color(255, 0, 255);
  println("pink: " + createRGB(pink).toHSV().toRGB());
} 
