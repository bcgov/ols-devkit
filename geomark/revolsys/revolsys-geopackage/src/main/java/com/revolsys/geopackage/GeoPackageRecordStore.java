package com.revolsys.geopackage;

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.sqlite.BusyHandler;
import org.sqlite.SQLiteConnection;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geopackage.field.GeoPackageGeometryFieldAdder;
import com.revolsys.geopackage.field.GeoPackageJdbcFieldAdder;
import com.revolsys.geopackage.function.GeoPackageEnvelopeValueFunction;
import com.revolsys.geopackage.function.GeoPackageIsEmptyFunction;
import com.revolsys.io.StringWriter;
import com.revolsys.io.file.Paths;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcRecordDefinition;
import com.revolsys.jdbc.io.JdbcRecordStoreSchema;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;

public class GeoPackageRecordStore extends AbstractJdbcRecordStore {

  private static final int APPLICATION_ID = ByteBuffer.wrap("GPKG".getBytes()).asIntBuffer().get();

  private Path file;

  private final BusyHandler busyHandler = new BusyHandler() {

    @Override
    protected int callback(final int nbPrevInvok) throws SQLException {
      // TODO Auto-generated method stub
      return 0;
    }
  };

  public GeoPackageRecordStore(final GeoPackage geoPackage,
    final Map<String, ? extends Object> connectionProperties) {
    super(geoPackage, connectionProperties);
    final String url = getUrl();
    if (url.startsWith(GeoPackage.JDBC_PREFIX)) {
      this.file = Path.of(url.substring(GeoPackage.JDBC_PREFIX.length()));
    } else {
      final UrlResource resource = new UrlResource(url);
      this.file = resource.getPath();
    }
    setQuoteNames(true);
    addSqlQueryAppender(EnvelopeIntersects.class, this::appendEvelopeIntersects);
  }

  private void addFunctions(final Connection connection) {
    try {
      final SQLiteConnection dbConnection = connection.unwrap(SQLiteConnection.class);
      BusyHandler.setHandler(dbConnection, this.busyHandler);
      GeoPackageIsEmptyFunction.add(dbConnection);
      GeoPackageEnvelopeValueFunction.add(dbConnection);
    } catch (final SQLException e) {
      Logs.error(this, e);
      // throw connection.getException("Add functions", "", e);
    }
  }

  private void appendEvelopeIntersects(final Query query, final StringBuilder sql,
    final QueryValue queryValue) {
    final EnvelopeIntersects envelopeIntersects = (EnvelopeIntersects)queryValue;
    final JdbcRecordDefinition recordDefinition = (JdbcRecordDefinition)query.getRecordDefinition();
    final QueryValue boundingBox1Value = envelopeIntersects.getBoundingBox1Value();
    final QueryValue boundingBox2Value = envelopeIntersects.getBoundingBox2Value();
    if (boundingBox1Value instanceof ColumnReference && boundingBox2Value instanceof Value) {
      final ColumnReference column = (ColumnReference)boundingBox1Value;
      final Value value = (Value)boundingBox2Value;
      final Object bboxValue = value.getQueryValue();

      final String fieldName = column.getName();
      if (recordDefinition.isGeometryField(fieldName) && bboxValue instanceof BoundingBox) {
        final BoundingBox boundingBox = (BoundingBox)bboxValue;
        final String idFieldName = recordDefinition.getIdFieldName();
        sql.append(idFieldName);
        sql.append(" in (select id from rtree_" + recordDefinition.getDbTableName() + "_"
          + fieldName.toLowerCase()
          + " where minx >= ? and maxx <= ? and miny >= ? and maxy <= ?)");
        final double minX = boundingBox.getMinX();
        final double maxX = boundingBox.getMaxX();
        final double minY = boundingBox.getMinY();
        final double maxY = boundingBox.getMaxY();
        envelopeIntersects.setRight(new CollectionValue(Arrays.asList(minX, maxX, minY, maxY)));
        return;
      }
    }
    sql.append("1 == 2");
    // if (boundingBox1Value == null) {
    // sql.append("NULL");
    // } else {
    // boundingBox1Value.appendSql(query, this, sql);
    // }
    // sql.append(" && ");
    // if (boundingBox2Value == null) {
    // sql.append("NULL");
    // } else {
    // boundingBox2Value.appendSql(query, this, sql);
    // }
  }

