package com.revolsys.geometry.noding;

import com.revolsys.geometry.index.chain.MonotoneChain;
import com.revolsys.geometry.index.strtree.StrTree;

public class MonotoneChainIndex extends StrTree<MonotoneChain> {
  private static final long serialVersionUID = 1L;

  public void insertItem(final MonotoneChain chain) {
    super.insertItem(chain, chain);
  }
}
