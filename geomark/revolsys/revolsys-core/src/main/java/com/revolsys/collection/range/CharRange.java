package com.revolsys.collection.range;

/**
 *
 * Ranges are immutable
 */
public class CharRange extends AbstractRange<Character> {
  public static boolean isLower(final char character) {
    return character >= 'a' && character <= 'z';
  }

  public static boolean isLowerOrUpper(final char character) {
    return isLower(character) || isUpper(character);
  }

  public static boolean isUpper(final char character) {
    return character >= 'A' && character <= 'Z';
  }

  private char from;

  private char to;

  protected CharRange(final char value) {
    this(value, value);
  }

  protected CharRange(final char from, final char to) {
    if (isLower(from)) {
      if (isLower(to)) {
      } else if (isUpper(to)) {
        throw new RangeInvalidException("Cannot mix lower and upper case " + from + "~" + to
          + "  must both between either a~z or A~Z");
      } else {
        throw new RangeInvalidException(from + "~" + to + " are not both between a~z or A~Z");
      }
    } else if (isUpper(from)) {
      if (isUpper(to)) {
      } else if (isLower(to)) {
        throw new RangeInvalidException("Cannot mix lower and upper case " + from + "~" + to
          + "  must both between either a~z or A~Z");
      } else {
        throw new IllegalArgumentException(from + "~" + to + " are not both between a~z or A~Z");
      }
    } else {
      throw new RangeInvalidException(from + "~" + to + " are not both between a~z or A~Z");
    }
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    if (value instanceof Character) {
      final Character character = (Character)value;
      return super.expand(character);
    } else if (value instanceof String) {
      final String string = (String)value;
      if (string.length() == 1) {
        final char character = string.charAt(0);
        return super.expand(character);
      }
    }
    return null;
  }

  @Override
  public Character getFrom() {
    return this.from;
  }

  @Override
  public Character getTo() {
    return this.to;
  }

  @Override
  protected AbstractRange<?> newRange(final Object from, final Object to) {
    return Ranges.newRange(((Character)from).charValue(), ((Character)to).charValue());
  }

  @Override
  public Character next(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Character) {
      final char character = (Character)value;
      if ('Z' == character || 'z' == character || !isLowerOrUpper(character)) {
        return null;
      } else {
        return (char)(character + 1);
      }
    } else {
      return null;
    }
  }

  @Override
  public Character previous(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Character) {
      final char character = (Character)value;
      if ('A' == character || 'a' == character || !isLowerOrUpper(character)) {
        return null;
      } else {
        return (char)(character - 1);
      }
    } else {
      return null;
    }
  }

  @Override
  public long size() {
    return this.to - this.from + 1;
  }
}
