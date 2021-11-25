package com.revolsys.record.query.functions;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.TableReference;
import com.revolsys.record.schema.RecordStore;

public class WithinDistance implements Condition, Function {
  public static final String NAME = "ST_DWithin";

  private QueryValue distanceValue;

  private QueryValue geometry1Value;

  private QueryValue geometry2Value;

  public WithinDistance(final QueryValue geometry1Value, final QueryValue geometry2Value,
    final QueryValue distanceValue) {
    this.geometry1Value = geometry1Value;
    this.geometry2Value = geometry2Value;
    this.distanceValue = distanceValue;
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    if (this.geometry1Value == null || this.geometry2Value == null || this.distanceValue == null) {
      sql.append("1 = 0");
    } else {
      sql.append(NAME + "(");
      this.geometry1Value.appendSql(query, recordStore, sql);
      sql.append(", ");
      this.geometry2Value.appendSql(query, recordStore, sql);
      sql.append(", ");
      this.distanceValue.appendSql(query, recordStore, sql);
      sql.append(")");
    }
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    if (this.geometry1Value != null) {
      index = this.geometry1Value.appendParameters(index, statement);
    }
    if (this.geometry2Value != null) {
      index = this.geometry2Value.appendParameters(index, statement);
    }
    if (this.distanceValue != null) {
      index = this.distanceValue.appendParameters(index, statement);
    }
    return index;
  }

  @Override
  public WithinDistance clone() {
    try {
      return (WithinDistance)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public WithinDistance clone(final TableReference oldTable, final TableReference newTable) {
    final WithinDistance clone = clone();
    if (this.geometry1Value != null) {
      clone.geometry1Value = this.geometry1Value.clone(oldTable, newTable);
    }
    if (this.geometry2Value != null) {
      clone.geometry2Value = this.geometry2Value.clone(oldTable, newTable);
    }
    if (this.distanceValue != null) {
      clone.distanceValue = this.distanceValue.clone(oldTable, newTable);
    }
    return clone;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof WithinDistance) {
      final WithinDistance condition = (WithinDistance)obj;
      if (DataType.equal(condition.geometry1Value, this.geometry1Value)) {
        if (DataType.equal(condition.geometry2Value, this.geometry1Value)) {
          if (DataType.equal(condition.distanceValue, this.distanceValue)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public QueryValue getDistanceValue() {
    return this.distanceValue;
  }

  public QueryValue getGeometry1Value() {
    return this.geometry1Value;
  }

  public QueryValue getGeometry2Value() {
    return this.geometry2Value;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public int getParameterCount() {
    return 3;
  }

  @Override
  public List<QueryValue> getParameters() {
    return Arrays.asList(this.geometry1Value, this.geometry2Value, this.distanceValue);
  }

  @Override
  public List<QueryValue> getQueryValues() {
    return Arrays.asList(this.geometry1Value, this.geometry2Value, this.distanceValue);
  }

  @Override
  public boolean test(final MapEx record) {
    if (this.geometry1Value == null || this.geometry2Value == null || this.distanceValue == null) {
      return false;
    } else {
      final Geometry geometry1 = this.geometry1Value.getValue(record);
      final Geometry geometry2 = this.geometry2Value.getValue(record);

      final Number acceptDistance = this.distanceValue.getValue(record);
      if (acceptDistance == null || geometry1 == null || geometry2 == null) {
        return false;
      } else {
        final double distance = geometry1.distanceGeometry(geometry2);
        return distance <= acceptDistance.doubleValue();
      }
    }
  }

  @Override
  public String toString() {
    final Object value = this.geometry1Value;
    final Object value1 = this.geometry2Value;
    final Object value2 = this.distanceValue;
    return "DWithin(" + DataTypes.toString(value) + "," + DataTypes.toString(value1) + ","
      + DataTypes.toString(value2) + ")";
  }

  @SuppressWarnings("unchecked")
  @Override
  public <QV extends QueryValue> QV updateQueryValues(final TableReference oldTable,
    final TableReference newTable,
    final java.util.function.Function<QueryValue, QueryValue> valueHandler) {
    final QueryValue distanceValue = valueHandler.apply(this.distanceValue);
    final QueryValue geometry1Value = valueHandler.apply(this.geometry1Value);
    final QueryValue geometry2Value = valueHandler.apply(this.geometry2Value);

    if (distanceValue == this.distanceValue && geometry1Value == this.geometry1Value
      && geometry2Value == this.geometry2Value) {
      return (QV)this;
    } else {
      final WithinDistance clone = clone();
      clone.distanceValue = distanceValue;
      clone.geometry1Value = geometry1Value;
      clone.geometry2Value = geometry2Value;
      return (QV)clone;
    }
  }
}
