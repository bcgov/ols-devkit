package com.revolsys.gis.converter.process;

public abstract class AbstractSourceToTargetProcess<T1, T2>
  implements SourceToTargetProcess<T1, T2> {
  @Override
  public void close() {
  }

  @Override
  public void init() {
  }
}
