package com.revolsys.record.query.functions;

import java.util.Arrays;
import java.util.List;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.query.AbstractBinaryQueryValue;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.record.schema.RecordStore;

public class EnvelopeIntersects extends AbstractBinaryQueryValue implements Condition, Function {

  public static final String NAME = "ST_INTERSECTS";

  public EnvelopeIntersects(final QueryValue boundingBox1Value,
    final QueryValue boundingBox2Value) {
    super(boundingBox1Value, boundingBox2Value);
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder buffer) {
    buffer.append("ST_INTERSECTS(");
    appendLeft(buffer, query, recordStore);
    buffer.append(", ");
    appendRight(buffer, query, recordStore);
    buffer.append(")");
  }

  @Override
  public EnvelopeIntersects clone() {
    return (EnvelopeIntersects)super.clone();
  }

  @Override
  public EnvelopeIntersects clone(final TableReference oldTable, final TableReference newTable) {
    return (EnvelopeIntersects)super.clone(oldTable, newTable);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof EnvelopeIntersects) {
      final EnvelopeIntersects condition = (EnvelopeIntersects)obj;
      return super.equals(condition);
    }
    return false;
  }

  private BoundingBox getBoundingBox(final QueryValue queryValue, final MapEx record) {
    if (queryValue == null) {
      return null;
    } else {
      final Object value = queryValue.getValue(record);
      if (value instanceof BoundingBox) {
        return (BoundingBox)value;
      } else if (value instanceof Geometry) {
        final Geometry geometry = (Geometry)value;
        return geometry.getBoundingBox();
      } else {
        return null;
      }
    }
  }

  public QueryValue getBoundingBox1Value() {
    return getLeft();
  }

  public QueryValue getBoundingBox2Value() {
    return getRight();
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getParameterCount() {
    return 2;
  }

  @Override
  public List<QueryValue> getParameters() {
    return Arrays.asList(getLeft(), getRight());
  }

  @Override
  public boolean test(final MapEx record) {
    final QueryValue left = getLeft();
    final BoundingBox boundingBox1 = getBoundingBox(left, record);
    final QueryValue right = getRight();
    final BoundingBox boundingBox2 = getBoundingBox(right, record);
    if (boundingBox1 == null || boundingBox2 == null) {
      return false;
    } else {
      return boundingBox1.bboxIntersects(boundingBox2);
    }
  }

  @Override
  public String toString() {
    final QueryValue left = getLeft();
    final QueryValue right = getRight();
    return NAME + "(" + DataTypes.toString(left) + "," + DataTypes.toString(right) + ")";
  }
}
