package com.revolsys.gis.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.revolsys.jdbc.io.JdbcQueryResultPager;
import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.query.Query;

public class PostgreSQLJdbcQueryResultPager extends JdbcQueryResultPager {

  private Integer numResults;

  private List<Record> results = null;

  public PostgreSQLJdbcQueryResultPager(final JdbcRecordStore recordStore,
    final Map<String, Object> properties, final Query query) {
    super(recordStore, properties, query);
  }

  @Override
  public void forEachInPage(final Consumer<Record> action) {
    synchronized (this) {
      final int pageSize = getPageSize();
      final int pageNumber = getPageNumber();
      if (pageNumber != -1) {
        String sql = getSql();

        final int startRowNum = (pageNumber - 1) * pageSize;
        sql = getSql() + " OFFSET " + startRowNum + " LIMIT " + pageSize;

        try (
          final PreparedStatement statement = this.connection.prepareStatement(sql);
          final ResultSet resultSet = createResultSet(statement);) {
          if (resultSet.next()) {
            int i = 0;
            do {
              final Record record = getNextRecord(resultSet);
              action.accept(record);
              i++;
            } while (resultSet.next() && i < pageSize);
          }
        } catch (final SQLException e) {
          throw this.connection.getException("updateResults", sql, e);
        }
      }

    }
  }

  @Override
  public List<Record> getList() {
    synchronized (this) {
      if (this.results == null) {
        final ArrayList<Record> results = new ArrayList<>();
        forEachInPage(results::add);
        this.results = results;
      }
      return this.results;
    }
  }

  @Override
  public int getNumResults() {
    if (this.numResults == null) {
      final JdbcRecordStore recordStore = getRecordStore();
      final Query query = getQuery();
      this.numResults = recordStore.getRecordCount(query);
      updateNumPages();
    }
    return this.numResults;
  }

  /**
   * Update the cached results for the current page.
   */
  @Override
  protected void updateResults() {
    this.results = null;
  }
}
