package com.revolsys.record.io.format.saif.util;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.io.PathUtil;

public final class PathCache {

  private static final Map<String, String> NAME_MAP = new HashMap<>();

  public static synchronized String getName(final String name) {
    if (name == null) {
      return null;
    } else {
      String path = NAME_MAP.get(name);
      if (path == null) {
        final int index = name.indexOf("::");
        if (index != -1) {
          final String localPart = name.substring(0, index);
          final String namespace = name.substring(index + 2);
          path = PathUtil.toPath(namespace, localPart);
        } else {
          path = "/" + name;
        }
        NAME_MAP.put(name, path);
      }
      return path;
    }
  }

  private PathCache() {
  }
}
