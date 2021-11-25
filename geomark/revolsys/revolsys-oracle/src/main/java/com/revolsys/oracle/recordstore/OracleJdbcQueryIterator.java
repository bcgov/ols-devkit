package com.revolsys.oracle.recordstore;

import java.util.Map;

import com.revolsys.jdbc.io.AbstractJdbcRecordStore;
import com.revolsys.jdbc.io.JdbcQueryIterator;
import com.revolsys.record.query.Query;

public class OracleJdbcQueryIterator extends JdbcQueryIterator {
  public OracleJdbcQueryIterator(final AbstractJdbcRecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    super(recordStore, query, properties);
  }

  @Override
  protected String getSql(final Query query) {
    String sql = super.getSql(query);

    final int offset = query.getOffset();
    final int limit = query.getLimit();
    if (offset > 0 || limit >= 0 && limit < Integer.MAX_VALUE) {
      final int startRowNum = offset + 1;
      final int endRowNum = offset + limit;
      sql = "SELECT * FROM (" //
        + "SELECT V.*,ROWNUM \"RNUM\" FROM ("//
        + sql + //
        ") V  "//
        + "WHERE ROWNUM <=  " + endRowNum + ")"//
        + "WHERE RNUM >= " + startRowNum;
    }
    return sql;
  }
}
