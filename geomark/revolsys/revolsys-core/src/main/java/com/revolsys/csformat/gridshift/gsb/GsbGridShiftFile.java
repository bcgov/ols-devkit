package com.revolsys.csformat.gridshift.gsb;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystemProxy;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.operation.gridshift.HorizontalShiftOperation;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.io.channels.DataReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;

public class GsbGridShiftFile {
  private static GeographicCoordinateSystem getCoordinateSystem(final String name) {
    final GeographicCoordinateSystem coordinateSystem = EpsgCoordinateSystems
      .getCoordinateSystem(name);
    if (coordinateSystem == null) {
      if ("CSRS98".equals(name)) { // NAD83(CSRS98)
        return EpsgCoordinateSystems.getCoordinateSystem(4140);
      } else if ("CSRSv2".equals(name)) { // NAD83(CSRS)v2
        return EpsgCoordinateSystems.getCoordinateSystem(8237);
      } else if ("CSRSv3".equals(name)) { // NAD83(CSRS)v3
        return EpsgCoordinateSystems.getCoordinateSystem(8240);
      } else if ("CSRSv4".equals(name)) { // NAD83(CSRS)v4
        return EpsgCoordinateSystems.getCoordinateSystem(8246);
      } else if ("CSRSv5".equals(name)) { // NAD83(CSRS)v5
        return EpsgCoordinateSystems.getCoordinateSystem(8249);
      } else if ("CSRSv6".equals(name)) { // NAD83(CSRS)v6
        return EpsgCoordinateSystems.getCoordinateSystem(8252);
      } else if ("CSRSv7".equals(name)) { // NAD83(CSRS)v7
        return EpsgCoordinateSystems.getCoordinateSystem(8255);
      } else {
        return null;
      }
    } else {
      return coordinateSystem;
    }
  }

  private final String version;

  private final List<GsbGridShiftGrid> grids = new ArrayList<>();

  private transient DataReader in;

  private GeographicCoordinateSystem fromCoordinateSystem;

  private GeographicCoordinateSystem toCoordinateSystem;

  private final HorizontalShiftOperation forwardOperation = new GsbGridShiftOperation(this);

  private final HorizontalShiftOperation inverseOperation = new GsbGridShiftInverseOperation(this);

  private final Resource resource;

  @SuppressWarnings("unused")
  public GsbGridShiftFile(final Object source, final boolean loadAccuracy) {
    this.resource = Resource.getResource(source);
    try (
      DataReader in = this.resource.newChannelReader()) {
      this.in = in;
      in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      final String overviewHeaderCountId = in.getString(8, StandardCharsets.ISO_8859_1);
      if (!"NUM_OREC".equals(overviewHeaderCountId)) {
        throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
      }
      int overviewHeaderCount = readInt();
      if (overviewHeaderCount == 11) {
      } else {
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        overviewHeaderCount = Integer.reverseBytes(overviewHeaderCount);
        if (overviewHeaderCount == 11) {
        } else {
          throw new IllegalArgumentException("Input file is not an NTv2 grid shift file");
        }
      }
      final int gridHeaderCount = readRecordInt();
      final int gridCount = readRecordInt();
      final String shiftType = readRecordString();
      if (!"SECONDS".equals(shiftType)) {
        throw new IllegalArgumentException(
          "shiftType=" + shiftType + " not supported, must be SECONDS");
      }
      this.version = readRecordString();
      final String fromEllipsoidName = readRecordString();
      final String toEllipsoidName = readRecordString();
      this.fromCoordinateSystem = readCoordinateSystem(fromEllipsoidName);
      this.toCoordinateSystem = readCoordinateSystem(toEllipsoidName);

      loadGrids(loadAccuracy, gridCount);
    } finally {
      this.in = null;
    }
  }

  public void addForwardGridShiftOperation(final HorizontalCoordinateSystemProxy sourceCs,
    final HorizontalCoordinateSystemProxy targetCs) {
    sourceCs.addGridShiftOperation(targetCs, this.inverseOperation);
  }

