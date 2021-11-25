package com.revolsys.util;

import java.io.PrintStream;
import java.lang.Character.UnicodeBlock;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

public interface Strings {

  String UPPERCASE_ASCII = "AEIOU" // grave
    + "AEIOUY" // acute
    + "AEIOUY" // circumflex
    + "AON" // tilde
    + "AEIOUY" // umlaut
    + "A" // ring
    + "C" // cedilla
    + "OU" // double acute
  ;

  String UPPERCASE_UNICODE = "\u00C0\u00C8\u00CC\u00D2\u00D9" //
    + "\u00C1\u00C9\u00CD\u00D3\u00DA\u00DD"//
    + "\u00C2\u00CA\u00CE\u00D4\u00DB\u0176"//
    + "\u00C3\u00D5\u00D1"//
    + "\u00C4\u00CB\u00CF\u00D6\u00DC\u0178"//
    + "\u00C5"//
    + "\u00C7"//
    + "\u0150\u0170"//
  ;

  static final Pattern InCombiningDiacriticalMarks = Pattern
    .compile("\\p{InCombiningDiacriticalMarks}+");

  static String cleanString(String text) {
    if (text == null) {
      return "";
    } else {
      text = normalizeNfc(text);
      int startIndex = 0;
      final int length = text.length();
      int endIndex = length;
      for (; startIndex < length && Character.isWhitespace(text.charAt(startIndex)); startIndex++) {
      }
      for (; endIndex > startIndex
        && Character.isWhitespace(text.charAt(endIndex - 1)); endIndex--) {
      }
      if (startIndex == endIndex) {
        return "";
      } else {
        StringBuilder result = null;
        if (startIndex > 0) {
          result = new StringBuilder();
        } else if (endIndex < length) {
          result = new StringBuilder();
        }
        for (int i = startIndex; i < endIndex; i++) {
          final char c = text.charAt(i);
          char replaceC = c;
          switch (c) {
            case '’':
            case '‘':
            case '‛':
              replaceC = '\'';
            break;
            case '“':
            case '”':
            case '‟':
              replaceC = '"';
            break;
            case '‐':
            case '‑':
            case '‒':
            case '–':
            case '—':
            case '―':
              replaceC = '-';
            default:
            break;
          }
          if (c == replaceC) {
            if (result != null) {
              result.append(c);
            }
          } else {
            if (result == null) {
              result = new StringBuilder(endIndex - startIndex);
              result.append(text, startIndex, i);
            }
            result.append(replaceC);
          }
        }
        if (result == null) {
          return text;
        } else {
          return result.toString();
        }
      }
    }

  }

  static String cleanWhitespace(final String string) {
    if (string == null) {
      return null;
    } else {
      int startIndex = 0;
      final int length = string.length();
      int endIndex = length;
      for (; startIndex < length
        && Character.isWhitespace(string.charAt(startIndex)); startIndex++) {
      }
      for (; endIndex > startIndex
        && Character.isWhitespace(string.charAt(endIndex - 1)); endIndex--) {
      }
      if (startIndex == endIndex) {
        return "";
      } else {
        final StringBuilder stringBuilder = new StringBuilder();
        boolean lastWasWhitespace = false;
        for (int i = startIndex; i < endIndex; i++) {
          final char character = string.charAt(i);
          if (Character.isWhitespace(character)) {
            if (!lastWasWhitespace) {
              lastWasWhitespace = true;
              stringBuilder.append(' ');
            }
          } else {
            stringBuilder.append(character);
            lastWasWhitespace = false;
          }

        }
        return stringBuilder.toString();
      }
    }
  }

