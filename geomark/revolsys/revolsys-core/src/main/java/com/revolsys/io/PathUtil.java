package com.revolsys.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.util.Property;

public interface PathUtil {
  public static String clean(final String path) {
    if (path == null) {
      return null;
    } else {
      final StringBuilder builder = new StringBuilder(path.length());
      boolean slash = true;
      builder.append('/');
      int startIndex;
      for (startIndex = 0; startIndex < path.length(); startIndex++) {
        final char c = path.charAt(startIndex);
        if (!Character.isWhitespace(c)) {
          break;
        }
      }
      for (int i = startIndex; i < path.length(); i++) {
        final char c = path.charAt(i);
        switch (c) {
          case '/':
          case '\\':
            if (!slash) {
              builder.append('/');
              slash = true;
            }
          break;

          default:
            builder.append(c);
            slash = false;
          break;
        }

      }
      for (int length = builder.length(); length > 1; length = builder.length()) {
        final char c = builder.charAt(length - 1);
        if (c == '/' || Character.isWhitespace(c)) {
          builder.deleteCharAt(length - 1);
        } else {
          break;
        }
      }
      return builder.toString();
    }
  }

  public static String cleanUpper(final String path) {
    if (path != null) {
      final String cleanedPath = clean(path);
      return cleanedPath.toUpperCase();
    } else {
      return null;
    }
  }

  public static String getChildName(String parentPath, String childPath) {
    if (parentPath != null && childPath != null) {
      parentPath = cleanUpper(parentPath);
      childPath = clean(childPath);
      final String upperChild = childPath.toUpperCase();
      if (upperChild.length() > parentPath.length()) {
        if (parentPath.length() == 1) {
          final int nextIndex = childPath.indexOf('/', 1);
          if (nextIndex == -1) {
            return childPath.substring(1);
          } else {
            return childPath.substring(1, nextIndex);
          }
        } else if (childPath.charAt(parentPath.length()) == '/') {
          if (upperChild.startsWith(parentPath)) {
            final int nextIndex = childPath.indexOf('/', parentPath.length() + 1);
            if (nextIndex == -1) {
              return childPath.substring(parentPath.length() + 1);
            } else {
              return childPath.substring(parentPath.length() + 1, nextIndex);
            }
          }
        }
      }
    }
    return null;
  }

  public static String getChildPath(String parentPath, String childPath) {
    if (parentPath != null && childPath != null) {
      parentPath = cleanUpper(parentPath);
      childPath = clean(childPath);
      final String upperChild = childPath.toUpperCase();
      if (upperChild.length() > parentPath.length()) {
        if (parentPath.length() == 1) {
          final int nextIndex = childPath.indexOf('/', 1);
          if (nextIndex == -1) {
            return childPath.substring(0);
          } else {
            return childPath.substring(0, nextIndex);
          }
        } else if (childPath.charAt(parentPath.length()) == '/') {
          if (upperChild.startsWith(parentPath)) {
            final int nextIndex = childPath.indexOf('/', parentPath.length() + 1);
            if (nextIndex == -1) {
              return childPath.substring(0);
            } else {
              return childPath.substring(0, nextIndex);
            }
          }
        }
      }
    }
    return null;
  }

  public static String getName(final String path) {
    if (path == null) {
      return null;
    } else {
      final StringBuilder name = new StringBuilder();
      int startIndex = path.length();
      while (startIndex > 0) {
        final char c = path.charAt(startIndex - 1);
        if (c == '/' || c == '\\' || Character.isWhitespace(c)) {
          startIndex--;
        } else {
          break;
        }
      }
      while (startIndex > 0) {
        final char c = path.charAt(startIndex - 1);
        if (c == '/' || c == '\\') {
          break;
        } else {
          name.append(c);
          startIndex--;
        }
      }
      if (name.length() == 0) {
        return "/";
      } else {
        return name.reverse().toString();
      }
    }
  }

  public static String getPath(String path) {
    if (path == null) {
      return null;
    } else {
      path = clean(path);

      final int index = path.lastIndexOf('/');
      if (index <= 0) {
        return "/";
      } else {
        return path.substring(0, index);
      }
    }
  }

  public static List<String> getPathElements(final String path) {
    if (path == null) {
      return Collections.emptyList();
    } else if (path.equals("/")) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(path.replaceAll("^/*", "").split("/+"));
    }
  }

  public static List<String> getPaths(String path) {
    if (path == null) {
      return Collections.emptyList();
    } else {
      path = clean(path);
      if (path.length() == 1) {
        return Collections.singletonList(path);
      } else {
        final List<String> paths = new ArrayList<>();
        paths.add("/");
        int startIndex = 1;
        while (startIndex != -1) {
          startIndex = path.indexOf('/', startIndex);
          if (startIndex != -1) {
            paths.add(path.substring(0, startIndex));
            startIndex++;
          }
        }
        paths.add(path);
        return paths;
      }
    }
  }

  public static boolean isAncestor(String parentPath, String childPath) {
    if (parentPath != null && childPath != null) {
      parentPath = cleanUpper(parentPath);
      childPath = cleanUpper(childPath);
      if (childPath.length() > parentPath.length()) {
        if (parentPath.length() == 1 || childPath.charAt(parentPath.length()) == '/') {
          if (childPath.startsWith(parentPath)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean isParent(String parentPath, String childPath) {
    if (parentPath != null && childPath != null) {
      parentPath = cleanUpper(parentPath);
      childPath = cleanUpper(childPath);
      if (childPath.length() > parentPath.length()) {
        if (parentPath.length() == 1 || childPath.charAt(parentPath.length()) == '/') {
          if (childPath.startsWith(parentPath)) {
            if (childPath.indexOf('/', parentPath.length() + 1) == -1) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public static String toPath(final String... parts) {
    if (parts.length == 0) {
      return "/";
    } else {
      final StringBuilder path = new StringBuilder();
      for (String part : parts) {
        if (part != null) {
          part = part.replaceAll("^/*", "");
          part = part.replaceAll("/*", "");
          if (Property.hasValue(part)) {
            path.append('/');
            path.append(part);
          }
        }
      }
      return path.toString().replaceAll("/+", "/");
    }
  }
}
