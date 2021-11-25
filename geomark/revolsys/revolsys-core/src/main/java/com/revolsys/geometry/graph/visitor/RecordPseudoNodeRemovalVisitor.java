package com.revolsys.geometry.graph.visitor;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.EdgePair;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.graph.attribute.NodeProperties;
import com.revolsys.geometry.graph.attribute.PseudoNodeAttribute;
import com.revolsys.geometry.graph.attribute.PseudoNodeProperty;
import com.revolsys.predicate.PredicateProxy;
import com.revolsys.record.Record;
import com.revolsys.record.property.DirectionalFields;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.count.LabelCountMap;
import com.revolsys.util.count.LabelCounters;

/**
 * Find and remove nodes that have exactly two edges for each feature type with
 * the same attribution and have the same geometry across all feature types.
 *
 * @author Paul Austin
 */
public class RecordPseudoNodeRemovalVisitor extends AbstractNodeListenerVisitor<Record>
  implements PredicateProxy<Node<Record>> {

  private LabelCounters mergedStatistics;

  private Predicate<Node<Record>> predicate;

  public RecordPseudoNodeRemovalVisitor() {
  }

  @Override
  public void accept(final Node<Record> node) {
    if (node.getEdges().size() > 1) {
      processPseudoNodes(node);
    }
  }

  @PreDestroy
  public void destroy() {
    if (this.mergedStatistics != null) {
      this.mergedStatistics.disconnect();
    }
    this.mergedStatistics = null;
  }

  @Override
  public Predicate<Node<Record>> getPredicate() {
    return this.predicate;
  }

  @PostConstruct
  public void init() {
    this.mergedStatistics = new LabelCountMap("Merged at psuedo node");
    this.mergedStatistics.connect();
  }

  private void mergeEdgePairs(final Node<Record> node, final List<EdgePair<Record>> edgePairs) {
    if (edgePairs != null) {
      for (final EdgePair<Record> edgePair : edgePairs) {
        final Edge<Record> edge1 = edgePair.getEdge1();
        final Edge<Record> edge2 = edgePair.getEdge2();
        final Record object = edge1.getObject();
        if (mergeEdges(node, edge1, edge2) != null) {
          this.mergedStatistics.addCount(object);
        }
      }
    }
  }

  protected Edge<Record> mergeEdges(final Node<Record> node, final Edge<Record> edge1,
    final Edge<Record> edge2) {
    final Record object1 = edge1.getObject();

    final Record object2 = edge2.getObject();

    final Record newObject = mergeObjects(node, object1, object2);
    // newObject.setIdValue(null);

    final RecordGraph graph = (RecordGraph)edge1.getGraph();
    final Edge<Record> newEdge = graph.addEdge(newObject);
    graph.remove(edge1);
    graph.remove(edge2);
    return newEdge;
  }

  protected Record mergeObjects(final Node<Record> node, final Record object1,
    final Record object2) {
    return DirectionalFields.merge(node, object1, object2);
  }

  private void processPseudoNodes(final Node<Record> node) {
    for (final RecordDefinition recordDefinition : NodeProperties.getEdgeRecordDefinitions(node)) {
      final PseudoNodeProperty property = PseudoNodeProperty.getProperty(recordDefinition);

      final PseudoNodeAttribute pseudoNodeAttribute = property.getProperty(node);
      processPseudoNodesForType(node, pseudoNodeAttribute);
    }
  }

  private void processPseudoNodesForType(final Node<Record> node,
    final PseudoNodeAttribute pseudoNodeAttribute) {
    final List<EdgePair<Record>> reversedEdgePairs = pseudoNodeAttribute.getReversedEdgePairs();
    mergeEdgePairs(node, reversedEdgePairs);

    final List<EdgePair<Record>> edgePairs = pseudoNodeAttribute.getEdgePairs();
    mergeEdgePairs(node, edgePairs);
  }

  public void setFilter(final Predicate<Node<Record>> filter) {
    this.predicate = filter;
  }
}