  static boolean contains(final CharSequence text, final char character) {
    if (text != null) {
      final int length = text.length();
      for (int i = 0; i < length; i++) {
        final char currentCharacter = text.charAt(i);
        if (currentCharacter == character) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean contains(final String text, final String matchText) {
    if (text == null || matchText == null) {
      return false;
    } else {
      return text.contains(matchText);
    }
  }

  static boolean containsWord(final String text, final String matchWord) {
    if (text == null || matchWord == null) {
      return false;
    } else {
      int startIndex = 0;
      for (int endIndex = text.indexOf(' ', startIndex); endIndex != -1; endIndex = text
        .indexOf(' ', startIndex)) {
        if (equalSubstring(text, startIndex, endIndex, matchWord)) {
          return true;
        }
        startIndex = endIndex++;
        while (text.charAt(startIndex) == ' ') {
          startIndex++;
        }
      }
      return equalSubstring(text, startIndex, text.length(), matchWord);
    }
  }

  static boolean endsWith(final String text, final String suffix) {
    if (text != null && suffix != null) {
      return text.endsWith(suffix);
    } else {
      return false;
    }
  }

  static boolean equalExceptOneCharacter(final String string1, final String string2) {
    final int length1 = string1.length();
    if (length1 != string2.length()) {
      return false;
    } else {
      boolean equal = true;
      for (int i = 0; i < length1; ++i) {
        if (string1.charAt(i) != string2.charAt(i)) {
          if (equal) {
            equal = false;
          } else {
            return false;
          }
        }
      }
      return true;
    }
  }

  static boolean equalExceptOneExtraCharacter(final String string1, final String string2) {
    final int length1 = string1.length();
    final int length2 = string2.length();
    if (length1 == length2) {
      return string1.equals(string2);
    } else {
      if (length1 == length2 + 1) {
        return equalExceptOneExtraCharacter(string2, string1);
      }
      if (length2 == length1 + 1) {
        int startMatchCount = 0;
        for (int i = 0; i < length1; i++) {
          final char c1 = string1.charAt(i);
          final char c2 = string2.charAt(i);
          if (c1 == c2) {
            startMatchCount++;
          } else {
            break;
          }
        }
        int endMatchCount = 0;
        for (int i = 1; i <= length1 - startMatchCount; i++) {
          final char c1 = string1.charAt(length1 - i);
          final char c2 = string2.charAt(length2 - i);
          if (c1 == c2) {
            endMatchCount++;
          } else {
            break;
          }
        }
        return startMatchCount + endMatchCount == length1;
      } else {
        return false;
      }
    }
  }

  static boolean equals(final String string1, final String string2) {
    if (string1 == null) {
      return string2 == null;
    } else {
      return string1.equals(string2);
    }
  }

  static boolean equalsIgnoreCase(final String string1, final String string2) {
    if (Property.hasValue(string1)) {
      return string1.equalsIgnoreCase(string2);
    } else {
      return Property.isEmpty(string2);
    }
  }

  static boolean equalSubstring(final String text, final int startIndex, final int endIndex,
    final String searchText) {
    final int searchLength = searchText.length();
    if (searchLength == endIndex - startIndex) {
      return text.substring(startIndex, endIndex).equals(searchText);
    } else {
      return false;
    }
  }

  static String firstPart(final String text, final char character) {
    final int index = text.indexOf(character);
    if (index == -1) {
      return "";
    } else {
      return text.substring(0, index);
    }
  }

  static int indexOf(final CharSequence text, final char character) {
    if (text != null) {
      final int length = text.length();
      for (int i = 0; i < length; i++) {
        final char currentCharacter = text.charAt(i);
        if (currentCharacter == character) {
          return i;
        }
      }
    }
    return -1;
  }

  static boolean isEqualTrim(final String oldValue, final String newValue) {
    final boolean oldHasValue = Property.hasValue(oldValue);
    final boolean newHasValue = Property.hasValue(newValue);
    if (oldHasValue) {
      if (newHasValue) {
        if (DataType.equal(oldValue.trim(), newValue.trim())) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      if (newHasValue) {
        return false;
      } else {
        return true;
      }
    }
  }

  static String lastPart(final String text, final char character) {
    final int index = text.lastIndexOf(character);
    if (index == -1) {
      return "";
    } else {
      return text.substring(index + 1);
    }
  }

  static String lowerCase(final String text) {
    if (text == null) {
      return null;
    } else {
      return text.toLowerCase();
    }
  }

  static boolean matches(final String text, final String regex) {
    if (text == null || regex == null) {
      return false;
    } else {
      return text.matches(regex);
    }
  }

  // Normalize splitting chacters into base + combining mark
  static String normalize(final CharSequence text) {
    return Normalizer.normalize(text, Form.NFD);
  }

  // Normalize preffering combined characters
  static String normalizeNfc(final CharSequence text) {
    return Normalizer.normalize(text, Form.NFC);
  }

  static String normalizeNfd(final CharSequence text) {
    return Normalizer.normalize(text, Form.NFD);
  }

  public static String normalizeToUsAscii(final CharSequence cs) {
    if (cs == null) {
      return null;
    } else {
      final String s = Normalizer.normalize(cs, Normalizer.Form.NFD);
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c == '\u0141') {
          c = 'L';
        } else if (c == '\u0142') {
          c = 'l';
        } else {
          final UnicodeBlock block = UnicodeBlock.of(c);
          if (block == UnicodeBlock.COMBINING_DIACRITICAL_MARKS
            || block == UnicodeBlock.COMBINING_DIACRITICAL_MARKS_EXTENDED
            || block == UnicodeBlock.COMBINING_DIACRITICAL_MARKS_SUPPLEMENT) {
          } else if (' ' <= c && c <= '~') {
            // US ASCII Visible characters
            sb.append(c);
          } else {
            sb.append('_');
          }
        }
      }
      return sb.toString();
    }
  }

  static void print(final PrintStream out, final char separator, final Object... values) {
    if (values != null) {
      boolean first = true;
      for (final Object value : values) {
        if (first) {
          first = false;
        } else {
          out.print(separator);
        }
        if (value == null) {
        } else {
          final String string = DataTypes.toString(value);
          if (Property.hasValue(string)) {
            out.print(string);
          }
        }
      }
    }
    out.println();
  }

  static void printErr(final char separator, final Object... values) {
    print(System.err, separator, values);
  }

  static void printOut(final char separator, final Object... values) {
    print(System.out, separator, values);
  }

  static String removeFromEnd(String fullAddress, final int len) {
    final int endIndex = fullAddress.length() - len;
    fullAddress = fullAddress.substring(0, endIndex);
    return fullAddress;
  }

  static String replace(final String text, final String from, final String to) {
    if (text == null) {
      return null;
    } else {
      return text.replace(from, to);
    }
  }

  static String replaceAll(String value, final Pattern pattern, final String replacement) {
    final Matcher matcher = pattern.matcher(value);
    if (matcher.find()) {
      final StringBuilder sb = new StringBuilder();
      do {
        matcher.appendReplacement(sb, replacement);
      } while (matcher.find());
      matcher.appendTail(sb);
      value = sb.toString();
    }
    return value;
  }

  static String replaceAll(final String text, final String from, final Object to) {
    if (text == null) {
      return null;
    } else {
      String toText;
      if (to == null) {
        toText = "";
      } else {
        toText = to.toString();
      }
      return text.replaceAll(from, toText);
    }
  }

  static String replaceAll(final String text, final String from, final String to) {
    if (text == null) {
      return null;
    } else {
      return text.replaceAll(from, to);
    }
  }

  static String replaceWord(final String text, final String oldValue, final String newValue,
    final char... separators) {
    if (Property.hasValue(oldValue)) {
      if (Property.hasValue(text)) {
        for (int index = text.indexOf(oldValue); index != -1; index = text.indexOf(text, index)) {
          if (index == 0 || Arrays.binarySearch(separators, text.charAt(index - 1)) != -1) {
            final int nextIndex = index + oldValue.length();
            if (nextIndex == text.length()
              || Arrays.binarySearch(separators, text.charAt(nextIndex)) != -1) {
              final StringBuilder newText = new StringBuilder();
              final boolean hasNewValue = Property.hasValue(newValue);
              if (index > 0) {
                String prefix;
                if (hasNewValue) {
                  prefix = text.substring(0, index);
                } else {
                  prefix = text.substring(0, index - 1);
                }
                newText.append(prefix);
              }
              if (hasNewValue) {
                newText.append(newValue);
              }
              if (nextIndex < text.length()) {
                if (hasNewValue) {
                  newText.append(text.substring(nextIndex));
                } else {
                  if (index > 0) {
                    newText.append(' ');
                  }
                  newText.append(text.substring(nextIndex + 1));
                }
              }
              return newText.toString();
            }
          }
        }
      }
    }
    return text;
  }

  static boolean startsWith(final String text, final String prefix) {
    if (text != null && prefix != null) {
      return text.startsWith(prefix);
    } else {
      return false;
    }
  }

  public static String stripAccents(String s) {
    if (s == null) {
      return null;
    } else {
      s = Normalizer.normalize(s, Normalizer.Form.NFD);
      final StringBuilder sb = new StringBuilder(s);
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c == '\u0141') {
          c = 'L';
        } else if (c == '\u0142') {
          c = 'l';
        } else {
          final UnicodeBlock block = UnicodeBlock.of(c);
          if (block == UnicodeBlock.COMBINING_DIACRITICAL_MARKS
            || block == UnicodeBlock.COMBINING_DIACRITICAL_MARKS_EXTENDED
            || block == UnicodeBlock.COMBINING_DIACRITICAL_MARKS_SUPPLEMENT) {
          } else {
            sb.append(c);
          }
        }
      }
      return sb.toString();
    }
  }

