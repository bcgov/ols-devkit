package com.revolsys.record.io.format.shp;

import java.io.File;

import com.revolsys.io.FileUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.RecordDefinition;

public class ShapefileRecordStore extends AbstractRecordStore {
  private ShapefileDirectoryWriter writer;

  public ShapefileRecordStore(final File directory) {
    directory.mkdirs();
    this.writer = new ShapefileDirectoryWriter(directory);
    this.writer.setLogCounts(false);
  }

  @Override
  public void close() {
    super.close();
    FileUtil.closeSilent(this.writer);
    this.writer = null;
  }

  @Override
  public int getRecordCount(final Query query) {
    return 0;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return this.writer.getRecordDefinition(typePath);
  }

  @Override
  public String getRecordStoreType() {
    return ShapefileConstants.DESCRIPTION;
  }

  @Override
  public void insertRecord(final Record record) {
    this.writer.write(record);
  }

  @Override
  public Record newRecord(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final RecordDefinition savedRecordDefinition = getRecordDefinition(typePath);
    if (savedRecordDefinition == null) {
      return new ArrayRecord(recordDefinition);
    } else {
      return new ArrayRecord(savedRecordDefinition);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    return this.writer;
  }

}
