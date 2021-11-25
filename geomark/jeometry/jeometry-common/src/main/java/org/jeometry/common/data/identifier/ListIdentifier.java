package org.jeometry.common.data.identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.type.DataType;

public class ListIdentifier extends AbstractIdentifier {

  private final List<Object> values;

  public ListIdentifier(final Collection<? extends Object> values) {
    if (values == null || values.size() == 0) {
      this.values = Collections.emptyList();
    } else {
      this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }
  }

  public ListIdentifier(final Object... values) {
    if (values == null || values.length == 0) {
      this.values = Collections.emptyList();
    } else {
      this.values = Collections.unmodifiableList(Arrays.asList(values));
    }
  }

  @Override
  public boolean equals(final Identifier identifier) {
    if (identifier instanceof ListIdentifier) {
      final ListIdentifier listIdentifier = (ListIdentifier)identifier;
      return DataType.equal(this.values, listIdentifier.values);
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final int index) {
    return (V)this.values.get(0);
  }

  @Override
  public int getValueCount() {
    return this.values.size();
  }

  @Override
  public List<Object> getValues() {
    return this.values;
  }

  @Override
  public boolean isSingle() {
    return this.values.size() == 1;
  }
}