  public void addForwardGridShiftOperation(final int sourceCsId, final int targetCsId) {
    final GeographicCoordinateSystem sourceCs = EpsgCoordinateSystems
      .getCoordinateSystem(sourceCsId);
    final GeographicCoordinateSystem targetCs = EpsgCoordinateSystems
      .getCoordinateSystem(targetCsId);
    if (sourceCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + sourceCsId);
    } else if (targetCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + targetCs);
    } else {
      sourceCs.addGridShiftOperation(targetCs, this.inverseOperation);
    }
  }

  public void addGridShiftOperation(final HorizontalCoordinateSystemProxy sourceCs,
    final HorizontalCoordinateSystemProxy targetCs) {
    sourceCs.addGridShiftOperation(targetCs, this.forwardOperation);
    targetCs.addGridShiftOperation(sourceCs, this.inverseOperation);
  }

  public void addGridShiftOperation(final int sourceCsId, final int targetCsId) {
    final GeographicCoordinateSystem sourceCs = EpsgCoordinateSystems
      .getCoordinateSystem(sourceCsId);
    final GeographicCoordinateSystem targetCs = EpsgCoordinateSystems
      .getCoordinateSystem(targetCsId);
    if (sourceCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + sourceCsId);
    } else if (targetCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + targetCs);
    } else {
      sourceCs.addGridShiftOperation(targetCs, this.forwardOperation);
      targetCs.addGridShiftOperation(sourceCs, this.inverseOperation);
    }
  }

  public void addInverseGridShiftOperation(final HorizontalCoordinateSystemProxy sourceCs,
    final HorizontalCoordinateSystemProxy targetCs) {
    sourceCs.addGridShiftOperation(targetCs, this.inverseOperation);
  }

  public void addInverseGridShiftOperation(final int sourceCsId, final int targetCsId) {
    final GeographicCoordinateSystem sourceCs = EpsgCoordinateSystems
      .getCoordinateSystem(sourceCsId);
    final GeographicCoordinateSystem targetCs = EpsgCoordinateSystems
      .getCoordinateSystem(targetCsId);
    if (sourceCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + sourceCsId);
    } else if (targetCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + targetCs);
    } else {
      sourceCs.addGridShiftOperation(targetCs, this.inverseOperation);
    }
  }

  public HorizontalShiftOperation getForwardOperation() {
    return this.forwardOperation;
  }

  public GeographicCoordinateSystem getFromCoordinateSystem() {
    return this.fromCoordinateSystem;
  }

  public GsbGridShiftGrid getGrid(final double lonPositiveWestSeconds, final double latSeconds) {
    for (final GsbGridShiftGrid topLevelSubGrid : this.grids) {
      final GsbGridShiftGrid gsbGridShiftGrid = topLevelSubGrid.getGrid(lonPositiveWestSeconds,
        latSeconds);
      if (gsbGridShiftGrid != null) {
        return gsbGridShiftGrid;
      }
    }
    return null;
  }

  public HorizontalShiftOperation getInverseOperation() {
    return this.inverseOperation;
  }

  public GeographicCoordinateSystem getToCoordinateSystem() {
    return this.toCoordinateSystem;
  }

  public String getVersion() {
    return this.version;
  }

  private void loadGrids(final boolean loadAccuracy, final int gridCount) {
    final List<GsbGridShiftGrid> grids = new ArrayList<>();
    for (int i = 0; i < gridCount; i++) {
      final GsbGridShiftGrid grid = new GsbGridShiftGrid(this, loadAccuracy);
      grids.add(grid);
    }
    final Map<String, GsbGridShiftGrid> gridByName = new HashMap<>();
    for (final GsbGridShiftGrid grid : grids) {
      if (!grid.hasParent()) {
        this.grids.add(grid);
      }
      final String name = grid.getName();
      gridByName.put(name, grid);
    }
    for (final GsbGridShiftGrid grid : grids) {
      if (grid.hasParent()) {
        final String parentName = grid.getParentName();
        final GsbGridShiftGrid parentGrid = gridByName.get(parentName);
        parentGrid.addGrid(grid);
      }
    }
  }

  private GeographicCoordinateSystem readCoordinateSystem(final String name) {
    final double semiMajorAxis = readRecordDouble();
    final double semiMinorAxis = readRecordDouble();
    final GeographicCoordinateSystem coordinateSystem = getCoordinateSystem(name);
    if (coordinateSystem == null) {
      final Ellipsoid ellipsoid = Ellipsoid.newMajorMinor(name, semiMajorAxis, semiMinorAxis);
      return new GeographicCoordinateSystem(name, ellipsoid);
    }
    return coordinateSystem;
  }

  protected float readFloat() {
    return this.in.getFloat();
  }

  @SuppressWarnings("unused")
  protected int readInt() {
    final int value = this.in.getInt();
    final int suffix = this.in.getInt();
    return value;
  }

  @SuppressWarnings("unused")
  protected double readRecordDouble() {
    final long prefix = this.in.getLong();
    final double value = this.in.getDouble();
    return value;
  }

  @SuppressWarnings("unused")
  protected int readRecordInt() {
    final long prefix = this.in.getLong();
    return readInt();
  }

  @SuppressWarnings("unused")
  protected String readRecordString() {
    final long prefix = this.in.getLong();
    final String value = this.in.getString(8, StandardCharsets.ISO_8859_1).trim();
    return value;
  }

  public void removeForwardGridShiftOperation(final HorizontalCoordinateSystemProxy sourceCs,
    final HorizontalCoordinateSystemProxy targetCs) {
    sourceCs.removeGridShiftOperation(targetCs, this.inverseOperation);
  }

  public void removeForwardGridShiftOperation(final int sourceCsId, final int targetCsId) {
    final GeographicCoordinateSystem sourceCs = EpsgCoordinateSystems
      .getCoordinateSystem(sourceCsId);
    final GeographicCoordinateSystem targetCs = EpsgCoordinateSystems
      .getCoordinateSystem(targetCsId);
    if (sourceCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + sourceCsId);
    } else if (targetCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + targetCs);
    } else {
      sourceCs.removeGridShiftOperation(targetCs, this.inverseOperation);
    }
  }

  public void removeGridShiftOperation(final HorizontalCoordinateSystemProxy sourceCs,
    final HorizontalCoordinateSystemProxy targetCs) {
    sourceCs.removeGridShiftOperation(targetCs, this.forwardOperation);
    targetCs.removeGridShiftOperation(sourceCs, this.inverseOperation);
  }

  public void removeGridShiftOperation(final int sourceCsId, final int targetCsId) {
    final GeographicCoordinateSystem sourceCs = EpsgCoordinateSystems
      .getCoordinateSystem(sourceCsId);
    final GeographicCoordinateSystem targetCs = EpsgCoordinateSystems
      .getCoordinateSystem(targetCsId);
    if (sourceCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + sourceCsId);
    } else if (targetCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + targetCs);
    } else {
      sourceCs.removeGridShiftOperation(targetCs, this.forwardOperation);
      targetCs.removeGridShiftOperation(sourceCs, this.inverseOperation);
    }
  }

  public void removeInverseGridShiftOperation(final HorizontalCoordinateSystemProxy sourceCs,
    final HorizontalCoordinateSystemProxy targetCs) {
    sourceCs.removeGridShiftOperation(targetCs, this.inverseOperation);
  }

  public void removeInverseGridShiftOperation(final int sourceCsId, final int targetCsId) {
    final GeographicCoordinateSystem sourceCs = EpsgCoordinateSystems
      .getCoordinateSystem(sourceCsId);
    final GeographicCoordinateSystem targetCs = EpsgCoordinateSystems
      .getCoordinateSystem(targetCsId);
    if (sourceCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + sourceCsId);
    } else if (targetCs == null) {
      throw new IllegalArgumentException("Coordinate system doesn't exist" + targetCs);
    } else {
      sourceCs.removeGridShiftOperation(targetCs, this.inverseOperation);
    }
  }

  @Override
  public String toString() {
    return this.fromCoordinateSystem + " -> " + this.toCoordinateSystem;
  }

  public void writeGridExtents() {
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder() //
      .addField("name", DataTypes.STRING) //
      .addField(GeometryDataTypes.POLYGON) //
      .setGeometryFactory(GeometryFactory.floating2d(this.fromCoordinateSystem))
      .getRecordDefinition();
    try (
      RecordWriter writer = RecordWriter.newRecordWriter(recordDefinition,
        this.resource.newResourceChangeExtension("tsv"))) {
      writeGridExtents(writer, this.grids);
    }
  }

  private void writeGridExtents(final RecordWriter writer, final List<GsbGridShiftGrid> grids) {
    for (final GsbGridShiftGrid grid : grids) {
      final String name = grid.getName();
      final Polygon polygon = grid.getBoundingBox().toPolygon(10);
      writer.write(name, polygon);
      writeGridExtents(writer, grid.getGrids());
    }
  }

}
