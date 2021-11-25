package com.revolsys.collection;

import java.lang.ref.WeakReference;

public class EmptyReference<T> extends WeakReference<T> {
  public EmptyReference() {
    super(null);
  }
}
