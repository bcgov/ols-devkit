package com.revolsys.geometry.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.LineStringUtil;

public class LineEqualIgnoreDirectionFilter implements Predicate<LineString> {
  private final int dimension;

  private final LineString line;

  public LineEqualIgnoreDirectionFilter(final LineString line, final int dimension) {
    this.line = line;
    this.dimension = dimension;
  }

  @Override
  public boolean test(final LineString line) {
    return LineStringUtil.equalsIgnoreDirection(line, this.line, this.dimension);
  }

}