  private String createIdFieldName(final RecordDefinition recordDefinition) {
    for (final String fieldName : Arrays.asList("id", "fid", "gpkg_id", "objectid")) {
      if (!recordDefinition.hasField(fieldName)) {
        return fieldName;
      }
    }
    for (int i = 1; i <= 100; i++) {
      final String fieldName = "id" + i;
      if (!recordDefinition.hasField(fieldName)) {
        return fieldName;
      }
    }
    throw new IllegalArgumentException("Gave up trying to find a name for the primary key column");
  }

  @Override
  protected RecordDefinition createRecordDefinitionDo(final RecordDefinition oldRecordDefinition) {
    final String tableName = oldRecordDefinition.getPathName().getName();

    final RecordDefinitionBuilder newRdBuilder = new RecordDefinitionBuilder(tableName)
      .setGeometryFactory(oldRecordDefinition.getGeometryFactory());

    final String idFieldName;
    if (isPrimaryKeyValid(oldRecordDefinition)) {
      idFieldName = oldRecordDefinition.getIdFieldName();
      newRdBuilder.addField(idFieldName, DataTypes.LONG, true);
    } else {
      idFieldName = createIdFieldName(oldRecordDefinition);
      newRdBuilder.addField(idFieldName, DataTypes.LONG, true);
    }
    newRdBuilder.setIdFieldName(idFieldName);
    for (final FieldDefinition field : oldRecordDefinition.getFields()) {
      if (!field.getName().equals(idFieldName)) {
        final FieldDefinition newField = field.clone();
        newRdBuilder.addField(newField);
      }
    }

    final RecordDefinition newRecordDefinition = newRdBuilder.getRecordDefinition();

    final StringWriter out = new StringWriter();
    final GeoPackageSqlDdlWriter ddlWriter = new GeoPackageSqlDdlWriter(new PrintWriter(out));
    ddlWriter.writeCreateTable(newRecordDefinition);

    final String createTable = out.toString();
    final String gpkgContents = ddlWriter.insertGpkgContents(newRecordDefinition);
    try (
      Connection connection = getSqliteJdbcConnection(true)) {
      executeSql(connection, createTable);
      executeSql(connection, gpkgContents);
      for (final FieldDefinition field : newRecordDefinition.getGeometryFields()) {
        final CoordinateSystem coordinateSystem = field.getHorizontalCoordinateSystem();
        if (coordinateSystem != null) {
          final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
          final String sridSql = "SELECT srs_id from gpkg_spatial_ref_sys where srs_id = ?";
          try (
            final PreparedStatement statement = connection.prepareStatement(sridSql)) {
            statement.setInt(1, coordinateSystemId);

            try (
              final ResultSet resultSet = statement.executeQuery()) {
              if (!resultSet.next()) {
                final String insertSrsSql = "INSERT INTO gpkg_spatial_ref_sys (srs_name, srs_id, organization, organization_coordsys_id, definition, description) VALUES (?,?,?,?,?,?)";
                final String coordinateSystemName = coordinateSystem.getCoordinateSystemName();
                final String esriWktCs = coordinateSystem.toEsriWktCs();

                try (
                  final PreparedStatement insertStatement = connection
                    .prepareStatement(insertSrsSql)) {
                  insertStatement.setString(1, coordinateSystemName);
                  insertStatement.setInt(2, coordinateSystemId);
                  insertStatement.setString(3, "EPSG");
                  insertStatement.setInt(4, coordinateSystemId);
                  insertStatement.setString(5, esriWktCs);
                  insertStatement.setNull(6, Types.CHAR);
                  insertStatement.executeUpdate();
                } catch (final SQLException e2) {
                  throw Exceptions.wrap(insertSrsSql, e2);
                }
              }
            }
          } catch (final SQLException e) {
            throw Exceptions.wrap(sridSql, e);
          }
        }
        final String gpkgGeometryColumns = ddlWriter.insertGpkgGeometryColumns(field);
        executeSql(connection, gpkgGeometryColumns);
        final String fieldName = field.getName();
        for (String sql : getSqlTemplates("rtree_tiggers.sql")) {
          sql = sql.replace("<t>", tableName);
          sql = sql.replace("<c>", fieldName);
          sql = sql.replace("<i>", idFieldName);
          executeSql(connection, sql);
        }
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }

    final RecordStoreSchema rootSchema = getRootSchema();
    rootSchema.refresh();
    return rootSchema.getRecordDefinition(newRecordDefinition.getPathName());
  }

  private void createRecordStore() {
    try (
      Connection connection = getSqliteJdbcConnection(true)) {
      executeSql(connection, "PRAGMA application_id = " + APPLICATION_ID + ";");
      executeSql(connection, "PRAGMA user_version = 10201;");

      for (final String fileName : Arrays.asList("gpkg_spatial_ref_sys.sql", "gpkg_contents.sql",
        "gpkg_data_columns.sql", "gpkg_data_column_constraints.sql", "gpkg_extensions.sql",
        "gpkg_geometry_columns.sql", "gpkg_metadata.sql", "gpkg_metadata_reference.sql"
      // , "gpkg_tile_matrix.sql", "gpkg_tile_matrix_set.sql"
      )) {
        for (final String sql : getSqlTemplates(fileName)) {
          executeSql(connection, sql);
        }

      }
    } catch (final SQLException e) {
      Exceptions.wrap(e);
    }
  }

  @Override
  public void execteBatch(final PreparedStatement statement) throws SQLException {
    statement.executeBatch();
  }

  private void executeSql(final Connection connection, final String sql) {
    if (sql.trim().length() > 0) {
      try (
        Statement statement = connection.createStatement()) {
        statement.execute(sql);
      } catch (final SQLException e) {
        throw Exceptions.wrap(sql, e);
      }
    }
  }

  @Override
  protected Set<String> getDatabaseSchemaNames() {
    return Collections.emptySet();
  }

  @Override
  public String getGeneratePrimaryKeySql(final JdbcRecordDefinition recordDefinition) {
    return "null";
  }

  private GeometryFactory getGeometryFactory(final Connection connection,
    final int coordinateSystemId) throws SQLException {
    if (coordinateSystemId <= 0) {
      return GeometryFactory.DEFAULT_2D;
    }

    final GeometryFactory geometryFactory = GeometryFactory.floating2d(coordinateSystemId);
    if (geometryFactory.isHasHorizontalCoordinateSystem()) {
      return geometryFactory;
    } else {
      try (
        PreparedStatement statement = connection
          .prepareStatement("select definition from gpkg_spatial_ref_sys where srs_id = ?")) {
        statement.setInt(1, coordinateSystemId);
        try (
          ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            final String wkt = resultSet.getString(1);
            return GeometryFactory.floating2d(wkt);
          }
        }
      }
    }
    return GeometryFactory.DEFAULT_2D;
  }

