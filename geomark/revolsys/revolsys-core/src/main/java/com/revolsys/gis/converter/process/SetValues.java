package com.revolsys.gis.converter.process;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.record.Record;

public class SetValues extends AbstractSourceToTargetProcess<Record, Record> {
  private Map<String, ? extends Object> values = Collections.emptyMap();

  public SetValues(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  public Map<String, ? extends Object> getValues() {
    return this.values;
  }

  @Override
  public void process(final Record source, final Record target) {
    for (final Entry<String, ? extends Object> entry : this.values.entrySet()) {
      final String name = entry.getKey();
      final Object value = entry.getValue();
      if (value != null) {
        target.setValueByPath(name, value);
      }
    }
  }

  public void setValues(final Map<String, ? extends Object> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return "set" + this.values;
  }
}
