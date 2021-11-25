package com.revolsys.record.io.format.xbase;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.Buffers;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.spring.resource.Resource;

public class XbaseRecordReader extends AbstractIterator<Record> implements RecordReader {
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

  private Charset charset = StandardCharsets.UTF_8;

  private boolean closeFile = true;

  private int currentDeletedCount = 0;

  private int deletedCount = 0;

  private ReadableByteChannel in;

  private Runnable initCallback;

  private int recordCount;

  private int position = 0;

  private ByteBuffer recordBuffer;

  private RecordDefinitionImpl recordDefinition;

  private RecordFactory recordFactory;

  private short recordSize;

  private Resource resource;

  private PathName typeName;

  private final ByteBuffer buffer1 = ByteBuffer.allocate(1);

  private boolean exists = false;

  public XbaseRecordReader(final Resource resource, final RecordFactory recordFactory)
    throws IOException {
    this.resource = resource;
    final String baseName = resource.getBaseName();
    this.typeName = PathName.newPathName("/" + baseName);

    this.recordFactory = recordFactory;
    final Resource codePageResource = resource.newResourceChangeExtension("cpg");
    if (codePageResource != null && codePageResource.exists()) {
      final String charsetName = codePageResource.contentsAsString();
      try {
        this.charset = Charset.forName(charsetName);
      } catch (final Exception e) {
        Logs.debug(this, "Charset " + charsetName + " not supported for " + resource, e);
      }
    }
  }

  public XbaseRecordReader(final Resource in, final RecordFactory recordFactory,
    final Runnable initCallback) throws IOException {
    this(in, recordFactory);
    this.initCallback = initCallback;
  }

  @Override
  protected void closeDo() {
    if (this.closeFile) {
      forceClose();
    }
  }

  public void forceClose() {
    FileUtil.closeSilent(this.in);
    this.recordFactory = null;
    this.in = null;
    this.initCallback = null;
    this.recordDefinition = null;
    this.recordBuffer = null;
    this.resource = null;
  }

  private Boolean getBoolean() {
    final char c = (char)this.recordBuffer.get();
    switch (c) {
      case 't':
      case 'T':
      case 'y':
      case 'Y':
        return Boolean.TRUE;

      case 'f':
      case 'F':
      case 'n':
      case 'N':
        return Boolean.FALSE;
      default:
        return null;
    }
  }

  private Date getDate(final int len) {
    final String dateString = getString(len);
    if (dateString.trim().length() == 0 || dateString.equals("0")) {
      return null;
    } else {
      return new java.sql.Date(Dates.getDate("yyyyMMdd", dateString).getTime());
    }
  }

  public int getDeletedCount() {
    return this.deletedCount;
  }

  private Object getMemo(final int len) throws IOException {
    return null;
    /*
     * String memoIndexString = new String(record, startIndex, len).trim(); if
     * (memoIndexString.length() != 0) { int memoIndex =
     * Integer.parseInt(memoIndexString.trim()); if (memoIn == null) { File
     * memoFile = new File(mappedFile.getParentFile(), typePath + ".dbt"); if
     * (memoFile.exists()) { if (log.isInfoEnabled()) { log.info("Opening memo
     * mappedFile: " + memoFile); } memoIn = new RandomAccessFile(memoFile, "
     * r"); } else { return null; } } memoIn.seek(memoIndex 512); StringBuilder
     * memo = new StringBuilder(512); byte[] memoBuffer = new byte[512]; while
     * (memoIn.read(memoBuffer) != -1) { int i = 0; while (i <
     * memoBuffer.length) { if (memoBuffer[i] == 0x1A) { return memo.toString();
     * } memo.append((char)memoBuffer[i]); i++; } } return memo.toString(); }
     * return null;
     */
  }

