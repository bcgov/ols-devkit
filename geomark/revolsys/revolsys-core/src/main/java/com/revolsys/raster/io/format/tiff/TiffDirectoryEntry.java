package com.revolsys.raster.io.format.tiff;

import java.io.PrintStream;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.code.TiffFieldType;
import com.revolsys.raster.io.format.tiff.code.TiffPrivateTag;
import com.revolsys.raster.io.format.tiff.code.TiffTag;

public interface TiffDirectoryEntry {
  default void dump(final PrintStream out) {
    final TiffTag tag = getTag();
    if (tag instanceof TiffPrivateTag) {
      out.print(tag.getId());
      out.print(" (0x");
      out.print(Integer.toHexString(tag.getId()));
    } else {
      out.print(tag.name());
      out.print(" (");
      out.print(tag.getId());
    }
    out.print(") ");

    final TiffFieldType type = getType();
    out.print(type);
    out.print(" (");
    out.print(type.getId());
    out.print(") ");

    final long count = getCount();
    out.print(count);
    out.print('<');
    if (isLoaded()) {
      if (isArray()) {
        if (type == TiffFieldType.UNDEFINED) {
          for (int i = 0; i < Math.min(24, count); i++) {
            if (i > 0) {
              out.print(' ');
            }
            final byte b = getByte(i);
            if (b == 0) {
              out.print("00");
            } else {
              final int v = b & 0xff;
              final String hexString = Integer.toHexString(v);
              out.print("0x");
              out.print(hexString);
            }
          }
          if (count > 24) {
            out.print(" ...");
          }
        } else {
          out.print(DataTypes.toString(getNumber(0)));
          for (int i = 1; i < Math.min(24, count); i++) {
            out.print(' ');
            out.print(DataTypes.toString(getNumber(i)));
          }
          if (count > 24) {
            out.print(" ...");
          }
        }
      } else {
        final String string = getString();
        if (string.length() > 24) {
          out.print(string.substring(0, 24).replace("\n", "\\n"));
          out.print(" ...");
        } else {
          out.print(string.replace("\n", "\\n"));
        }
      }
    } else {
      out.print("...");
    }
    out.println('>');
  }

  default byte getByte() {
    final Number number = getNumber();
    return number.byteValue();
  }

  default byte getByte(final int index) {
    final Number number = getNumber(index);
    return number.byteValue();
  }

  default byte[] getByteArray() {
    return new byte[] {
      getByte()
    };
  }

  default long getCount() {
    return 1;
  }

  TiffDirectory getDirectory();

  default double getDouble() {
    final Number number = getNumber();
    return number.doubleValue();
  }

  default double getDouble(final int index) {
    final Number number = getNumber(index);
    return number.doubleValue();
  }

  default double[] getDoubleArray() {
    final int count = (int)getCount();
    final double[] value = new double[count];
    for (int i = 0; i < count; i++) {
      value[i] = getDouble(i);
    }
    return value;
  }

  default float getFloat() {
    final Number number = getNumber();
    return number.floatValue();
  }

  default float getFloat(final int index) {
    final Number number = getNumber(index);
    return number.floatValue();
  }

  default int getInt() {
    if (getTag().isArray()) {
      final int[] intArray = getIntArray();
      if (intArray.length == 1) {
        return intArray[0];
      } else {
        throw new IllegalArgumentException("Cannot get int from array");
      }
    } else {
      final Number number = getNumber();
      return number.intValue();
    }
  }

  default int getInt(final int index) {
    final Number number = getNumber(index);
    return number.intValue();
  }

  default int[] getIntArray() {
    final int count = (int)getCount();
    final int[] value = new int[count];
    for (int i = 0; i < count; i++) {
      value[i] = getInt(i);
    }
    return value;
  }

  default long getLong() {
    final Number number = getNumber();
    return number.longValue();
  }

  default long getLong(final int index) {
    final Number number = getNumber(index);
    return number.longValue();
  }

  default long[] getLongArray() {
    final int count = (int)getCount();
    final long[] value = new long[count];
    for (int i = 0; i < count; i++) {
      value[i] = getLong(i);
    }
    return value;
  }

  default Number getNumber() {
    final Object value = getValue();
    if (value instanceof Number) {
      return (Number)value;
    } else {
      throw new IllegalStateException("Value is not a number:" + DataTypes.toString(value));
    }
  }

  default Number getNumber(final int index) {
    if (index == 0) {
      return getNumber();
    } else {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }

  long getOffset();

  default short getShort() {
    final Number number = getNumber();
    return number.shortValue();
  }

  default short getShort(final int index) {
    final Number number = getNumber(index);
    return number.shortValue();
  }

  default int getSizeBytes() {
    return (int)getCount() * getValueSizeBytes();
  }

  default String getString() {
    return getValue().toString();
  }

  TiffTag getTag();

  TiffFieldType getType();

  <V> V getValue();

  default int getValueSizeBytes() {
    final TiffFieldType type = getType();
    return type.getSizeBytes();
  }

  default boolean isArray() {
    return getCount() > 1;
  }

  default boolean isInline() {
    final int maxInlineSize = getDirectory().getMaxInlineSize();
    final long size = getCount() * getValueSizeBytes();
    return size <= maxInlineSize;
  }

  default boolean isLoaded() {
    return true;
  }

  void readEntry(TiffTag tag, TiffDirectory directory, DataReader in);

  default void readValue(final DataReader in) {
  }

  void writeEntry(TiffDirectory directory, ChannelWriter out);

  void writeValue(ChannelWriter out);
}
