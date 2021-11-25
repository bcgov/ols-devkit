package com.revolsys.gis.converter;

import java.util.function.Predicate;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.record.Record;

public class FilterRecordConverter {
  private Converter<Record, Record> converter;

  private Predicate<Record> filter;

  public FilterRecordConverter() {
  }

  public FilterRecordConverter(final Predicate<Record> filter,
    final Converter<Record, Record> converter) {
    this.filter = filter;
    this.converter = converter;
  }

  public Converter<Record, Record> getConverter() {
    return this.converter;
  }

  public Predicate<Record> getFilter() {
    return this.filter;
  }

  @Override
  public String toString() {
    return "filter=" + this.filter + "\nconverter=" + this.converter;
  }
}
