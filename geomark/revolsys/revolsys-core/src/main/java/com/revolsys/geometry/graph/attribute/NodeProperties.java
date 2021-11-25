package com.revolsys.geometry.graph.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.jeometry.common.compare.NumericComparator;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class NodeProperties {
  protected static class Methods {
    public static Set<Double> edgeAngles(final Node<?> node) {
      final Set<Double> angles = new TreeSet<>(new NumericComparator<Double>());
      for (final Edge<?> edge : node.getInEdges()) {
        final double toAngle = edge.getToAngle();
        angles.add(toAngle);
      }
      for (final Edge<?> edge : node.getOutEdges()) {
        final double fromAngle = edge.getFromAngle();
        angles.add(fromAngle);
      }
      return angles;
    }

    public static Map<String, Set<Double>> edgeAnglesByType(final Node<?> node) {
      final Map<String, Set<Double>> anglesByType = new HashMap<>();
      for (final Edge<?> edge : node.getInEdges()) {
        final String typePath = edge.getTypeName();
        final double toAngle = edge.getToAngle();
        final Set<Double> angles = getAnglesForType(anglesByType, typePath);
        angles.add(toAngle);
      }
      for (final Edge<?> edge : node.getOutEdges()) {
        final String typePath = edge.getTypeName();
        final double fromAngle = edge.getFromAngle();
        final Set<Double> angles = getAnglesForType(anglesByType, typePath);
        angles.add(fromAngle);
      }
      return anglesByType;
    }

    public static Set<RecordDefinition> edgeRecordDefinitions(final Node<?> node) {
      final Set<RecordDefinition> recordDefinitions = new HashSet<>();
      for (final Edge<?> edge : node.getEdges()) {
        final Object object = edge.getObject();
        if (object instanceof Record) {
          final Record record = (Record)object;
          final RecordDefinition recordDefinition = record.getRecordDefinition();
          recordDefinitions.add(recordDefinition);
        }
      }
      return recordDefinitions;
    }

    public static <T> Map<LineString, Map<String, Set<Edge<T>>>> edgesByLineAndTypeName(
      final Node<T> node) {
      final List<Edge<T>> edges = node.getEdges();
      final Map<LineString, Map<String, Set<Edge<T>>>> lineEdgeMap = new HashMap<>();
      for (final Edge<T> edge : new HashSet<>(edges)) {
        LineString line = edge.getLineString();
        Map<String, Set<Edge<T>>> edgesByType = edgesByTypeForLine(lineEdgeMap, line);
        if (edgesByType == null) {
          edgesByType = new HashMap<>();
          if (edge.getEnd(node).isTo()) {
            line = line.reverse();
          }
          lineEdgeMap.put(line, edgesByType);
        }
        Set<Edge<T>> typeEdges = edgesByType.get(edge.getTypeName());
        if (typeEdges == null) {
          typeEdges = new HashSet<>();
          final String typePath = edge.getTypeName();
          edgesByType.put(typePath, typeEdges);
        }
        typeEdges.add(edge);
      }
      return lineEdgeMap;
    }

    public static <T> Map<String, List<Edge<T>>> edgesByType(final Node<T> node) {
      final Map<String, List<Edge<T>>> edgesByType = new HashMap<>();
      for (final Edge<T> edge : node.getEdges()) {
        final String typePath = edge.getTypeName();
        List<Edge<T>> typeEdges = edgesByType.get(typePath);
        if (typeEdges == null) {
          typeEdges = new ArrayList<>();
          edgesByType.put(typePath, typeEdges);
        }
        typeEdges.add(edge);
      }
      return edgesByType;
    }

    private static <T> Map<String, Set<Edge<T>>> edgesByTypeForLine(
      final Map<LineString, Map<String, Set<Edge<T>>>> lineEdgeMap, final LineString line) {
      for (final Entry<LineString, Map<String, Set<Edge<T>>>> entry : lineEdgeMap.entrySet()) {
        final LineString keyLine = entry.getKey();
        if (LineStringUtil.equalsIgnoreDirection2d(line, keyLine)) {
          return entry.getValue();
        }
      }
      return null;
    }

    public static <T> Map<String, Map<LineString, Set<Edge<T>>>> edgesByTypeNameAndLine(
      final Node<T> node) {
      final List<Edge<T>> edges = node.getEdges();
      final Map<String, Map<LineString, Set<Edge<T>>>> typeLineEdgeMap = new HashMap<>();
      for (final Edge<T> edge : new HashSet<>(edges)) {
        final String typePath = edge.getTypeName();
        Map<LineString, Set<Edge<T>>> lineEdgeMap = typeLineEdgeMap.get(typePath);
        if (lineEdgeMap == null) {
          lineEdgeMap = new HashMap<>();
          typeLineEdgeMap.put(typePath, lineEdgeMap);
        }

        Edge.addEdgeToEdgesByLine(node, lineEdgeMap, edge);
      }
      return typeLineEdgeMap;
    }

    public static Set<String> edgeTypeNames(final Node<?> node) {
      final Set<String> typePaths = new HashSet<>();
      for (final Edge<?> edge : node.getEdges()) {
        final String typePath = edge.getTypeName();
        typePaths.add(typePath);
      }
      return typePaths;
    }

    public static Set<Double> getAnglesForType(final Map<String, Set<Double>> anglesByType,
      final String typePath) {
      Set<Double> angles = anglesByType.get(typePath);
      if (angles == null) {
        angles = new TreeSet<>(new NumericComparator<Double>());
        anglesByType.put(typePath, angles);
      }
      return angles;
    }
  }

  public static Set<Double> getEdgeAngles(final Node<?> node) {
    return getField(node, "edgeAngles", Methods::edgeAngles);
  }

  public static Map<String, Set<Double>> getEdgeAnglesByType(final Node<?> node) {
    return getField(node, "edgeAnglesByType", Methods::edgeAnglesByType);
  }

  public static <T> Set<Double> getEdgeAnglesByType(final Node<T> node, final String typePath) {
    final Map<String, Set<Double>> anglesByType = getEdgeAnglesByType(node);
    final Set<Double> angles = anglesByType.get(typePath);
    return angles;
  }

  public static Set<RecordDefinition> getEdgeRecordDefinitions(final Node<? extends Object> node) {
    return getField(node, "edgeRecordDefinitions", Methods::edgeRecordDefinitions);
  }

  /**
   * Get the map of edge angles, which contains a map of type names to the list
   * of edges with that angle and type name.
   *
   * @param <T>
   * @param node The node.
   * @return The map.
   */
  public static <T> Map<LineString, Map<String, Set<Edge<T>>>> getEdgesByLineAndTypeName(
    final Node<T> node) {
    return getField(node, "edgesByLineAndTypeName", Methods::edgesByLineAndTypeName);
  }

  public static <T> Map<String, List<Edge<T>>> getEdgesByType(final Node<T> node) {
    return getField(node, "edgesByType", Methods::edgesByType);
  }

  public static <T> List<Edge<T>> getEdgesByType(final Node<T> node, final String typePath) {
    final Map<String, List<Edge<T>>> edgesByType = getEdgesByType(node);
    final List<Edge<T>> edges = edgesByType.get(typePath);
    if (edges != null) {
      return new ArrayList<>(edges);
    }
    return Collections.emptyList();
  }

  public static <T> Map<String, Map<LineString, Set<Edge<T>>>> getEdgesByTypeNameAndLine(
    final Node<T> node) {
    return getField(node, "edgesByTypeNameAndLine", Methods::edgesByTypeNameAndLine);
  }

  public static Set<String> getEdgeTypeNames(final Node<? extends Object> node) {
    return getField(node, "edgeTypeNames", Methods::edgeTypeNames);
  }

  @SuppressWarnings("unchecked")
  private static <T, V> V getField(final Node<T> node, final String name,
    final Function<Node<T>, V> function) {
    final String fieldName = NodeProperties.class.getName() + "." + name;
    if (!node.hasProperty(fieldName)) {
      final FunctionObjectPropertyProxy<Node<T>, V> proxy = new FunctionObjectPropertyProxy<>(
        function);
      node.setProperty(fieldName, proxy);
    }
    final V value = (V)node.getProperty(fieldName);
    return value;
  }

}
