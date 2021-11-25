package com.revolsys.record.query;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class Column implements QueryValue, ColumnReference {

  private FieldDefinition fieldDefinition;

  private final String name;

  private TableReference table;

  public Column(final CharSequence name) {
    this(name.toString());
  }

  public Column(final String name) {
    this.name = name;
  }

  public Column(final TableReference tableReference, final CharSequence name) {
    this.table = tableReference;
    this.name = name.toString();
  }

  @Override
  public void appendDefaultSelect(final Query query, final RecordStore recordStore,
    final Appendable sql) {
    if (this.fieldDefinition == null) {
      appendName(sql);
    } else {
      this.fieldDefinition.appendSelect(query, recordStore, sql);
    }
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    if (this.fieldDefinition == null) {
      sql.append(toString());
    } else {
      this.fieldDefinition.appendColumnName(sql, null);
    }
  }

  @Override
  public void appendName(final Appendable string) {
    try {
      if (this.table != null) {
        this.table.appendColumnPrefix(string);
      }
      final String name = this.name;
      if ("*".equals(name) || name.indexOf('"') != -1 || name.indexOf('.') != -1
        || name.matches("([A-Z][_A-Z1-9]*\\.)?[A-Z][_A-Z1-9]*\\*")) {
        string.append(name);
      } else {
        string.append('"');
        string.append(name);
        string.append('"');
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int appendParameters(final int index, final PreparedStatement statement) {
    return index;
  }

  @Override
  public ColumnReference clone() {
    try {
      return (ColumnReference)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public ColumnReference clone(final TableReference oldTable, final TableReference newTable) {
    if (oldTable != newTable) {
      final ColumnReference newColumn = newTable.getColumn(this.name);
      if (newColumn != null) {
        return newColumn;
      }
    }
    return clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Column) {
      final ColumnReference value = (ColumnReference)obj;
      return DataType.equal(value.getName(), this.getName());
    } else {
      return false;
    }
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return this.fieldDefinition;
  }

  @Override
  public int getFieldIndex() {
    if (this.fieldDefinition == null) {
      return -1;
    } else {
      return this.fieldDefinition.getIndex();
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getStringValue(final MapEx record) {
    final Object value = getValue(record);
    if (this.fieldDefinition == null) {
      return DataTypes.toString(value);
    } else {
      return this.fieldDefinition.toString(value);
    }
  }

  @Override
  public TableReference getTable() {
    return this.table;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final MapEx record) {
    if (record == null) {
      return null;
    } else {
      final String name = getName();
      return (V)record.getValue(name);
    }
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    FieldDefinition field = this.fieldDefinition;
    if (field == null) {
      field = recordDefinition.getField(this.name);
    }
    return field.getValueFromResultSet(recordDefinition, resultSet, indexes, internStrings);
  }

  @Override
  public <V> V toColumnTypeException(final Object value) {
    if (value == null) {
      return null;
    } else {
      if (this.fieldDefinition == null) {
        return (V)value;
      } else {
        return this.fieldDefinition.toColumnTypeException(value);
      }
    }
  }

  @Override
  public <V> V toFieldValueException(final Object value) {
    if (value == null) {
      return null;
    } else {
      if (this.fieldDefinition == null) {
        return (V)value;
      } else {
        return this.fieldDefinition.toFieldValueException(value);
      }
    }
  }

  @Override
  public <V> V toFieldValueException(final RecordState state, final Object value) {
    if (value == null) {
      return null;
    } else {
      if (this.fieldDefinition == null) {
        return (V)value;
      } else {
        return this.fieldDefinition.toFieldValueException(state, value);
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    appendName(sb);
    return sb.toString();
  }

  @Override
  public String toString(final Object value) {
    if (this.fieldDefinition == null) {
      return DataTypes.toString(value);
    } else {
      return this.fieldDefinition.toString(value);
    }
  }
}
