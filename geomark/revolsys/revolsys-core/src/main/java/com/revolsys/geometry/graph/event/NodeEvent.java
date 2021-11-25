package com.revolsys.geometry.graph.event;

import java.util.EventObject;

import com.revolsys.geometry.graph.Node;

public class NodeEvent<T> extends EventObject {
  public static final String NODE_ADDED = "Added";

  public static final String NODE_CHANGED = "Changed";

  public static final String NODE_REMOVED = "Removed";

  /**
   *
   */
  private static final long serialVersionUID = 6966061452365729885L;

  private String action;

  private String notes;

  private String ruleName;

  private String typePath;

  public NodeEvent(final Node<T> node) {
    super(node);
  }

  public NodeEvent(final Node<T> node, final String ruleName, final String action) {
    super(node);
    this.ruleName = ruleName;
    this.action = action;
  }

  public NodeEvent(final Node<T> node, final String typePath, final String ruleName,
    final String action, final String notes) {
    super(node);
    this.typePath = typePath;
    this.ruleName = ruleName;
    this.action = action;
    this.notes = notes;
  }

  public String getAction() {
    return this.action;
  }

  public Node<T> getNode() {
    return (Node<T>)getSource();
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

  public boolean isAction(final String action) {
    return this.action.equals(action);
  }

  public boolean isAddAction() {
    return this.action.equals(NODE_ADDED);
  }
}
