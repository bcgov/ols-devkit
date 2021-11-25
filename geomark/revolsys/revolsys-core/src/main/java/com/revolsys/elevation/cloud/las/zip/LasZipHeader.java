package com.revolsys.elevation.cloud.las.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.LasVariableLengthRecord;
import com.revolsys.elevation.cloud.las.LasVariableLengthRecordConverterFunction;
import com.revolsys.elevation.cloud.las.Version;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.io.Buffers;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.util.Pair;

public class LasZipHeader implements MapSerializer {
  public static final Pair<String, Integer> KAY_LAS_ZIP = new Pair<>("laszip encoded", 22204);

  public static final int LASZIP_CHUNK_SIZE_DEFAULT = 50000;

  public static final byte LASZIP_CODER_ARITHMETIC = 0;

  public static final Version VERSION_1_0 = new Version(1, 0);

  public static LasZipHeader getLasZipHeader(final LasPointCloud pointCloud) {
    final LasPointCloudHeader header = pointCloud.getHeader();
    return getLasZipHeader(header);
  }

  public static LasZipHeader getLasZipHeader(final LasPointCloudHeader header) {
    return header.getLasPropertyValue(KAY_LAS_ZIP);
  }

  public static void init() {
    new LasVariableLengthRecordConverterFunction(KAY_LAS_ZIP,
      (pointCloud, bytes) -> new LasZipHeader(bytes), //
      (pointCloud, variable) -> {
        final Object value = variable.getValue();
        if (value instanceof LasZipHeader) {
          final LasZipHeader header = (LasZipHeader)value;
          return header.toBytes(pointCloud, variable);
        } else {
          return null;
        }
      });
  }

  public static LasZipHeader newLasZipHeader(final LasPointCloud pointCloud,
    final LasZipCompressorType compressor, final int lazVersion) {
    final LasPointFormat pointFormat = pointCloud.getPointFormat();
    final LasPointCloudHeader header = pointCloud.getHeader();
    final int recordLength = header.getRecordLength();
    final int extraByteCount = recordLength - pointFormat.getRecordLength();
    final LasZipHeader lasZipHeader = newLasZipHeader(pointFormat, extraByteCount, compressor,
      lazVersion);
    return lasZipHeader;
  }

  public static LasZipHeader newLasZipHeader(final LasPointFormat pointFormat,
    final int extraByteCount, final LasZipCompressorType compressor, final int lazVersion) {
    final LasZipHeader header = new LasZipHeader();
    header.initialize(pointFormat, extraByteCount, compressor, lazVersion);
    return header;
  }

  public static void setLasZipHeader(final LasPointCloud pointCloud,
    final LasZipCompressorType compressor) {
    final LasZipHeader lasZipHeader = newLasZipHeader(pointCloud, compressor, 1);
    setLasZipHeader(pointCloud, lasZipHeader);
  }

  public static void setLasZipHeader(final LasPointCloud pointCloud, final LasZipHeader header) {
    pointCloud.getHeader().addLasProperty(KAY_LAS_ZIP, "laszip", header);
  }

  private long chunkSize = LASZIP_CHUNK_SIZE_DEFAULT;

  private int coder = LASZIP_CODER_ARITHMETIC;

  private LasZipCompressorType compressor = LasZipCompressorType.POINTWISE_CHUNKED;

  private int itemCount;

  private long numberOfSpecialEvlrs = -1;

  private long offsetToSpecialEvlrs = -1;

  private long options = 0;

  private int[] sizes;

  private LasZipItemType[] types;

  private final Version version;

  private int[] versions;

  private LasZipHeader() {
    this.version = new Version(3, 2, 8);
    setItemCount(0);
  }

