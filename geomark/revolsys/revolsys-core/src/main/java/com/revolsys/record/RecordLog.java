package com.revolsys.record;

import java.nio.file.Path;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Writer;
import com.revolsys.io.file.AtomicPathUpdator;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.util.Counter;
import com.revolsys.util.LongCounter;

public class RecordLog implements BaseCloseable {
  private static final String LOG_MESSAGE = "LOG_MESSAGE";

  public static final String LOG_LOCALITY = "LOG_LOCALITY";

  public static void error(final Class<?> logCategory, final String message, final Record record) {
    throw new UnsupportedOperationException();
    // final RecordLog recordLog = getForThread();
    // if (record == null) {
    // Logs.error(logCategory, message + "\tnull");
    // } else if (recordLog == null) {
    // final RecordDefinition recordDefinition = record.getRecordDefinition();
    // Logs.error(logCategory, message + "\t" + recordDefinition.getPath() +
    // record.getIdentifier());
    // } else {
    // recordLog.error(message, record);
    // }
  }

  public static RecordDefinition getLogRecordDefinition(
    final RecordDefinitionProxy recordDefinition, final boolean usesLocality) {
    if (usesLocality) {
      return getLogRecordDefinition(recordDefinition, LOG_LOCALITY);
    } else {
      return getLogRecordDefinition(recordDefinition);
    }
  }

  public static RecordDefinition getLogRecordDefinition(
    final RecordDefinitionProxy recordDefinition, final String... prefixFieldNames) {
    final RecordDefinition recordDefinition2 = recordDefinition.getRecordDefinition();
    final String path = recordDefinition2.getPath();
    final String parentPath = PathUtil.getPath(path);
    final String tableName = PathUtil.getName(path);
    final String logTableName;
    if (tableName.toUpperCase().equals(tableName)) {
      logTableName = tableName + "_LOG";
    } else {
      logTableName = tableName + "_log";
    }
    final PathName logTypeName = PathName.newPathName(PathUtil.toPath(parentPath, logTableName));
    final RecordDefinitionImpl logRecordDefinition = new RecordDefinitionImpl(logTypeName);
    for (final String fieldName : prefixFieldNames) {
      logRecordDefinition.addField(fieldName, DataTypes.STRING, 255, false);
    }
    logRecordDefinition.addField(LOG_MESSAGE, DataTypes.STRING, 255, true);
    for (final FieldDefinition fieldDefinition : recordDefinition2.getFields()) {
      final FieldDefinition logFieldDefinition = new FieldDefinition(fieldDefinition);
      final DataType dataType = logFieldDefinition.getDataType();
      if (recordDefinition2.getGeometryField() == fieldDefinition) {
        logRecordDefinition.addField("GEOMETRY", dataType);
      } else {
        logRecordDefinition.addField(new FieldDefinition(fieldDefinition));
      }
    }
    logRecordDefinition.setGeometryFactory(recordDefinition2.getGeometryFactory());
    return logRecordDefinition;
  }

  private final Counter counter = new LongCounter("");

  private final RecordWriter writer;

  private AtomicPathUpdator pathUpdator;

  public RecordLog(final AtomicPathUpdator pathUpdator, final RecordDefinitionProxy record) {
    this(getLogRecordDefinition(record, true), pathUpdator.getPath());
    this.pathUpdator = pathUpdator;
  }

  public RecordLog(final AtomicPathUpdator pathUpdator, final RecordDefinitionProxy record,
    final String... prefixFieldNames) {
    this(getLogRecordDefinition(record, prefixFieldNames), pathUpdator.getPath());
    this.pathUpdator = pathUpdator;
  }

  public RecordLog(final Path path, final RecordDefinition recordDefinition,
    final boolean usesLocality) {
    this(getLogRecordDefinition(recordDefinition, usesLocality), path);
  }

  private RecordLog(final RecordDefinition logRecordDefinition, final Object targetFile) {
    this.writer = RecordWriter.newRecordWriter(logRecordDefinition, targetFile);
  }

  @Override
  public synchronized void close() {
    this.writer.flush();
    this.writer.close();
    final AtomicPathUpdator pathUpdator = this.pathUpdator;
    if (pathUpdator != null) {
      if (this.counter.get() == 0) {
        pathUpdator.deleteFiles();
      } else {
        pathUpdator.close();
      }
    }
  }

  public synchronized void error(final Object message, final Record record) {
    log(null, message, record, null);
  }

  public synchronized void error(final Object message, final Record record,
    final Geometry geometry) {
    log(null, message, record, geometry);
  }

  public synchronized void error(final String localityName, final Object message,
    final Record record) {
    log(localityName, message, record, null);
  }

  public synchronized void error(final String localityName, final Object message,
    final Record record, final Geometry geometry) {
    log(localityName, message, record, geometry);
  }

  public Counter getCounter() {
    return this.counter;
  }

  private void log(final Object localityName, final Object message, final Record record,
    Geometry geometry) {
    final Writer<Record> writer = this.writer;
    final Record logRecord = this.writer.newRecord();
    logRecord.setValues(record);
    if (geometry == null) {
      geometry = record.getGeometry();
    }
    logRecord.setGeometryValue(geometry);
    logRecord.setValue(LOG_LOCALITY, localityName);
    logRecord.setValue(LOG_MESSAGE, message);
    synchronized (writer) {
      this.counter.add();
      writer.write(logRecord);
    }
  }

  public void log(final Object message, final Record record, Geometry geometry,
    final Object... extraValues) {
    final Writer<Record> writer = this.writer;
    final Record logRecord = this.writer.newRecord();
    logRecord.setValues(record);
    if (geometry == null) {
      geometry = record.getGeometry();
    }
    logRecord.setGeometryValue(geometry);
    logRecord.setValue(LOG_MESSAGE, message);
    int i = 0;
    for (final Object value : extraValues) {
      logRecord.setValue(i, value);
      i++;
    }
    synchronized (writer) {
      this.counter.add();
      writer.write(logRecord);
    }
  }

  @Override
  public String toString() {
    if (this.writer == null) {
      return super.toString();
    } else {
      return this.writer.toString();
    }
  }
}
