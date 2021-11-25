package com.revolsys.oracle.recordstore.esri;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class ArcSdeObjectIdJdbcFieldDefinition extends JdbcFieldDefinition {
  public static void replaceAttribute(final String schemaName,
    final RecordDefinition recordDefinition, final Integer registrationId,
    final String rowIdColumn) {
    final JdbcFieldDefinition objectIdAttribute = (JdbcFieldDefinition)recordDefinition
      .getField(rowIdColumn);
    if (objectIdAttribute != null
      && !(objectIdAttribute instanceof ArcSdeObjectIdJdbcFieldDefinition)) {
      final String name = objectIdAttribute.getName();
      final String description = objectIdAttribute.getDescription();
      final Map<String, Object> properties = objectIdAttribute.getProperties();

      final ArcSdeObjectIdJdbcFieldDefinition newObjectIdAttribute = new ArcSdeObjectIdJdbcFieldDefinition(
        objectIdAttribute.getDbName(), name, description, properties, schemaName, registrationId);
      newObjectIdAttribute.setRecordDefinition(recordDefinition);
      final RecordDefinitionImpl recordDefinitionImpl = (RecordDefinitionImpl)recordDefinition;
      recordDefinitionImpl.replaceField(objectIdAttribute, newObjectIdAttribute);
      if (recordDefinition.getIdFieldName() == null && recordDefinition.hasIdField()) {
        recordDefinitionImpl.setIdFieldName(name);
      }
    }
  }

  /** The SDE.TABLE_REGISTRY REGISTRATION_ID for the table. */
  private final long registrationId;

  /** The name of the database schema the table owned by. */
  private final String schemaName;

  public ArcSdeObjectIdJdbcFieldDefinition(final String dbName, final String name,
    final String description, final Map<String, Object> properties, final String schemaName,
    final long registrationId) {
    super(dbName, name, DataTypes.INT, -1, 19, 0, true, description, properties);
    this.schemaName = schemaName;
    this.registrationId = registrationId;
  }

  /**
   * Generate an OBJECT ID using ESRI's sde.version_user_ddl.next_row_id
   * function.
   */
  @Override
  public void addInsertStatementPlaceHolder(final StringBuilder sql, final boolean generateKeys) {
    sql.append(" sde.version_user_ddl.next_row_id('");
    sql.append(this.schemaName);
    sql.append("', ");
    sql.append(this.registrationId);
    sql.append(")");
  }

  @Override
  public ArcSdeObjectIdJdbcFieldDefinition clone() {
    return new ArcSdeObjectIdJdbcFieldDefinition(getDbName(), getName(), getDescription(),
      getProperties(), this.schemaName, this.registrationId);
  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return resultSet.getInt(indexes.incrementAndGet());
  }

  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    return parameterIndex;
  }

  /**
   * Ignore any inserted value.
   */
  @Override
  public int setInsertPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Record object) throws SQLException {
    return parameterIndex;
  }
}
