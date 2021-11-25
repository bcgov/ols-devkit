package com.revolsys.gis.postgresql.type;

import java.sql.SQLException;

import org.jeometry.common.data.type.DataTypes;
import org.postgresql.util.PGobject;

public class PostgreSQLTidWrapper extends PGobject {
  private static final long serialVersionUID = 0L;

  public PostgreSQLTidWrapper() {
    setType("tid");
  }

  public PostgreSQLTidWrapper(final Object value) {
    setType("tid");
    setTid(DataTypes.LONG.toObject(value));
  }

  @Override
  public PostgreSQLTidWrapper clone() {
    try {
      return (PostgreSQLTidWrapper)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  public Long getTid() {
    final String value = getValue();
    final int commaIndex = value.indexOf(',');
    final int block = Integer.parseInt(value.substring(1, commaIndex), 10);
    final int index = Integer.parseInt(value.substring(commaIndex + 1, value.length() - 1), 10);
    return (long)block << 32 | index & 0xffffffffL;
  }

  public void setTid(final Long tid) {
    try {
      if (tid == null) {
        super.setValue(null);
      } else {
        final int block = (int)(tid >> 32);
        final int index = tid.intValue();
        final StringBuilder string = new StringBuilder();
        string.append('(');
        string.append(block);
        string.append(',');
        string.append(index);
        string.append(')');
        super.setValue(string.toString());
      }
    } catch (final SQLException e) {
    }
  }
}
