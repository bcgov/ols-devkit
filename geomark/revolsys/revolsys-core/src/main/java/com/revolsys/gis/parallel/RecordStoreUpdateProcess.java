package com.revolsys.gis.parallel;

import javax.annotation.PreDestroy;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInProcess;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.schema.RecordStore;

/**
 * <p>
 * The RecordStoreUpdateProcess process reads each object from the input channel
 * and updates the object in the record store based on the object's state.
 * </p>
 * <p>
 * The following actions will be performed based on the state of the object.
 * </p>
 * <dl>
 * <dt>NEW</dt>
 * <dd>Insert the object into the record store.</dd>
 * <dt>PERSISTED</dt>
 * <dd>No action performed.</dd>
 * <dt>MODIFIED</dt>
 * <dd>Update the object in the record store.</dd>
 * <dt>DELETED</dt>
 * <dd>Delete the object from the record store.</dd>
 * </dl>
 */

public class RecordStoreUpdateProcess extends BaseInProcess<Record> {
  /** The record store. */
  private RecordStore recordStore;

  /**
   * Construct a new RecordStoreUpdateProcess.
   */
  public RecordStoreUpdateProcess() {
  }

  @Override
  @PreDestroy
  public void close() {
    this.recordStore.close();
  }

  /**
   * Get the record store.
   *
   * @return The record store.
   */
  public RecordStore getRecordStore() {
    return this.recordStore;
  }

  /**
   * Process each object from the channel
   *
   * @param in The input channel.
   * @param record The object to process.
   */
  @Override
  protected void process(final Channel<Record> in, final Record record) {
    final RecordState state = record.getState();
    switch (state) {
      case NEW:
        this.recordStore.insertRecord(record);
      break;
      case PERSISTED:
      break;
      case MODIFIED:
        this.recordStore.updateRecord(record);
      break;
      case DELETED:
        this.recordStore.deleteRecord(record);
      break;
      default:
      break;
    }
  }

  /**
   * Set the record store.
   *
   * @param recordStore The record store.
   */
  public void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }
}
