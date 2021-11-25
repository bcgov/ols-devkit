package com.revolsys.webservice;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.transaction.Transaction;

public class WebServiceFeatureLayerQuery extends Query {

  private final WebServiceFeatureLayer featureLayer;

  public WebServiceFeatureLayerQuery(final WebServiceFeatureLayer featureLayer) {
    this.featureLayer = featureLayer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R extends Record> R getRecord() {
    return (R)this.featureLayer.getRecord(this);
  }

  @Override
  public long getRecordCount() {
    return this.featureLayer.getRecordCount(this);
  }

  @Override
  public RecordReader getRecordReader() {
    return this.featureLayer.getRecordReader(this);
  }

  @Override
  public RecordReader getRecordReader(final Transaction transaction) {
    return getRecordReader();
  }
}
