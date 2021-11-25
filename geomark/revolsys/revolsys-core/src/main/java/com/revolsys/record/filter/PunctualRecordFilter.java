package com.revolsys.record.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.record.Record;

public class PunctualRecordFilter implements Predicate<Record> {
  public static final PunctualRecordFilter FILTER = new PunctualRecordFilter();

  private PunctualRecordFilter() {
  }

  @Override
  public boolean test(final Record record) {
    final Geometry geometry = record.getGeometry();
    return geometry instanceof Punctual;
  }

  @Override
  public String toString() {
    return "Punctual";
  }

}