  @Override
  public JdbcConnection getJdbcConnection() {
    return getJdbcConnection(false);
  }

  @Override
  public JdbcConnection getJdbcConnection(final boolean autoCommit) {
    final JdbcConnection connection = super.getJdbcConnection(autoCommit);
    addFunctions(connection);
    return connection;
  }

  @Override
  public Identifier getNextPrimaryKey(final String typePath) {
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RD extends RecordDefinition> RD getRecordDefinition(
    final RecordDefinition recordDefinition) {
    PathName pathName = recordDefinition.getPathName();
    if (pathName.getElementCount() > 1) {
      pathName = pathName.getLastElement();
    }
    final RecordDefinition rd = getRecordDefinition(pathName);
    if (rd == null && recordDefinition != null && isCreateMissingTables()) {
      return (RD)createRecordDefinitionDo(recordDefinition);
    }
    return (RD)rd;
  }

  @Override
  public String getRecordStoreType() {
    return "GeoPackageFactory";
  }

  @Override
  public String getSequenceName(final JdbcRecordDefinition recordDefinition) {
    return null;
  }

  private Connection getSqliteJdbcConnection(final boolean autoCommit) {
    try {
      final Connection connection = getDataSource().getConnection();
      connection.setAutoCommit(autoCommit);
      return connection;
    } catch (final SQLException e) {
      throw Exceptions.wrap(e);
    }
  }

  private String[] getSqlTemplates(final String fileName) {
    final String sqlStatements = Resource
      .getResource("classpath:/com/revolsys/geopackage/" + fileName)
      .contentsAsString();
    return sqlStatements.split("-- END --");
  }

  @Override
  @PostConstruct
  public void initializeDo() {
    super.initializeDo();
    setUsesSchema(false);

    addFieldAdder("BOOLEAN", DataTypes.BOOLEAN);
    addFieldAdder("TINYINT", DataTypes.BYTE);
    addFieldAdder("SMALLINT", DataTypes.SHORT);
    addFieldAdder("MEDIUMINT", DataTypes.INT);
    addFieldAdder("INT", DataTypes.LONG);
    addFieldAdder("LONG", DataTypes.LONG);
    addFieldAdder("BIGINT", DataTypes.LONG);
    addFieldAdder("INTEGER", DataTypes.LONG);
    addFieldAdder("FLOAT", DataTypes.FLOAT);
    addFieldAdder("DOUBLE", DataTypes.DOUBLE);
    addFieldAdder("REAL", DataTypes.DOUBLE);
    addFieldAdder("STRING", DataTypes.STRING);
    addFieldAdder("VARCHAR", DataTypes.STRING);
    addFieldAdder("TEXT", DataTypes.STRING);
    addFieldAdder("BLOB", DataTypes.BLOB);

    final GeoPackageJdbcFieldAdder dateAdder = new GeoPackageJdbcFieldAdder();

    addFieldAdder("DATE", dateAdder);
    addFieldAdder("DATETIME", dateAdder);

    final GeoPackageGeometryFieldAdder geometryAdder = new GeoPackageGeometryFieldAdder();
    addFieldAdder("GEOMETRY", geometryAdder);
    addFieldAdder("POINT", geometryAdder);
    addFieldAdder("LINESTRING", geometryAdder);
    addFieldAdder("POLYGON", geometryAdder);
    addFieldAdder("GEOMETRYCOLLECTION", geometryAdder);
    addFieldAdder("MULTIPOINT", geometryAdder);
    addFieldAdder("MULTILINESTRING", geometryAdder);
    addFieldAdder("MULTIPOLYGON", geometryAdder);

    if (!Paths.exists(this.file)) {
      if (isCreateMissingRecordStore()) {

        createRecordStore();
      } else {
        throw new IllegalArgumentException(this.file + " does not exist");
      }
    }
  }

  @Override
  public PreparedStatement insertStatementPrepareRowId(final JdbcConnection connection,
    final RecordDefinition recordDefinition, final String sql) throws SQLException {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    final String[] idColumnNames = new String[idFields.size()];
    for (int i = 0; i < idFields.size(); i++) {
      final FieldDefinition idField = idFields.get(0);
      final String columnName = ((JdbcFieldDefinition)idField).getDbName();
      idColumnNames[i] = columnName;
    }
    return connection.prepareStatement(sql, idColumnNames);
  }

  private boolean isPrimaryKeyValid(final RecordDefinition recordDefinition) {
    final List<FieldDefinition> idFields = recordDefinition.getIdFields();
    if (idFields.size() == 1) {
      final FieldDefinition idField = idFields.get(0);
      if (Number.class.isAssignableFrom(idField.getTypeClass())) {
        final DataType dataType = idField.getDataType();
        return !(dataType == DataTypes.FLOAT || dataType == DataTypes.DOUBLE
          || dataType == DataTypes.DECIMAL);
      }
    }
    return false;
  }

  @Override
  public boolean isSchemaExcluded(final String schemaName) {
    return false;
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition) {
    final RecordDefinition rd = getRecordDefinition(recordDefinition);
    return super.newRecordWriter(rd);
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElementsDo(
    final JdbcRecordStoreSchema schema, final PathName schemaPath) {
    final String schemaName = schema.getPath();

    final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
    try (
      final Connection connection = getSqliteJdbcConnection(true)) {
      try (
        final PreparedStatement statement = connection.prepareStatement(
          "select * from gpkg_contents where data_type in ('attributes', 'features') and table_name <> 'ogr_empty_table'");

        ResultSet resultSet = statement.executeQuery();) {
        while (resultSet.next()) {
          final String tableType = resultSet.getString("data_type");

          final String tableName = resultSet.getString("table_name");
          final PathName pathName = PathName.newPathName(tableName);
          final JdbcRecordDefinition recordDefinition = new JdbcRecordDefinition(schema, pathName,
            tableName);

          final String description = resultSet.getString("description");
          recordDefinition.setDescription(description);

          if ("features".equals(tableType)) {
            final int coordinateSystemId = resultSet.getInt("srs_id");
            final GeometryFactory geometryFactory = getGeometryFactory(connection,
              coordinateSystemId);
            recordDefinition.setGeometryFactory(geometryFactory);
            final double minX = resultSet.getDouble("min_x");
            final double minY = resultSet.getDouble("min_y");
            final double maxX = resultSet.getDouble("max_x");
            final double maxY = resultSet.getDouble("max_y");
            final BoundingBox boundingBox = geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
            recordDefinition.setBoundingBox(boundingBox);
          }
          final List<String> idFieldNames = new ArrayList<>();
          try (
            PreparedStatement columnStatement = connection
              .prepareStatement("PRAGMA table_info(" + tableName + ")")) {
            try (
              final ResultSet columnsRs = columnStatement.executeQuery()) {
              while (columnsRs.next()) {
                final String dbColumnName = columnsRs.getString("name");
                final String fieldName = dbColumnName;
                final int sqlType = Types.OTHER;
                String dataType = columnsRs.getString("type");
                int length = -1;
                final int scale = -1;
                if (dataType.startsWith("TEXT(")) {
                  length = Integer.parseInt(dataType.substring(5, dataType.length() - 1));
                  dataType = "TEXT";
                }
                final boolean required = columnsRs.getString("notnull").equals("1");
                final boolean primaryKey = columnsRs.getString("pk").equals("1");
                if (primaryKey) {
                  idFieldNames.add(fieldName);
                }
                final Object defaultValue = columnsRs.getString("dflt_value");
                final FieldDefinition field = addField(recordDefinition, dbColumnName, fieldName,
                  dataType, sqlType, length, scale, required, null);
                field.setDefaultValue(defaultValue);
              }
            }
          }
          recordDefinition.setIdFieldNames(idFieldNames);
          elementsByPath.put(pathName, recordDefinition);
        }
      }
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to load metadata for schema " + schemaName, e);
    }

    return elementsByPath;
  }

  public MapEx selectMapNoFunctions(final String sql, final Object... parameters) {
    try (
      Connection connection = getSqliteJdbcConnection(true)) {
      try (
        final PreparedStatement statement = connection.prepareStatement(sql)) {
        JdbcUtils.setParameters(statement, parameters);

        try (
          final ResultSet resultSet = statement.executeQuery()) {
          if (resultSet.next()) {
            return JdbcUtils.readMap(resultSet);
          } else {
            throw new IllegalArgumentException(
              "Value not found for " + sql + " " + Arrays.asList(parameters));
          }
        }
      }
    } catch (final SQLException e) {
      throw Exceptions.wrap(sql, e);
    }
  }

  @Override
  public String toString() {
    if (this.file == null) {
      return super.toString();
    } else {
      return this.file.toString();
    }
  }
}
