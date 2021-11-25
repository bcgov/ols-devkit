package com.revolsys.elevation.tin.compactbinary;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.tin.IntArrayScaleTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.EndOfFileException;
import com.revolsys.io.channels.DataReader;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;

public class ScaledIntegerTriangulatedIrregularNetworkReader extends BaseObjectWithProperties
  implements BaseCloseable {
  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

  private final Resource resource;

  private GeometryFactory geometryFactory;

  private BoundingBox boundingBox;

  private DataReader in;

  private boolean closed = false;

  private boolean exists;

  public ScaledIntegerTriangulatedIrregularNetworkReader(final Resource resource,
    final MapEx properties) {
    this.resource = resource;
    setProperties(properties);
    // Make sure all properties are added
    getProperties().putAll(properties);
  }

  @Override
  public void close() {
    if (!this.closed) {
      this.closed = true;
      try {
        this.in.close();
      } finally {
        this.boundingBox = null;
        this.in = null;
      }
    }
  }

  public void forEachTriangle(final TriangleConsumer action) {
    open();
    final GeometryFactory geometryFactory = this.geometryFactory;
    final DataReader in = this.in;
    try {
      boolean hasMore = true;
      while (hasMore) {
        int triangleVertexCount = 0;
        try {
          final double x1 = geometryFactory.toDoubleX(in.getInt());
          final double y1 = geometryFactory.toDoubleY(in.getInt());
          final double z1 = geometryFactory.toDoubleZ(in.getInt());
          final double x2 = geometryFactory.toDoubleX(in.getInt());
          final double y2 = geometryFactory.toDoubleY(in.getInt());
          final double z2 = geometryFactory.toDoubleZ(in.getInt());
          final double x3 = geometryFactory.toDoubleX(in.getInt());
          final double y3 = geometryFactory.toDoubleY(in.getInt());
          final double z3 = geometryFactory.toDoubleZ(in.getInt());
          action.accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
          triangleVertexCount = 9;
        } catch (final EndOfFileException e) {
          if (triangleVertexCount == 0) {
            hasMore = false;
          } else {
            throw e;
          }
        }
      }
    } finally {
      close();
    }
  }

  public boolean isClosed() {
    return this.closed || !this.exists;
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    open();
    int capacity = 10;
    int[] triangleXCoordinates = new int[capacity];
    int[] triangleYCoordinates = new int[capacity];
    int[] triangleZCoordinates = new int[capacity];
    int coordinateIndex = 0;
    boolean hasMore = true;
    while (hasMore) {
      try {
        if (coordinateIndex >= capacity) {
          final int minCapacity = coordinateIndex + 1;
          capacity = capacity + (capacity >> 1);
          if (capacity - minCapacity < 0) {
            capacity = minCapacity;
          }
          if (capacity - MAX_ARRAY_SIZE > 0) {
            if (minCapacity < 0) {
              throw new OutOfMemoryError();
            } else if (minCapacity > MAX_ARRAY_SIZE) {
              capacity = Integer.MAX_VALUE;
            } else {
              capacity = MAX_ARRAY_SIZE;
            }
          }

          triangleXCoordinates = Arrays.copyOf(triangleXCoordinates, capacity);
          triangleYCoordinates = Arrays.copyOf(triangleYCoordinates, capacity);
          triangleZCoordinates = Arrays.copyOf(triangleZCoordinates, capacity);
        }

        triangleXCoordinates[coordinateIndex] = this.in.getInt();
        triangleYCoordinates[coordinateIndex] = this.in.getInt();
        triangleZCoordinates[coordinateIndex] = this.in.getInt();
        coordinateIndex++;
      } catch (final EndOfFileException e) {
        if (coordinateIndex % 3 == 0) {
          hasMore = false;
        } else {
          throw e;
        }
      }
    }

    triangleXCoordinates = Arrays.copyOf(triangleXCoordinates, coordinateIndex);
    triangleYCoordinates = Arrays.copyOf(triangleYCoordinates, coordinateIndex);
    triangleZCoordinates = Arrays.copyOf(triangleZCoordinates, coordinateIndex);
    final int triangleCount = coordinateIndex / 3;

    final IntArrayScaleTriangulatedIrregularNetwork tin = new IntArrayScaleTriangulatedIrregularNetwork(
      this.geometryFactory, this.boundingBox, triangleCount, triangleXCoordinates,
      triangleYCoordinates, triangleZCoordinates);
    tin.setProperties(getProperties());
    return tin;
  }

  public void open() {
    if (this.in == null && !this.closed) {
      this.in = this.resource.newChannelReader();
      if (this.in == null) {
        this.exists = false;
        this.closed = true;
      } else {
        this.exists = true;
        readHeader();
      }
    }
  }

  private void readHeader() {
    @SuppressWarnings("unused")
    final String fileType = this.in.getString(
      ScaledIntegerTriangulatedIrregularNetwork.FILE_TYPE_BYTES.length,
      StandardCharsets.ISO_8859_1); // File type
    @SuppressWarnings("unused")
    final short version = this.in.getShort();
    this.geometryFactory = GeometryFactory.readOffsetScaled3d(this.in);
    final double minX = this.in.getDouble();
    final double minY = this.in.getDouble();
    final double maxX = this.in.getDouble();
    final double maxY = this.in.getDouble();
    this.boundingBox = this.geometryFactory.newBoundingBox(2, minX, minY, maxX, maxY);
  }
}