  @Override
  protected Record getNext() {
    try {
      Record record = null;
      this.deletedCount = this.currentDeletedCount;
      this.currentDeletedCount = 0;
      int deleteFlag = ' ';
      do {
        this.recordBuffer.clear();
        final int readCount = Buffers.readAll(this.in, this.recordBuffer);
        if (readCount == -1) {
          throw new NoSuchElementException();
        } else if (readCount == 1 && readCount != this.recordSize) {
          throw new NoSuchElementException();
        } else if (readCount != this.recordSize) {
          throw new IllegalStateException("Unexpected end of mappedFile");
        } else {
          deleteFlag = this.recordBuffer.get();
          if (deleteFlag == -1) {
            throw new NoSuchElementException();
          } else if (deleteFlag == ' ') {
            record = loadRecord();
          } else if (deleteFlag != 0x1A) {
            this.currentDeletedCount++;
            this.position++;
          }
        }
      } while (deleteFlag == '*');
      if (record == null) {
        throw new NoSuchElementException();
      }
      return record;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private BigDecimal getNumber(final int len) {
    BigDecimal number = null;
    final String string = getString(len);
    final String numberString = string.replace('*', ' ');
    if (numberString.trim().length() != 0) {
      try {
        number = new BigDecimal(numberString.trim());
      } catch (final Throwable e) {
        Logs.error(this, "'" + numberString + " 'is not a valid number", e);
      }
    }
    return number;
  }

  public int getPosition() {
    return this.position;
  }

  public int getRecordCount() {
    return this.recordCount;
  }

  @Override
  public RecordDefinitionImpl getRecordDefinition() {
    open();
    return this.recordDefinition;
  }

  private String getString(final int len) {
    final byte[] bytes = new byte[len];
    this.recordBuffer.get(bytes, 0, len);
    final String text = new String(bytes, this.charset);
    return text.trim();
  }

  public PathName getTypeName() {
    return this.typeName;
  }

  @Override
  protected void initDo() {
    try {
      this.in = this.resource.newReadableByteChannel();
      if (this.in == null) {
        this.exists = false;
        close();
      } else {
        this.exists = true;
        loadHeader();
      }
      readRecordDefinition();
      if (this.initCallback != null) {
        this.initCallback.run();
      }
      if (this.exists) {
        this.recordBuffer = ByteBuffer.allocateDirect(this.recordSize);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error initializing mappedFile ", e);
    }
  }

  public boolean isCloseFile() {
    return this.closeFile;
  }

  /**
   * Load the header record from the shape mappedFile.
   *
   * @throws IOException If an I/O error occurs.
   */
  @SuppressWarnings("unused")
  private void loadHeader() throws IOException {
    final ByteBuffer header = ByteBuffer.allocate(32);
    header.order(ByteOrder.LITTLE_ENDIAN);
    if (Buffers.readAll(this.in, header) == 32) {
      final int version = header.get();
      final int y = header.get();
      final int m = header.get();
      final int d = header.get();
      // properties.put(new QName("date"), new Date(y, m - 1, d));
      this.recordCount = header.getInt();
      final short headerSize = header.getShort();

      this.recordSize = header.getShort();
    } else {
      throw new RuntimeException("Invalid file:" + this.resource);
    }
  }

  protected Record loadRecord() throws IOException {
    final Record record = this.recordFactory.newRecord(this.recordDefinition);
    for (int i = 0; i < this.recordDefinition.getFieldCount(); i++) {
      int length = this.recordDefinition.getFieldLength(i);
      final DataType type = this.recordDefinition.getFieldType(i);
      Object value = null;

      if (type == DataTypes.STRING) {
        if (length < 255) {
          value = getString(length);
        } else {
          value = getMemo(length);
          length = 10;
        }
      } else if (type == DataTypes.DECIMAL || type == DataTypes.FLOAT) {
        value = getNumber(length);
      } else if (type == DataTypes.BOOLEAN) {
        value = getBoolean();
      } else if (type == DataTypes.DATE_TIME) {
        value = getDate(length);
      }
      record.setValue(i, value);
    }
    return record;
  }

  private void readRecordDefinition() throws IOException {
    this.recordDefinition = new RecordDefinitionImpl(this.typeName);
    if (this.exists) {
      int readCount = Buffers.readAll(this.in, this.buffer1);
      if (readCount == -1) {
        throw new RuntimeException("Unexpected end of file: " + this.resource);
      }
      final ByteBuffer fieldHeaderBuffer = ByteBuffer.allocate(31);
      int b = this.buffer1.get();
      while (b != 0x0D) {
        this.buffer1.clear();
        readCount = Buffers.readAll(this.in, fieldHeaderBuffer);
        if (readCount != 31) {
          throw new RuntimeException("Unexpected end of file: " + this.resource);
        }
        final StringBuilder fieldName = new StringBuilder();
        boolean endOfName = false;
        for (int i = 0; i < 11; i++) {
          if (!endOfName && b != 0) {
            fieldName.append((char)b);
          } else {

            endOfName = true;
          }
          if (i != 10) {
            b = fieldHeaderBuffer.get();
          }
        }
        final char fieldType = (char)fieldHeaderBuffer.get();
        fieldHeaderBuffer.getInt();
        int length = fieldHeaderBuffer.get() & 0xFF;
        final int decimalCount = fieldHeaderBuffer.get();
        fieldHeaderBuffer.clear();
        readCount = Buffers.readAll(this.in, this.buffer1);
        if (readCount == -1) {
          throw new RuntimeException("Unexpected end of file: " + this.resource);
        }
        b = this.buffer1.get();
        final DataType dataType = DATA_TYPES.get(fieldType);
        if (fieldType == MEMO_TYPE) {
          length = Integer.MAX_VALUE;
        }
        this.recordDefinition.addField(fieldName.toString(), dataType, length, decimalCount, false);
      }
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void setCloseFile(final boolean closeFile) {
    this.closeFile = closeFile;
  }

  public void setTypeName(final PathName typeName) {
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    if (this.resource == null) {
      return super.toString();
    } else {
      return this.resource.toString();
    }
  }

}
