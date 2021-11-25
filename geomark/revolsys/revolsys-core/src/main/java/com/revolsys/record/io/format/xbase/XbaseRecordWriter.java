package com.revolsys.record.io.format.xbase;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.identifier.SingleIdentifier;
import org.jeometry.common.data.identifier.TypedIdentifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.Buffers;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

/**
 * <p>Xbase fields suffer a number of limitations:</p>
 *
 * <ul>
 *   <li>Field names can only be up to 10 characters long. If required names will be truncated to 10 character.
 *   Duplicate field names will have a sequential number appended to avoid duplicates. Name may be
 *   truncated again to fit the sequential number.</li>
 *   <li>Only Boolean, Number, String and Date (YYYYMMDD) field types are supported. Other types will be converted to strings.</li>
 *   <li>String can have a maximum length of 254 characters. If length &lt; 1 then length of 254 will be used. Strings will be silently truncated to 254 characters. Strings are left aligned in the field and padded with spaces instead of a null terminator.</li>
 *   <li>boolean fields have length 1 and scale 0.</li>
 *   <li>byte fields have length 3 and scale 0.</li>
 *   <li>short fields have length 5 and scale 0.</li>
 *   <li>int fields have length 10 and scale 0.</li>
 *   <li>long and BigInteger fields have length 18 and scale 0.</li>
 *   <li>float, double, Number fields can have a maximum length of 18 characters. If length &lt; 1 then length of 18 will be used.
 *   If scale is &lt; 0 scale of 3 is used. Scale must be &lt;= 15. Scale must be &lt;= length -3 to allow for '-' sign and digit and a '.'.
 *   <b>NOTE: For floating point numbers an explicit length and scale is recommended.</b></li>
 * <ul>
 */
public class XbaseRecordWriter extends AbstractRecordWriter {

  private Charset charset = StandardCharsets.UTF_8;

  private final List<String> fieldNames = new ArrayList<>();

  private final List<XBaseFieldDefinition> fields = new ArrayList<>();

  private boolean initialized;

  private WritableByteChannel out;

  private ByteBuffer recordBuffer;

  private int recordCount = 0;

  private Map<String, String> shortNames = new HashMap<>();

  private boolean useZeroForNull = true;

  public XbaseRecordWriter(final RecordDefinitionProxy recordDefinition, final Resource resource) {
    super(recordDefinition);
    setResource(resource);
  }

  protected int addDbaseField(final String fullName, final DataType dataType,
    final Class<?> typeJavaClass, int length, int scale) {
    char type = XBaseFieldDefinition.NUMBER_TYPE;
    if (typeJavaClass == Boolean.class) {
      type = XBaseFieldDefinition.LOGICAL_TYPE;
    } else if (Date.class.isAssignableFrom(typeJavaClass)) {
      type = XBaseFieldDefinition.DATE_TYPE;
    } else if (typeJavaClass == Long.class || typeJavaClass == BigInteger.class) {
      length = 18;
      scale = 0;
    } else if (typeJavaClass == Integer.class) {
      length = 10;
      scale = 0;
    } else if (typeJavaClass == Short.class) {
      length = 5;
      scale = 0;
    } else if (typeJavaClass == Byte.class) {
      length = 3;
      scale = 0;
    } else if (Number.class.isAssignableFrom(typeJavaClass)) {
    } else {
      type = XBaseFieldDefinition.CHARACTER_TYPE;
    }
    final XBaseFieldDefinition field = addFieldDefinition(fullName, type, length, scale);
    return field.getLength();
  }

