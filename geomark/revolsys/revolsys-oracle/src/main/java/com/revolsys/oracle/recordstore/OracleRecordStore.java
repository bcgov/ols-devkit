package com.revolsys.oracle.recordstore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.io.WktCsParser;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

import com.revolsys.collection.ResultPager;
import com.revolsys.collection.map.IntHashMap;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.field.JdbcFieldAdder;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.jdbc.io.RecordStoreIteratorFactory;
import com.revolsys.oracle.recordstore.esri.ArcSdeStGeometryFieldDefinition;
import com.revolsys.oracle.recordstore.esri.ArcSdeStGeometryRecordStoreExtension;
import com.revolsys.oracle.recordstore.field.OracleBlobFieldAdder;
import com.revolsys.oracle.recordstore.field.OracleClobFieldAdder;
import com.revolsys.oracle.recordstore.field.OracleJdbcRowIdFieldDefinition;
import com.revolsys.oracle.recordstore.field.OracleSdoGeometryFieldAdder;
import com.revolsys.oracle.recordstore.field.OracleSdoGeometryJdbcFieldDefinition;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.property.ShortNameProperty;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.query.functions.GeometryEqual2d;
import com.revolsys.record.query.functions.WithinDistance;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.util.Property;

public class OracleRecordStore extends AbstractJdbcRecordStore {
  public static final List<String> ORACLE_INTERNAL_SCHEMAS = Arrays.asList("ANONYMOUS",
    "APEX_030200", "AURORA$JIS$UTILITY$", "AURORA$ORB$UNAUTHENTICATED", "AWR_STAGE", "CSMIG",
    "CTXSYS", "DBSNMP", "DEMO", "DIP", "DMSYS", "DSSYS", "EXFSYS", "LBACSYS", "MDSYS", "OLAPSYS",
    "ORACLE_OCM", "ORDDATA", "ORDPLUGINS", "ORDSYS", "OSE$HTTP$ADMIN", "OUTLN", "PERFSTAT", "SDE",
    "SYS", "SYSTEM", "TRACESVR", "TSMSYS", "WMSYS", "XDB");

  private final IntHashMap<CoordinateSystem> oracleCoordinateSystems = new IntHashMap<>();

  private boolean useSchemaSequencePrefix = true;

  public OracleRecordStore() {
    this(ArrayRecord.FACTORY);
  }

  public OracleRecordStore(final DataSource dataSource) {
    super(dataSource);
    initSettings();
  }

  public OracleRecordStore(final Oracle databaseFactory,
    final Map<String, ? extends Object> connectionProperties) {
    super(databaseFactory, connectionProperties);
    initSettings();
  }

  public OracleRecordStore(final RecordFactory<? extends Record> recordFactory) {
    super(recordFactory);
    initSettings();
  }

  public OracleRecordStore(final RecordFactory<? extends Record> recordFactory,
    final DataSource dataSource) {
    this(recordFactory);
    setDataSource(dataSource);
  }

