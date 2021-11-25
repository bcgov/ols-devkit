package com.revolsys.geometry.util;

import com.revolsys.geometry.model.AboveBelow;
import com.revolsys.geometry.model.Side;

public enum OutCode {
  LEFT_TOP(Side.LEFT, AboveBelow.ABOVE), //
  TOP(Side.ON, AboveBelow.ABOVE), //
  RIGHT_TOP(Side.RIGHT, AboveBelow.ABOVE), //
  LEFT(Side.LEFT, AboveBelow.ON), //
  INSIDE(Side.ON, AboveBelow.ON), //
  RIGHT(Side.RIGHT, AboveBelow.ON), //
  LEFT_BOTTOM(Side.LEFT, AboveBelow.BELOW), //
  BOTTOM(Side.ON, AboveBelow.BELOW), //
  RIGHT_BOTTOM(Side.RIGHT, AboveBelow.BELOW), //
  ;

  /** Bit flag indicating a point is to the left of a rectangle. */
  public static final int OUT_LEFT = 1; // 0001 Wikipedia value, others have
                                        // different bit orders

  /** Bit flag indicating a point is to the right of a rectangle. */
  public static final int OUT_RIGHT = 2; // 0010 Wikipedia value, others have
                                         // different bit orders

  /** Bit flag indicating a point is below a rectangle. */
  public static final int OUT_BOTTOM = 4; // 0100 Wikipedia value, others have
                                          // different bit orders

  /** Bit flag indicating a point is above a rectangle. */
  public static final int OUT_TOP = 8; // 1000 Wikipedia value, others have
                                       // different bit orders

  public static int getOutcode(final double minX, final double minY, final double maxX,
    final double maxY, final double x, final double y) {
    int out = 0;
    if (x < minX) {
      out = OUT_LEFT;
    } else if (x > maxX) {
      out = OUT_RIGHT;
    }
    if (y < minY) {
      out |= OUT_BOTTOM;
    } else if (y > maxY) {
      out |= OUT_TOP;
    }
    return out;
  }

  public static boolean isBottom(final int outCode) {
    return (outCode & OUT_BOTTOM) != 0;
  }

  public static boolean isInside(final int outCode) {
    return outCode == 0;
  }

  public static boolean isLeft(final int outCode) {
    return (outCode & OUT_LEFT) != 0;
  }

  public static boolean isRight(final int outCode) {
    return (outCode & OUT_RIGHT) != 0;
  }

  public static boolean isTop(final int outCode) {
    return (outCode & OUT_TOP) != 0;
  }

  public static OutCode outcode(final double minX, final double minY, final double maxX,
    final double maxY, final double x, final double y) {
    if (x < minX) {
      if (y < minY) {
        return LEFT_BOTTOM;
      } else if (y > maxY) {
        return LEFT_TOP;
      } else {
        return LEFT;
      }
    } else if (x > maxX) {
      if (y < minY) {
        return RIGHT_BOTTOM;
      } else if (y > maxY) {
        return RIGHT_TOP;
      } else {
        return RIGHT;
      }

    } else {
      if (y < minY) {
        return BOTTOM;
      } else if (y > maxY) {
        return TOP;
      } else {
        return INSIDE;
      }

    }
  }

  private boolean left;

  private boolean right;

  private boolean top;

  private boolean bottom;

  private boolean inside;

  private int code;

  private OutCode(final Side side, final AboveBelow aboveBelow) {
    this.left = side.isLeft();
    this.right = side.isRight();
    this.top = aboveBelow.isAbove();
    this.bottom = aboveBelow.isBelow();
    this.inside = aboveBelow.isOn() && side.isOn();
    int code = 0;
    if (this.left) {
      code |= OUT_LEFT;
    } else if (this.right) {
      code |= OUT_RIGHT;
    }
    if (this.top) {
      code |= OUT_TOP;
    } else if (this.bottom) {
      code |= OUT_BOTTOM;
    }
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }

  public boolean isBottom() {
    return this.bottom;
  }

  public boolean isInside() {
    return this.inside;
  }

  public boolean isLeft() {
    return this.left;
  }

  public boolean isOutside() {
    return !this.inside;
  }

  public boolean isRight() {
    return this.right;
  }

  public boolean isTop() {
    return this.top;
  }

  public boolean isX(final OutCode out) {
    return (this.code & out.code) != 0;
  }

}
