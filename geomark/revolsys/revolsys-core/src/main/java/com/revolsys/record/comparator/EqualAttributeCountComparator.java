package com.revolsys.record.comparator;

import java.util.Comparator;
import java.util.List;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

/**
 * The EqualAttributeCountComparator compares the number of attributes of the
 * two objects which are equal to a test object. Unless invert is true a smaller
 * number of equal attributes will appear before a large amount.
 *
 * @author Paul Austin
 */
public class EqualAttributeCountComparator implements Comparator<Record> {
  private final List<String> fieldNames;

  private final Record object;

  public EqualAttributeCountComparator(final Record object) {
    this(object, false);
  }

  public EqualAttributeCountComparator(final Record object, final boolean invert) {
    this.object = object;
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    this.fieldNames = recordDefinition.getFieldNames();
  }

  @Override
  public int compare(final Record object1, final Record object2) {
    final int compare;
    if (object1 == null) {
      if (object2 == null) {
        compare = 0;
      } else {
        compare = -1;
      }
    } else if (object2 == null) {
      compare = 1;
    } else {
      int count1 = 0;
      int count2 = 0;

      for (final String fieldName : this.fieldNames) {
        final Object value = this.object.getValue(fieldName);

        final Object value1 = object1.getValue(fieldName);
        if (DataType.equal(value, value1)) {
          count1++;
        }

        final Object value2 = object1.getValue(fieldName);
        if (DataType.equal(value, value2)) {
          count2++;
        }
      }
      compare = CompareUtil.compare(count1, count2);
    }
    return compare;
  }
}
