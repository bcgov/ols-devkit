package com.revolsys.record.io.format.scaledint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.EndOfFileException;
import com.revolsys.io.channels.DataReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;

public class AbstractScaledIntegerPointCloudGeometryReader<G extends Geometry>
  extends AbstractIterator<G> {
  private final Resource resource;

  private DataReader reader;

  private GeometryFactory geometryFactory;

  private boolean exists = false;

  private ByteBuffer byteBuffer;

  public AbstractScaledIntegerPointCloudGeometryReader(final Resource resource,
    final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    final DataReader reader = this.reader;
    this.reader = null;
    if (reader != null) {
      reader.close();
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected G getNext() {
    final DataReader reader = this.reader;
    if (reader == null) {
      throw new NoSuchElementException();
    } else {
      try {
        final int xInt = reader.getInt();
        final int yInt = reader.getInt();
        final int zInt = reader.getInt();
        final double x = this.geometryFactory.toDoubleX(xInt);
        final double y = this.geometryFactory.toDoubleY(yInt);
        final double z;
        if (zInt == Integer.MIN_VALUE) {
          z = Double.NaN;
        } else {
          z = this.geometryFactory.toDoubleZ(zInt);
        }
        return (G)this.geometryFactory.point(x, y, z);
      } catch (final EndOfFileException e) {
        throw new NoSuchElementException();
      }
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    final DataReader reader = this.resource.newChannelReader(this.byteBuffer);
    this.reader = reader;
    if (reader == null) {
      this.exists = false;
    } else {
      this.exists = true;
      final String fileType = reader.getString(4, StandardCharsets.UTF_8); // File
                                                                           // type
      if (!ScaledIntegerPointCloud.FILE_TYPE_HEADER.equals(fileType)) {
        throw new IllegalArgumentException("File must start with the text: "
          + ScaledIntegerPointCloud.FILE_TYPE_HEADER + " not " + fileType);
      }
      @SuppressWarnings("unused")
      final short version = reader.getShort();
      @SuppressWarnings("unused")
      final short flags = reader.getShort();
      this.geometryFactory = GeometryFactory.readOffsetScaled3d(reader);
    }
  }

  public boolean isExists() {
    return this.exists;
  }

  public RecordDefinition newRecordDefinition(final String name) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(name) //
      .addField("POINT", GeometryDataTypes.POINT) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    return recordDefinition;
  }

  public void setByteBuffer(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
    if (byteBuffer != null) {
      byteBuffer.order(ByteOrder.BIG_ENDIAN);
    }
  }

}
