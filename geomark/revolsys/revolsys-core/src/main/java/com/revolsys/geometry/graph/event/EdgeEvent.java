package com.revolsys.geometry.graph.event;

import java.util.EventObject;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.model.LineString;

public class EdgeEvent<T> extends EventObject {
  public static final String EDGE_ADDED = "Added";

  public static final String EDGE_CHANGED = "Changed";

  public static final String EDGE_REMOVED = "Removed";

  /**
   *
   */
  private static final long serialVersionUID = -2271176357444777709L;

  private String action;

  private final LineString line;

  private String notes;

  private String ruleName;

  private final String typePath;

  public EdgeEvent(final Edge<T> edge) {
    super(edge);
    this.line = edge.getLineString();
    this.typePath = edge.getTypeName();
  }

  public EdgeEvent(final Edge<T> edge, final String ruleName, final String action) {
    super(edge);
    this.line = edge.getLineString();
    this.typePath = edge.getTypeName();
    this.ruleName = ruleName;
    this.action = action;
  }

  public EdgeEvent(final Edge<T> edge, final String ruleName, final String action,
    final String notes) {
    super(edge);
    this.line = edge.getLineString();
    this.typePath = edge.getTypeName();
    this.ruleName = ruleName;
    this.action = action;
    this.notes = notes;
  }

  public String getAction() {
    return this.action;
  }

  public Edge<T> getEdge() {
    return getSource();
  }

  public LineString getLine() {
    return this.line;
  }

  public String getNotes() {
    return this.notes;
  }

  public T getObject() {
    return getEdge().getObject();
  }

  public String getRuleName() {
    return this.ruleName;
  }

  @Override
  public Edge<T> getSource() {
    return (Edge<T>)super.getSource();
  }

  public String getTypeName() {
    return this.typePath;
  }

  public boolean isAction(final String action) {
    return this.action.equals(action);
  }

  public boolean isAddAction() {
    return this.action.equals(EDGE_ADDED);
  }

}
