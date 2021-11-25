package com.revolsys.geometry.graph.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.EdgePair;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.record.Record;
import com.revolsys.record.property.DirectionalFields;

public class PseudoNodeAttribute {
  private final List<EdgePair<Record>> edgePairs = new ArrayList<>();

  private final Set<String> equalExcludeFieldNames = new HashSet<>();

  private final List<EdgePair<Record>> reversedEdgePairs = new ArrayList<>();

  private final String typePath;

  public PseudoNodeAttribute(final Node<Record> node, final String typePath,
    final Collection<String> equalExcludeFieldNames) {
    this.typePath = typePath;
    if (equalExcludeFieldNames != null) {
      this.equalExcludeFieldNames.addAll(equalExcludeFieldNames);
    }
    final Map<String, Map<LineString, Set<Edge<Record>>>> edgesByTypeNameAndLine = NodeProperties
      .getEdgesByTypeNameAndLine(node);
    final Map<LineString, Set<Edge<Record>>> edgesByLine = edgesByTypeNameAndLine.get(typePath);
    init(node, edgesByLine);
  }

  public List<EdgePair<Record>> getEdgePairs() {
    return this.edgePairs;
  }

  public List<EdgePair<Record>> getReversedEdgePairs() {
    return this.reversedEdgePairs;
  }

  public String getTypeName() {
    return this.typePath;
  }

  private void init(final Node<Record> node, final Map<LineString, Set<Edge<Record>>> edgesByLine) {
    if (isPseudoNode(node, edgesByLine)) {

    }
  }

  protected boolean isPseudoNode(final Node<Record> node,
    final Map<LineString, Set<Edge<Record>>> edgesByLine) {
    final Set<LineString> lines = edgesByLine.keySet();
    if (!LineStringUtil.hasLoop(lines)) {
      if (edgesByLine.size() == 2) {
        final Iterator<Set<Edge<Record>>> edgeIter = edgesByLine.values().iterator();
        final Set<Edge<Record>> edges1 = edgeIter.next();
        final Set<Edge<Record>> edges2 = edgeIter.next();
        final int size1 = edges1.size();
        final int size2 = edges2.size();
        if (size1 == size2) {
          if (size1 == 1) {
            final Edge<Record> edge1 = edges1.iterator().next();
            final Edge<Record> edge2 = edges2.iterator().next();
            final EdgePair<Record> edgePair = newEdgePair(node, edge1, edge2);
            if (edgePair != null) {
              if (edge1.getEnd(node) == edge2.getEnd(node)) {
                this.reversedEdgePairs.add(edgePair);
              } else {
                this.edgePairs.add(edgePair);
              }
              return true;
            }
          } else {
            final List<Edge<Record>> unmatchedEdges1 = new ArrayList<>(edges1);
            final List<Edge<Record>> unmatchedEdges2 = new ArrayList<>(edges2);
            // Find non-reversed matches
            matchEdges(node, unmatchedEdges1, unmatchedEdges2, this.edgePairs, false);
            if (unmatchedEdges2.isEmpty()) {
              return true;
            } else {
              // Find reversed matches
              matchEdges(node, unmatchedEdges1, unmatchedEdges2, this.reversedEdgePairs, true);
              if (unmatchedEdges2.isEmpty()) {
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }

  private void matchEdges(final Node<Record> node, final List<Edge<Record>> edges1,
    final List<Edge<Record>> edges2, final List<EdgePair<Record>> pairedEdges,
    final boolean reversed) {
    final Iterator<Edge<Record>> edgeIter1 = edges1.iterator();
    while (edgeIter1.hasNext()) {
      final Edge<Record> edge1 = edgeIter1.next();
      boolean matched = false;
      final Iterator<Edge<Record>> edgeIter2 = edges2.iterator();
      while (!matched && edgeIter2.hasNext()) {
        final Edge<Record> edge2 = edgeIter2.next();
        boolean match = false;
        if (edge1.getEnd(node) == edge2.getEnd(node)) {
          match = reversed;
        } else {
          match = !reversed;
        }
        if (match) {
          final EdgePair<Record> edgePair = newEdgePair(node, edge1, edge2);
          if (edgePair != null) {
            matched = true;
            edgeIter1.remove();
            edgeIter2.remove();
            pairedEdges.add(edgePair);
          }
        }
      }
    }
  }

  private EdgePair<Record> newEdgePair(final Node<Record> node, final Edge<Record> edge1,
    final Edge<Record> edge2) {
    final Record object1 = edge1.getObject();
    final Record object2 = edge2.getObject();
    if (DirectionalFields.canMergeRecords(node, object1, object2, this.equalExcludeFieldNames)) {
      return new EdgePair<>(edge1, edge2);
    } else {
      return null;
    }
  }
}
