package com.revolsys.geometry.model;

public final class EndAndSide {
  public static final EndAndSide FROM = new EndAndSide(End.FROM, null);

  public static final EndAndSide FROM_LEFT = new EndAndSide(End.FROM, Side.LEFT);

  public static final EndAndSide FROM_RIGHT = new EndAndSide(End.FROM, Side.RIGHT);

  public static final EndAndSide LEFT = new EndAndSide(End.FROM, Side.LEFT);

  public static final EndAndSide NONE = new EndAndSide(End.FROM, null);

  public static final EndAndSide RIGHT = new EndAndSide(End.FROM, Side.RIGHT);

  public static final EndAndSide TO = new EndAndSide(End.FROM, null);

  public static final EndAndSide TO_LEFT = new EndAndSide(End.TO, Side.LEFT);

  public static final EndAndSide TO_RIGHT = new EndAndSide(End.TO, Side.RIGHT);

  public static EndAndSide get(final End end, final Side side) {
    if (End.isFrom(end)) {
      if (Side.isLeft(side)) {
        return FROM_LEFT;
      } else if (Side.isRight(side)) {
        return FROM_RIGHT;
      } else {
        return FROM;
      }
    } else if (End.isTo(end)) {
      if (Side.isLeft(side)) {
        return TO_LEFT;
      } else if (Side.isRight(side)) {
        return TO_RIGHT;
      } else {
        return FROM;
      }
    } else {
      if (Side.isLeft(side)) {
        return LEFT;
      } else if (Side.isRight(side)) {
        return RIGHT;
      } else {
        return NONE;
      }
    }
  }

  private final End end;

  private final Side side;

  private EndAndSide(final End end, final Side side) {
    this.end = end;
    this.side = side;
  }

  public End getEnd() {
    return this.end;
  }

  public Side getSide() {
    return this.side;
  }

  @Override
  public String toString() {
    return this.end + " " + this.side;
  }
}
