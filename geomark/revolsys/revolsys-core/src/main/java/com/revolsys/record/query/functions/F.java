package com.revolsys.record.query.functions;

import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class F {
  public static WithinDistance dWithin(final FieldDefinition field, final Geometry geometry,
    final double distance) {
    final Value geometryValue = Value.newValue(field, geometry);
    final Value distanceValue = Value.newValue(distance);
    return new WithinDistance(field, geometryValue, distanceValue);
  }

  public static WithinDistance dWithin(final String name, final Geometry geometry,
    double distance) {
    if (distance < 0) {
      distance = 0;
    }
    final Column column = new Column(name);
    final Value geometryValue = Value.newValue(geometry);
    final Value distanceValue = Value.newValue(distance);
    return new WithinDistance(column, geometryValue, distanceValue);
  }

  public static EnvelopeIntersects envelopeIntersects(final FieldDefinition field,
    final BoundingBox boundingBox) {
    if (field == null) {
      return null;
    } else {
      final Value value = Value.newValue(field, boundingBox);
      return new EnvelopeIntersects(field, value);
    }
  }

  public static EnvelopeIntersects envelopeIntersects(final List<QueryValue> values) {
    final QueryValue left = values.get(0);
    QueryValue right = values.get(1);
    if (!(left instanceof ColumnReference)) {
      throw new IllegalArgumentException(
        "geo.intersections first argument must be a column reference");
    }
    final ColumnReference field = (ColumnReference)left;
    if (right instanceof Value) {
      final Value value = (Value)right;
      final String text = value.getValue().toString();
      final FieldDefinition fieldDefinition = field.getFieldDefinition();
      final GeometryFactory geometryFactory = fieldDefinition.getGeometryFactory();
      final Geometry geometry = geometryFactory.geometry(text);
      right = Value.newValue(fieldDefinition, geometry);
    } else {
      throw new IllegalArgumentException(
        "geo.intersections first argument must be a geometry: " + right);
    }
    return new EnvelopeIntersects(left, right);
  }

  public static EnvelopeIntersects envelopeIntersects(final Query query,
    final BoundingBoxProxy boundingBox) {
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    final FieldDefinition field = recordDefinition.getGeometryField();
    final BoundingBox bbox = boundingBox.getBoundingBox();
    final Value value = Value.newValue(field, bbox);
    final EnvelopeIntersects condition = new EnvelopeIntersects(field, value);
    query.and(condition);
    return condition;
  }

  public static EnvelopeIntersects envelopeIntersects(final Query query, final Geometry geometry) {
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    final FieldDefinition field = recordDefinition.getGeometryField();
    final BoundingBox bbox = geometry.getBoundingBox();
    final Value value = Value.newValue(field, bbox);
    final EnvelopeIntersects condition = new EnvelopeIntersects(field, value);
    query.and(condition);
    return condition;
  }

  public static Lower lower(final QueryValue value) {
    return new Lower(value);
  }

  public static RegexpReplace regexpReplace(final QueryValue value, final String pattern,
    final String replace) {
    return new RegexpReplace(value, pattern, replace);
  }

  public static RegexpReplace regexpReplace(final QueryValue value, final String pattern,
    final String replace, final String flags) {
    return new RegexpReplace(value, pattern, replace, flags);
  }

  public static ToChar toChar(final ColumnReference column, final String format) {
    return new ToChar(column, format);
  }

  public static Upper upper(final FieldDefinition field) {
    return new Upper(field);
  }

  public static Upper upper(final QueryValue value) {
    return new Upper(value);
  }

  public static Upper upper(final String name) {
    final Column column = new Column(name);
    return upper(column);
  }
}
