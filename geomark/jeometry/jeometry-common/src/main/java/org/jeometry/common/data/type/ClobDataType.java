package org.jeometry.common.data.type;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Clob;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.jdbc.StringClob;

public class ClobDataType extends AbstractDataType {

  public ClobDataType() {
    super("Clob", Clob.class, true);
  }

  @Override
  protected boolean equalsNotNull(final Object value1, final Object value2) {
    if (value1 instanceof StringClob) {
      final StringClob clob1 = (StringClob)value1;
      if (value2 instanceof StringClob) {
        final StringClob clob2 = (StringClob)value2;
        return clob1.equals(clob2);
      }
    }
    return false;
  }

  @Override
  protected Clob toObjectDo(final Object value) {
    if (value instanceof Clob) {
      return (Clob)value;
    } else {
      final String string = DataTypes.toString(value);
      if (string == null || string.length() == 0) {
        return null;
      } else {
        return new StringClob(string);
      }
    }
  }

  @Override
  protected String toStringDo(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return (String)value;
    } else if (value instanceof Clob) {
      final Clob clob = (Clob)value;
      try {
        final long length = clob.length();
        if (length == 0) {
          return null;
        } else {
          try (
            Reader in = clob.getCharacterStream();
            StringWriter out = new StringWriter();) {
            try {
              final char[] buffer = new char[4906];
              int count;
              while ((count = in.read(buffer)) > -1) {
                out.write(buffer, 0, count);
              }
            } catch (final IOException e) {
              throw new RuntimeException(e);
            }
            return out.toString();
          }
        }
      } catch (final Exception e) {
        throw Exceptions.wrap("Error reading Clob", e);
      }
    } else {
      return value.toString();
    }
  }

}
