package com.revolsys.record;

import java.util.AbstractMap;
import java.util.Set;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.BaseCloneable;

public class DelegatingRecord extends AbstractMap<String, Object> implements Record {
  private final Record record;

  public DelegatingRecord(final Record record) {
    this.record = record;
  }

  @Override
  public Record clone() {
    final Record clone = BaseCloneable.clone(this.record);
    return new DelegatingRecord(clone);
  }

  @Override
  public int compareTo(final Record o) {
    return this.record.compareTo(o);
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    return this.record.entrySet();
  }

  public Record getRecord() {
    return this.record;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.record.getRecordDefinition();
  }

  @Override
  public RecordFactory getRecordFactory() {
    return this.record.getRecordFactory();
  }

  @Override
  public RecordState getState() {
    return this.record.getState();
  }

  @Override
  public <T extends Object> T getValue(final int index) {
    return this.record.getValue(index);
  }

  @Override
  public int hashCode() {
    return this.record.hashCode();
  }

  @Override
  public boolean isState(final RecordState state) {
    return this.record.isState(state);
  }

  @Override
  public RecordState setState(final RecordState state) {
    return this.record.setState(state);
  }

  @Override
  public boolean setValue(final int index, final Object value) {
    return this.record.setValue(index, value);
  }

  @Override
  public void setValues(final Iterable<? extends Object> values) {
    this.record.setValues(values);
  }

  @Override
  public Record setValues(final Object... values) {
    this.record.setValues(values);
    return this;
  }

  @Override
  public String toString() {
    return Record.toString(this);
  }

}
