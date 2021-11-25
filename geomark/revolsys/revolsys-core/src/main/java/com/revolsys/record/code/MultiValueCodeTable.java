package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;

public class MultiValueCodeTable extends AbstractMultiValueCodeTable {

  static CodeTable newCodeTable(final String name, final RecordReader reader) {
    final MultiValueCodeTable codeTable = new MultiValueCodeTable(name);
    final int fieldCount = reader.getRecordDefinition().getFieldCount();
    for (final Record record : reader) {
      final Identifier id = record.getIdentifier(0);
      final List<Object> values = new ArrayList<>();
      for (int i = 1; i < fieldCount; i++) {
        final Object value = record.getValue(i);
        values.add(value);
      }
      codeTable.addValue(id, values);
    }
    return codeTable;
  }

  public MultiValueCodeTable(final String name) {
    setName(name);
  }

  @Override
  public void addValue(final Identifier id, final Object... values) {
    super.addValue(id, values);
  }

  public void addValue(final Object id, final Object... values) {
    super.addValue(Identifier.newIdentifier(id), values);
  }

  @Override
  public MultiValueCodeTable clone() {
    return (MultiValueCodeTable)super.clone();
  }

  @Override
  public String getIdFieldName() {
    return getName();
  }

  @Override
  protected Identifier loadId(final List<Object> values, final boolean createId) {
    return null;
  }

  @Override
  public void refresh() {
  }

}
