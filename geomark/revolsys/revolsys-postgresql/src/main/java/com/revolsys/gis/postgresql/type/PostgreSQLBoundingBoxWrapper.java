package com.revolsys.gis.postgresql.type;

import java.sql.SQLException;

import org.postgresql.util.PGobject;

import com.revolsys.geometry.model.BoundingBox;

public class PostgreSQLBoundingBoxWrapper extends PGobject {

  private static final long serialVersionUID = 0L;

  public PostgreSQLBoundingBoxWrapper() {
  }

  public PostgreSQLBoundingBoxWrapper(final BoundingBox boundingBox) {
    setBoundingBox(boundingBox);
  }

  @Override
  public PostgreSQLBoundingBoxWrapper clone() {
    try {
      return (PostgreSQLBoundingBoxWrapper)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  public BoundingBox getBoundingBox() {
    return null;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    final StringBuilder string = new StringBuilder();
    if (boundingBox.getAxisCount() > 2) {
      setType("box3d");
      string.append("BOX3D(");
      string.append(boundingBox.getMinX());
      string.append(' ');
      string.append(boundingBox.getMinY());
      string.append(' ');
      string.append(boundingBox.getMin(2));
      string.append(',');
      string.append(boundingBox.getMaxX());
      string.append(' ');
      string.append(boundingBox.getMaxY());
      string.append(' ');
      string.append(boundingBox.getMax(2));
      string.append(')');
    } else {
      setType("box2d");
      string.append("BOX(");
      string.append(boundingBox.getMinX());
      string.append(' ');
      string.append(boundingBox.getMinY());
      string.append(',');
      string.append(boundingBox.getMaxX());
      string.append(' ');
      string.append(boundingBox.getMaxY());
      string.append(')');
    }
    try {
      super.setValue(string.toString());
    } catch (final SQLException e) {
    }
  }
}
