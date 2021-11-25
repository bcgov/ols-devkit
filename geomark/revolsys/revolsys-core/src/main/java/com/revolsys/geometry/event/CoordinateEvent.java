package com.revolsys.geometry.event;

import java.util.EventObject;

import com.revolsys.geometry.model.Point;

public class CoordinateEvent extends EventObject {
  public static final String NODE_ADDED = "Coordinate added";

  public static final String NODE_CHANGED = "Coordinate changed";

  public static final String NODE_REMOVED = "Coordinate removed";

  /**
   *
   */
  private static final long serialVersionUID = -1809350055079477785L;

  private String action;

  private String notes;

  private String ruleName;

  private String typePath;

  public CoordinateEvent(final Point coordinate) {
    super(coordinate);
  }

  public CoordinateEvent(final Point coordinate, final String ruleName, final String action) {
    super(coordinate);
    this.ruleName = ruleName;
    this.action = action;
  }

  public CoordinateEvent(final Point coordinate, final String path, final String ruleName,
    final String action, final String notes) {
    super(coordinate);
    this.typePath = path;
    this.ruleName = ruleName;
    this.action = action;
    this.notes = notes;
  }

  public String getAction() {
    return this.action;
  }

  public Point getCoordinate() {
    return (Point)getSource();
  }

  public String getNotes() {
    return this.notes;
  }

  public String getRuleName() {
    return this.ruleName;
  }

  public String getTypeName() {
    return this.typePath;
  }

}
