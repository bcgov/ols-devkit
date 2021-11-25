package com.revolsys.csformat.gridshift.nadcon5;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.operation.CoordinatesOperationPoint;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class Nadcon5Region {

  private static final Map<String, Nadcon5Region> REGION_BY_NAME = new LinkedHashMap<>();

  public static final List<String> REGION_NAMES = new ArrayList<>();

  public static final List<Nadcon5Region> REGIONS = new ArrayList<>();

  static {
    addRegion("StGeorge", "20160901", 56.3, 56.8, 190.0, 190.8, "SG1897", "SG1952", "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("StPaul", "20160901", 56.9, 57.4, 189.3, 190.4, "SP1897", "SP1952", "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("StLawrence", "20160901", 62.7, 64.0, 187.5, 192.0, "SL1952", "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("Alaska", "20160901", 50.0, 73.0, 172.0, 232.0, Nadcon5.NAD27, "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("Conus", "20160901", 24.0, 50.0, 235.0, 294.0, "USSD", Nadcon5.NAD27, "NAD83(1986)",
      "NAD83(HARN)", "NAD83(FBN)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("Hawaii", "20160901", 18.0, 23.0, 199.0, 206.0, "OHD", "NAD83(1986)", "NAD83(1993)",
      "NAD83(PA11)");
    addRegion("PRVI", "20160901", 17.0, 19.0, 291.0, 296.0, "PR40", "NAD83(1986)", "NAD83(1993)",
      "NAD83(1997)", "NAD83(2002)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("AS", "20160901", -16.0, -13.0, 188.0, 193.0, "AS62", "NAD83(1993)", "NAD83(2002)",
      "NAD83(PA11)");
    addRegion("GuamCNMI", "20160901", 12.0, 22.0, 143.0, 147.0, "GU63", "NAD83(1993)",
      "NAD83(2002)", "NAD83(MA11)");
  }

  private static void addRegion(final String name, final String dateString, final double minY,
    final double maxY, final double minX, final double maxX, final String... datumNames) {
    final int index = REGIONS.size();
    final Nadcon5Region region = new Nadcon5Region(index, name, dateString, minX, minY, maxX, maxY,
      datumNames);
    REGIONS.add(region);
    REGION_NAMES.add(name);
    REGION_BY_NAME.put(name, region);

  }

  public static Nadcon5Region getRegion(final String name) {
    return REGION_BY_NAME.get(name);
  }

  private final BoundingBox boundingBox;

  private final Date date;

  private String dateString;

  private final List<String> datumNames;

  private final Nadcon5RegionDatumGrids[] grids;

  private final String name;

  private final double minX;

  private final double minY;

  private final double maxX;

  private final double maxY;

  private final int index;

  public Nadcon5Region(final int index, final String name, final String dateString,
    final double minX, final double minY, final double maxX, final double maxY,
    final String... datumNames) {
    this.index = index;
    this.name = name;
    try {
      this.dateString = dateString;
      this.date = new Date(new SimpleDateFormat("yyyyMMdd").parse(dateString).getTime());
    } catch (final ParseException e) {
      throw Exceptions.wrap("Invalid date " + dateString, e);
    }
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
    this.boundingBox = GeometryFactory.nad83().newBoundingBox(minX - 360, minY, maxX - 360, maxY);
    this.datumNames = Arrays.asList(datumNames);

    this.grids = new Nadcon5RegionDatumGrids[datumNames.length - 1];

    String sourceDatumName = datumNames[0];
    for (int datumIndex = 1; datumIndex < datumNames.length; datumIndex++) {
      final String targetDatumName = datumNames[datumIndex];
      final int fileIndex = datumIndex - 1;
      this.grids[fileIndex] = new Nadcon5RegionDatumGrids(this, sourceDatumName, targetDatumName);
      sourceDatumName = targetDatumName;
    }
  }

  public boolean covers(final CoordinatesOperationPoint point) {
    final double x = point.x;
    final double y = point.y;
    return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public Date getDate() {
    return this.date;
  }

  public String getDateString() {
    return this.dateString;
  }

  public int getDatumIndex(final String datumName) {
    final int datumIndex = this.datumNames.indexOf(datumName);

    if (datumIndex == -1) {
      throw new IllegalArgumentException("datum=" + datumName + " not found for " + this.name);
    } else {
      return datumIndex;
    }
  }

  public List<String> getDatumNames() {
    return this.datumNames;
  }

  protected List<Nadcon5RegionDatumGrids> getGrids(final int sourceDatumIndex,
    final int targetDatumIndex) {
    final List<Nadcon5RegionDatumGrids> grids = new ArrayList<>();
    if (sourceDatumIndex == -1 || targetDatumIndex == -1) {

    } else if (targetDatumIndex > sourceDatumIndex) {
      for (int i = sourceDatumIndex; i < targetDatumIndex; ++i) {
        grids.add(this.grids[i]);
      }
    } else {
      for (int i = sourceDatumIndex; i > targetDatumIndex; --i) {
        grids.add(this.grids[i]);
      }
    }
    return grids;
  }

  public List<Nadcon5RegionDatumGrids> getGrids(final String sourceDatumName,
    final String targetDatumName) {
    final int sourceDatumIndex = getDatumIndex(sourceDatumName);
    final int targetDatumIndex = getDatumIndex(sourceDatumName);
    return getGrids(sourceDatumIndex, targetDatumIndex);
  }

  public int getIndex() {
    return this.index;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
