package org.jeometry.common.awt;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;

public interface WebColors {
  Map<Color, String> COLOR_NAMES = getColorNames();

  Color AliceBlue = new Color(240, 248, 255);

  Color AntiqueWhite = new Color(250, 235, 215);

  Color Aqua = new Color(0, 255, 255);

  Color Aquamarine = new Color(127, 255, 212);

  Color Azure = new Color(240, 255, 255);

  Color Beige = new Color(245, 245, 220);

  Color Bisque = new Color(255, 228, 196);

  Color Black = new Color(0, 0, 0);

  Color BlanchedAlmond = new Color(255, 235, 205);

  Color Blue = new Color(0, 0, 255);

  Color BlueViolet = new Color(138, 43, 226);

  Color Brown = new Color(165, 42, 42);

  Color BurlyWood = new Color(222, 184, 135);

  Color CadetBlue = new Color(95, 158, 160);

  Color Chartreuse = new Color(127, 255, 0);

  Color Chocolate = new Color(210, 105, 30);

  Color Coral = new Color(255, 127, 80);

  Color CornflowerBlue = new Color(100, 149, 237);

  Color Cornsilk = new Color(255, 248, 220);

  Color Crimson = new Color(220, 20, 60);

  Color Cyan = new Color(0, 255, 255);

  Color DarkBlue = new Color(0, 0, 139);

  Color DarkCyan = new Color(0, 139, 139);

  Color DarkGoldenRod = new Color(184, 134, 11);

  Color DarkGray = new Color(169, 169, 169);

  Color DarkGreen = new Color(0, 100, 0);

  Color DarkKhaki = new Color(189, 183, 107);

  Color DarkMagenta = new Color(139, 0, 139);

  Color DarkOliveGreen = new Color(85, 107, 47);

  Color DarkOrange = new Color(255, 140, 0);

  Color DarkOrchid = new Color(153, 50, 204);

  Color DarkRed = new Color(139, 0, 0);

  Color DarkSalmon = new Color(233, 150, 122);

  Color DarkSeaGreen = new Color(143, 188, 143);

  Color DarkSlateBlue = new Color(72, 61, 139);

  Color DarkSlateGray = new Color(47, 79, 79);

  Color DarkTurquoise = new Color(0, 206, 209);

  Color DarkViolet = new Color(148, 0, 211);

  Color DeepPink = new Color(255, 20, 147);

  Color DeepSkyBlue = new Color(0, 191, 255);

  Color DimGray = new Color(105, 105, 105);

  Color DimGrey = new Color(105, 105, 105);

  Color DodgerBlue = new Color(30, 144, 255);

  Color FireBrick = new Color(178, 34, 34);

  Color FloralWhite = new Color(255, 250, 240);

  Color ForestGreen = new Color(34, 139, 34);

  Color Fuchsia = new Color(255, 0, 255);

  Color Gainsboro = new Color(220, 220, 220);

  Color GhostWhite = new Color(248, 248, 255);

  Color Gold = new Color(255, 215, 0);

  Color GoldenRod = new Color(218, 165, 32);

  Color Gray = new Color(128, 128, 128);

  Color Green = new Color(0, 128, 0);

  Color GreenYellow = new Color(173, 255, 47);

  Color HoneyDew = new Color(240, 255, 240);

  Color HotPink = new Color(255, 105, 180);

  Color IndianRed = new Color(205, 92, 92);

  Color Indigo = new Color(75, 0, 130);

  Color Ivory = new Color(255, 255, 240);

  Color Khaki = new Color(240, 230, 140);

  Color Lavender = new Color(230, 230, 250);

  Color LavenderBlush = new Color(255, 240, 245);

  Color LawnGreen = new Color(124, 252, 0);

  Color LemonChiffon = new Color(255, 250, 205);

  Color LightBlue = new Color(173, 216, 230);

  Color LightCoral = new Color(240, 128, 128);

  Color LightCyan = new Color(224, 255, 255);

  Color LightGoldenRodYellow = new Color(250, 250, 210);

  Color LightGray = new Color(211, 211, 211);

  Color LightGreen = new Color(144, 238, 144);

  Color LightPink = new Color(255, 182, 193);

  Color LightSalmon = new Color(255, 160, 122);

  Color LightSeaGreen = new Color(32, 178, 170);

  Color LightSkyBlue = new Color(135, 206, 250);

  Color LightSlateGray = new Color(119, 136, 153);

  Color LightSteelBlue = new Color(176, 196, 222);

  Color LightYellow = new Color(255, 255, 224);

  Color Lime = new Color(0, 255, 0);

  Color LimeGreen = new Color(50, 205, 50);

  Color Linen = new Color(250, 240, 230);

  Color Magenta = new Color(255, 0, 255);

  Color Maroon = new Color(128, 0, 0);

  Color MediumAquaMarine = new Color(102, 205, 170);

