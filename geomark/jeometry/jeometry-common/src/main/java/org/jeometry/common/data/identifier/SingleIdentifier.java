package org.jeometry.common.data.identifier;

import java.util.Collections;
import java.util.List;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

public class SingleIdentifier implements Identifier, Comparable<Object> {
  private final Object value;

  protected SingleIdentifier(final Object value) {
    this.value = value;
  }

  @Override
  public int compareTo(final Object object) {
    Object otherValue;
    if (object instanceof Identifier) {
      final Identifier identifier = (Identifier)object;
      if (identifier.isSingle()) {
        otherValue = identifier.getValue(0);
      } else {
        return -1;
      }
    } else {
      otherValue = object;
    }
    return CompareUtil.compare(this.value, otherValue);
  }

  @Override
  public boolean equals(final Identifier identifier) {
    if (identifier != null && identifier.isSingle()) {
      final Object otherValue = identifier.getValue(0);
      return DataType.equal(this.value, otherValue);
    } else {
      return false;
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Identifier) {
      final Identifier identifier = (Identifier)other;
      return equals(identifier);
    } else {
      return DataType.equal(this.value, other);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final int index) {
    if (index == 0) {
      return (V)this.value;
    } else {
      return null;
    }
  }

  @Override
  public List<Object> getValues() {
    return Collections.singletonList(this.value);
  }

  @Override
  public int hashCode() {
    if (this.value == null) {
      return 0;
    } else {
      return this.value.hashCode();
    }
  }

  @Override
  public String toString() {
    return DataTypes.toString(this.value);
  }
}
