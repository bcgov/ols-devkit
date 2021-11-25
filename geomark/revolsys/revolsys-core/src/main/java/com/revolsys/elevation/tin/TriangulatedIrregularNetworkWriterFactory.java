package com.revolsys.elevation.tin;

import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;

public interface TriangulatedIrregularNetworkWriterFactory extends IoFactory {
  TriangulatedIrregularNetworkWriter newTriangulatedIrregularNetworkWriter(final Resource resource);
}
