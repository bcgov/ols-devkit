package com.revolsys.geometry.graph.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.filter.EqualFilter;
import com.revolsys.geometry.filter.LinearIntersectionFilter;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.graph.comparator.EdgeLengthComparator;
import com.revolsys.geometry.graph.filter.EdgeObjectFilter;
import com.revolsys.geometry.graph.filter.EdgeTypeNameFilter;
import com.revolsys.geometry.model.LineString;
import com.revolsys.record.Record;
import com.revolsys.record.filter.RecordGeometryFilter;
import com.revolsys.util.ObjectProcessor;
import com.revolsys.util.count.LabelCountMap;
import com.revolsys.util.count.LabelCounters;
import com.revolsys.visitor.AbstractVisitor;

public class LinearIntersectionNotEqualLineEdgeCleanupVisitor extends AbstractVisitor<Edge<Record>>
  implements ObjectProcessor<RecordGraph> {

  private LabelCounters duplicateStatistics;

  private Set<String> equalExcludeFieldNames = new HashSet<>(
    Arrays.asList(Record.EXCLUDE_ID, Record.EXCLUDE_GEOMETRY));

  private Comparator<Record> newerComparator;

  public LinearIntersectionNotEqualLineEdgeCleanupVisitor() {
    super.setComparator(new EdgeLengthComparator<Record>(true));
  }

  @Override
  public void accept(final Edge<Record> edge) {
    final String typePath = edge.getTypeName();

    final Graph<Record> graph = edge.getGraph();
    final LineString line = edge.getLineString();

    Predicate<Edge<Record>> attributeAndGeometryFilter = new EdgeTypeNameFilter<>(typePath);

    final Predicate<Edge<Record>> filter = getPredicate();
    if (filter != null) {
      attributeAndGeometryFilter = attributeAndGeometryFilter.and(filter);
    }

    final Predicate<Record> notEqualLineFilter = new RecordGeometryFilter<>(new EqualFilter<>(line))
      .negate();

    final RecordGeometryFilter<LineString> linearIntersectionFilter = new RecordGeometryFilter<>(
      new LinearIntersectionFilter(line));

    attributeAndGeometryFilter = attributeAndGeometryFilter
      .and(new EdgeObjectFilter<>(notEqualLineFilter.and(linearIntersectionFilter)));

    final List<Edge<Record>> intersectingEdges = graph.getEdges(line, attributeAndGeometryFilter);

    if (!intersectingEdges.isEmpty()) {
      if (intersectingEdges.size() == 1 && line.getLength() > 10) {
        if (line.getVertexCount() > 2) {
          final Edge<Record> edge2 = intersectingEdges.get(0);
          final LineString line2 = edge2.getLineString();

          if (middleCoordinatesEqual(line, line2)) {
            final boolean firstEqual = line.equalsVertex(2, 0, line2, 0);
            if (!firstEqual) {
              final Node<Record> fromNode1 = edge.getFromNode();
              final Node<Record> fromNode2 = edge2.getFromNode();
              if (fromNode1.distancePoint(fromNode2) < 2) {
                graph.moveNodesToMidpoint(typePath, fromNode1, fromNode2);
                return;
              }
            }
            final boolean lastEqual = line.equalsVertex(2, line.getVertexCount() - 1, line2,
              line.getVertexCount() - 1);
            if (!lastEqual) {
              final Node<Record> toNode1 = edge.getToNode();
              final Node<Record> toNode2 = edge2.getToNode();
              if (toNode1.distancePoint(toNode2) < 2) {
                graph.moveNodesToMidpoint(typePath, toNode1, toNode2);
                return;
              }
            }
          }
        }
      }
      Logs.error(this, "Has intersecting edges " + line);
    }
  }

  @PreDestroy
  public void destroy() {
    if (this.duplicateStatistics != null) {
      this.duplicateStatistics.disconnect();
    }
    this.duplicateStatistics = null;
  }

  public Set<String> getEqualExcludeFieldNames() {
    return this.equalExcludeFieldNames;
  }

  public Comparator<Record> getNewerComparator() {
    return this.newerComparator;
  }

  @PostConstruct
  public void init() {
    this.duplicateStatistics = new LabelCountMap("Duplicate intersecting lines");
    this.duplicateStatistics.connect();
  }

  private boolean middleCoordinatesEqual(final LineString points1, final LineString points2) {
    if (points1.getVertexCount() == points2.getVertexCount()) {
      for (int i = 1; i < points2.getVertexCount(); i++) {
        if (!points1.equalsVertex(2, i, points1, i)) {
          return false;
        }
      }
      return true;

    } else {
      return false;
    }
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.forEachEdge(this);
  }

  @Override
  public void setComparator(final Comparator<Edge<Record>> comparator) {
    throw new IllegalArgumentException("Cannot override comparator");
  }

  public void setEqualExcludeFieldNames(final Collection<String> equalExcludeFieldNames) {
    setEqualExcludeFieldNames(new HashSet<>(equalExcludeFieldNames));
  }

  public void setEqualExcludeFieldNames(final Set<String> equalExcludeFieldNames) {
    this.equalExcludeFieldNames = new HashSet<>(equalExcludeFieldNames);
    this.equalExcludeFieldNames.add(Record.EXCLUDE_ID);
    this.equalExcludeFieldNames.add(Record.EXCLUDE_GEOMETRY);
  }

  public void setNewerComparator(final Comparator<Record> newerComparator) {
    this.newerComparator = newerComparator;
  }
}
