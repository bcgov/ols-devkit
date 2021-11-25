package com.revolsys.geopackage.function;

import java.sql.SQLException;

import org.sqlite.Function;
import org.sqlite.SQLiteConnection;

public class GeoPackageIsEmptyFunction extends Function {
  public static final GeoPackageIsEmptyFunction INSTANCE = new GeoPackageIsEmptyFunction();

  public static void add(final SQLiteConnection dbConnection) throws SQLException {
    Function.create(dbConnection, "ST_IsEmpty", INSTANCE, 1, 0);
  }

  @Override
  protected void xFunc() throws SQLException {
    final int argCount = args();
    if (argCount != 1) {
      throw new SQLException("Single argument is required. args: " + argCount);
    }

    boolean empty = true;
    final byte[] bytes = value_blob(0);
    if (bytes != null && bytes.length > 4) {
      if (bytes[0] == 'G' && bytes[1] == 'P') {
        final byte flags = bytes[3];
        empty = (flags >> 4 & 1) == 1;

      }
    }
    result(Boolean.compare(empty, false));
  }

}