  private void appendEnvelopeIntersects(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    final EnvelopeIntersects envelopeIntersects = (EnvelopeIntersects)queryValue;
    final FieldDefinition geometryField = query.getGeometryField();

    if (geometryField instanceof OracleSdoGeometryJdbcFieldDefinition) {
      sql.append("SDO_RELATE(");
      final QueryValue boundingBox1Value = envelopeIntersects.getBoundingBox1Value();
      if (boundingBox1Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, boundingBox1Value);
      }
      sql.append(",");
      final QueryValue boundingBox2Value = envelopeIntersects.getBoundingBox2Value();
      if (boundingBox2Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, boundingBox2Value);
      }
      sql.append(",'mask=ANYINTERACT querytype=WINDOW') = 'TRUE'");
    } else if (geometryField instanceof ArcSdeStGeometryFieldDefinition) {
      sql.append("SDE.ST_ENVINTERSECTS(");
      final QueryValue boundingBox1Value = envelopeIntersects.getBoundingBox1Value();
      if (boundingBox1Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, boundingBox1Value);
      }
      sql.append(",");
      final QueryValue boundingBox2Value = envelopeIntersects.getBoundingBox2Value();
      if (boundingBox2Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, boundingBox2Value);
      }
      sql.append(") = 1");
    } else {
      throw new IllegalArgumentException(
        "Unknown geometry attribute type " + geometryField.getClass());
    }
  }

  private void appendGeometryEqual2d(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    final GeometryEqual2d equals = (GeometryEqual2d)queryValue;
    final FieldDefinition geometryField = query.getGeometryField();

    if (geometryField instanceof OracleSdoGeometryJdbcFieldDefinition) {
      sql.append("MDSYS.SDO_EQUAL(");
      final QueryValue geometry1Value = equals.getGeometry1Value();
      if (geometry1Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, geometry1Value);
      }
      sql.append(",");
      final QueryValue geometry2Value = equals.getGeometry2Value();
      if (geometry2Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, geometry2Value);
      }
      sql.append(") = 'TRUE'");
    } else if (geometryField instanceof ArcSdeStGeometryFieldDefinition) {
      sql.append("SDE.ST_EQUALS(");
      final QueryValue geometry1Value = equals.getGeometry1Value();
      if (geometry1Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, geometry1Value);
      }
      sql.append(",");
      final QueryValue geometry2Value = equals.getGeometry2Value();
      if (geometry2Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, geometry2Value);
      }
      sql.append(") = 1");
    } else {
      throw new IllegalArgumentException(
        "Unknown geometry attribute type " + geometryField.getClass());
    }
  }

  private void appendILike(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    final ILike iLike = (ILike)queryValue;
    final QueryValue left = iLike.getLeft();
    final QueryValue right = iLike.getRight();

    sql.append("UPPER(CAST(");
    if (left == null) {
      sql.append("NULL");
    } else {
      left.appendSql(query, this, sql);
    }
    sql.append(" AS VARCHAR(4000))) LIKE UPPER(");
    if (right == null) {
      sql.append("NULL");
    } else {
      right.appendSql(query, this, sql);
    }
    sql.append(")");
  }

  private void appendWithinDistance(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    final WithinDistance withinDistance = (WithinDistance)queryValue;
    final FieldDefinition geometryField = query.getGeometryField();
    if (geometryField instanceof OracleSdoGeometryJdbcFieldDefinition) {
      sql.append("MDSYS.SDO_WITHIN_DISTANCE(");
      final QueryValue geometry1Value = withinDistance.getGeometry1Value();
      if (geometry1Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, geometry1Value);
      }
      sql.append(", ");
      final QueryValue geometry2Value = withinDistance.getGeometry2Value();
      if (geometry2Value == null) {
        sql.append("NULL");
      } else {
        appendQueryValue(query, sql, geometry2Value);
      }
      sql.append(",'distance = ' || ");
      final QueryValue distanceValue = withinDistance.getDistanceValue();
      if (distanceValue == null) {
        sql.append("0");
      } else {
        appendQueryValue(query, sql, distanceValue);
      }
      sql.append(") = 'TRUE'");
    } else if (geometryField instanceof ArcSdeStGeometryFieldDefinition) {
      final Column column = (Column)withinDistance.getGeometry1Value();
      final GeometryFactory geometryFactory = column.getFieldDefinition()
        .getRecordDefinition()
        .getGeometryFactory();
      final Value geometry2Value = (Value)withinDistance.getGeometry2Value();
      final Value distanceValue = (Value)withinDistance.getDistanceValue();
      final Number distance = (Number)distanceValue.getValue();
      final Object geometryObject = geometry2Value.getValue();
      BoundingBoxEditor boundingBox;
      if (geometryObject instanceof BoundingBoxProxy) {
        boundingBox = ((BoundingBoxProxy)geometryObject).bboxEditor();
      } else {
        boundingBox = geometryFactory.bboxEditor();
      }
      boundingBox.expandDelta(distance.doubleValue());
      boundingBox.setGeometryFactory(geometryFactory);
      sql.append("(SDE.ST_ENVINTERSECTS(");
      appendQueryValue(query, sql, column);
      sql.append(",");
      sql.append(boundingBox.getMinX());
      sql.append(",");
      sql.append(boundingBox.getMinY());
      sql.append(",");
      sql.append(boundingBox.getMaxX());
      sql.append(",");
      sql.append(boundingBox.getMaxY());
      sql.append(") = 1 AND SDE.ST_DISTANCE(");
      appendQueryValue(query, sql, column);
      sql.append(", ");
      appendQueryValue(query, sql, geometry2Value);
      sql.append(") <= ");
      appendQueryValue(query, sql, distanceValue);
      sql.append(")");
    } else {
      throw new IllegalArgumentException(
        "Unknown geometry attribute type " + geometryField.getClass());
    }
  }

  public synchronized CoordinateSystem getCoordinateSystem(final int oracleSrid) {
    CoordinateSystem coordinateSystem = this.oracleCoordinateSystems.get(oracleSrid);
    if (coordinateSystem == null) {
      try {
        final Map<String, Object> result = selectMap(
          "SELECT * FROM MDSYS.SDO_CS_SRS WHERE SRID = ?", oracleSrid);
        if (result == null) {
          coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(oracleSrid);
        } else {
          final String wkt = (String)result.get("WKTEXT");
          coordinateSystem = WktCsParser.read(wkt);
          coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(coordinateSystem);
        }
      } catch (final Throwable e) {
        Logs.error(this, "Unable to load coordinate system: " + oracleSrid, e);
        return null;
      }
      this.oracleCoordinateSystems.put(oracleSrid, coordinateSystem);
    }
    return coordinateSystem;
  }

  @Override
  public String getGeneratePrimaryKeySql(final JdbcRecordDefinition recordDefinition) {
    final String sequenceName = getSequenceName(recordDefinition);
    return sequenceName + ".NEXTVAL";
  }

  public GeometryFactory getGeometryFactory(final int oracleSrid, final int axisCount,
    final double[] scales) {
    GeometryFactory geometryFactory = GeometryFactory.fixed(oracleSrid, axisCount, scales);
    if (!geometryFactory.isHasHorizontalCoordinateSystem() && oracleSrid > 0) {
      final CoordinateSystem coordinateSystem = getCoordinateSystem(oracleSrid);
      if (coordinateSystem != null) {
        geometryFactory = geometryFactory.convertCoordinateSystem(coordinateSystem);
      }
    }

    return geometryFactory;
  }

  @Override
  public Identifier getNextPrimaryKey(final String sequenceName) {
    final String sql = "SELECT " + sequenceName + ".NEXTVAL FROM SYS.DUAL";
    return Identifier.newIdentifier(selectLong(sql));
  }

  @Override
  public int getRecordCount(Query query) {
    if (query == null) {
      return 0;
    } else {
      final TableReference table = query.getTable();
      query = query.clone(table, table);
      query.setSql(null);
      query.clearOrderBy();
      final String sql = "select count(mainquery.rowid) from (" + query.getSelectSql()
        + ") mainquery";
      try (
        Transaction transaction = newTransaction(TransactionOptions.REQUIRED);
        JdbcConnection connection = getJdbcConnection()) {
        try (
          final PreparedStatement statement = connection.prepareStatement(sql)) {
          setPreparedStatementParameters(statement, query);
          try (
            final ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
              final int rowCount = resultSet.getInt(1);
              return rowCount;
            } else {
              return 0;
            }
          }
        } catch (final SQLException e) {
          throw connection.getException("getRecordCount", sql, e);
        } catch (final IllegalArgumentException e) {
          Logs.error(this, "Cannot get row count: " + query, e);
          return 0;
        }
      }
    }
  }

  @Override
  public String getRecordStoreType() {
    return "Oracle";
  }

  @Override
  protected String getSequenceName(final JdbcRecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      return null;
    } else {
      final String dbSchemaName = recordDefinition.getQuotedDbSchemaName();
      final String shortName = ShortNameProperty.getShortName(recordDefinition);
      final String sequenceName;
      if (Property.hasValue(shortName)) {
        if (this.useSchemaSequencePrefix) {
          sequenceName = dbSchemaName + "." + shortName.toLowerCase() + "_SEQ";
        } else {
          sequenceName = shortName.toLowerCase() + "_SEQ";
        }
      } else {
        final String tableName = recordDefinition.getDbTableName();
        if (this.useSchemaSequencePrefix) {
          sequenceName = dbSchemaName + "." + tableName + "_SEQ";
        } else {
          sequenceName = tableName + "_SEQ";
        }
      }
      return sequenceName;
    }
  }

  @Override
  @PostConstruct
  public void initializeDo() {
    super.initializeDo();
    final JdbcFieldAdder fieldAdder = new JdbcFieldAdder();
    addFieldAdder("NUMBER", fieldAdder);

    addFieldAdder("CHAR", fieldAdder);
    addFieldAdder("NCHAR", fieldAdder);
    addFieldAdder("VARCHAR", fieldAdder);
    addFieldAdder("VARCHAR2", fieldAdder);
    addFieldAdder("NVARCHAR2", new JdbcFieldAdder(DataTypes.STRING));
    addFieldAdder("LONG", fieldAdder);
    addFieldAdder("CLOB", fieldAdder);
    addFieldAdder("NCLOB", fieldAdder);

    addFieldAdder("DATE", fieldAdder);
    addFieldAdder("TIMESTAMP", fieldAdder);

    final OracleSdoGeometryFieldAdder sdoGeometryAttributeAdder = new OracleSdoGeometryFieldAdder(
      this);
    addFieldAdder("SDO_GEOMETRY", sdoGeometryAttributeAdder);
    addFieldAdder("MDSYS.SDO_GEOMETRY", sdoGeometryAttributeAdder);

    final OracleBlobFieldAdder blobAdder = new OracleBlobFieldAdder();
    addFieldAdder("BLOB", blobAdder);

    final OracleClobFieldAdder clobAdder = new OracleClobFieldAdder();
    addFieldAdder("CLOB", clobAdder);

    setPrimaryKeySql(
      "SELECT distinct cols.table_name, cols.column_name FROM all_constraints cons, all_cons_columns cols WHERE cons.constraint_type = 'P' AND cons.constraint_name = cols.constraint_name AND cons.owner = cols.owner AND cons.owner =?");
    setPrimaryKeyTableCondition(" AND cols.table_name = ?");

    setSchemaPermissionsSql("select distinct p.owner \"SCHEMA_NAME\" "
      + "from ALL_TAB_PRIVS_RECD P "
      + "where p.privilege in ('SELECT', 'INSERT', 'UPDATE', 'DELETE') union all select USER \"SCHEMA_NAME\" from DUAL");
    setSchemaTablePermissionsSql(
      "select distinct p.owner \"SCHEMA_NAME\", p.table_name, p.privilege, comments \"REMARKS\", c.table_type \"TABLE_TYPE\" "
        + "  from ALL_TAB_PRIVS_RECD P "
        + "    join all_tab_comments C on (p.owner = c.owner and p.table_name = c.table_name) "
        + "where p.owner = ? and c.table_type in ('TABLE', 'VIEW') and p.privilege in ('SELECT', 'INSERT', 'UPDATE', 'DELETE') "
        + "  union all "
        + "select user \"SCHEMA_NAME\", t.table_name, 'ALL', comments, c.table_type \"TABLE_TYPE\" "
        + "from user_tables t join user_tab_comments c on (t.table_name = c.table_name) and c.table_type in ('TABLE', 'VIEW')");

    addRecordStoreExtension(new ArcSdeStGeometryRecordStoreExtension());
  }

  private void initSettings() {
    setExcludeTablePatterns(".*\\$.*");
    setIteratorFactory(new RecordStoreIteratorFactory(this::newOracleIterator));
    addSqlQueryAppender(GeometryEqual2d.class, this::appendGeometryEqual2d);
    addSqlQueryAppender(EnvelopeIntersects.class, this::appendEnvelopeIntersects);
    addSqlQueryAppender(WithinDistance.class, this::appendWithinDistance);
    addSqlQueryAppender(ILike.class, this::appendILike);
  }

  @Override
  public PreparedStatement insertStatementPrepareRowId(final JdbcConnection connection,
    final RecordDefinition recordDefinition, final String sql) throws SQLException {
    final List<Integer> idFieldIndexes = recordDefinition.getIdFieldIndexes();
    final int[] indexes = new int[idFieldIndexes.size()];
    for (int i = 0; i < indexes.length; i++) {
      indexes[i] = idFieldIndexes.get(i) + 1;
    }
    return connection.prepareStatement(sql, indexes);
  }

  @Override
  public boolean isIdFieldRowid(final RecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.size() == 1) {
      final FieldDefinition idField = idFields.get(0);
      if (idField instanceof OracleJdbcRowIdFieldDefinition) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return ORACLE_INTERNAL_SCHEMAS.contains(schemaName);
  }

  public boolean isUseSchemaSequencePrefix() {
    return this.useSchemaSequencePrefix;
  }

  private OracleJdbcQueryIterator newOracleIterator(final RecordStore recordStore,
    final Query query, final Map<String, Object> properties) {
    return new OracleJdbcQueryIterator((OracleRecordStore)recordStore, query, properties);
  }

  @Override
  protected JdbcFieldDefinition newRowIdFieldDefinition() {
    return new OracleJdbcRowIdFieldDefinition();
  }

  @Override
  public ResultPager<Record> page(final Query query) {
    return new OracleJdbcQueryResultPager(this, getProperties(), query);
  }

  public void setUseSchemaSequencePrefix(final boolean useSchemaSequencePrefix) {
    this.useSchemaSequencePrefix = useSchemaSequencePrefix;
  }

}
