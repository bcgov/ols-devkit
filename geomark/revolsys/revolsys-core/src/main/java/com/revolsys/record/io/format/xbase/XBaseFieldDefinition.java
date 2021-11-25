package com.revolsys.record.io.format.xbase;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

public class XBaseFieldDefinition {
  public static final char CHARACTER_TYPE = 'C';

  private static final Map<Character, DataType> DATA_TYPES = new HashMap<>();

  public static final char DATE_TYPE = 'D';

  public static final char FLOAT_TYPE = 'F';

  public static final char LOGICAL_TYPE = 'L';

  public static final char MEMO_TYPE = 'M';

  public static final char NUMBER_TYPE = 'N';

  public static final char OBJECT_TYPE = 'o';

  static {
    DATA_TYPES.put(CHARACTER_TYPE, DataTypes.STRING);
    DATA_TYPES.put(NUMBER_TYPE, DataTypes.DECIMAL);
    DATA_TYPES.put(LOGICAL_TYPE, DataTypes.BOOLEAN);
    DATA_TYPES.put(DATE_TYPE, DataTypes.DATE_TIME);
    DATA_TYPES.put(MEMO_TYPE, DataTypes.STRING);
    DATA_TYPES.put(FLOAT_TYPE, DataTypes.FLOAT);
    DATA_TYPES.put(OBJECT_TYPE, DataTypes.OBJECT);

  }

  private final DataType dataType;

  private final int decimalPlaces;

  private final String fullName;

  private final int length;

  private final String name;

  private DecimalFormat numberFormat;

  private double precisionScale;

  private final char type;

  public XBaseFieldDefinition(final String name, final String fullName, final char type,
    final int length) {
    this(name, fullName, type, length, 0);
  }

  public XBaseFieldDefinition(final String name, final String fullName, final char type,
    final int length, final int decimalPlaces) {
    this.name = name;
    this.fullName = fullName;
    this.type = type;
    this.dataType = DATA_TYPES.get(type);
    this.length = length;
    this.decimalPlaces = decimalPlaces;
    if (type == NUMBER_TYPE) {
      final StringBuilder format = new StringBuilder("0");
      if (decimalPlaces > 0) {
        format.append(".");
        for (int i = 0; i < decimalPlaces; i++) {
          format.append("#");
        }
        this.precisionScale = Math.pow(10, decimalPlaces);
      } else if (decimalPlaces == -1 && length > 2) {
        format.append(".");
        for (int i = 0; i < length - 2; i++) {
          format.append("#");
        }
      } else {
        this.precisionScale = 1;
      }
      this.numberFormat = new DecimalFormat(format.toString());
    }
  }

  public DataType getDataType() {
    return this.dataType;
  }

  public int getDecimalPlaces() {
    return this.decimalPlaces;
  }

  public String getFullName() {
    return this.fullName;
  }

  public int getLength() {
    return this.length;
  }

  public String getName() {
    return this.name;
  }

  public DecimalFormat getNumberFormat() {
    return this.numberFormat;
  }

  public double getPrecisionScale() {
    return this.precisionScale;
  }

  public char getType() {
    return this.type;
  }

  @Override
  public String toString() {
    return this.name + ":" + this.dataType + "(" + this.length + ")";
  }

}
