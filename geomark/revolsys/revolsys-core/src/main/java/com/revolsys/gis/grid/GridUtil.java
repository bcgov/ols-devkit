package com.revolsys.gis.grid;

public class GridUtil {
  private static final int HUNDREDTH_GRID_SQUARE = 10;

  private static final int QUARTER_GRID_SQUARE = 2;

  public static final int SIXTEENTH_GRID_SQUARE = 4;

  public static final int TWELTH_GRID_HEIGHT = 3;

  public static String formatSheetNumber100(final int sheetNumber) {
    if (sheetNumber < 10) {
      return "00" + sheetNumber;
    } else if (sheetNumber < 100) {
      return "0" + sheetNumber;
    } else {
      return String.valueOf(sheetNumber);
    }
  }

  public static char getLetter12(final double x, final double y, final double width,
    final double height) {
    final double xSheet = (x + 180) / width;
    final int col = 3 - (int)(Math.floor(xSheet - 0.00000000001) % SIXTEENTH_GRID_SQUARE);

    final double ySheet = (y + 90) / height;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % TWELTH_GRID_HEIGHT;

    if (row % 2 == 0) {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + col);
    } else {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + 3 - col);
    }
  }

  public static char getLetter16(final double x, final double y, final double width,
    final double height) {
    final double xSheet = (x + 180) / width;
    final int col = (int)(Math.ceil(xSheet - 0.00000000001) % SIXTEENTH_GRID_SQUARE);

    final double ySheet = (y + 90) / height;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % SIXTEENTH_GRID_SQUARE;

    if (row % 2 == 0) {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + col);
    } else {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + 3 - col);
    }
  }

  public static char getLetter16(final int row, final int col) {
    if (row % 2 == 0) {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + col);
    } else {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + 3 - col);
    }
  }

  public static int getLetter16Col(final char c) {
    final int num = Character.toLowerCase(c) - 'a';
    final int lineNumber = getLetter16Row(c);
    if (lineNumber % 2 == 0) {
      return num % SIXTEENTH_GRID_SQUARE;
    } else {
      return 3 - num % SIXTEENTH_GRID_SQUARE;
    }
  }

  public static int getLetter16Row(final char c) {
    final int num = Character.toLowerCase(c) - 'a';
    return num / SIXTEENTH_GRID_SQUARE;
  }

  public static char getLetter4(final double x, final double y, final double width,
    final double height) {
    final double xSheet = (x + 180) / width;
    final int col = (int)(Math.ceil(xSheet - 0.00000000001) % QUARTER_GRID_SQUARE);

    final double ySheet = (y + 90) / height;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % QUARTER_GRID_SQUARE;

    if (row % 2 == 0) {
      return (char)('a' + row * QUARTER_GRID_SQUARE + col);
    } else {
      return (char)('a' + row * QUARTER_GRID_SQUARE + 1 - col);
    }
  }

  public static int getLetter4Col(final char c) {
    switch (Character.toLowerCase(c)) {
      case 'a':
      case 'd':
        return 0;
      case 'b':
      case 'c':
        return 1;
      default:
        return 0;
    }
  }

  public static int getLetter4Row(final char c) {
    final int num = Character.toLowerCase(c) - 'a';
    return num / QUARTER_GRID_SQUARE;
  }

  public static char getLetter8(final int row, final int col) {
    if (row % 2 == 0) {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + col);
    } else {
      return (char)('a' + row * SIXTEENTH_GRID_SQUARE + 3 - col);
    }
  }

  public static int getLetter8Col(final char c) {
    final int num = Character.toLowerCase(c) - 'a';
    final int lineNumber = getLetter16Row(c);
    if (lineNumber % 2 == 0) {
      return num % SIXTEENTH_GRID_SQUARE;
    } else {
      return 3 - num % SIXTEENTH_GRID_SQUARE;
    }
  }

  public static int getLetter8Row(final char c) {
    final int num = Character.toLowerCase(c) - 'a';
    return num / SIXTEENTH_GRID_SQUARE;
  }

  public static String getNumber100(final double x, final double y, final double width,
    final double height) {
    final double xSheet = (x + 180) / width;
    final int col = (int)(Math.ceil(xSheet - 0.00000000001 - 1) % HUNDREDTH_GRID_SQUARE);

    final double ySheet = (y + 90) / height;
    final int row = (int)Math.floor(ySheet + 0.00000000001) % HUNDREDTH_GRID_SQUARE;

    final int sheetNumber = row * HUNDREDTH_GRID_SQUARE + col + 1;
    return formatSheetNumber100(sheetNumber);
  }

  public static String getNumber100(final int row, final int col) {
    final int sheetNumber = row * HUNDREDTH_GRID_SQUARE + col + 1;
    return formatSheetNumber100(sheetNumber);
  }

  public static int getNumber16(final int row, final int col) {
    if (row % 2 == 0) {
      return 1 + row * SIXTEENTH_GRID_SQUARE + col;
    } else {
      return 1 + row * SIXTEENTH_GRID_SQUARE + 3 - col;
    }
  }

  public static String getNumber4(final int row, final int col) {
    final int sheetNumber = row * QUARTER_GRID_SQUARE + col + 1;
    return String.valueOf(sheetNumber);
  }

  public static int getNumberCol100(final int sheetNumber) {
    final int col = 9 - (sheetNumber - 1) % HUNDREDTH_GRID_SQUARE;
    return col;
  }

  public static int getNumberCol100(final String sheetNumber) {
    return getNumberCol100(Integer.parseInt(sheetNumber));
  }

  public static int getNumberCol16(final int num) {
    final int lineNumber = getNumberRow16(num);
    if (lineNumber % 2 == 0) {
      return (num - 1) % SIXTEENTH_GRID_SQUARE;
    } else {
      return 3 - (num - 1) % SIXTEENTH_GRID_SQUARE;
    }
  }

  public static int getNumberCol4(final int num) {
    return (num - 1) % QUARTER_GRID_SQUARE;
  }

  public static int getNumberCol4(final String num) {
    return getNumberCol4(Integer.parseInt(num));
  }

  public static int getNumberRow100(final int sheetNumber) {
    return (sheetNumber - 1) / HUNDREDTH_GRID_SQUARE;
  }

  public static int getNumberRow100(final String sheetNumber) {
    return getNumberRow100(Integer.parseInt(sheetNumber));
  }

  public static int getNumberRow16(final int num) {
    return (num - 1) / SIXTEENTH_GRID_SQUARE;
  }

  public static int getNumberRow4(final int num) {
    return (num - 1) / QUARTER_GRID_SQUARE;
  }

  public static int getNumberRow4(final String num) {
    return getNumberRow4(Integer.parseInt(num));
  }
}
