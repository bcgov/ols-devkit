package org.jeometry.common.io;

public interface PathNameProxy {
  default boolean equalsPathName(final PathName pathName) {
    if (pathName != null) {
      final PathName pathName1 = getPathName();
      return pathName.equals(pathName1);
    }
    return false;
  }

  PathName getPathName();
}
