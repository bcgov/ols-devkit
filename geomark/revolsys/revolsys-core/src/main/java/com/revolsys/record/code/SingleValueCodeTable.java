package com.revolsys.record.code;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class SingleValueCodeTable extends AbstractSingleValueCodeTable {

  static CodeTable newCodeTable(final String name, final RecordReader reader) {
    final SingleValueCodeTable codeTable = new SingleValueCodeTable(name);
    for (final Record record : reader) {
      final Identifier id = record.getIdentifier(0);
      final Object value = record.getValue(1);
      codeTable.addValue(id, value);
    }
    return codeTable;
  }

  public SingleValueCodeTable(final String name) {
    setName(name);
  }

  @Override
  public SingleValueCodeTable addValue(final Identifier id, final Object value) {
    super.addValue(id, value);
    return this;
  }

  public SingleValueCodeTable addValue(final Object id, final Object value) {
    return addValue(Identifier.newIdentifier(id), value);
  }

  @Override
  public SingleValueCodeTable clone() {
    return (SingleValueCodeTable)super.clone();
  }

  @Override
  public String getIdFieldName() {
    return getName();
  }

  @Override
  public void refresh() {
  }

}
