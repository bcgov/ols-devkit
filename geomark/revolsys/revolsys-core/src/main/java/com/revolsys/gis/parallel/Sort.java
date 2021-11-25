package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.record.comparator.RecordFieldComparator;

public class Sort extends BaseInOutProcess<Record, Record> {

  private Comparator<Record> comparator;

  private String fieldName;

  private final List<Record> objects = new ArrayList<>();

  public Comparator<Record> getComparator() {
    return this.comparator;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    if (this.comparator != null) {
      Collections.sort(this.objects, this.comparator);
    }
    for (final Record object : this.objects) {
      out.write(object);
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    this.objects.add(object);
  }

  public void setComparator(final Comparator<Record> comparator) {
    this.comparator = comparator;
  }

  public void setFieldName(final String fieldName) {
    this.fieldName = fieldName;
    this.comparator = new RecordFieldComparator(fieldName);
  }

}
