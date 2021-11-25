package com.revolsys.record.filter;

import java.util.Collection;
import java.util.function.Predicate;

import org.jeometry.common.io.PathName;

import com.revolsys.record.Record;

public interface RecordFilters {
  static Predicate<Record> fieldEquals(final CharSequence fieldName, final Object value) {
    return (record) -> {
      return record.equalValue(fieldName, value);
    };
  }

  static Predicate<Record> fieldPathEquals(final String fieldPath, final Object value) {
    return (record) -> {
      return record.equalPathValue(fieldPath, value);
    };
  }

  static Predicate<Record> fieldPathsEquals(final Collection<? extends CharSequence> fieldPaths,
    final Object value) {
    return (record) -> {
      for (final CharSequence fieldPath : fieldPaths) {
        if (!record.equalPathValue(fieldPath, value)) {
          return false;
        }
      }
      return true;
    };
  }

  static Predicate<Record> fieldsEquals(final Collection<? extends CharSequence> fieldNames,
    final Object value) {
    return (record) -> {
      for (final CharSequence fieldName : fieldNames) {
        if (!record.equalValue(fieldName, value)) {
          return false;
        }
      }
      return true;
    };
  }

  static Predicate<Record> isNotNull(final String fieldName) {
    return (record) -> {
      return record.hasValue(fieldName);
    };
  }

  static Predicate<Record> isNull(final String fieldName) {
    return (record) -> {
      return !record.hasValue(fieldName);
    };
  }

  static Predicate<Record> typeName(final PathName pathName) {
    return (record) -> {
      final PathName typePathName = record.getPathName();
      return pathName.equals(typePathName);
    };
  }

  static Predicate<Record> typeNames(final Collection<PathName> pathNames) {
    return (record) -> {
      final PathName typePathName = record.getPathName();
      return pathNames.contains(typePathName);
    };
  }
}
