package com.revolsys.elevation.gridded.img;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.map.MapEx;

class ImgField {
  private static final int BIT = 0;

  private static final int BIT2 = 1;

  private static final int NIBBLE = 2;

  private static final int BYTE_UNSIGNED = 3;

  private static final int BYTE = 4;

  private static final int SHORT_UNSIGNED = 5;

  private static final int SHORT = 6;

  private static final int INT_UNSIGNED = 7;

  private static final int INT = 8;

  private static final int FLOAT = 9;

  private static final int DOUBLE = 10;

  public static ImgField newObject(final String fieldName, final String fieldTypeName) {
    return new ImgField(' ', 'o', fieldName, null, fieldTypeName, Collections.emptyList());
  }

  public static ImgField newPointer(final char pointerType, final String fieldName,
    final String fieldTypeName) {
    return new ImgField(pointerType, 'o', fieldName, null, fieldTypeName, Collections.emptyList());
  }

  private char pointerType = '\0';

  private char itemType;

  private final List<String> enumValues;

  private String name;

  private ImgFieldType fieldType;

  private String fieldTypeName;

  public ImgField() {
    this.enumValues = Collections.emptyList();
  }

  public ImgField(final char pointerType, final char itemType, final String fieldName) {
    this(pointerType, itemType, fieldName, null, null, Collections.emptyList());
  }

  public ImgField(final char pointerType, final char itemType, final String fieldName,
    final ImgFieldType fieldType, final String fieldTypeName, final List<String> enumValues) {
    this.itemType = itemType;
    this.pointerType = pointerType;
    this.name = fieldName;
    this.fieldType = fieldType;
    this.fieldTypeName = fieldTypeName;
    this.name = fieldName;
    this.enumValues = enumValues;
  }

  public ImgField(final char itemType, final String fieldName) {
    this(itemType, fieldName, Collections.emptyList());
  }

  public ImgField(final char itemType, final String fieldName, final List<String> enumNames) {
    this.itemType = itemType;
    this.name = fieldName;
    this.enumValues = enumNames;
  }

  public ImgField(final char itemType, final String fieldName, final String... enumValues) {
    this.itemType = itemType;
    this.name = fieldName;
    this.enumValues = Arrays.asList(enumValues);
  }

  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unused")
  public Object readValue(final ImgGriddedElevationReader reader) {
    int pointerSize = 0;
    if (this.pointerType != '\0') {
      pointerSize = reader.readInt();
      final int p2 = reader.readInt(); // skip why?
    }

    switch (this.itemType) {
      case 'c':
        if (pointerSize == 0) {
          return null;
        } else {
          return reader.readString0(pointerSize);
        }
      case 'C':
        return reader.readChar();

      case 'e':
        final int enumIndex = reader.readUnsignedShort();
        if (enumIndex >= 0 && enumIndex < this.enumValues.size()) {
          return this.enumValues.get(enumIndex);
        } else {
          return null;
        }
      case 's':
        return reader.readUnsignedShort();

      case 'S':
        return reader.readShort();

      case 't':
      case 'l':
        return reader.readUnsignedInt();

      case 'L':
        return reader.readInt();

      case 'f':
        return reader.readFloat();

      case 'd':
        return reader.readDouble();

      case 'b': {
        if (pointerSize == 0) {
          return null;
        } else {
          final int height = reader.readInt();
          final int width = reader.readInt();
          final int cellCount = width * height;
          final short baseItemType = reader.readShort();
          reader.readShort(); // We ignore the 2 byte objecttype value.
          if (baseItemType == BIT) {
            throw new RuntimeException("EPT_1 field type not supported");

            //
            // if( pabyData[nIndexValue >> 3] & 1 << (nIndexValue & 0x7) )
            // {
            // dfDoubleRet = 1;
            // nIntRet = 1;
            // }
            // else
            // {
            // dfDoubleRet = 0.0;
            // nIntRet = 0;
            // }
          } else if (baseItemType == BIT2) {
            throw new RuntimeException("EPT_2 field type not supported");
            // const final int nBitOffset = nIndexValue & 0x3;
            // const final int nByteOffset = nIndexValue >> 2;
            //

            //
            // const final int nMask = 0x3;
            // nIntRet = pabyData[nByteOffset] >> nBitOffset & nMask;
            // dfDoubleRet = nIntRet;
          } else if (baseItemType == NIBBLE) {
            throw new RuntimeException("NIBBLE field type not supported");
            // const final int nBitOffset = nIndexValue & 0x7;
            // const final int nByteOffset = nIndexValue >> 3;
            //

            //
            // const final int nMask = 0x7;
            // nIntRet = pabyData[nByteOffset] >> nBitOffset & nMask;
            // dfDoubleRet = nIntRet;
          } else if (baseItemType == BYTE_UNSIGNED) {
            final short[] cells = new short[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readUnsignedByte();
            }
            return new ImgGridData(width, height, baseItemType, cells);
          } else if (baseItemType == BYTE) {
            final byte[] cells = new byte[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readByte();
            }
            return new ImgGridData(width, height, baseItemType, cells);

          } else if (baseItemType == SHORT) {
            final short[] cells = new short[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readShort();
            }
            return new ImgGridData(width, height, baseItemType, cells);
          } else if (baseItemType == SHORT_UNSIGNED) {
            final int[] cells = new int[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readUnsignedShort();
            }
            return new ImgGridData(width, height, baseItemType, cells);
          } else if (baseItemType == INT) {
            final int[] cells = new int[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readInt();
            }
            return new ImgGridData(width, height, baseItemType, cells);
          } else if (baseItemType == INT_UNSIGNED) {
            final long[] cells = new long[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readUnsignedInt();
            }
            return new ImgGridData(width, height, baseItemType, cells);
          } else if (baseItemType == FLOAT) {
            final float[] cells = new float[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readFloat();
            }
            return new ImgGridData(width, height, baseItemType, cells);
          } else if (baseItemType == DOUBLE) {
            final double[] cells = new double[cellCount];
            for (int i = 0; i < cellCount; i++) {
              cells[i] = reader.readDouble();
            }
            return new ImgGridData(width, height, baseItemType, cells);
          } else {
            throw new IllegalArgumentException("Unknown base item type: " + baseItemType);
          }
        }
      }

      case 'o':
        if (this.fieldType == null && this.fieldTypeName != null) {
          this.fieldType = reader.findType(this.fieldTypeName);
        }
        if (this.fieldType != null) {
          if ('*' == this.pointerType) {
            return this.fieldType.readFieldValues(reader);
          } else {
            final List<MapEx> values = new ArrayList<>();
            for (int i = 0; i < pointerSize; i++) {
              final MapEx value = this.fieldType.readFieldValues(reader);
              values.add(value);
            }
            return values;
          }
        } else {
          return MapEx.EMPTY;
        }

      default:
        return null;
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
