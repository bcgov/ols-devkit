package com.revolsys.record.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;

public class RecordEqualsFilter<R extends Record> implements Predicate<R> {
  private final Collection<CharSequence> equalExclude = new HashSet<>();

  private final R searchRecord;

  public RecordEqualsFilter(final R searchRecord) {
    this(searchRecord, null);
  }

  public RecordEqualsFilter(final R searchRecord,
    final Collection<? extends CharSequence> equalExclude) {
    this.searchRecord = searchRecord;
    if (equalExclude != null) {
      this.equalExclude.addAll(equalExclude);
    }
  }

  @Override
  public boolean test(final R object) {
    final Geometry serachGeometry = this.searchRecord.getGeometry();
    final Geometry geometry = object.getGeometry();

    if (DataType.equal(serachGeometry, geometry, this.equalExclude)) {
      if (DataType.equal(this.searchRecord, object, this.equalExclude)) {
        return true;
      }
    }
    return false;
  }

}
