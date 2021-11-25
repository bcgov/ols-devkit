package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.Punctual;

public interface PunctualEditor extends GeometryEditor<PointEditor>, Punctual {

  @Override
  Punctual newGeometry();
}
