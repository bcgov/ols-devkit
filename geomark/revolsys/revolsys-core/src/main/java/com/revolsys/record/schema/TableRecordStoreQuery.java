package com.revolsys.record.schema;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.revolsys.record.ChangeTrackRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOption;
import com.revolsys.transaction.TransactionOptions;

public class TableRecordStoreQuery extends Query {

  private final AbstractTableRecordStore recordStore;

  private final TableRecordStoreConnection connection;

  public TableRecordStoreQuery(final AbstractTableRecordStore recordStore,
    final TableRecordStoreConnection connection) {
    super(recordStore.getRecordDefinition());
    this.recordStore = recordStore;
    this.connection = connection;
  }

  public int deleteRecords() {
    try (
      Transaction transaction = this.connection.newTransaction(TransactionOptions.REQUIRED)) {
      return this.recordStore.getRecordStore().deleteRecords(this);
    }
  }

  @Override
  public int deleteRecords(final TableRecordStoreConnection connection, final Query query) {
    return this.recordStore.deleteRecords(this.connection, this);
  }

  @Override
  public <R extends Record> R getRecord() {
    return this.recordStore.getRecord(this.connection, this);
  }

  @Override
  public long getRecordCount() {
    return this.recordStore.getRecordCount(this.connection, this);
  }

  @Override
  public RecordReader getRecordReader() {
    return this.recordStore.getRecordReader(this.connection, this);
  }

  @Override
  public RecordReader getRecordReader(final Transaction transaction) {
    return this.recordStore.getRecordReader(this.connection, this, transaction);
  }

  @Override
  public Record insertOrUpdateRecord(final Supplier<Record> newRecordSupplier,
    final Consumer<Record> updateAction) {
    return this.recordStore.insertOrUpdateRecord(this.connection, this, newRecordSupplier,
      updateAction);
  }

  @Override
  public Record insertRecord(final Supplier<Record> newRecordSupplier) {
    return this.recordStore.insertRecord(this.connection, this, newRecordSupplier);
  }

  @Override
  public Transaction newTransaction() {
    return this.connection.newTransaction();
  }

  @Override
  public Transaction newTransaction(final TransactionOption... options) {
    return this.connection.newTransaction(options);
  }

  @Override
  public Record updateRecord(final Consumer<Record> updateAction) {
    return this.recordStore.updateRecord(this.connection, this, updateAction);
  }

  @Override
  public int updateRecords(final Consumer<? super ChangeTrackRecord> updateAction) {
    return this.recordStore.updateRecords(this.connection, this, updateAction);
  }
}
