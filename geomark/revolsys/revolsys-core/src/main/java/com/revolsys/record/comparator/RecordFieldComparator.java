package com.revolsys.record.comparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jeometry.common.compare.CompareUtil;

import com.revolsys.record.Record;
import com.revolsys.record.Records;

public class RecordFieldComparator implements Comparator<Record> {
  private List<String> fieldNames;

  private boolean invert;

  private boolean nullFirst;

  public RecordFieldComparator() {
  }

  public RecordFieldComparator(final boolean sortAsceding, final String... fieldNames) {
    this(Arrays.asList(fieldNames));
    this.invert = !sortAsceding;
  }

  public RecordFieldComparator(final List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public RecordFieldComparator(final String... fieldNames) {
    this(Arrays.asList(fieldNames));
  }

  @Override
  public int compare(final Record object1, final Record object2) {
    for (final String fieldName : this.fieldNames) {
      final int compare = compare(object1, object2, fieldName);
      if (compare != 0) {
        return compare;
      }
    }

    return 0;
  }

  public int compare(final Record record1, final Record record2, final String fieldName) {
    final Comparable<Object> value1 = Records.getFieldByPath(record1, fieldName);
    final Comparable<Object> value2 = Records.getFieldByPath(record2, fieldName);
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        if (this.nullFirst) {
          return -1;
        } else {
          return 1;
        }
      }
    } else if (value2 == null) {
      if (this.nullFirst) {
        return 1;
      } else {
        return -1;
      }
    } else {
      final int compare = CompareUtil.compare(value1, value2);
      if (this.invert) {
        return -compare;
      } else {
        return compare;
      }
    }
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public boolean isInvert() {
    return this.invert;
  }

  public boolean isNullFirst() {
    return this.nullFirst;
  }

  public void setFieldNames(final List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public void setInvert(final boolean invert) {
    this.invert = invert;
  }

  public void setNullFirst(final boolean nullFirst) {
    this.nullFirst = nullFirst;
  }
}
