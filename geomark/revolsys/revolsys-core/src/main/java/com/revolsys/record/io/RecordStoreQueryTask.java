package com.revolsys.record.io;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.parallel.process.AbstractProcess;
import com.revolsys.record.Record;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreQueryTask extends AbstractProcess {

  private final BoundingBox boundingBox;

  private List<Record> objects;

  private final String path;

  private final RecordStore recordStore;

  public RecordStoreQueryTask(final RecordStore recordStore, final String path,
    final BoundingBox boundingBox) {
    this.recordStore = recordStore;
    this.path = path;
    this.boundingBox = boundingBox;
  }

  public void cancel() {
    this.objects = null;
  }

  @Override
  public String getBeanName() {
    return getClass().getName();
  }

  @Override
  public void run() {
    this.objects = new ArrayList<>();
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(this.path);
    final Query query = Query.intersects(recordDefinition, this.boundingBox);
    try (
      final RecordReader reader = this.recordStore.getRecords(query)) {
      for (final Record object : reader) {
        try {
          this.objects.add(object);
        } catch (final NullPointerException e) {
          return;
        }
      }
    }
  }

  @Override
  public void setBeanName(final String name) {
  }
}
