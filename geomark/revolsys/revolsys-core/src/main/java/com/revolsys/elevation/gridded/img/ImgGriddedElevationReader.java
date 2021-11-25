package com.revolsys.elevation.gridded.img;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.channels.DataReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.InputStreamResource;
import com.revolsys.spring.resource.NoSuchResourceException;
import com.revolsys.spring.resource.Resource;

public class ImgGriddedElevationReader extends BaseObjectWithProperties
  implements GriddedElevationModelReader {

  private BoundingBox boundingBox;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private boolean initialized;

  private final Resource resource;

  DataReader channel;

  private ImgEntry root;

  private ImgFieldTypeDictionary dictionary;

  private int xSize;

  private int ySize;

  private char c;

  private final List<ImgBand> bands = new ArrayList<>();

  private final StringBuilder stringBuilder = new StringBuilder();

  private double upperLeftCenterX;

  private double upperLeftCenterY;

  private double lowerRightCenterX;

  private double lowerRightCenterY;

  private double gridCellWidth;

  private double gridCellHeight;

  private String units;

  public ImgGriddedElevationReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    this.resource = resource;
    setProperties(properties);
    if (this.geometryFactory == GeometryFactory.DEFAULT_3D) {
      this.geometryFactory = GeometryFactory.floating3d(resource, GeometryFactory.DEFAULT_3D);
    }
  }

  @Override
  public void close() {
    if (this.channel != null) {
      this.channel.close();
      this.channel = null;
    }
  }

  protected ImgFieldType findType(final String typeName) {
    return this.dictionary.getFieldType(typeName);
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      init();
      final GeometryFactory geometryFactory = getGeometryFactory();
      this.boundingBox = geometryFactory.newBoundingBox(
        this.upperLeftCenterX - this.gridCellWidth / 2,
        this.lowerRightCenterY - this.gridCellHeight / 2,
        this.lowerRightCenterX + this.gridCellWidth / 2,
        this.upperLeftCenterY + this.gridCellHeight / 2);
    }
    return this.boundingBox;
  }

  protected DataReader getChannel() {

    final String fileExtension = this.resource.getFileNameExtension();
    try {
      if (fileExtension.equals("zip")) {
        final ZipInputStream in = this.resource.newBufferedInputStream(ZipInputStream::new);
        final String fileName = this.resource.getBaseName();
        final String baseName = FileUtil.getBaseName(fileName);
        final String projName = baseName + ".prj";
        for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in
          .getNextEntry()) {
          final String name = zipEntry.getName();
          if (name.equals(projName)) {
            final String wkt = FileUtil.getString(new InputStreamReader(in, StandardCharsets.UTF_8),
              false);
            final GeometryFactory geometryFactory = GeometryFactory.floating3d(wkt);
            if (geometryFactory.isHasHorizontalCoordinateSystem()) {
              this.geometryFactory = geometryFactory;
            }
          } else if (name.equals(fileName)) {
            return new InputStreamResource(in).newChannelReader();
          }
        }
        throw new IllegalArgumentException("Cannot find " + fileName + " in " + this.resource);
      } else if (fileExtension.equals("gz")) {
        final InputStream in = this.resource.newBufferedInputStream();
        final GZIPInputStream gzIn = new GZIPInputStream(in);
        return new InputStreamResource(gzIn).newChannelReader();
      } else {
        return this.resource.newChannelReader();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to open: " + this.resource, e);
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.geometryFactory == GeometryFactory.DEFAULT_3D) {
      init();
      if (this.bands.size() == 0) {
        this.geometryFactory = GeometryFactory.DEFAULT_3D;
      } else {
        this.geometryFactory = this.bands.get(0).getGeometryFactory();
      }
    }
    return this.geometryFactory;
  }

  private double[] getGeoTransform() {

    final double[] padfGeoTransform = new double[6];
    padfGeoTransform[0] = 0.0;
    padfGeoTransform[1] = 1.0;
    padfGeoTransform[2] = 0.0;
    padfGeoTransform[3] = 0.0;
    padfGeoTransform[4] = 0.0;
    padfGeoTransform[5] = 1.0;

    if (loadMapInfo()) {
      padfGeoTransform[0] = this.upperLeftCenterX - this.gridCellWidth * 0.5;
      padfGeoTransform[1] = this.gridCellWidth;
      if (padfGeoTransform[1] == 0.0) {
        padfGeoTransform[1] = 1.0;
      }
      padfGeoTransform[2] = 0.0;
      if (this.upperLeftCenterY >= this.lowerRightCenterY) {
        padfGeoTransform[5] = -this.gridCellHeight;
      } else {
        padfGeoTransform[5] = this.gridCellHeight;
      }
      if (padfGeoTransform[5] == 0.0) {
        padfGeoTransform[5] = 1.0;
      }

      padfGeoTransform[3] = this.upperLeftCenterY - padfGeoTransform[5] * 0.5;
      padfGeoTransform[4] = 0.0;

      if (this.units.equals("ds")) {
        padfGeoTransform[0] /= 3600.0;
        padfGeoTransform[1] /= 3600.0;
        padfGeoTransform[2] /= 3600.0;
        padfGeoTransform[3] /= 3600.0;
        padfGeoTransform[4] /= 3600.0;
        padfGeoTransform[5] /= 3600.0;
      }

      return padfGeoTransform;
    } else if (this.bands.size() == 0) {
      return null;
    } else {
      throw new RuntimeException("Transform not supported");
    }
  }

  @Override
  public double getGridCellHeight() {
    return this.gridCellHeight;
  }

  @Override
  public double getGridCellWidth() {
    return this.gridCellWidth;
  }

  private void init() {
    if (!this.initialized) {
      this.initialized = true;
      this.channel = getChannel();
      if (this.channel == null) {
        throw new NoSuchResourceException(this.resource);
      } else {
        this.channel.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        readHeader();
        for (final ImgBand band : this.bands) {
          band.loadBlockInfo();
        }
      }
    }
  }

  private boolean loadMapInfo() {
    if (!this.bands.isEmpty()) {
      final ImgBand firstBand = this.bands.get(0);
      final ImgEntry mapInfoEntry = firstBand.getMapInfo();

      if (mapInfoEntry != null) {
        @SuppressWarnings("unused")
        final String proName = mapInfoEntry.getString("proName");

        final MapEx upperLeftCenter = mapInfoEntry.getValue("upperLeftCenter");
        this.upperLeftCenterX = upperLeftCenter.getDouble("x");
        this.upperLeftCenterY = upperLeftCenter.getDouble("y");

        final MapEx lowerRightCenter = mapInfoEntry.getValue("lowerRightCenter");
        this.lowerRightCenterX = lowerRightCenter.getDouble("x");
        this.lowerRightCenterY = lowerRightCenter.getDouble("y");

        final MapEx pixelSize = mapInfoEntry.getValue("pixelSize");
        this.gridCellWidth = pixelSize.getDouble("width", Double.NaN);
        this.gridCellHeight = pixelSize.getDouble("height", Double.NaN);

        if (!Double.isFinite(this.gridCellWidth)) {
          this.gridCellWidth = pixelSize.getDouble("x");
          this.gridCellHeight = pixelSize.getDouble("y");
        }

        this.units = mapInfoEntry.getString("units");
        return true;
      }
    }
    return false;
  }

  private void parseBandInfo() {
    // Find the first band node.
    ImgEntry node = this.root.getChild();
    while (node != null) {
      if (node.equalsType("Eimg_Layer")) {
        final int width = node.getInteger("width");
        final int height = node.getInteger("height");
        if (width > 0 && height > 0) {
          if (this.bands.isEmpty()) {
            this.xSize = width;
            this.ySize = height;
          } else if (width != this.xSize || height != this.ySize) {
            throw new IllegalArgumentException();
          }

          final int bandNumber = this.bands.size() + 1;
          final ImgBand band = ImgBand.newBand(this, bandNumber, node);
          this.bands.add(band);
        }
      }

      node = node.getNext();
    }
  }

  @Override
  public final GriddedElevationModel read() {
    init();
    for (final ImgBand band : this.bands) {
      band.loadBlockInfo();
      return band.getGriddedElevationModel();
    }
    return null;
  }

  protected byte readByte() {
    return this.channel.getByte();
  }

  protected char readChar() {
    this.c = (char)readByte();
    return this.c;
  }

  private boolean readDictionary() {
    final List<ImgFieldType> types = new ArrayList<>();
    for (readChar(); this.c != '.'; readChar()) {
      if (this.c == '{') {
        readChar();
        final List<ImgField> fields = readFields();
        this.stringBuilder.setLength(0);
        for (readChar(); this.c != ',' && this.c != '.'; readChar()) {
          this.stringBuilder.append(this.c);
        }

        final String typeName = this.stringBuilder.toString();
        final ImgFieldType type = new ImgFieldType(typeName, fields);
        types.add(type);

      } else {
        throw new IllegalArgumentException();
      }
    }
    this.dictionary = new ImgFieldTypeDictionary(types);

    return false;
  }

  protected double readDouble() {
    return this.channel.getDouble();
  }

  private List<ImgField> readFields() {
    final List<ImgField> fields = new ArrayList<>();
    do {
      @SuppressWarnings("unused")
      final int itemCount = Integer.parseInt(readStringCurrent(':'));

      char itemType = readChar();
      String fieldTypeName = null;
      char pointerType = '\0';
      if (itemType == '*' || itemType == 'p') {
        pointerType = itemType;
        itemType = readChar();
      }
      List<String> enumValues = Collections.emptyList();
      ImgFieldType fieldType = null;
      if ('o' == itemType) {
        fieldTypeName = readString();
      } else if ('x' == itemType) {
        if (readChar() == '{') {
          readChar();
          final List<ImgField> subFields = readFields();
          final String typeName = readString();
          fieldType = new ImgFieldType(typeName, subFields);
        }
      } else {
        if (itemType == 'e') {
          enumValues = new ArrayList<>();
          final int enumCount = Integer.parseInt(readString(':'));
          for (int enumIndex = 0; enumIndex < enumCount; enumIndex++) {
            final String enumValue = readString();
            enumValues.add(enumValue);
          }
        }
      }

      final String fieldName = readString();
      final ImgField field = new ImgField(pointerType, itemType, fieldName, fieldType,
        fieldTypeName, enumValues);
      fields.add(field);
      readChar();
    } while (this.c != '}');
    return fields;
  }

  protected float readFloat() {
    return this.channel.getFloat();
  }

  @SuppressWarnings("unused")
  private void readHeader() {
    init();
    final int byteCount = 16;
    if ("EHFA_HEADER_TAG".equals(readString0(byteCount))) {
      final int headerPosition = readInt();
      this.channel.seek(headerPosition);
      final int version = readInt();
      final int freeList = readInt();

      final int rootPosition = readInt();
      final short entryHeaderLength = readShort();
      final int dictionaryPosition = readInt();
      seek(dictionaryPosition);
      while (readDictionary()) {
      }
      this.root = new ImgEntry(this, rootPosition);
      // System.out.println(Json.toString(this.root.toMap(), true));
      parseBandInfo();
      getGeoTransform();
    } else {
      throw new IllegalArgumentException(this.resource + " is not a valid IMG file");
    }
  }

  protected int readInt() {
    return this.channel.getInt();
  }

  protected short readShort() {
    return this.channel.getShort();
  }

  protected String readString() {
    final char endChar = ',';
    return readString(endChar);
  }

  protected String readString(final char endChar) {
    this.stringBuilder.setLength(0);
    for (readChar(); this.c != endChar; readChar()) {
      this.stringBuilder.append(this.c);
    }
    return this.stringBuilder.toString();
  }

  protected String readString0() {
    this.stringBuilder.setLength(0);
    for (readChar(); this.c != 0; readChar()) {
      this.stringBuilder.append(this.c);
    }
    return this.stringBuilder.toString();
  }

  protected String readString0(final int byteCount) {
    final String string = this.channel.getUsAsciiString(byteCount - 1);
    readByte();
    return string;
  }

  private String readStringCurrent(final char endChar) {
    this.stringBuilder.setLength(0);
    for (; this.c != endChar; readChar()) {
      this.stringBuilder.append(this.c);
    }
    return this.stringBuilder.toString();
  }

  protected short readUnsignedByte() {
    return this.channel.getUnsignedByte();
  }

  protected long readUnsignedInt() {
    return this.channel.getUnsignedInt();
  }

  protected int readUnsignedShort() {
    return this.channel.getUnsignedShort();
  }

  protected void seek(final long position) {
    this.channel.seek(position);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

  @Override
  public String toString() {
    return this.resource.toString();
  }

}
