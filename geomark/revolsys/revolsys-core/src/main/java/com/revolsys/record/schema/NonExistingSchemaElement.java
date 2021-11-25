package com.revolsys.record.schema;

import org.jeometry.common.io.PathName;

class NonExistingSchemaElement extends AbstractRecordStoreSchemaElement {
  public NonExistingSchemaElement(final PathName pathName) {
    super(pathName);
  }

  @Override
  public String getIconName() {
    return "error";
  }
}
