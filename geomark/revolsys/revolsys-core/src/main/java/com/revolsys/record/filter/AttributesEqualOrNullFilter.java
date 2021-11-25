package com.revolsys.record.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.Records;

public class AttributesEqualOrNullFilter implements Predicate<Record> {
  public static boolean test(final Record object1, final Record object2,
    final Collection<String> fieldNames) {
    for (final String fieldName : fieldNames) {
      final Object value1 = Records.getFieldByPath(object1, fieldName);
      final Object value2 = Records.getFieldByPath(object2, fieldName);

      if (value1 != null && value2 != null && !DataType.equal(value1, value2)) {
        return false;
      }
    }
    return true;
  }

  public static boolean test(final Record object1, final Record object2,
    final String... fieldNames) {
    return test(object1, object2, Arrays.asList(fieldNames));
  }

  private final Collection<String> fieldNames;

  private final Record object;

  public AttributesEqualOrNullFilter(final Record object, final Collection<String> fieldNames) {
    this.fieldNames = fieldNames;
    this.object = object;
  }

  public AttributesEqualOrNullFilter(final Record object, final String... fieldNames) {
    this(object, Arrays.asList(fieldNames));
  }

  @Override
  public boolean test(final Record object) {
    return test(this.object, object, this.fieldNames);
  }

  @Override
  public String toString() {
    return "AttributeEquals" + this.fieldNames;
  }

}
