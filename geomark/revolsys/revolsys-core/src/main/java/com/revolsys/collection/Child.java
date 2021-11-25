package com.revolsys.collection;

public interface Child<P> {

  <V extends P> V getParent();
}
