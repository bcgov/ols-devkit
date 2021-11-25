package com.revolsys.record;

import com.revolsys.record.schema.RecordDefinition;

public abstract class BaseRecord extends AbstractRecord {

  protected transient RecordDefinition recordDefinition;

  private RecordState state = RecordState.INITIALIZING;

  public BaseRecord() {
  }

  public BaseRecord(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public RecordState getState() {
    return this.state;
  }

  public boolean isInitializing() {
    return this.state == RecordState.INITIALIZING;
  }

  @Override
  public boolean isState(final RecordState state) {
    return this.state == state;
  }

  @Override
  public RecordState setState(final RecordState state) {
    final RecordState oldState = this.getState();
    this.state = state;
    return oldState;
  }

}
