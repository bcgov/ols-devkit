package com.revolsys.io;

import java.util.ArrayList;
import java.util.List;

public class FileNames {

  public static String getBaseName(final String fileName) {
    if (fileName == null) {
      return null;
    } else {
      int endIndex = fileName.length();
      int slashIndex;
      for (slashIndex = fileName.lastIndexOf("/", endIndex - 1); slashIndex != -1
        && slashIndex == endIndex - 1; slashIndex = fileName.lastIndexOf("/", endIndex - 1)) {
        endIndex--;
      }
      final int dotIndex = fileName.lastIndexOf('.', endIndex - 1);
      if (dotIndex == -1) {
        if (slashIndex == -1) {
          return fileName.substring(0, endIndex);
        } else if (slashIndex == fileName.length() - 1) {
          return "";
        } else {
          return fileName.substring(slashIndex + 1, endIndex);
        }
      } else {
        if (slashIndex == -1) {
          return fileName.substring(0, dotIndex);
        } else if (slashIndex > dotIndex) {
          if (slashIndex == fileName.length() - 1) {
            return "";
          } else {
            return fileName.substring(slashIndex + 1, endIndex);
          }
        } else {
          return fileName.substring(slashIndex + 1, dotIndex);
        }
      }
    }
  }

  public static String getFileNameExtension(final String fileName) {
    final int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex != -1) {
      final int startIndex = fileName.lastIndexOf("/");
      if (startIndex == -1) {
        return fileName.substring(dotIndex + 1);
      } else if (dotIndex > startIndex) {
        return fileName.substring(dotIndex + 1);
      }
    }
    return "";
  }

  public static List<String> getFileNameExtensions(final String fileName) {
    final List<String> extensions = new ArrayList<>();
    int startIndex = fileName.lastIndexOf("/");
    if (startIndex == -1) {
      startIndex = 0;
    }
    for (int dotIndex = fileName.indexOf('.', startIndex); dotIndex > 0; dotIndex = fileName
      .indexOf('.', startIndex)) {
      dotIndex++;
      final String extension = fileName.substring(dotIndex);
      extensions.add(extension);
      startIndex = dotIndex;
    }
    return extensions;
  }

}
