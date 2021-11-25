package com.revolsys.record.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.Records;

/**
 * Filter Records by the value of the fieldName.
 *
 * @author Paul Austin
 */
public class FieldValuesFilter implements Predicate<Record> {
  private boolean allowNulls;

  /** The fieldName name, or path to match. */
  private String fieldName;

  /** The value to match. */
  private List<Object> values = new ArrayList<>();

  /**
   * Construct a new FieldValuesFilter.
   */
  public FieldValuesFilter() {
  }

  /**
   * Construct a new FieldValuesFilter.
   *
   * @param fieldName The attribute name.
   * @param values The list of values.
   */
  public FieldValuesFilter(final String fieldName, final boolean allowNulls,
    final List<Object> values) {
    this.fieldName = fieldName;
    this.values = values;
    this.allowNulls = allowNulls;
  }

  /**
   * Construct a new FieldValuesFilter.
   *
   * @param fieldName The attribute name.
   * @param values The array of values.
   */
  public FieldValuesFilter(final String fieldName, final boolean allowNulls,
    final Object... values) {
    this(fieldName, allowNulls, Arrays.asList(values));
  }

  /**
   * Construct a new FieldValuesFilter.
   *
   * @param fieldName The attribute name.
   * @param values The list of values.
   */
  public FieldValuesFilter(final String fieldName, final List<Object> values) {
    this.fieldName = fieldName;
    this.values = values;
  }

  /**
   * Get the fieldName name, or path to match.
   *
   * @return The fieldName name, or path to match.
   */
  public String getFieldName() {
    return this.fieldName;
  }

  /**
   * @return the values
   */
  public List<Object> getValues() {
    return this.values;
  }

  public boolean isAllowNulls() {
    return this.allowNulls;
  }

  public void setAllowNulls(final boolean allowNulls) {
    this.allowNulls = allowNulls;
  }

  /**
   * Set the fieldName name, or path to match.
   *
   * @param fieldName The fieldName name, or path to match.
   */
  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @param values the values to set
   */
  public void setValues(final List<Object> values) {
    this.values = values;
  }

  /**
   * Match the fieldName on the data object with the required value.
   *
   * @param object The object.
   * @return True if the object matched the filter, false otherwise.
   */
  @Override
  public boolean test(final Record object) {
    final Object propertyValue = Records.getFieldByPath(object, this.fieldName);
    if (propertyValue == null) {
      if (this.allowNulls) {
        return true;
      } else {
        return false;
      }
    } else {
      for (final Object value : this.values) {
        if (DataType.equal(value, propertyValue)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    return this.fieldName + " in " + this.values;
  }

}
