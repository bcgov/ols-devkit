package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.filter.AttributesEqualFilter;
import com.revolsys.record.filter.AttributesEqualOrNullFilter;

public class CompareFilterFactory implements Function<Record, Predicate<Record>> {
  private List<String> equalFieldNames = new ArrayList<>();

  private List<String> equalOrNullFieldNames = new ArrayList<>();

  @Override
  public Predicate<Record> apply(final Record object) {
    Predicate<Record> predicate = Predicates.all();
    if (!this.equalFieldNames.isEmpty()) {
      final Predicate<Record> valuesFilter = new AttributesEqualFilter(object,
        this.equalFieldNames);
      predicate = valuesFilter;
    }
    if (!this.equalOrNullFieldNames.isEmpty()) {
      final Predicate<Record> valuesFilter = new AttributesEqualOrNullFilter(object,
        this.equalOrNullFieldNames);
      predicate = predicate.and(valuesFilter);
    }

    return predicate;
  }

  public List<String> getEqualFieldNames() {
    return this.equalFieldNames;
  }

  public List<String> getEqualOrNullFieldNames() {
    return this.equalOrNullFieldNames;
  }

  public void setEqualFieldNames(final List<String> equalFieldNames) {
    this.equalFieldNames = equalFieldNames;
  }

  public void setEqualOrNullFieldNames(final List<String> equalOrNullFieldNames) {
    this.equalOrNullFieldNames = equalOrNullFieldNames;
  }

}
