package com.revolsys.jdbc.field;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class JdbcFieldDefinition extends FieldDefinition {
  private String dbName;

  private boolean quoteName = false;

  private int sqlType;

  private boolean generated = false;

  JdbcFieldDefinition() {
    setName(JdbcFieldDefinitions.UNKNOWN);
  }

  public JdbcFieldDefinition(final String dbName, final String name, final DataType type,
    final int sqlType, final int length, final int scale, final boolean required,
    final String description, final Map<String, Object> properties) {
    super(name, type, length, scale, required, description, properties);
    this.dbName = dbName;
    this.sqlType = sqlType;
  }

  @Override
  public JdbcFieldDefinition addField(final AbstractJdbcRecordStore recordStore,
    final JdbcRecordDefinition recordDefinition, final ResultSetMetaData metaData,
    final ColumnIndexes columnIndexes) throws SQLException {
    final int columnIndex = columnIndexes.incrementAndGet();
    final String name = metaData.getColumnName(columnIndex);
    final JdbcFieldDefinition newField = clone();
    newField.setName(name);
    recordDefinition.addField(newField);
    return newField;
  }

  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
    addStatementPlaceHolder(sql);
  }

  public void addSelectStatementPlaceHolder(final StringBuilder sql) {
    addStatementPlaceHolder(sql);
  }

  public void addStatementPlaceHolder(final StringBuilder sql) {
    sql.append('?');
  }

  @Override
  public void appendColumnName(final Appendable sql) {
    appendColumnName(sql, this.quoteName);
  }

  @Override
  public void appendColumnName(final Appendable sql, boolean quoteName) {
    try {
      quoteName |= this.quoteName;
      if (quoteName) {
        sql.append('"');
      }
      final String dbName = getDbName();
      sql.append(dbName);
      if (quoteName) {
        sql.append('"');
      }
    } catch (final IOException e) {
      Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public JdbcFieldDefinition clone() {
    return new JdbcFieldDefinition(this.dbName, getName(), getDataType(), getSqlType(), getLength(),
      getScale(), isRequired(), getDescription(), getProperties());
  }

  public String getDbName() {
    return this.dbName;
  }

  public int getSqlType() {
    return this.sqlType;
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return resultSet.getObject(indexes.incrementAndGet());
  }

  @Override
  public boolean isGenerated() {
    return this.generated;
  }

  public boolean isQuoteName() {
    return this.quoteName;
  }

  @Override
  public boolean isSortable() {
    switch (this.sqlType) {
      case Types.ARRAY:
      case Types.BLOB:
      case Types.CLOB:
      case Types.JAVA_OBJECT:
      case Types.OTHER:
      case Types.STRUCT:
      case Types.SQLXML:
        return false;
      default:
        return true;
    }
  }

  public JdbcFieldDefinition setGenerated(final boolean generated) {
    this.generated = generated;
    if (generated) {
      ((JdbcRecordDefinition)getRecordDefinition()).setHasGeneratedFields(true);
    }
    return this;
  }

  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    return setPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record record) throws SQLException {
    final String name = getName();
    final Object value = record.getValue(name);
    return setInsertPreparedStatementValue(statement, parameterIndex, value);
  }

  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      statement.setNull(parameterIndex, this.sqlType);
    } else {
      statement.setObject(parameterIndex, value);
    }
    return parameterIndex + 1;
  }

  public void setQuoteName(final boolean quoteName) {
    this.quoteName = quoteName;
  }

  public void setSqlType(final int sqlType) {
    this.sqlType = sqlType;
  }
}
