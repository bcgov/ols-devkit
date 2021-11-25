package com.revolsys.elevation.cloud.las;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.elevation.cloud.las.zip.LasZipHeader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.channels.DataReader;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.html.HtmlWriter;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Pair;

public class LasPointCloudHeader
  implements BoundingBoxProxy, Cloneable, GeometryFactoryProxy, MapSerializer {

  private static final int GLOBAL_ENCODING_WKT = 0b10000;

  private static final Map<Pair<String, Integer>, LasVariableLengthRecordConverter> CONVERTER_FACTORY_BY_KEY = new HashMap<>();

  static {
    LasProjection.init();
    LasZipHeader.init();
  }

  public static void addVariableLengthRecordConverter(
    final LasVariableLengthRecordConverter converter) {
    final Pair<String, Integer> key = converter.getKey();
    CONVERTER_FACTORY_BY_KEY.put(key, converter);
  }

  public static LasVariableLengthRecordConverter getVariableLengthRecordConverter(
    final Pair<String, Integer> key) {
    return CONVERTER_FACTORY_BY_KEY.get(key);
  }

  private double[] bounds;

  private int fileSourceId;

  private String generatingSoftware = "RevolutionGIS";

  private GeometryFactory geometryFactory = GeometryFactory.fixed3d(1000.0, 1000.0, 1000.0);

  private int globalEncoding = 0;

  private Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = new LinkedHashMap<>();

  private Version version = LasVersion.VERSION_1_2;

  private long pointCount = 0;

  private long[] pointCountByReturn = {
    0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l
  };

  private LasPointFormat pointFormat = LasPointFormat.Core;

  private UUID projectId;

  private RecordDefinition recordDefinition;

  private int recordLength = 20;

  private Resource resource;

  private String systemIdentifier = "TRANSFORMATION";

  private LocalDate date = LocalDate.now();

  private int headerSize;

  private long pointRecordsOffset;

  private boolean laszip;

  private final LasPointCloud pointCloud;

  @SuppressWarnings("unused")
  public LasPointCloudHeader(final LasPointCloud pointCloud, final DataReader reader,
    final GeometryFactory geometryFactory) {
    this.pointCloud = pointCloud;
    setGeometryFactory(geometryFactory);
    try {
      final String headerBytes = reader.getUsAsciiString(4);
      if (headerBytes.equals("LASF")) {
        this.fileSourceId = reader.getUnsignedShort();
        this.globalEncoding = reader.getUnsignedShort();

        final long uuidLeast = reader.getLong();
        final long uuidMost = reader.getLong();
        this.projectId = new UUID(uuidMost, uuidLeast);

        this.version = new Version(reader);
        this.systemIdentifier = reader.getUsAsciiString(32);
        this.generatingSoftware = reader.getUsAsciiString(32);
        final int dayOfYear = reader.getUnsignedShort() + 1;
        final int year = reader.getUnsignedShort();
        this.date = LocalDate.ofYearDay(year, dayOfYear);
        this.headerSize = reader.getUnsignedShort();
        this.pointRecordsOffset = reader.getUnsignedInt();
        final long numberOfVariableLengthRecords = reader.getUnsignedInt();
        int pointFormatId = reader.getUnsignedByte();
        if (pointFormatId > 127) {
          pointFormatId -= 128;
          this.laszip = true;
        }
        this.pointFormat = LasPointFormat.getById(pointFormatId);
        this.recordLength = reader.getUnsignedShort();
        this.pointCount = (int)reader.getUnsignedInt();
        for (int i = 0; i < 5; i++) {
          this.pointCountByReturn[i] = reader.getUnsignedInt();
        }
        final double scaleX = 1 / reader.getDouble();
        final double scaleY = 1 / reader.getDouble();
        final double scaleZ = 1 / reader.getDouble();
        final double offsetX = reader.getDouble();
        final double offsetY = reader.getDouble();
        final double offsetZ = reader.getDouble();

        this.geometryFactory = geometryFactory.newWithOffsetsAndScales(offsetX, scaleX, offsetY,
          scaleY, offsetZ, scaleZ);

        final double maxX = reader.getDouble();
        final double minX = reader.getDouble();
        final double maxY = reader.getDouble();
        final double minY = reader.getDouble();
        final double maxZ = reader.getDouble();
        final double minZ = reader.getDouble();
        long startOfFirstExetendedDataRecord = 0;
        long numberOfExtendedVariableLengthRecords = 0;
        if (this.headerSize > 227) {

          if (this.version.atLeast(LasVersion.VERSION_1_3)) {
            final long startOfWaveformDataPacketRecord = reader.getUnsignedLong(); // TODO
            // unsigned
            // long
            // long support
            // needed
            if (this.version.atLeast(LasVersion.VERSION_1_4)) {
              startOfFirstExetendedDataRecord = reader.getUnsignedLong();
              numberOfExtendedVariableLengthRecords = reader.getUnsignedInt();
              this.pointCount = reader.getUnsignedLong();
              for (int i = 0; i < 15; i++) {
                this.pointCountByReturn[i] = reader.getUnsignedLong();
              }
            }
          }
        }
        this.headerSize += readVariableLengthRecords(reader, numberOfVariableLengthRecords);
        this.bounds = new double[] {
          minX, minY, minZ, maxX, maxY, maxZ
        };
        if (this.version.equals(LasVersion.VERSION_1_0)) {
          reader.skipBytes(2);
          this.headerSize += 2;
        }
        final int skipCount = (int)(this.pointRecordsOffset - this.headerSize);
        reader.skipBytes(skipCount); // Skip to first point record

        readExtendedVariableLengthRecords(reader, startOfFirstExetendedDataRecord,
          numberOfExtendedVariableLengthRecords);

        this.recordDefinition = this.pointFormat.newRecordDefinition(this.geometryFactory);

      } else {
        throw new IllegalArgumentException(this.resource + " is not a valid LAS file");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error reading " + this.resource, e);
    }
  }

  public LasPointCloudHeader(final LasPointCloud pointCloud, final LasPointFormat pointFormat,
    final GeometryFactory geometryFactory) {
    this.pointCloud = pointCloud;
    this.pointFormat = pointFormat;
    if (this.pointFormat.getId() > 5) {
      this.globalEncoding |= GLOBAL_ENCODING_WKT;
    }
    this.recordLength = pointFormat.getRecordLength();
    setGeometryFactory(geometryFactory);
    this.bounds = RectangleUtil.newBounds(3);
    this.projectId = UUID.randomUUID();
    setVersion(LasVersion.VERSION_1_2);
  }

  public void addCounts(final LasPoint lasPoint) {
    this.pointCount++;
    final byte returnNumber = lasPoint.getReturnNumber();
    if (returnNumber > 0) {
      final int returnNumberIndex = returnNumber - 1;
      this.pointCountByReturn[returnNumberIndex]++;
    }
    final double x = lasPoint.getX();
    final double y = lasPoint.getY();
    final double z = lasPoint.getZ();
    RectangleUtil.expand(this.bounds, 3, x, y, z);
  }

  public void addExtendedLasProperty(final Pair<String, Integer> key, final String description,
    final Object value) {
    final LasVariableLengthRecord property = new LasVariableLengthRecord(this.pointCloud, true, key,
      description, value);
    this.lasProperties.put(key, property);
  }

  public void addLasProperty(final Pair<String, Integer> key, final String description,
    final Object value) {
    final LasVariableLengthRecord property = new LasVariableLengthRecord(this.pointCloud, key,
      description, value);
    this.lasProperties.put(key, property);
  }

  protected void addProperty(final LasVariableLengthRecord property) {
    final Pair<String, Integer> key = property.getKey();
    property.setHeader(this);
    this.lasProperties.put(key, property);
  }

  public void clear() {
    this.pointCount = 0;
    Arrays.fill(this.pointCountByReturn, 0);
    Arrays.fill(this.bounds, Double.NaN);
  }

  @Override
  public LasPointCloudHeader clone() {
    try {
      final LasPointCloudHeader clone = (LasPointCloudHeader)super.clone();
      clone.bounds = this.bounds.clone();
      clone.pointCountByReturn = this.pointCountByReturn.clone();
      clone.lasProperties = new LinkedHashMap<>();
      for (LasVariableLengthRecord variable : this.lasProperties.values()) {
        variable = variable.clone();
        clone.addProperty(variable);
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  public Object convertVariableLenthRecord(final Pair<String, Integer> key, final byte[] bytes) {
    final LasVariableLengthRecordConverter converter = getVariableLengthRecordConverter(key);
    if (converter == null) {
      return bytes;
    } else {
      return converter.readObject(this, bytes);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.geometryFactory.newBoundingBox(3, this.bounds);
  }

  public double[] getBounds() {
    return this.bounds;
  }

  public LocalDate getDate() {
    return this.date;
  }

  public int getDayOfYear() {
    return this.date.getDayOfYear() - 1;
  }

  public int getFileSourceId() {
    return this.fileSourceId;
  }

  public String getGeneratingSoftware() {
    return this.generatingSoftware;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public int getGlobalEncoding() {
    return this.globalEncoding;
  }

  public Map<Pair<String, Integer>, LasVariableLengthRecord> getLasProperties() {
    return this.lasProperties;
  }

  protected LasVariableLengthRecord getLasProperty(final Pair<String, Integer> key) {
    return this.lasProperties.get(key);
  }

  public List<Pair<String, Integer>> getLasPropertyKeys() {
    return Lists.toArray(this.lasProperties.keySet());
  }

  public <V> V getLasPropertyValue(final Pair<String, Integer> key) {
    final LasVariableLengthRecord property = this.lasProperties.get(key);
    if (property == null) {
      return null;
    } else {
      return property.getValue();
    }
  }

  public <V> V getLasPropertyValue(final Pair<String, Integer> key, final V defaultValue) {
    final LasVariableLengthRecord property = this.lasProperties.get(key);
    if (property == null) {
      return defaultValue;
    } else {
      final V value = property.getValue();
      if (value == null) {
        return defaultValue;
      } else {
        return value;
      }
    }
  }

  public LasPointCloud getPointCloud() {
    return this.pointCloud;
  }

  public long getPointCount() {
    return this.pointCount;
  }

  public long[] getPointCountByReturn() {
    return this.pointCountByReturn;
  }

  public LasPointFormat getPointFormat() {
    return this.pointFormat;
  }

  public int getPointFormatId() {
    return this.pointFormat.getId();
  }

  public UUID getProjectId() {
    return this.projectId;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public int getRecordLength() {
    return this.recordLength;
  }

  public String getSystemIdentifier() {
    return this.systemIdentifier;
  }

  public Version getVersion() {
    return this.version;
  }

  public int getYear() {
    return this.date.getYear();
  }

  public boolean isLaszip() {
    return this.laszip;
  }

  public LasPoint newLasPoint(final LasPointCloud lasPointCloud, final double x, final double y,
    final double z) {
    return this.pointFormat.newLasPoint(lasPointCloud, x, y, z);
  }

  private void readExtendedVariableLengthRecords(final DataReader reader, final long position,
    final long variableCount) throws IOException {
    if (variableCount != 0 && reader.isSeekable()) {
      final long currentPosition = reader.position();
      reader.seek(position);
      for (int i = 0; i < variableCount; i++) {
        @SuppressWarnings("unused") // Ignore reserved value
        final int reserved = reader.getUnsignedShort();
        final String userId = reader.getUsAsciiString(16);
        final int recordId = reader.getUnsignedShort();
        final long valueLength = reader.getUnsignedLong();
        final String description = reader.getUsAsciiString(32);
        if ((int)valueLength != valueLength) {
          throw new IllegalArgumentException("Extended variable length record " + userId + " "
            + recordId + " has length " + valueLength + " > " + Integer.MAX_VALUE);
        }
        final byte[] bytes = reader.getBytes((int)valueLength);
        final LasVariableLengthRecord property = new LasVariableLengthRecord(this, true, userId,
          recordId, description, bytes);
        addProperty(property);
      }

      for (final LasVariableLengthRecord property : this.lasProperties.values()) {
        if (property.isExtended()) {
          property.getValue();
        }
      }
      reader.seek(currentPosition);
    }
  }

  private int readVariableLengthRecords(final DataReader reader,
    final long numberOfVariableLengthRecords) throws IOException {
    int byteCount = 0;
    for (int i = 0; i < numberOfVariableLengthRecords; i++) {
      @SuppressWarnings("unused") // Ignore reserved value
      final int reserved = reader.getUnsignedShort();
      final String userId = reader.getUsAsciiString(16);
      final int recordId = reader.getUnsignedShort();
      final int valueLength = reader.getUnsignedShort();
      final String description = reader.getUsAsciiString(32);
      final byte[] bytes = reader.getBytes(valueLength);
      final LasVariableLengthRecord property = new LasVariableLengthRecord(this, userId, recordId,
        description, bytes);
      addProperty(property);
      byteCount += 54 + valueLength;
    }

    for (final LasVariableLengthRecord property : this.lasProperties.values()) {
      property.getValue();
    }
    return byteCount;
  }

  protected void removeLasProperties(final String userId) {
    for (final Iterator<LasVariableLengthRecord> iterator = this.lasProperties.values()
      .iterator(); iterator.hasNext();) {
      final LasVariableLengthRecord property = iterator.next();
      if (userId.equals(property.getUserId())) {
        iterator.remove();
      }
    }
  }

  public void setCoordinateSystemInternal(final CoordinateSystem coordinateSystem) {
    this.geometryFactory = this.geometryFactory.convertCoordinateSystem(coordinateSystem);
  }

  public void setDate(final LocalDate date) {
    if (date == null) {
      this.date = LocalDate.now();
    } else {
      this.date = date;
    }
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      double scaleX = geometryFactory.getScaleX();
      if (scaleX == 0) {
        scaleX = 1000;
      }
      double scaleY = geometryFactory.getScaleY();
      if (scaleY == 0) {
        scaleY = 1000;
      }
      double scaleZ = geometryFactory.getScaleZ();
      if (scaleZ == 0) {
        scaleZ = 1000;
      }
      this.geometryFactory = geometryFactory.convertScales(scaleX, scaleY, scaleZ);

      LasProjection.setCoordinateSystem(this, this.geometryFactory);
    }
  }

  public void setVersion(final Version version) {
    final Version minVersion = this.pointFormat.getMinVersion();
    if (version.atLeast(minVersion)) {
      this.version = version;
    } else {
      this.version = minVersion;
    }
  }

  @Override
  public double toDoubleX(final int x) {
    return this.geometryFactory.toDoubleX(x);
  }

  @Override
  public double toDoubleY(final int y) {
    return this.geometryFactory.toDoubleY(y);
  }

  @Override
  public double toDoubleZ(final int z) {
    return this.geometryFactory.toDoubleZ(z);
  }

  @Override
  public int toIntX(final double x) {
    return this.geometryFactory.toIntX(x);
  }

  @Override
  public int toIntY(final double y) {
    return this.geometryFactory.toIntY(y);
  }

  @Override
  public int toIntZ(final double z) {
    return this.geometryFactory.toIntZ(z);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    addToMap(map, "version", this.version);
    addToMap(map, "fileSourceId", this.fileSourceId, 0);
    addToMap(map, "systemIdentifier", this.systemIdentifier);
    addToMap(map, "generatingSoftware", this.generatingSoftware);
    addToMap(map, "date", this.date);
    if (this.geometryFactory != null) {
      final int coordinateSystemId = this.geometryFactory.getHorizontalCoordinateSystemId();
      if (coordinateSystemId > 0) {
        addToMap(map, "coordinateSystemId", coordinateSystemId);
      }

      if (this.geometryFactory.isHasHorizontalCoordinateSystem()) {
        final String name = this.geometryFactory.getHorizontalCoordinateSystemName();
        addToMap(map, "coordinateSystemName", name);
        final HorizontalCoordinateSystem coordinateSystem = this.geometryFactory
          .getHorizontalCoordinateSystem();
        addToMap(map, "coordinateSystem", coordinateSystem.toEsriWktCs());
      }
    }
    addToMap(map, "boundingBox", getBoundingBox());
    addToMap(map, "headerSize", this.headerSize);
    addToMap(map, "pointRecordsOffset", this.pointRecordsOffset, 0);
    addToMap(map, "pointFormat", this.pointFormat.getId());
    addToMap(map, "pointCount", this.pointCount);
    int returnCount = 15;
    if (this.pointFormat.getId() < 6) {
      returnCount = 5;
    }
    int returnIndex = 0;
    final List<Long> pointCountByReturn = new ArrayList<>();
    for (final long pointCountForReturn : this.pointCountByReturn) {
      if (returnIndex < returnCount) {
        pointCountByReturn.add(pointCountForReturn);
      }
      returnIndex++;
    }
    addToMap(map, "pointCountByReturn", pointCountByReturn);
    return map;
  }

  public void writeHtml(final HtmlWriter writer) {
    final DecimalFormat numberFormat = new DecimalFormat("#,###");
    writer.table() //
      .tableRowLabelValue("File Source ID", this.fileSourceId) //
      // TODO Global endcoding
      .tableRowLabelValue("Project ID", this.projectId) //
      .tableRowLabelValue("Version", this.version) //
      .tableRowLabelValue("System Identifier", this.systemIdentifier) //
      .tableRowLabelValue("Generating Software", this.generatingSoftware) //
      .tableRowLabelValue("Date", this.date) //
      .tableRowLabelValue("Format", this.pointFormat.getId() + " " + this.pointFormat) //
      .tableRowLabelValue("Record Length", this.recordLength) //
      .tableRowLabelValue("Point Count", numberFormat.format(this.pointCount)) //
    ;
    if (this.lasProperties.size() > 0) {
      writer.tr() //
        .thLabel("Properties") //
        .td()//
        .table();
      for (final LasVariableLengthRecord variable : this.lasProperties.values()) {
        writer.tableRowLabelValue(variable.getKey(), variable.getValue());
      }
      writer.endTag();
      writer.endTag();
      writer.endTag();
    }
    if (this.pointCount > 0) {
      writer.tr() //
        .thLabel("Point Count by Return") //
        .td()//
        .table();
      for (int i = 0; i < this.pointCountByReturn.length; i++) {
        final long pointCount = this.pointCountByReturn[i];
        if (pointCount > 0) {
          writer.tableRowLabelValue(i + 1, numberFormat.format(pointCount));
        }
      }
      writer.endTag();
      writer.endTag();
      writer.endTag();
    }
    writer.endTag();
  }

}
