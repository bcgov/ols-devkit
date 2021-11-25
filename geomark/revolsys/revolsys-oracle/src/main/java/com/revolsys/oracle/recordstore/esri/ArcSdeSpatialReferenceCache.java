package com.revolsys.oracle.recordstore.esri;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.record.schema.RecordStoreSchema;

public class ArcSdeSpatialReferenceCache {

  public static ArcSdeSpatialReferenceCache get(final AbstractJdbcRecordStore recordStore) {
    ArcSdeSpatialReferenceCache spatialReferences = recordStore
      .getProperty("esriSpatialReferences");
    if (spatialReferences == null) {
      spatialReferences = new ArcSdeSpatialReferenceCache();
      recordStore.setProperty("esriSpatialReferences", spatialReferences);
    }
    return spatialReferences;
  }

  public static ArcSdeSpatialReferenceCache get(final RecordStoreSchema schema) {
    final AbstractJdbcRecordStore recordStore = (AbstractJdbcRecordStore)schema.getRecordStore();
    return get(recordStore);

  }

  protected static ArcSdeSpatialReference getSpatialReference(final Connection connection,
    final RecordStoreSchema schema, final int esriSrid) {
    final ArcSdeSpatialReferenceCache cache = get(schema);
    return cache.getSpatialReference(connection, esriSrid);
  }

  private final Map<Integer, ArcSdeSpatialReference> spatialReferences = new HashMap<>();

  public ArcSdeSpatialReferenceCache() {
  }

  protected synchronized ArcSdeSpatialReference getSpatialReference(final Connection connection,
    final int esriSrid) {
    ArcSdeSpatialReference spatialReference = this.spatialReferences.get(esriSrid);
    if (spatialReference == null) {
      spatialReference = getSpatialReference(connection,
        "SELECT SRID, SR_NAME, X_OFFSET, Y_OFFSET, Z_OFFSET, M_OFFSET, XYUNITS, Z_SCALE, M_SCALE, CS_ID, DEFINITION FROM SDE.ST_SPATIAL_REFERENCES WHERE SRID = ?",
        esriSrid);
      if (spatialReference == null) {
        spatialReference = getSpatialReference(connection,
          "SELECT SRID, DESCRIPTION, FALSEX, FALSEY, FALSEZ, FALSEM, XYUNITS, ZUNITS, MUNITS, AUTH_SRID, SRTEXT FROM SDE.SPATIAL_REFERENCES WHERE SRID = ?",
          esriSrid);
      }
    }
    return spatialReference;
  }

  protected ArcSdeSpatialReference getSpatialReference(final Connection connection,
    final String sql, final int esriSrid) {
    try (
      PreparedStatement statement = connection.prepareStatement(sql);) {
      statement.setInt(1, esriSrid);
      try (
        ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          final String name = resultSet.getString(2);
          final BigDecimal xOffset = resultSet.getBigDecimal(3);
          final BigDecimal yOffset = resultSet.getBigDecimal(4);
          final BigDecimal zOffset = resultSet.getBigDecimal(5);
          final BigDecimal mOffset = resultSet.getBigDecimal(6);
          final double scale = resultSet.getBigDecimal(7).doubleValue();
          final double zScale = resultSet.getBigDecimal(8).doubleValue();
          final double mScale = resultSet.getBigDecimal(9).doubleValue();
          final int srid = resultSet.getInt(10);
          final String wkt = resultSet.getString(11);
          final GeometryFactory geometryFactory;
          if (srid <= 0) {
            if ("UNKNOWN".equalsIgnoreCase(wkt)) {
              geometryFactory = GeometryFactory.fixed3d(scale, scale, zScale);
            } else {
              geometryFactory = GeometryFactory.fixed3d(wkt, scale, scale, zScale);
            }
          } else {
            geometryFactory = GeometryFactory.fixed3d(srid, scale, scale, zScale);
          }

          final ArcSdeSpatialReference spatialReference = new ArcSdeSpatialReference(
            geometryFactory);
          spatialReference.setEsriSrid(esriSrid);
          spatialReference.setName(name);
          spatialReference.setXOffset(xOffset);
          spatialReference.setYOffset(yOffset);
          spatialReference.setZOffset(zOffset);
          spatialReference.setMOffset(mOffset);
          spatialReference.setXyScale(scale);
          spatialReference.setZScale(zScale);
          spatialReference.setMScale(mScale);
          spatialReference.setSrid(srid);
          spatialReference.setCsWkt(wkt);
          this.spatialReferences.put(esriSrid, spatialReference);
          return spatialReference;
        }
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to get srid " + esriSrid, e);
    }
    return null;
  }

}