  static String substring(final String text, final char character, final int toIndex) {
    int startIndex = 0;
    for (int i = 0; i < toIndex && startIndex != -1; i++) {
      final int index = text.indexOf(character, startIndex);
      if (index == -1) {
        return "";
      }
      startIndex = index + 1;
    }
    if (startIndex == -1) {
      return text;
    } else {
      return text.substring(startIndex);
    }
  }

  static String substring(final String text, final char character, final int fromIndex,
    final int toIndex) {
    if (fromIndex < 0) {
      throw new StringIndexOutOfBoundsException(fromIndex);
    } else if (toIndex < 0) {
      throw new StringIndexOutOfBoundsException(toIndex);
    }
    int startIndex = 0;
    for (int i = 0; i < fromIndex && startIndex != -1; i++) {
      final int index = text.indexOf(character, startIndex);
      if (index == -1) {
        return "";
      }
      startIndex = index + 1;
    }
    int endIndex = startIndex;
    for (int i = fromIndex; i < toIndex && endIndex != -1; i++) {
      if (i > fromIndex) {
        endIndex++;
      }
      final int index = text.indexOf(character, endIndex);
      if (index == -1) {
        return text.substring(startIndex);
      } else {
        endIndex = index;
      }
    }
    if (endIndex == -1) {
      return "";
    } else {
      return text.substring(startIndex, endIndex);
    }
  }

