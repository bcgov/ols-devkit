package com.revolsys.geometry.graph.visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.model.End;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class NodeRemovalVisitor implements Consumer<Node<Record>> {

  private final Collection<String> excludedFieldNames = new HashSet<>();

  private final RecordGraph graph;

  public NodeRemovalVisitor(final RecordDefinition recordDefinition, final RecordGraph graph,
    final Collection<String> excludedFieldNames) {
    super();
    this.graph = graph;
    if (excludedFieldNames != null) {
      this.excludedFieldNames.addAll(excludedFieldNames);
    }
  }

  @Override
  public void accept(final Node<Record> node) {
    if (node.getDegree() == 2) {
      final List<Edge<Record>> edges = node.getEdges();
      if (edges.size() == 2) {
        final Edge<Record> edge1 = edges.get(0);
        final Edge<Record> edge2 = edges.get(1);
        if (edge1 != edge2) {
          final Record object1 = edge1.getObject();
          final Record object2 = edge2.getObject();
          if (DataType.equal(object1, object2, this.excludedFieldNames)) {
            final End end1 = edge1.getEnd(node);
            if (end1 == edge2.getEnd(node)) {
              // if (!fixReversedEdges(node, reversedEdges, edge1, edge2)) {
              return;
              // }
            }
            if (end1.isFrom()) {
              this.graph.merge(node, edge2, edge1);
            } else {
              this.graph.merge(node, edge1, edge2);
            }
          }
        }
      }
    }
  }

}
