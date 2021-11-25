package com.revolsys.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class CaseConverter {
  public static final String LOWER_CAMEL_CASE_RE = "";

  private static void addWord(final String text, final List<String> list, final int tokenStart) {
    final String lastWord = text.substring(tokenStart);
    list.add(lastWord);
  }

  private static int addWordCharacter(final String text, final List<String> list, int tokenStart,
    final int pos) {
    final int newTokenStart = pos - 1;
    if (newTokenStart != tokenStart) {
      if (tokenStart < newTokenStart) {
        list.add(text.substring(tokenStart, newTokenStart));
      }
      tokenStart = newTokenStart;
    }
    return tokenStart;
  }

  private static int addWordSeparator(final String text, final List<String> list, int tokenStart,
    final int pos) {
    if (tokenStart < pos) {
      list.add(text.substring(tokenStart, pos));
    }
    tokenStart = pos + 1;
    return tokenStart;
  }

  public static String captialize(final String text) {
    final char firstChar = text.charAt(0);
    return Character.toUpperCase(firstChar) + text.substring(1).toLowerCase();
  }

  public static List<String> splitWords(final String text) {
    if (text == null) {
      return Collections.emptyList();
    } else {
      final int length = text.length();
      if (length == 0) {
        return Collections.emptyList();
      } else {
        final List<String> list = new ArrayList<>();
        int currentType = Character.getType(text.charAt(0));
        int tokenStart = 0;
        for (int pos = tokenStart + 1; pos < length; pos++) {
          final char character = text.charAt(pos);
          final boolean separator = Character.isWhitespace(character) || character == '_';
          final int type = Character.getType(character);
          if (type == currentType) {
            if (separator) {
              tokenStart = pos + 1;
            } else {
              continue;
            }
          } else if (separator) {
            tokenStart = addWordSeparator(text, list, tokenStart, pos);
          } else if (type == Character.LOWERCASE_LETTER
            && currentType == Character.UPPERCASE_LETTER) {
            tokenStart = addWordCharacter(text, list, tokenStart, pos);
          } else {
            if (tokenStart != pos) {
              list.add(text.substring(tokenStart, pos));
            }
            tokenStart = pos;
          }
          currentType = type;
        }
        if (tokenStart < length) {
          addWord(text, list, tokenStart);
        }
        return list;
      }
    }
  }

  public static String toCapitalizedWords(final String text) {
    final List<String> words = splitWords(text);
    final StringBuilder result = new StringBuilder();
    for (final Iterator<String> iter = words.iterator(); iter.hasNext();) {
      final String word = iter.next();
      result.append(captialize(word));
      if (iter.hasNext()) {
        result.append(" ");
      }
    }
    return result.toString();
  }

  public static String toLowerCamelCase(final String text) {
    final List<String> words = splitWords(text);
    if (words.size() == 0) {
      return "";
    } else if (words.size() == 1) {
      return words.get(0).toLowerCase();
    } else {
      final StringBuilder result = new StringBuilder();
      final Iterator<String> iter = words.iterator();
      result.append(iter.next().toLowerCase());
      while (iter.hasNext()) {
        final String word = iter.next();
        result.append(captialize(word));
      }
      return result.toString();
    }
  }

  public static String toLowerFirstChar(final String text) {
    if (text.length() > 0) {
      final char c = text.charAt(0);
      return Character.toLowerCase(c) + text.substring(1);
    } else {
      return text;
    }
  }

  public static String toLowerUnderscore(final String text) {
    final List<String> words = splitWords(text);
    final StringBuilder result = new StringBuilder();
    for (final Iterator<String> iter = words.iterator(); iter.hasNext();) {
      final String word = iter.next();
      result.append(word.toLowerCase());
      if (iter.hasNext()) {
        result.append("_");
      }
    }
    return result.toString();
  }

  public static String toSentence(final String text) {
    final List<String> words = splitWords(text);
    if (words.size() == 0) {
      return "";
    } else if (words.size() == 1) {
      return captialize(words.get(0));
    } else {
      final StringBuilder result = new StringBuilder();
      final Iterator<String> iter = words.iterator();
      result.append(captialize(iter.next()));
      while (iter.hasNext()) {
        final String word = iter.next();
        result.append(word.toLowerCase());
        if (iter.hasNext()) {
          result.append(" ");
        }
      }
      return result.toString();
    }
  }

  public static String toUpperCamelCase(final String text) {
    final List<String> words = splitWords(text);
    final StringBuilder result = new StringBuilder();
    for (final String word : words) {
      result.append(captialize(word));
    }
    return result.toString();
  }

  public static String toUpperFirstChar(final String text) {
    if (text.length() > 0) {
      final char c = text.charAt(0);
      return Character.toUpperCase(c) + text.substring(1);
    } else {
      return text;
    }
  }

  public static String toUpperUnderscore(final String text) {
    final List<String> words = splitWords(text);
    final StringBuilder result = new StringBuilder();
    for (final Iterator<String> iter = words.iterator(); iter.hasNext();) {
      final String word = iter.next();
      result.append(word.toUpperCase());
      if (iter.hasNext()) {
        result.append("_");
      }
    }
    return result.toString();
  }

  private CaseConverter() {
  }
}