  /**
   * Construct a new string using the same style as java.util.List.toString.
   * @param iterator
   * @return
   */
  static String toListString(final Iterable<? extends Object> iterable) {
    if (iterable == null) {
      return "[]";
    } else {
      final Iterator<? extends Object> iterator = iterable.iterator();
      return toListString(iterator);
    }
  }

  static String toListString(final Iterator<? extends Object> iterator) {
    if (iterator == null) {
      return "[]";
    } else {
      final StringBuilder string = new StringBuilder("[");
      if (iterator.hasNext()) {
        string.append(iterator.next());
        while (iterator.hasNext()) {
          string.append(", ");
          string.append(iterator.next());
        }
      }
      string.append("]");
      return string.toString();
    }
  }

  static String toString(final boolean skipNulls, final String separator,
    final Collection<? extends Object> values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      StringBuilders.append(string, values, skipNulls, separator);
      return string.toString();
    }
  }

  static String toString(final boolean skipNulls, final String separator, final Object... values) {
    return toString(skipNulls, separator, Arrays.asList(values));
  }

  /**
   * Convert the collection to a string, using the "," separator between each
   * value. Nulls will be the empty string "".
   *
   * @param values The values.
   * @param separator The separator.
   * @return The string.
   */
  static String toString(final Collection<? extends Object> values) {
    return toString(",", values);
  }

  static String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

  /**
   * Convert the collection to a string, using the separator between each value.
   * Nulls will be the empty string "".
   *
   * @param separator The separator.
   * @param values The values.
   * @return The string.
   */
  static String toString(final String separator, final Collection<? extends Object> values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      StringBuilders.append(string, values, separator);
      return string.toString();
    }
  }

  static String toString(final String separator, final int... values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      boolean first = true;
      for (final int value : values) {
        if (first) {
          first = false;
        } else {
          string.append(separator);
        }
        string.append(value);
      }
      return string.toString();
    }
  }

  static String toString(final String separator, final Object... values) {
    if (values == null) {
      return null;
    } else {
      StringBuilder stringBuilder = null;
      String string = null;
      for (final Object value : values) {
        if (value != null) {
          final String newString = DataTypes.toString(value);
          if (Property.hasValue(newString)) {
            if (stringBuilder == null) {
              if (string == null) {
                string = newString;
              } else {
                stringBuilder = new StringBuilder(string);
                stringBuilder.append(separator);
                stringBuilder.append(newString);
              }
            } else {
              stringBuilder.append(separator);
              stringBuilder.append(newString);
            }

          }
        }
      }
      if (stringBuilder == null) {
        return string;
      } else {
        return stringBuilder.toString();
      }
    }
  }

  static String toString(final String separator, final String... values) {
    return toString(separator, Arrays.asList(values));
  }

  static List<String> toStringList(final Collection<?> values) {
    final List<String> strings = new ArrayList<>();
    if (values != null) {
      for (final Object value : values) {
        strings.add(DataTypes.toString(value));
      }
    }
    return strings;
  }

  static String toUpperCaseSansAccent(final String text) {
    if (text == null) {
      return null;
    } else {
      final String txtUpper = text.toUpperCase();
      final StringBuilder sb = new StringBuilder();
      final int n = txtUpper.length();
      for (int i = 0; i < n; i++) {
        final char c = txtUpper.charAt(i);
        final int pos = UPPERCASE_UNICODE.indexOf(c);
        if (pos > -1) {
          sb.append(UPPERCASE_ASCII.charAt(pos));
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }
  }

  static String trim(final String text) {
    if (text == null) {
      return null;
    } else {
      return text.trim();
    }
  }

  static int trimLength(final String text) {
    if (text == null) {
      return 0;
    } else {
      return text.trim().length();
    }
  }

  static String upperCase(final String text) {
    if (text == null) {
      return null;
    } else {
      return text.toUpperCase();
    }
  }
}
