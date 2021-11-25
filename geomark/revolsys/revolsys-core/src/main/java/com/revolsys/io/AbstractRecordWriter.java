package com.revolsys.io;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractRecordWriter extends AbstractWriter<Record> implements RecordWriter {
  private boolean writeNulls = false;

  private boolean writeCodeValues = false;

  private boolean indent = false;

  private Resource resource;

  protected final RecordDefinition recordDefinition;

  public AbstractRecordWriter(final RecordDefinitionProxy recordDefinition) {
    if (recordDefinition == null) {
      this.recordDefinition = null;
    } else {
      this.recordDefinition = recordDefinition.getRecordDefinition();
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public Resource getResource() {
    return this.resource;
  }

  @Override
  public boolean isIndent() {
    return this.indent;
  }

  @Override
  public boolean isWriteCodeValues() {
    return this.writeCodeValues;
  }

  @Override
  public boolean isWriteNulls() {
    return this.writeNulls;
  }

  @Override
  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  protected void setResource(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void setWriteCodeValues(final boolean writeCodeValues) {
    this.writeCodeValues = writeCodeValues;
  }

  @Override
  public void setWriteNulls(final boolean writeNulls) {
    this.writeNulls = writeNulls;
  }

  @Override
  public String toString() {
    if (this.resource == null) {
      return super.toString();
    } else {
      return this.resource.toString();
    }
  }
}
