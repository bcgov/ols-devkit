package com.revolsys.record.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.Records;

public class AttributesEqualFilter implements Predicate<Record> {
  public static boolean test(final Record object1, final Record object2,
    final boolean nullEqualsEmptyString, final Collection<String> fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = Records.getFieldByPath(object1, fieldName);
      final Object value2 = Records.getFieldByPath(object2, fieldName);
      if (nullEqualsEmptyString) {
        if (value1 == null) {
          if (value2 != null && !"".equals(value2)) {
            return false;
          }
        } else if (value2 == null) {
          if (value1 != null && !"".equals(value1)) {
            return false;
          }
        } else if (!DataType.equal(value1, value2)) {
          return false;
        }
      } else {
        if (!DataType.equal(value1, value2)) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean test(final Record object1, final Record object2,
    final boolean nullEqualsEmptyString, final String... fieldNames) {
    return test(object1, object2, nullEqualsEmptyString, Arrays.asList(fieldNames));
  }

  public static boolean test(final Record object1, final Record object2,
    final String... fieldNames) {
    return test(object1, object2, false, Arrays.asList(fieldNames));
  }

  private final Collection<String> fieldNames;

  private boolean nullEqualsEmptyString;

  private final Record object;

  public AttributesEqualFilter(final Record object, final Collection<String> fieldNames) {
    this.fieldNames = fieldNames;
    this.object = object;
  }

  public AttributesEqualFilter(final Record object, final String... fieldNames) {
    this(object, Arrays.asList(fieldNames));
  }

  public boolean isNullEqualsEmptyString() {
    return this.nullEqualsEmptyString;
  }

  public void setNullEqualsEmptyString(final boolean nullEqualsEmptyString) {
    this.nullEqualsEmptyString = nullEqualsEmptyString;
  }

  @Override
  public boolean test(final Record object) {
    return test(this.object, object, this.nullEqualsEmptyString, this.fieldNames);
  }

  @Override
  public String toString() {
    return "AttributeEquals" + this.fieldNames;
  }

}