  private LasZipHeader(final byte[] bytes) {
    try {
      final ByteBuffer buffer = ByteBuffer.wrap(bytes);
      final int compressorId = Buffers.getLEUnsignedShort(buffer);
      this.compressor = LasZipCompressorType.getById(compressorId);
      this.coder = Buffers.getLEUnsignedShort(buffer);
      final short versionMajor = Buffers.getUnsignedByte(buffer);
      final short versionMinor = Buffers.getUnsignedByte(buffer);
      final int versionRevision = Buffers.getLEUnsignedShort(buffer);
      this.version = new Version(versionMajor, versionMinor, versionRevision);
      this.options = Buffers.getLEUnsignedInt(buffer);
      this.chunkSize = Buffers.getLEUnsignedInt(buffer);
      this.numberOfSpecialEvlrs = Buffers.getLEUnsignedLong(buffer);
      this.offsetToSpecialEvlrs = Buffers.getLEUnsignedLong(buffer);
      final int itemCount = Buffers.getLEUnsignedShort(buffer);
      setItemCount(itemCount);
      for (int i = 0; i < this.itemCount; i++) {
        final int typeId = Buffers.getLEUnsignedShort(buffer);
        this.types[i] = LasZipItemType.fromId(typeId);
        this.sizes[i] = Buffers.getLEUnsignedShort(buffer);
        this.versions[i] = Buffers.getLEUnsignedShort(buffer);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public long getChunkSize() {
    return this.chunkSize;
  }

  public LasZipCompressorType getCompressor() {
    return this.compressor;
  }

  public int getNumItems() {
    return this.itemCount;
  }

  public LasZipItemType getType(final int i) {
    return this.types[i];
  }

  public LasZipItemType[] getTypes() {
    return this.types;
  }

  public Version getVersion() {
    return this.version;
  }

  public int getVersion(final int i) {
    return this.versions[i];
  }

  public int[] getVersions() {
    return this.versions;
  }

  private void initialize(final LasPointFormat pointFormat, final int extraByteCount,
    final LasZipCompressorType compressor, final int lazVersion) {
    boolean compatible = false;

    compatible = (this.options & 1) == 1;

    // switch over the point types we know
    switch (pointFormat) {
      case Core:
        setTypes(false, extraByteCount//
          , LasZipItemType.POINT10 //
        );
      break;
      case GpsTime:
        setTypes(false, extraByteCount//
          , LasZipItemType.POINT10 //
          , LasZipItemType.GPSTIME11 //
        );
      break;
      case Rgb:
        setTypes(false, extraByteCount//
          , LasZipItemType.POINT10 //
          , LasZipItemType.RGB12 //
        );
      break;
      case GpsTimeRgb:
        setTypes(false, extraByteCount//
          , LasZipItemType.POINT10 //
          , LasZipItemType.GPSTIME11 //
          , LasZipItemType.RGB12 //
        );
      break;
      case GpsTimeWavePackets:
        setTypes(false, extraByteCount//
          , LasZipItemType.POINT10 //
          , LasZipItemType.GPSTIME11 //
          , LasZipItemType.WAVEPACKET13 //
        );
      break;
      case GpsTimeRgbWavePackets:
        setTypes(false, extraByteCount//
          , LasZipItemType.POINT10 //
          , LasZipItemType.GPSTIME11 //
          , LasZipItemType.RGB12 //
          , LasZipItemType.WAVEPACKET13 //
        );
      break;
      case ExtendedGpsTime:
        if (compatible) {
          setTypes(false, extraByteCount + 5//
            , LasZipItemType.POINT10 //
            , LasZipItemType.GPSTIME11 //
          );
        } else {
          setTypes(false, extraByteCount//
            , LasZipItemType.POINT14 //
          );
        }
      break;
      case ExtendedGpsTimeRgb:
        if (compatible) {
          setTypes(false, extraByteCount + 5//
            , LasZipItemType.POINT10 //
            , LasZipItemType.GPSTIME11 //
            , LasZipItemType.RGB12 //
          );
        } else {
          setTypes(false, extraByteCount//
            , LasZipItemType.POINT14 //
            , LasZipItemType.RGB14 //
          );
        }
      break;
      case ExtendedGpsTimeRgbNir:
        if (compatible) {
          setTypes(false, extraByteCount + 7//
            , LasZipItemType.POINT10 //
            , LasZipItemType.GPSTIME11 //
            , LasZipItemType.RGB12 //
          );
        } else {
          setTypes(false, extraByteCount//
            , LasZipItemType.POINT14 //
            , LasZipItemType.RGBNIR14 //
          );
        }
      break;
      case ExtendedGpsTimeWavePackets:
        if (compatible) {
          setTypes(false, extraByteCount + 5//
            , LasZipItemType.POINT10 //
            , LasZipItemType.GPSTIME11 //
            , LasZipItemType.WAVEPACKET13 //
          );
        } else {
          setTypes(false, extraByteCount//
            , LasZipItemType.POINT14 //
            , LasZipItemType.WAVEPACKET14 //
          );
        }
      break;
      case ExtendedGpsTimeRgbNirWavePackets:
        if (compatible) {
          setTypes(false, extraByteCount + 7//
            , LasZipItemType.POINT10 //
            , LasZipItemType.GPSTIME11 //
            , LasZipItemType.RGB12 //
            , LasZipItemType.WAVEPACKET13 //
          );
        } else {
          setTypes(false, extraByteCount//
            , LasZipItemType.POINT14 //
            , LasZipItemType.RGBNIR14 //
            , LasZipItemType.WAVEPACKET14 //
          );
        }
      break;
      default:
    }

    requestVersion(lazVersion);
    if (this.types[0] == LasZipItemType.POINT14) {
      this.compressor = LasZipCompressorType.LAYERED_CHUNKED;
    } else {
      if (compressor == LasZipCompressorType.LAYERED_CHUNKED) {
        this.compressor = LasZipCompressorType.POINTWISE_CHUNKED;
      } else if (compressor == null) {
        this.compressor = LasZipCompressorType.POINTWISE_CHUNKED;
      } else {
        this.compressor = compressor;
      }
    }
    if (this.compressor != LasZipCompressorType.POINTWISE) {
      if (this.chunkSize == 0) {
        this.chunkSize = LASZIP_CHUNK_SIZE_DEFAULT;
      }
    }
  }

  public boolean isCompressor(final LasZipCompressorType compressor) {
    return this.compressor == compressor;
  }

  public LasZipItemCodec[] newLazCodecs(final ArithmeticCodingCodec codec) {
    final int itemCount = this.getNumItems();
    final LasZipItemCodec[] itemCodecs = new LasZipItemCodec[itemCount];
    for (int i = 0; i < itemCount; i++) {
      final LasZipItemType type = this.types[i];
      final int version = this.versions[i];
      final int size = this.sizes[i];
      itemCodecs[i] = type.newCodec(codec, version, size);
    }
    return itemCodecs;
  }

  private void requestVersion(final int requested_version) {
    for (int i = 0; i < this.itemCount; i++) {
      switch (this.types[i]) {
        case POINT10:
        case GPSTIME11:
        case RGB12:
        case BYTE:
          this.versions[i] = requested_version;
        break;
        case WAVEPACKET13:
          this.versions[i] = 1;
        break;
        case POINT14:
        case RGB14:
        case RGBNIR14:
        case WAVEPACKET14:
        case BYTE14:
          this.versions[i] = 3;
        break;
        default:
          throw new IllegalArgumentException("item type not supported");
      }
    }
  }

  private void setItemCount(final int itemCount) {
    this.itemCount = itemCount;
    this.types = new LasZipItemType[itemCount];
    this.sizes = new int[itemCount];
    this.versions = new int[itemCount];
  }

  private void setType(final int index, final LasZipItemType type) {
    this.types[index] = type;
    this.sizes[index] = type.getSize();
    this.versions[index] = 0;
  }

  private void setType(final int index, final LasZipItemType type, final int size) {
    this.types[index] = type;
    this.sizes[index] = size;
    this.versions[index] = 0;
  }

  private void setTypes(final boolean version14, final int extraByteCount,
    final LasZipItemType... types) {
    final int itemCount = types.length;
    if (extraByteCount < 0) {
      throw new IllegalArgumentException("Extra byte count to small");
    }
    if (extraByteCount > 0) {
      setItemCount(itemCount + 1);
    } else {
      setItemCount(itemCount);
    }
    for (int i = 0; i < types.length; i++) {
      final LasZipItemType type = types[i];
      setType(i, type);
    }
    if (extraByteCount > 0) {
      if (version14) {
        setType(itemCount, LasZipItemType.BYTE14, extraByteCount);
      } else {
        setType(itemCount, LasZipItemType.BYTE, extraByteCount);
      }
    }
  }

  public byte[] toBytes(final LasPointCloud cloud, final LasVariableLengthRecord variable) {
    final ByteArrayOutputStream bytes = new ByteArrayOutputStream(34 + 6 * this.itemCount);
    try (
      ChannelWriter out = new ChannelWriter(Channels.newChannel(bytes))) {
      out.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      out.putUnsignedShort(this.compressor.getId());
      out.putUnsignedShort(this.coder);
      out.putUnsignedByte(this.version.getMajor());
      out.putUnsignedByte(this.version.getMinor());
      out.putUnsignedShort(this.version.getRevision());
      out.putUnsignedInt(this.options);
      out.putUnsignedInt(this.chunkSize);
      out.putUnsignedLong(this.numberOfSpecialEvlrs);
      out.putUnsignedLong(this.offsetToSpecialEvlrs);
      out.putUnsignedShort(this.itemCount);
      for (int i = 0; i < this.itemCount; i++) {
        out.putUnsignedShort(this.types[i].getId());
        out.putUnsignedShort(this.sizes[i]);
        out.putUnsignedShort(this.versions[i]);
      }
    }
    return bytes.toByteArray();
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    addToMap(map, "compressor", this.compressor);
    addToMap(map, "coder", this.coder);
    addToMap(map, "version", this.version);
    addToMap(map, "options", this.options);
    addToMap(map, "chunkSize", this.chunkSize);
    addToMap(map, "numberOfSpecialEvlrs", this.numberOfSpecialEvlrs);
    addToMap(map, "offsetToSpecialEvlrs", this.offsetToSpecialEvlrs);
    addToMap(map, "numItems", this.itemCount);
    addToMap(map, "types", this.types);
    addToMap(map, "sizes", this.sizes);
    addToMap(map, "versions", this.versions);

    return map;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }
}
