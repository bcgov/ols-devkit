package com.revolsys.record.io;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.revolsys.io.IoFactory;
import com.revolsys.io.file.Paths;
import com.revolsys.record.schema.RecordStore;

public interface RecordStoreFactory extends IoFactory {
  default boolean canOpenPath(final Path path) {
    if (isAvailable()) {
      final String fileNameExtension = Paths.getFileNameExtension(path);
      return getRecordStoreFileExtensions().contains(fileNameExtension);
    } else {
      return false;
    }
  }

  default boolean canOpenUrl(final String url) {
    if (isAvailable()) {
      for (final Pattern pattern : getUrlPatterns()) {
        if (pattern.matcher(url).matches()) {
          return true;
        }
      }
    }
    return false;
  }

  default List<String> getRecordStoreFileExtensions() {
    return Collections.emptyList();
  }

  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<Pattern> getUrlPatterns();

  RecordStore newRecordStore(Map<String, ? extends Object> connectionProperties);

  default Map<String, Object> parseUrl(final String url) {
    return Collections.emptyMap();
  }

  default String toUrl(final Map<String, Object> urlParameters) {
    return null;
  }
}
