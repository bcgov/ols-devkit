package com.revolsys.gis.converter;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.record.Record;
import com.revolsys.record.Records;

public class GetRecordValue implements Converter<Record, Object> {
  private String attributePath;

  private Map<? extends Object, ? extends Object> valueMap;

  public GetRecordValue() {
  }

  public GetRecordValue(final String attributePath) {
    this.attributePath = attributePath;
  }

  public GetRecordValue(final String attributePath,
    final Map<? extends Object, ? extends Object> valueMap) {
    this.attributePath = attributePath;
    this.valueMap = valueMap;
  }

  @Override
  public Object convert(final Record source) {
    Object value = Records.getFieldByPath(source, this.attributePath);
    if (!this.valueMap.isEmpty()) {
      if (this.valueMap.containsKey(value)) {
        value = this.valueMap.get(value);
      }
    }
    return value;
  }

  @Override
  public String toString() {
    return this.attributePath;
  }
}