  protected XBaseFieldDefinition addFieldDefinition(final String fullName, final char type,
    int length, int scale) {
    if (type == XBaseFieldDefinition.NUMBER_TYPE) {
      if (length < 1) {
        length = 18;
      } else {
        if (scale > 0) {
          // Allow for . and sign
          length += 2;
        }
        length = Math.min(18, length);
      }
      if (scale < 0) {
        scale = 3;
      }
      scale = Math.min(15, scale);
      scale = Math.min(length - 3, scale);
      scale = Math.max(0, scale);
    } else {
      if (type == XBaseFieldDefinition.CHARACTER_TYPE) {
        if (length < 1) {
          length = 254;
        } else {
          length = Math.min(254, length);
        }
      } else if (type == XBaseFieldDefinition.LOGICAL_TYPE) {
        length = 1;
      } else if (type == XBaseFieldDefinition.DATE_TYPE) {
        length = 8;
      }
      scale = 0;
    }
    String name = this.shortNames.get(fullName);
    if (name == null) {
      name = fullName.toUpperCase();
    }
    if (name.length() > 10) {
      name = name.substring(0, 10);
    }
    int i = 1;
    while (this.fieldNames.contains(name)) {
      final String suffix = String.valueOf(i);
      name = name.substring(0, name.length() - suffix.length()) + i;
      i++;
    }

    final XBaseFieldDefinition field = new XBaseFieldDefinition(name, fullName, type, length,
      scale);
    this.fieldNames.add(name);
    this.fields.add(field);
    return field;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void close() {
    try {
      if (this.out != null) {
        try {
          this.recordBuffer.put((byte)0x1a);
          Buffers.writeAll(this.out, this.recordBuffer);

          if (this.out instanceof SeekableByteChannel) {
            final SeekableByteChannel out = (SeekableByteChannel)this.out;
            out.position(1);
            final ByteBuffer buffer = ByteBuffer.allocate(7);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            final Date now = new Date();
            buffer.put((byte)now.getYear());
            buffer.put((byte)(now.getMonth() + 1));
            buffer.put((byte)now.getDate());

            buffer.putInt(this.recordCount);
            Buffers.writeAll(out, buffer);
          }
        } finally {
          try {
            this.out.close();
          } finally {
            this.out = null;
          }
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Charset getCharset() {
    return this.charset;
  }

  public Map<String, String> getShortNames() {
    return this.shortNames;
  }

  protected boolean hasField(final String name) {
    if (Property.hasValue(name)) {
      return this.fieldNames.contains(name.toUpperCase());
    } else {
      return false;
    }
  }

  protected void init() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      final Resource resource = getResource();
      if (resource != null) {
        final Map<String, String> shortNames = getProperty("shortNames");
        if (shortNames != null) {
          this.shortNames = shortNames;
        }
        this.out = resource.newWritableByteChannel();
        writeHeader();
        final Resource codePageResource = resource.newResourceChangeExtension("cpg");
        if (codePageResource != null) {
          try (
            final Writer writer = codePageResource.newWriter()) {
            writer.write(this.charset.name());
          }
        }
      }
    }
  }

  public boolean isUseZeroForNull() {
    return this.useZeroForNull;
  }

  protected void preFirstWrite(final Record object) throws IOException {
  }

  public void setCharset(final Charset charset) {
    this.charset = charset;
  }

  public void setShortNames(final Map<String, String> shortNames) {
    this.shortNames = shortNames;
  }

  public void setUseZeroForNull(final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  @Override
  public void write(final Record record) {
    try {
      if (!this.initialized) {
        init();
        preFirstWrite(record);
      }
      if (this.out != null) {
        this.recordBuffer.put((byte)' ');
        for (final XBaseFieldDefinition field : this.fields) {
          if (!writeField(record, field)) {
            final String fieldName = field.getFullName();
            Logs.warn(this,
              "Unable to write field '" + fieldName + "' with value " + record.getValue(fieldName));
          }
        }
        Buffers.writeAll(this.out, this.recordBuffer);
        this.recordCount++;
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected boolean writeField(final Record record, final XBaseFieldDefinition field)
    throws IOException {
    if (this.out == null) {
      return true;
    } else {
      final String fieldName = field.getFullName();
      Object value;
      if (isWriteCodeValues()) {
        value = record.getCodeValue(fieldName);
      } else {
        value = record.getValue(fieldName);
      }
      if (value instanceof SingleIdentifier) {
        final SingleIdentifier identifier = (SingleIdentifier)value;
        value = identifier.getValue(0);
      } else if (value instanceof TypedIdentifier) {
        final TypedIdentifier identifier = (TypedIdentifier)value;
        value = identifier.getIdentifier().getValue(0);
      }
      final int fieldLength = field.getLength();
      switch (field.getType()) {
        case XBaseFieldDefinition.NUMBER_TYPE:
          String numString = "";
          final DecimalFormat numberFormat = field.getNumberFormat();
          if (value == null) {
            if (this.useZeroForNull) {
              numString = numberFormat.format(0);
            }
          } else {
            if (value instanceof Number) {
              Number number = (Number)value;
              final int decimalPlaces = field.getDecimalPlaces();
              if (decimalPlaces >= 0) {
                if (number instanceof BigDecimal) {
                  final BigDecimal bigDecimal = new BigDecimal(number.toString());
                  number = bigDecimal.setScale(decimalPlaces, RoundingMode.HALF_UP);
                } else if (number instanceof Double || number instanceof Float) {
                  final double doubleValue = number.doubleValue();
                  final double precisionScale = field.getPrecisionScale();
                  number = Doubles.makePrecise(precisionScale, doubleValue);
                }
              }
              numString = numberFormat.format(number);
            } else {
              throw new IllegalArgumentException("Not a number " + fieldName + "=" + value);
            }
          }
          final byte[] numberBytes = numString.getBytes();
          final int numLength = numberBytes.length;
          if (numLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              this.recordBuffer.put((byte)'9');
            }
          } else {
            for (int i = numLength; i < fieldLength; i++) {
              this.recordBuffer.put((byte)' ');
            }
            this.recordBuffer.put(numberBytes);
          }
          return true;
        case XBaseFieldDefinition.FLOAT_TYPE:
          String floatString = "";
          if (value != null) {
            floatString = value.toString();
          }
          final byte[] floatBytes = floatString.getBytes();
          final int floatLength = floatBytes.length;
          if (floatLength > fieldLength) {
            for (int i = 0; i < fieldLength; i++) {
              this.recordBuffer.put((byte)'9');
            }
          } else {
            for (int i = floatLength; i < fieldLength; i++) {
              this.recordBuffer.put((byte)' ');
            }
            this.recordBuffer.put(floatBytes);
          }
          return true;

        case XBaseFieldDefinition.CHARACTER_TYPE:
          String string = "";
          if (value != null) {
            final Object value1 = value;
            string = DataTypes.toString(value1);
          }
          final byte[] stringBytes = string.getBytes(this.charset);
          if (stringBytes.length >= fieldLength) {
            this.recordBuffer.put(stringBytes, 0, fieldLength);
          } else {
            this.recordBuffer.put(stringBytes);
            for (int i = stringBytes.length; i < fieldLength; i++) {
              this.recordBuffer.put((byte)' ');
            }
          }
          return true;

        case XBaseFieldDefinition.DATE_TYPE:
          if (value instanceof Date) {
            final Date date = (Date)value;
            final String dateString = Dates.format("yyyyMMdd", date);
            this.recordBuffer.put(dateString.getBytes());

          } else if (value == null) {
            this.recordBuffer.put("        ".getBytes());
          } else {
            final byte[] dateBytes = value.toString().getBytes();
            this.recordBuffer.put(dateBytes, 0, 8);
          }
          return true;

        case XBaseFieldDefinition.LOGICAL_TYPE:
          boolean logical = false;
          if (value instanceof Boolean) {
            final Boolean boolVal = (Boolean)value;
            logical = boolVal.booleanValue();
          } else if (value != null) {
            logical = Boolean.valueOf(value.toString());
          }
          if (logical) {
            this.recordBuffer.put((byte)'T');
          } else {
            this.recordBuffer.put((byte)'F');
          }
          return true;

        default:
          return false;
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void writeHeader() throws IOException {
    if (this.out != null) {

      final ByteBuffer headerBuffer = ByteBuffer.allocateDirect(32);
      headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

      int recordLength = 1;

      this.fields.clear();
      int fieldCount = 0;
      final RecordDefinition recordDefinition = getRecordDefinition();
      for (final String name : recordDefinition.getFieldNames()) {
        final int index = recordDefinition.getFieldIndex(name);
        final int length = recordDefinition.getFieldLength(index);
        final int scale = recordDefinition.getFieldScale(index);
        final DataType fieldType = recordDefinition.getFieldType(index);
        final Class<?> typeJavaClass = fieldType.getJavaClass();
        final int fieldLength = addDbaseField(name, fieldType, typeJavaClass, length, scale);
        if (fieldLength > 0) {
          recordLength += fieldLength;
          fieldCount++;
        }
      }

      this.recordBuffer = ByteBuffer.allocateDirect(recordLength);

      headerBuffer.put((byte)0x03);
      final Date now = new Date();
      headerBuffer.put((byte)now.getYear());
      headerBuffer.put((byte)(now.getMonth() + 1));
      headerBuffer.put((byte)now.getDate());
      // Write 0 as the number of records, come back and update this when closed
      headerBuffer.putInt(0);
      final short headerLength = (short)(33 + fieldCount * 32);

      headerBuffer.putShort(headerLength);
      headerBuffer.putShort((short)recordLength);
      headerBuffer.putShort((short)0);
      headerBuffer.put((byte)0);
      headerBuffer.put((byte)0);
      headerBuffer.putInt(0);
      headerBuffer.putInt(0);
      headerBuffer.putInt(0);
      headerBuffer.put((byte)0);
      headerBuffer.put((byte)1);
      headerBuffer.putShort((short)0);
      Buffers.writeAll(this.out, headerBuffer);

      for (final XBaseFieldDefinition field : this.fields) {
        if (field.getDataType() != DataTypes.OBJECT) {
          final String name = field.getName();
          final byte[] nameBytes = name.getBytes();
          final int length = field.getLength();
          int decimalPlaces = field.getDecimalPlaces();
          if (decimalPlaces < 0) {
            decimalPlaces = 0;
          } else if (decimalPlaces > 15) {
            decimalPlaces = Math.min(length, 15);
          } else if (decimalPlaces > length) {
            decimalPlaces = Math.min(length, 15);
          }
          headerBuffer.put(nameBytes, 0, Math.min(10, nameBytes.length));
          final int numPad = 11 - nameBytes.length;
          for (int i = 0; i < numPad; i++) {
            headerBuffer.put((byte)0);
          }
          headerBuffer.put((byte)field.getType());
          headerBuffer.putInt(0);
          headerBuffer.put((byte)length);
          headerBuffer.put((byte)decimalPlaces);
          headerBuffer.putShort((short)0);
          headerBuffer.put((byte)0);
          headerBuffer.putShort((short)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          headerBuffer.put((byte)0);
          Buffers.writeAll(this.out, headerBuffer);
        }
      }
      headerBuffer.put((byte)0x0d);
      Buffers.writeAll(this.out, headerBuffer);

    }
  }
}
