package com.revolsys.record.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.IteratorReader;
import com.revolsys.record.Record;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.SqlCondition;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Property;

public class RecordStoreQueryReader extends IteratorReader<Record> implements RecordReader {

  private BoundingBox boundingBox;

  private List<Query> queries = new ArrayList<>();

  private RecordStore recordStore;

  private List<String> typePaths;

  private String whereClause;

  public RecordStoreQueryReader() {
    Transaction.assertInTransaction();
    setIterator(new RecordStoreMultipleQueryIterator(this));
  }

  public RecordStoreQueryReader(final RecordStore recordStore) {
    this();
    setRecordStore(recordStore);
  }

  public void addQuery(final Query query) {
    this.queries.add(query);
  }

  @Override
  @PreDestroy
  public void close() {
    super.close();
    this.boundingBox = null;
    this.recordStore = null;
    this.queries = null;
    this.typePaths = null;
    this.whereClause = null;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public List<Query> getQueries() {
    return this.queries;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return iterator().getRecordDefinition();
  }

  @Override
  public RecordStore getRecordStore() {
    return this.recordStore;
  }

  public String getWhereClause() {
    return this.whereClause;
  }

  @Override
  public RecordIterator iterator() {
    if (this.queries.size() == 1) {
      return newQueryIterator(0);
    } else {
      return (RecordIterator)super.iterator();
    }
  }

  protected RecordIterator newQueryIterator(final int i) {
    if (i < this.queries.size()) {
      final Query query = this.queries.get(i);
      if (Property.hasValue(this.whereClause)) {
        query.and(new SqlCondition(this.whereClause));
      }
      if (this.boundingBox != null) {
        F.envelopeIntersects(query, this.boundingBox);
      }

      return this.recordStore.newIterator(query, getProperties());
    }
    throw new NoSuchElementException();
  }

  @Override
  @PostConstruct
  public void open() {
    if (this.typePaths != null) {
      for (final String tableName : this.typePaths) {
        final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(tableName);
        if (recordDefinition != null) {
          Query query;
          if (this.boundingBox == null) {
            query = new Query(recordDefinition);
            query.setWhereCondition(new SqlCondition(this.whereClause));
          } else {
            query = Query.intersects(recordDefinition, this.boundingBox);
          }
          addQuery(query);
        }
      }
    }
    super.open();
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  /**
   * @param queries the queries to set
   */
  public void setQueries(final Collection<Query> queries) {
    this.queries.clear();
    for (final Query query : queries) {
      addQuery(query);
    }
  }

  public void setQueries(final List<Query> queries) {
    this.queries.clear();
    for (final Query query : queries) {
      addQuery(query);
    }
  }

  public void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  /**
   * @param typePaths the typePaths to set
   */
  public void setTypeNames(final List<String> typePaths) {
    this.typePaths = typePaths;

  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }
}
