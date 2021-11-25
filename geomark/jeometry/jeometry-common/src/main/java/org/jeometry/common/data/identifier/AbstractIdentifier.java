package org.jeometry.common.data.identifier;

import java.util.List;

import org.jeometry.common.data.type.DataTypes;

public abstract class AbstractIdentifier implements Identifier {

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other instanceof Identifier) {
      final Identifier identifier = (Identifier)other;
      return equals(identifier);
    } else {
      final List<Object> values = getValues();
      if (values.size() == 1) {
        return values.get(0).equals(other);
      } else {
        return false;
      }
    }
  }

  @Override
  public int hashCode() {
    int hashCode;
    if (isSingle()) {
      hashCode = getValue(0).hashCode();
    } else {
      hashCode = 1;
      final int valueCount = getValueCount();
      for (int i = 0; i < valueCount; i++) {
        final Object value = getValue(i);
        hashCode *= 31;
        if (value != null) {
          hashCode += value.hashCode();
        }
      }
    }
    return hashCode;
  }

  @Override
  public String toString() {
    final int valueCount = getValueCount();
    if (valueCount == 0) {
      return null;
    } else {
      final StringBuilder result = new StringBuilder();
      boolean first = true;
      for (int i = 0; i < valueCount; i++) {
        final Object value = getValue(i);
        if (value != null) {
          final String string = DataTypes.toString(value);
          if (string != null) {
            if (first) {
              first = false;
            } else {
              result.append(":");
            }
            result.append(string);
          }
        }
      }
      return result.toString();
    }
  }
}