  Color MediumBlue = new Color(0, 0, 205);

  Color MediumOrchid = new Color(186, 85, 211);

  Color MediumPurple = new Color(147, 112, 219);

  Color MediumSeaGreen = new Color(60, 179, 113);

  Color MediumSlateBlue = new Color(123, 104, 238);

  Color MediumSpringGreen = new Color(0, 250, 154);

  Color MediumTurquoise = new Color(72, 209, 204);

  Color MediumVioletRed = new Color(199, 21, 133);

  Color MidnightBlue = new Color(25, 25, 112);

  Color MintCream = new Color(245, 255, 250);

  Color MistyRose = new Color(255, 228, 225);

  Color Moccasin = new Color(255, 228, 181);

  Color NavajoWhite = new Color(255, 222, 173);

  Color Navy = new Color(0, 0, 128);

  Color OldLace = new Color(253, 245, 230);

  Color Olive = new Color(128, 128, 0);

  Color OliveDrab = new Color(107, 142, 35);

  Color Orange = new Color(255, 165, 0);

  Color OrangeRed = new Color(255, 69, 0);

  Color Orchid = new Color(218, 112, 214);

  Color PaleGoldenRod = new Color(238, 232, 170);

  Color PaleGreen = new Color(152, 251, 152);

  Color PaleTurquoise = new Color(175, 238, 238);

  Color PaleVioletRed = new Color(219, 112, 147);

  Color PapayaWhip = new Color(255, 239, 213);

  Color PeachPuff = new Color(255, 218, 185);

  Color Peru = new Color(205, 133, 63);

  Color Pink = new Color(255, 192, 203);

  Color Plum = new Color(221, 160, 221);

  Color PowderBlue = new Color(176, 224, 230);

  Color Purple = new Color(128, 0, 128);

  Color Red = new Color(255, 0, 0);

  Color RosyBrown = new Color(188, 143, 143);

  Color RoyalBlue = new Color(65, 105, 225);

  Color SaddleBrown = new Color(139, 69, 19);

  Color Salmon = new Color(250, 128, 114);

  Color SandyBrown = new Color(244, 164, 96);

  Color SeaGreen = new Color(46, 139, 87);

  Color SeaShell = new Color(255, 245, 238);

  Color Sienna = new Color(160, 82, 45);

  Color Silver = new Color(192, 192, 192);

  Color SkyBlue = new Color(135, 206, 235);

  Color SlateBlue = new Color(106, 90, 205);

  Color SlateGray = new Color(112, 128, 144);

  Color Snow = new Color(255, 250, 250);

  Color SpringGreen = new Color(0, 255, 127);

  Color SteelBlue = new Color(70, 130, 180);

  Color Tan = new Color(210, 180, 140);

  Color Teal = new Color(0, 128, 128);

  Color Thistle = new Color(216, 191, 216);

  Color Tomato = new Color(255, 99, 71);

  Color Turquoise = new Color(64, 224, 208);

  Color Violet = new Color(238, 130, 238);

  Color Wheat = new Color(245, 222, 179);

  Color White = new Color(255, 255, 255);

  Color WhiteSmoke = new Color(245, 245, 245);

  Color Yellow = new Color(255, 255, 0);

  Color YellowGreen = new Color(154, 205, 50);

  static Color blend(final Color origin, final Color over) {
    if (over == null) {
      return origin;
    } else if (origin == null) {
      return over;
    } else {
      final int a = over.getAlpha();
      final int rb = (over.getRGB() & 0x00ff00ff) * (a + 1)
        + (origin.getRGB() & 0x00ff00ff) * (0xff - a) & 0xff00ff00;
      final int g = (over.getRGB() & 0x0000ff00) * (a + 1)
        + (origin.getRGB() & 0x0000ff00) * (0xff - a) & 0x00ff0000;
      return new Color(over.getRGB() & 0xff000000 | (rb | g) >> 8);
    }
  }

  static int colorToRGB(final int alpha, final int red, final int green, final int blue) {
    int rgba = alpha;
    rgba <<= 8;
    rgba += red;
    rgba <<= 8;
    rgba += green;
    rgba <<= 8;
    rgba += blue;
    return rgba;
  }

  static int fromHex(final String string, final int start, final int end, final int defaultValue) {
    if (end <= string.length()) {
      try {
        String text = string.substring(start, end);
        if (text.length() == 1) {
          text += text;
        }
        return Integer.decode("0x" + text);
      } catch (final NumberFormatException e) {

      }
    }
    return defaultValue;
  }

