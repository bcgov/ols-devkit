package com.revolsys.record.io.format.esri.rest.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.net.urlcache.FileResponseCache;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.record.io.format.json.JsonParser.EventType;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class ArcGisRestServerFeatureIterator extends AbstractIterator<Record>
  implements RecordReader {
  private static Map<DataType, BiFunction<GeometryFactory, MapEx, Geometry>> GEOMETRY_CONVERTER_BY_TYPE = new HashMap<>();

  static {
    GEOMETRY_CONVERTER_BY_TYPE.put(GeometryDataTypes.POINT,
      ArcGisRestServerFeatureIterator::parsePoint);
    GEOMETRY_CONVERTER_BY_TYPE.put(GeometryDataTypes.MULTI_POINT,
      ArcGisRestServerFeatureIterator::parseMultiPoint);
    GEOMETRY_CONVERTER_BY_TYPE.put(GeometryDataTypes.MULTI_LINE_STRING,
      ArcGisRestServerFeatureIterator::parseMultiLineString);
    GEOMETRY_CONVERTER_BY_TYPE.put(GeometryDataTypes.MULTI_POLYGON,
      ArcGisRestServerFeatureIterator::parseMultiPolygon);
  }

  public static Geometry parseMultiLineString(final GeometryFactory geometryFactory,
    final MapEx properties) {
    final List<LineString> lines = new ArrayList<>();
    final List<List<List<Number>>> paths = properties.getValue("paths", Collections.emptyList());
    for (final List<List<Number>> points : paths) {
      final LineString lineString = geometryFactory.lineString(points);
      lines.add(lineString);
    }
    return geometryFactory.geometry(lines);
  }

  public static Geometry parseMultiPoint(final GeometryFactory geometryFactory,
    final MapEx properties) {
    final List<Point> lines = new ArrayList<>();
    final List<List<Number>> paths = properties.getValue("paths", Collections.emptyList());
    for (final List<Number> pointCoordinates : paths) {
      final Point point = geometryFactory.point(pointCoordinates);
      lines.add(point);
    }
    return geometryFactory.geometry(lines);
  }

  public static Geometry parseMultiPolygon(final GeometryFactory geometryFactory,
    final MapEx properties) {
    final List<Polygon> polygons = new ArrayList<>();
    final List<LinearRing> rings = new ArrayList<>();
    final List<List<List<Number>>> paths = properties.getValue("rings", Collections.emptyList());
    for (final List<List<Number>> points : paths) {
      final LinearRing ring = geometryFactory.linearRing(points);
      if (ring.isClockwise()) {
        if (!rings.isEmpty()) {
          final Polygon polygon = geometryFactory.polygon(rings);
          polygons.add(polygon);
        }
        rings.clear();
      }
      rings.add(ring);
    }
    if (!rings.isEmpty()) {
      final Polygon polygon = geometryFactory.polygon(rings);
      polygons.add(polygon);
    }
    return geometryFactory.geometry(polygons);
  }

  public static Geometry parsePoint(final GeometryFactory geometryFactory, final MapEx properties) {
    final double x = Maps.getDouble(properties, "x");
    final double y = Maps.getDouble(properties, "y");
    final double z = Maps.getDouble(properties, "z", Double.NaN);
    final double m = Maps.getDouble(properties, "m", Double.NaN);
    if (Double.isNaN(m)) {
      if (Double.isNaN(z)) {
        return geometryFactory.point(x, y);
      } else {
        return geometryFactory.point(x, y, z);
      }
    } else {
      return geometryFactory.point(x, y, z, m);
    }
  }

  private JsonParser parser;

  private boolean closed;

  private RecordDefinition recordDefinition;

  private RecordFactory<?> recordFacory;

  private BiFunction<GeometryFactory, MapEx, Geometry> geometryConverter;

  private GeometryFactory geometryFactory;

  private int recordCount = 0;

  private int pageRecordCount = 0;

  private Map<String, Object> queryParameters;

  private final int queryOffset;

  private final int queryLimit;

  private int pageSize;

  private final boolean supportsPaging;

  private final FeatureLayer layer;

  private Resource resource;

  private final String where;

  private final boolean pageByObjectId;

  private int totalRecordCount;

  private int currentRecordId = 0;

  private final String idFieldName;

  public ArcGisRestServerFeatureIterator(final FeatureLayer layer,
    final Map<String, Object> queryParameters, final int offset, final int limit,
    final RecordFactory<?> recordFactory, final boolean pageByObjectId) {
    this.layer = layer;
    this.queryParameters = queryParameters;
    this.where = (String)queryParameters.get("where");
    this.queryOffset = offset;
    this.queryLimit = limit;
    this.pageSize = layer.getMaxRecordCount();
    if (this.pageSize > 1000) {
      this.pageSize = 1000;
    }
    if (this.queryLimit < this.pageSize) {
      this.pageSize = this.queryLimit;
    }
    this.recordDefinition = layer.getRecordDefinition();
    this.recordFacory = recordFactory;
    if (this.recordDefinition.hasGeometryField()) {
      final DataType geometryType = this.recordDefinition.getGeometryField().getDataType();
      this.geometryConverter = GEOMETRY_CONVERTER_BY_TYPE.get(geometryType);
      this.geometryFactory = this.recordDefinition.getGeometryFactory();
      if (this.geometryConverter == null) {
        Logs.error(this, "Unsupported geometry type " + geometryType);
        throw new IllegalArgumentException("Unsupported geometry type " + geometryType);
      }
    }
    if (!pageByObjectId && layer.getCurrentVersion() >= 10.3 && layer.isSupportsPagination()) {
      this.supportsPaging = true;
      this.pageByObjectId = false;
    } else {
      final Map<String, Object> countParameters = new LinkedHashMap<>(queryParameters);
      this.totalRecordCount = layer.getRecordCount(countParameters, queryParameters);
      this.supportsPaging = false;
      this.pageByObjectId = true;
    }
    this.idFieldName = getIdFieldName();
  }

  @Override
  protected void closeDo() {
    FileUtil.closeSilent(this.parser);
    this.parser = null;
    this.geometryConverter = null;
    this.geometryFactory = null;
    this.queryParameters = null;
    this.recordDefinition = null;
    this.recordFacory = null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @SuppressWarnings("resource")
  @Override
  protected Record getNext() throws NoSuchElementException {
    int previousRecordOffset = this.currentRecordId;
    final int maxRetries = 3;
    for (int retry = 0; retry < maxRetries; retry++) {
      if (this.closed) {
        throw new NoSuchElementException();
      } else {
        JsonParser parser = this.parser;
        if (this.recordCount < this.queryLimit) {
          if (parser == null) {
            parser = newParser();
          }
          if (!parser.skipToNextObjectInArray()) {
            if (this.pageByObjectId) {
              parser = newParser();
              if (!parser.skipToNextObjectInArray()) {
                throw new NoSuchElementException();
              }
            } else if (this.supportsPaging) {
              if (this.pageRecordCount == this.pageSize) {
                parser = newParser();
                if (!parser.skipToNextObjectInArray()) {
                  throw new NoSuchElementException();
                }
              }
            } else {
              throw new NoSuchElementException();
            }
          }
        } else {
          throw new NoSuchElementException();
        }
        if (this.closed) {
          throw new NoSuchElementException();
        } else {
          try {
            if (parser.isEvent(EventType.endArray, EventType.endDocument)) {
              throw new NoSuchElementException();
            }

            final MapEx recordMap = this.parser.getMap();
            final Record record = this.recordFacory.newRecord(this.recordDefinition);
            record.setState(RecordState.INITIALIZING);

            final MapEx fieldValues = recordMap.getValue("attributes");
            this.currentRecordId = fieldValues.getInteger(this.idFieldName, -1);
            if (this.pageByObjectId) {
              if (this.currentRecordId == -1) {
                throw new NoSuchElementException();
              }
            }
            record.setValues(fieldValues);
            if (this.geometryConverter != null) {
              final MapEx geometryProperties = recordMap.getValue("geometry");
              if (Property.hasValue(geometryProperties)) {
                final Geometry geometry = this.geometryConverter.apply(this.geometryFactory,
                  geometryProperties);
                record.setGeometryValue(geometry);
              }
            }
            if (parser.hasNext()) {
              final EventType nextEvent = parser.next();
              if (nextEvent == EventType.endArray || nextEvent == EventType.endDocument) {
                this.parser = null;
              }
            } else {
              this.parser = null;
            }
            record.setState(RecordState.PERSISTED);
            this.pageRecordCount++;
            this.recordCount++;
            return record;
          } catch (final NoSuchElementException e) {
            throw e;
          } catch (final Throwable e) {
            if (retry + 1 == maxRetries) {
              throw new RuntimeException("Unable to read: " + getPathName(), e);
            }
            if (this.pageByObjectId) {
              if (this.currentRecordId == previousRecordOffset) {
                if (retry > 1) {
                  Logs.error(this, "Unable to read record: " + getPathName() + " "
                    + this.idFieldName + "=" + (this.currentRecordId + 1));
                  this.currentRecordId++;
                  previousRecordOffset = this.currentRecordId;
                }
              } else {
                Logs.error(this, "Unable to read record: " + getPathName() + " " + this.idFieldName
                  + "=" + (this.currentRecordId + 1));
                this.currentRecordId++;
              }
            } else {
              close();
              Exceptions.throwUncheckedException(e);
            }
          }
        }
      }
    }
    throw new RuntimeException("Unable to read: " + getPathName());
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  protected JsonParser newParser() {
    if (this.closed) {
      throw new NoSuchElementException();
    } else if (this.pageByObjectId && this.totalRecordCount == 0) {
      throw new NoSuchElementException();
    } else {
      this.pageRecordCount = 0;
      if (this.pageByObjectId) {
        String where;
        if (this.where == null || this.where.equals(this.idFieldName + " > 0")) {
          where = this.idFieldName + " > " + this.currentRecordId;
        } else {
          where = "(" + this.where + ") AND " + this.idFieldName + " > " + this.currentRecordId;
        }
        this.queryParameters.put("where", where);
        this.queryParameters.put("orderByFields", this.idFieldName);
      } else if (this.supportsPaging) {
        this.queryParameters.put("resultOffset", this.queryOffset + this.recordCount);
        if (this.pageSize > 0) {
          this.queryParameters.put("resultRecordCount", this.pageSize);
        }
      }
      this.resource = this.layer.getResource("query", this.queryParameters);
      try (
        BaseCloseable noCache = FileResponseCache.disable()) {
        this.parser = new JsonParser(this.resource);
      }
      if (!this.parser.skipToAttribute("features")) {
        throw new NoSuchElementException();
      }
      return this.parser;
    }
  }
}
