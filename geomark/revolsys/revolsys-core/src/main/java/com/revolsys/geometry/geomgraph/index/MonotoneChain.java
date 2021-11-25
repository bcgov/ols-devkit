package com.revolsys.geometry.geomgraph.index;

public class MonotoneChain {

  final int chainIndex;

  final MonotoneChainEdge edge;

  public MonotoneChain(final MonotoneChainEdge edge, final int chainIndex) {
    this.edge = edge;
    this.chainIndex = chainIndex;
  }

  public MonotoneChainEdge getEdge() {
    return this.edge;
  }
}