  static Color getColor(final CharSequence color) {
    int red = 0;
    int green = 0;
    int blue = 0;
    int opacity = 255;
    final String colorString = color.toString().trim();
    final int length = colorString.length();
    if (length > 1 && length < 6) {
      red = fromHex(colorString, 1, 2, 0);
      green = fromHex(colorString, 2, 3, red);
      blue = fromHex(colorString, 3, 4, green);
      opacity = fromHex(colorString, 4, 5, 255);
    } else if (length == 7) {
      red = fromHex(colorString, 1, 3, 0);
      green = fromHex(colorString, 4, 5, red);
      blue = fromHex(colorString, 5, 7, green);
      opacity = 255;
    } else if (length == 9) {
      red = fromHex(colorString, 1, 3, 0);
      green = fromHex(colorString, 4, 5, red);
      blue = fromHex(colorString, 5, 7, green);
      opacity = fromHex(colorString, 7, 9, 255);
    }
    return new Color(red, green, blue, opacity);
  }

  static Map<Color, String> getColorNames() {
    final Map<Color, String> colorNames = new HashMap<>();
    for (final Field field : WebColors.class.getFields()) {
      final int modifiers = field.getModifiers();
      if (Modifier.isStatic(modifiers)) {
        final Class<?> fieldClass = field.getType();
        if (Color.class.isAssignableFrom(fieldClass)) {
          try {
            final Color color = (Color)field.get(null);
            colorNames.put(color, field.getName());
          } catch (final Throwable e) {
            Logs.error(WebColors.class, "Unable to get field value: " + field, e);
          }
        }
      }
    }
    return Collections.unmodifiableMap(colorNames);
  }

  static String getName(final Color color) {
    final Color newColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
    final Map<Color, String> colorNames = getColorNames();
    return colorNames.get(newColor);
  }

  static Color getRgbaColor(final String string) {
    try {
      final String[] values = string.replaceAll("[^0-9,.]", "").split(",");
      final int red = Integer.valueOf(values[0]);
      final int green = Integer.valueOf(values[1]);
      final int blue = Integer.valueOf(values[2]);
      final int alpha = (int)(Double.valueOf(values[3]) * 255);
      final Color color = new Color(red, green, blue, alpha);
      return color;
    } catch (final Throwable e) {
      Logs.error(WebColors.class, "Not a valid rgba color " + string, e);
      return Color.BLACK;
    }
  }

  static Color getRgbColor(final String string) {
    try {
      final String[] values = string.replaceAll("[^0-9,]", "").split(",");
      final int red = Integer.valueOf(values[0]);
      final int green = Integer.valueOf(values[1]);
      final int blue = Integer.valueOf(values[2]);
      final Color color = new Color(red, green, blue, 255);
      return color;
    } catch (final Throwable e) {
      Logs.error(WebColors.class, "Not a valid rgb color " + string, e);
      return Color.BLACK;
    }
  }

  static Color getWebColor(final String colorName) {
    if (colorName != null) {
      for (final Field field : WebColors.class.getFields()) {
        final String fieldName = field.getName();
        if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
          if (fieldName.equalsIgnoreCase(colorName)) {
            try {
              return (Color)field.get(WebColors.class);
            } catch (final Throwable e) {
              Exceptions.throwUncheckedException(e);
            }
          }
        }
      }
    }
    return Color.BLACK;
  }

  static Color newAlpha(final Color color, final int alpha) {
    final int red = color.getRed();
    final int green = color.getGreen();
    final int blue = color.getBlue();
    return new Color(red, green, blue, alpha);
  }

  static Color toColor(final Object value) {
    if (value instanceof Color) {
      return (Color)value;
    }
    if (value != null) {
      final String string = DataTypes.toString(value);
      if (string != null) {
        if (string.startsWith("#")) {
          return getColor(string);
        } else if (string.startsWith("rgb(")) {
          return getRgbColor(string);
        } else if (string.startsWith("rgba(")) {
          return getRgbaColor(string);
        } else {
          final Color color = getWebColor(string);
          if (color != null) {
            return color;
          }

        }
      }
    }
    Logs.error(WebColors.class, "Not a valid color " + value);
    return Color.BLACK;
  }

  static String toHex(final Object value) {
    if (value == null) {
      return null;
    } else {
      final StringBuilder hex = new StringBuilder("#");
      final Color color = toColor(value);
      final int red = color.getRed();
      final String redHex = Integer.toHexString(red);
      if (redHex.length() == 1) {
        hex.append('0');
      }
      hex.append(redHex);
      final int green = color.getGreen();
      final String greenHex = Integer.toHexString(green);
      if (greenHex.length() == 1) {
        hex.append('0');
      }
      hex.append(greenHex);
      final int blue = color.getBlue();
      final String blueHex = Integer.toHexString(blue);
      if (blueHex.length() == 1) {
        hex.append('0');
      }
      hex.append(blueHex);

      return hex.toString();
    }
  }

  static String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Color color = toColor(value);
      final String colorName = getName(color);
      if (colorName != null) {
        return colorName;
      } else {
        final int alpha = color.getAlpha();
        if (alpha == 255) {
          return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
        } else {
          return "rgba(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ","
            + alpha / 255.0 + ")";
        }
      }
    }
  }
}
