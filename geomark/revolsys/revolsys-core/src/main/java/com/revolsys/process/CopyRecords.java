package com.revolsys.process;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.parallel.process.AbstractProcess;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class CopyRecords extends AbstractProcess {

  private boolean hasSequence;

  private Map<String, Boolean> orderBy = new HashMap<>();

  private RecordStore sourceRecordStore;

  private RecordStore targetRecordStore;

  private PathName typePath;

  public CopyRecords() {
  }

  public CopyRecords(final RecordStore sourceRecordStore, final PathName typePath,
    final Map<String, Boolean> orderBy, final RecordStore targetRecordStore,
    final boolean hasSequence) {
    this.sourceRecordStore = sourceRecordStore;
    this.typePath = typePath;
    this.orderBy = orderBy;
    this.targetRecordStore = targetRecordStore;
    this.hasSequence = hasSequence;
  }

  public CopyRecords(final RecordStore sourceRecordStore, final PathName typePath,
    final RecordStore targetRecordStore, final boolean hasSequence) {
    this(sourceRecordStore, typePath, new HashMap<String, Boolean>(), targetRecordStore,
      hasSequence);
  }

  public Map<String, Boolean> getOrderBy() {
    return this.orderBy;
  }

  public RecordStore getSourceRecordStore() {
    return this.sourceRecordStore;
  }

  public RecordStore getTargetRecordStore() {
    return this.targetRecordStore;
  }

  public PathName getTypePath() {
    return this.typePath;
  }

  public boolean isHasSequence() {
    return this.hasSequence;
  }

  @Override
  public void run() {
    try {
      final Query query = this.sourceRecordStore.newQuery(this.typePath);
      query.setOrderBy(this.orderBy);

      try (
        final RecordReader reader = this.sourceRecordStore.getRecords(query);
        final RecordWriter targetWriter = this.targetRecordStore.newRecordWriter();) {
        final RecordDefinition targetRecordDefinition = this.targetRecordStore
          .getRecordDefinition(this.typePath);
        if (targetRecordDefinition == null) {
          Logs.error(this, "Cannot find target table: " + this.typePath);
        } else {
          if (this.hasSequence) {
            final String idFieldName = targetRecordDefinition.getIdFieldName();
            Identifier maxId = this.targetRecordStore.newPrimaryIdentifier(this.typePath);
            for (final Record sourceRecord : reader) {
              final Record targetRecord = this.targetRecordStore.newRecord(this.typePath,
                sourceRecord);
              final Identifier sourceId = sourceRecord.getIdentifier(idFieldName);
              while (CompareUtil.compare(maxId, sourceId) < 0) {
                maxId = this.targetRecordStore.newPrimaryIdentifier(this.typePath);
              }
              targetWriter.write(targetRecord);
            }
          } else {
            for (final Record sourceRecord : reader) {
              final Record targetRecord = this.targetRecordStore.newRecord(this.typePath,
                sourceRecord);
              targetWriter.write(targetRecord);
            }
          }
        }
      }
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to copy records for " + this.typePath, e);
    }
  }

  public void setHasSequence(final boolean hasSequence) {
    this.hasSequence = hasSequence;
  }

  public void setSourceRecordStore(final RecordStore sourceRecordStore) {
    this.sourceRecordStore = sourceRecordStore;
  }

  public void setTargetRecordStore(final RecordStore targetRecordStore) {
    this.targetRecordStore = targetRecordStore;
  }

  public void setTypePath(final PathName typePath) {
    this.typePath = typePath;
  }

  @Override
  public String toString() {
    return "Copy " + this.typePath;
  }

}
