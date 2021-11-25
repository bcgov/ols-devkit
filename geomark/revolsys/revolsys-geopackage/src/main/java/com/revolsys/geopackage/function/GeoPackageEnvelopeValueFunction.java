package com.revolsys.geopackage.function;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;

import org.sqlite.Function;
import org.sqlite.SQLiteConnection;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geopackage.field.GeoPackageGeometryJdbcFieldDefinition;

public class GeoPackageEnvelopeValueFunction extends Function {
  public static final GeoPackageEnvelopeValueFunction MIN_X = new GeoPackageEnvelopeValueFunction(
    0);

  public static final GeoPackageEnvelopeValueFunction MAX_X = new GeoPackageEnvelopeValueFunction(
    1);

  public static final GeoPackageEnvelopeValueFunction MIN_Y = new GeoPackageEnvelopeValueFunction(
    2);

  public static final GeoPackageEnvelopeValueFunction MAX_Y = new GeoPackageEnvelopeValueFunction(
    3);

  public static void add(final SQLiteConnection dbConnection) throws SQLException {
    Function.create(dbConnection, "ST_MinX", MIN_X, 1, 0);
    Function.create(dbConnection, "ST_MaxX", MAX_X, 1, 0);
    Function.create(dbConnection, "ST_MinY", MIN_Y, 1, 0);
    Function.create(dbConnection, "ST_MaxY", MAX_Y, 1, 0);
  }

  private final int envelopeValueIndex;

  public GeoPackageEnvelopeValueFunction(final int envelopeValueIndex) {
    this.envelopeValueIndex = envelopeValueIndex;
  }

  @Override
  protected void xFunc() throws SQLException {
    final int argCount = args();
    if (argCount != 1) {
      throw new SQLException("Single argument is required. args: " + argCount);
    }

    final byte[] bytes = value_blob(0);
    final ByteBuffer buffer = ByteBuffer.wrap(bytes);
    if (buffer.get() == 'G') {
      if (buffer.get() == 'P') {
        final byte version = buffer.get();
        final byte flags = buffer.get();

        final boolean empty = (flags >> 4 & 1) == 1;
        if (!empty) {
          final int envelopeType = flags >> 1 & 7;

          ByteOrder byteOrder;
          if ((flags & 1) == 0) {
            byteOrder = ByteOrder.BIG_ENDIAN;
          } else {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
          }
          buffer.order(byteOrder);
          final int coordinateSystemId = buffer.getInt();
          double value;
          switch (envelopeType) {
            case 0:
              final GeometryFactory geometryFactory = GeometryFactory
                .floating2d(coordinateSystemId);
              final Geometry geometry = GeoPackageGeometryJdbcFieldDefinition
                .parseWkb(geometryFactory, bytes);
              final BoundingBox boundingBox = geometry.getBoundingBox();
              switch (this.envelopeValueIndex) {
                case 0:
                  value = boundingBox.getMinX();
                break;
                case 1:
                  value = boundingBox.getMaxX();
                break;
                case 2:
                  value = boundingBox.getMinY();
                break;
                case 3:
                  value = boundingBox.getMaxY();
                break;
                default:
                  return;
              }
            break;
            case 5:
            case 6:
            case 7:
              throw new SQLException("Invalid GeoPackage envelope type: " + envelopeType);
            default:
              for (int i = 0; i < this.envelopeValueIndex; i++) {
                buffer.getDouble();
              }
              value = buffer.getDouble();

          }
          result(value);
          return;
        }
      }
    }
    result();
  }

}
