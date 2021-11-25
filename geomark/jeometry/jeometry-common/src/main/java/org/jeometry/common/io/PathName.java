package org.jeometry.common.io;

import java.util.LinkedList;
import java.util.List;

public final class PathName implements Comparable<PathName>, CharSequence {
  public static final PathName ROOT = new PathName("/");

  private static String clean(final String path) {
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

  private static String getName(final String path) {
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

  public static PathName newPathName(final Object path) {
    if (path instanceof PathName) {
      return (PathName)path;
    } else if (path != null) {
      String pathString = path.toString();
      pathString = clean(pathString);
      if ("/".equals(pathString)) {
        return ROOT;
      } else if (pathString != null) {
        return new PathName(pathString);
      }
    }
    return null;
  }

  private final String name;

  private PathName parent;

  private final String path;

  private final String upperPath;

  protected PathName(final String path) {
    this.path = path;
    this.upperPath = path.toUpperCase();
    this.name = getName(path);
  }

  @Override
  public char charAt(final int index) {
    return this.path.charAt(index);
  }

  @Override
  public int compareTo(final PathName pathName) {
    return getUpperPath().compareTo(pathName.getUpperPath());
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof PathName) {
      return equals((PathName)object);
    } else {
      final String upperPath = object.toString().toUpperCase();
      return this.upperPath.equals(upperPath);
    }
  }

  public boolean equals(final PathName pathName) {
    if (pathName != null) {
      return this.upperPath.equals(pathName.upperPath);
    }
    return false;
  }

  public boolean equalsPath(final String path) {
    if (path == null) {
      return false;
    } else {
      final String upperPath = getUpperPath();
      final String upperPath2 = path.toUpperCase();
      return upperPath.equals(upperPath2);
    }
  }

  /**
   * If this path is an ancestor of the other path return the path that is a direct child of this path.
   * Returns null if this path is not an ancestor of the current path.
   *
   * @param path The path to test.
   * @return True if this path is an ancestor of the other path.
   */
  public PathName getChild(PathName path) {
    if (path != null) {
      for (PathName parentPath = path.getParent(); parentPath != null; parentPath = parentPath
        .getParent()) {
        if (equals(parentPath)) {
          return path;
        }
        path = parentPath;
      }
    }
    return null;
  }

  public int getElementCount() {
    if (this.parent == null) {
      return 1;
    } else {
      return 1 + this.parent.getElementCount();
    }
  }

  public List<String> getElements() {
    final LinkedList<String> elements = new LinkedList<>();
    PathName currentPath = this;
    for (PathName parentPath = getParent(); parentPath != null; parentPath = parentPath
      .getParent()) {
      elements.addFirst(currentPath.getName());
      currentPath = parentPath;
    }
    return elements;
  }

  public PathName getLastElement() {
    return newPathName(this.name);
  }

  public String getName() {
    return this.name;
  }

  public PathName getNamePath() {
    if (this.name.length() == 0) {
      return null;
    } else {
      return new PathName("/" + this.name);
    }
  }

  public PathName getParent() {
    if (this.parent == null && this.path.length() > 1) {
      final String parentPath = getParentPath();
      this.parent = newPathName(parentPath);
    }
    return this.parent;
  }

  public String getParentPath() {
    if (this.path == null) {
      return null;
    } else if (this.path.length() > 1) {
      final int index = this.path.lastIndexOf('/');
      if (index == 0) {
        return "/";
      } else {
        return this.path.substring(0, index);
      }
    } else {
      return null;
    }
  }

  public String getPath() {
    return this.path;
  }

  public List<PathName> getPaths() {
    final LinkedList<PathName> elements = new LinkedList<>();
    elements.add(this);
    for (PathName parentPath = getParent(); parentPath != null; parentPath = parentPath
      .getParent()) {
      elements.addFirst(parentPath);
    }
    return elements;
  }

  public String getUpperPath() {
    return this.upperPath;
  }

  @Override
  public int hashCode() {
    return this.upperPath.hashCode();
  }

  /**
   * Test if that this path is an ancestor of the other path.
   *
   * @param path The path to test.
   * @return True if this path is an ancestor of the other path.
   */
  public boolean isAncestorOf(final PathName path) {
    if (path != null) {
      for (PathName parentPath = path.getParent(); parentPath != null; parentPath = parentPath
        .getParent()) {
        if (equals(parentPath)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Test if that this path is a child of the other path.
   *
   * @param path The path to test.
   * @return True if this path is a child of the other path.
   */
  public boolean isChildOf(final PathName path) {
    if (path != null) {
      final PathName parent = getParent();
      return path.equals(parent);
    }
    return false;
  }

  /**
   * Test if that this path is an descendant of the other path.
   *
   * @param path The path to test.
   * @return True if this path is an descendant of the other path.
   */
  public boolean isDescendantOf(final PathName path) {
    if (path != null) {
      for (PathName parentPath = getParent(); parentPath != null; parentPath = parentPath
        .getParent()) {
        if (path.equals(parentPath)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Test if that this path is the parent of the other path.
   *
   * @param path The path to test.
   * @return True if this path is the parent of the other path.
   */
  public boolean isParentOf(final PathName path) {
    if (path != null) {
      final PathName otherParent = path.getParent();
      return equals(otherParent);
    }
    return false;
  }

  /**
   * Test if that this path is a sibling of the other path.
   *
   * @param path The path to test.
   * @return True if this path is a sibling of the other path.
   */
  public boolean isSiblingOf(final PathName path) {
    if (path != null) {
      final PathName parent1 = getParent();
      final PathName parent2 = path.getParent();
      return parent1.equals(parent2);
    }
    return false;
  }

  @Override
  public int length() {
    return this.path.length();
  }

  public PathName newChild(final String name) {
    final String childPath = getPath() + "/" + name;
    return newPathName(childPath);
  }

  @Override
  public CharSequence subSequence(final int start, final int end) {
    return this.path.subSequence(start, end);
  }

  @Override
  public String toString() {
    return this.path;
  }
}
