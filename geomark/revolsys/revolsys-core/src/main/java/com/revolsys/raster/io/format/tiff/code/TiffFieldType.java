package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.revolsys.io.channels.DataReader;
import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.TiffDirectoryEntry;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryAscii;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryDoubleArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryFloatArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedByteArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedIntArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedLongArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedRationalArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedShortArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUndefinedArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedByteArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedIntArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedLongArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedRationalArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedShortArray;

public enum TiffFieldType {
  // 8-bit unsigned integer
  BYTE(//
    1, //
    TiffDirectoryEntryUnsignedByteArray::new//
  ),
  // 8-bit byte that contains a 7-bit ASCII code; the last
  // byte must be NUL (binary zero)
  ASCII(//
    2, //
    TiffDirectoryEntryAscii::new//
  ),
  // 16-bit (2-byte) unsigned integer
  SHORT(//
    3, //
    2, //
    TiffDirectoryEntryUnsignedShortArray::new//
  ),
  // 32-bit (4-byte) unsigned integer
  LONG(//
    4, //
    4, //
    TiffDirectoryEntryUnsignedIntArray::new//
  ),
  // Two LONGs: the first represents the
  // numerator of a fraction; the second, the
  // denominator
  RATIONAL(//
    5, //
    8, //
    TiffDirectoryEntryUnsignedRationalArray::new//
  ),
  // 8-bit signed (twos-complement) integer.
  SBYTE(//
    6, //
    TiffDirectoryEntrySignedByteArray::new//
  ),
  // 8-bit byte that may contain anything,
  // depending on the definition of the field
  UNDEFINED(//
    7, //
    TiffDirectoryEntryUndefinedArray::new//
  ),
  // 16-bit (2-byte) signed (twos-complement) integer
  SSHORT(//
    8, //
    2, //
    TiffDirectoryEntrySignedShortArray::new//
  ),
  // 32-bit (4-byte) signed (twos-complement) integer
  SLONG(//
    9, //
    4, //
    TiffDirectoryEntrySignedIntArray::new//
  ),
  // Two SLONG’s: the first represents the
  // numerator of a fraction, the second the
  // denominator
  SRATIONAL(//
    10, //
    8, //
    TiffDirectoryEntrySignedRationalArray::new//
  ),
  // Single precision (4-byte) IEEE format
  FLOAT(//
    11, //
    TiffDirectoryEntryFloatArray::new//
  ),
  // Double precision (8-byte) IEEE format
  DOUBLE(//
    12, //
    8, //
    TiffDirectoryEntryDoubleArray::new//
  ),
  // 64-bit (8-byte) unsigned (twos-complement) integer
  LONG8(//
    16, //
    8, //
    TiffDirectoryEntryUnsignedLongArray::new//
  ),
  // 64-bit (8-byte) signed (twos-complement) integer
  SLONG8(//
    17, //
    8, //
    TiffDirectoryEntrySignedLongArray::new//
  );

  private static Map<Integer, TiffFieldType> ENUM_BY_ID = new HashMap<>();

  static {
    for (final TiffFieldType fieldType : TiffFieldType.values()) {
      ENUM_BY_ID.put(fieldType.id, fieldType);
    }
  }

  public static TiffFieldType readValue(final DataReader in) {
    final int tag = in.getUnsignedShort();
    return valueById(tag);
  }

  public static TiffFieldType valueById(final int type) {
    return ENUM_BY_ID.get(type);
  }

  private final int id;

  private final int sizeBytes;

  private final Supplier<TiffDirectoryEntry> newDirectoryEntryFunction;

  private TiffFieldType(final int id, final int sizeBytes,
    final Supplier<TiffDirectoryEntry> newDirectoryEntryFunction) {
    this.id = id;
    this.sizeBytes = sizeBytes;
    this.newDirectoryEntryFunction = newDirectoryEntryFunction;
  }

  private TiffFieldType(final int type,
    final Supplier<TiffDirectoryEntry> newDirectoryEntryFunction) {
    this(type, 1, newDirectoryEntryFunction);
  }

  public int getId() {
    return this.id;
  }

  public int getSizeBytes() {
    return this.sizeBytes;
  }

  public TiffDirectoryEntry newDirectoryEntry(final TiffTag tag, final TiffDirectory directory,
    final DataReader in) {
    final TiffDirectoryEntry entry = this.newDirectoryEntryFunction.get();
    entry.readEntry(tag, directory, in);
    return entry;
  }

}
