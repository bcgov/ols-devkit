package com.revolsys.elevation.cloud.las;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.elevation.cloud.las.zip.LasZipCompressorType;
import com.revolsys.elevation.cloud.las.zip.LasZipHeader;
import com.revolsys.elevation.cloud.las.zip.LasZipPointCloudWriterFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.StringWriter;
import com.revolsys.io.ZipUtil;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.DataReader;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.html.HtmlWriter;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Pair;

public class LasPointCloud extends BaseObjectWithProperties
  implements PointCloud<LasPoint>, BaseCloseable, MapSerializer, Iterable<LasPoint> {

  public static void forEachPoint(final Object source, final Consumer<? super LasPoint> action) {
    try (
      final LasPointCloud pointCloud = PointCloud.newPointCloud(source)) {
      pointCloud.forEachPoint(action);
    }
  }

  public static LasPointCloudWriter newWriter(final LasPointFormat pointFormat,
    final GeometryFactory geometryFactory, final Object target, final MapEx properties) {
    @SuppressWarnings("resource")
    final LasPointCloud pointCloud = new LasPointCloud(pointFormat, geometryFactory);
    pointCloud.setProperties(properties);
    return pointCloud.newWriter(target, properties);
  }

  private boolean allLoaded = false;

  private ByteBuffer byteBuffer;

  private final long[] classificationCounts = new long[256];

  private boolean classificationsLoaded;

  private boolean exists;

  private double fileGpsTime = 0;

  private GeometryFactory geometryFactory = GeometryFactory.fixed3d(1000.0, 1000.0, 1000.0);

  private LasPointCloudHeader header;

  private Resource lasResource;

  private List<LasPoint> points = new ArrayList<>();

  private ChannelReader reader;

  private Resource resource;

  public LasPointCloud(final LasPointFormat pointFormat, final GeometryFactory geometryFactory) {
    final LasPointCloudHeader header = new LasPointCloudHeader(this, pointFormat, geometryFactory);
    setHeader(header);
    this.geometryFactory = header.getGeometryFactory();
  }

  public LasPointCloud(final Resource resource, final MapEx properties) {
    setProperties(properties);
    this.resource = resource;
    this.lasResource = resource;
    if (resource.getFileNameExtension().equals("zip")) {
      final Pair<Resource, GeometryFactory> result = ZipUtil
        .getZipResourceAndGeometryFactory(resource, ".las", this.geometryFactory);
      this.lasResource = result.getValue1();
      if (this.geometryFactory == null) {
        this.geometryFactory = result.getValue2();
      }
    } else {
      if (this.geometryFactory == null || !this.geometryFactory.isHasHorizontalCoordinateSystem()) {
        final GeometryFactory geometryFactoryFromPrj = GeometryFactory.floating3d(resource);
        if (geometryFactoryFromPrj != null) {
          this.geometryFactory = geometryFactoryFromPrj;
        }
      }
    }

    this.reader = open();
  }

  @SuppressWarnings("unchecked")
  public <P extends LasPoint> P addPoint(final double x, final double y, final double z) {
    final LasPoint lasPoint = newLasPoint(x, y, z);
    this.points.add(lasPoint);
    this.header.addCounts(lasPoint);
    return (P)lasPoint;
  }

  public void clear() {
    closeReader();
    this.header.clear();
    this.points = new ArrayList<>();
  }

  @Override
  public void close() {
    closeReader();
  }

  protected void closeReader() {
    final DataReader reader = this.reader;
    this.reader = null;
    if (reader != null) {
      reader.close();
    }
  }

  @Override
  public void forEachPoint(final Consumer<? super LasPoint> action) {
    final Iterable<LasPoint> iterable = iterable();
    try {
      iterable.forEach(action);
    } catch (RuntimeException | Error e) {
      if (iterable instanceof BaseCloseable) {
        ((BaseCloseable)iterable).close();
      }
      throw e;
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    if (this.header == null) {
      return BoundingBox.empty();
    } else {
      return this.header.getBoundingBox();
    }
  }

  public long[] getClassificationCounts() {
    return this.classificationCounts.clone();
  }

  public double getCurrentGpsTime() {
    if (isGpsTime()) {
      return System.currentTimeMillis() / 1000.0 - 315964800;
    } else {
      final Calendar calendar = new GregorianCalendar();
      final long time = calendar.getTimeInMillis();
      calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      final long startOfWeekTime = calendar.getTimeInMillis();
      final long gpsWeekTime = time - startOfWeekTime;
      return gpsWeekTime / 1000.0;
    }
  }

  @Override
  public Predicate<Point> getDefaultFilter() {
    return point -> LasClassification.GROUND == ((LasPoint)point).getClassification();
  }

  public double getFileGpsTime() {
    return this.fileGpsTime;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public LasPointCloudHeader getHeader() {
    return this.header;
  }

  public long getPointCount() {
    return this.header.getPointCount();
  }

  public LasPointFormat getPointFormat() {
    return this.header.getPointFormat();
  }

  public List<LasPoint> getPoints() {
    loadAllPoints();
    return this.points;
  }

  public Resource getResource() {
    return this.resource;
  }

  public boolean isExists() {
    return this.exists;
  }

  public boolean isGpsTime() {
    final int globalEncoding = this.header.getGlobalEncoding();
    return (globalEncoding & 1) != 0;
  }

  public Iterable<LasPoint> iterable() {
    if (this.allLoaded || !this.points.isEmpty()) {
      return this.points;
    } else {
      ChannelReader reader = this.reader;
      this.reader = null;
      if (reader == null) {
        reader = open();
      }
      if (reader == null) {
        return Collections.emptyList();
      } else {
        try {
          final LasZipHeader lasZipHeader = LasZipHeader.getLasZipHeader(this);
          if (lasZipHeader == null) {
            return new LasPointCloudIterator(this, reader);
          } else {
            final LasZipCompressorType compressor = lasZipHeader.getCompressor();
            return compressor.newIterator(this, reader);
          }
        } catch (RuntimeException | Error e) {
          reader.close();
          throw e;
        }
      }
    }
  }

  @Override
  public Iterator<LasPoint> iterator() {
    return iterable().iterator();
  }

  private synchronized void loadAllPoints() {
    if (!this.allLoaded && this.lasResource != null) {
      final List<LasPoint> points = new ArrayList<>((int)getPointCount());
      forEachPoint(points::add);
      this.points = points;
      this.allLoaded = true;
    }
  }

  public LasPoint newLasPoint(final double x, final double y, final double z) {
    return this.header.newLasPoint(this, x, y, z);
  }

  @Override
  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final QuadEdgeDelaunayTinBuilder tinBuilder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    forEachPoint((lasPoint) -> {
      tinBuilder.insertVertex(lasPoint);
    });
    final TriangulatedIrregularNetwork tin = tinBuilder.newTriangulatedIrregularNetwork();
    return tin;
  }

  public LasPointCloudWriter newWriter(final Object target, final MapEx properties) {
    final Resource resource = Resource.getResource(target);
    return newWriter(resource, properties);
  }

  public LasPointCloudWriter newWriter(final Resource resource, final MapEx properties) {
    final String fileNameExtension = resource.getFileNameExtension();
    if ("las".equals(fileNameExtension)) {
      final LasPointCloudWriter writer = new LasPointCloudWriter(this, resource, properties);
      writer.open();
      return writer;
    } else if ("laz".equals(fileNameExtension)) {
      return new LasZipPointCloudWriterFactory(this, resource, properties).newWriter();
    } else {
      throw new IllegalArgumentException(
        "Cannot write a LAS point cloud to file extension: " + resource);
    }
  }

  private ChannelReader open() {
    final ChannelReader reader;
    if (this.lasResource == null) {
      reader = null;
    } else {
      reader = this.lasResource.newChannelReader(this.byteBuffer);
    }
    if (reader == null) {
      this.exists = false;
    } else {
      reader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
      this.exists = true;
      final LasPointCloudHeader header = new LasPointCloudHeader(this, reader,
        this.geometryFactory);
      setHeader(header);
      this.geometryFactory = this.header.getGeometryFactory();
      if (this.header.getPointCount() == 0) {
        reader.close();
        return null;
      }
    }
    return reader;
  }

  @Override
  public void refreshClassificationCounts() {
    Arrays.fill(this.classificationCounts, 0);
    forEachPoint(point -> {
      final short classification = point.getClassification();
      this.classificationCounts[classification]++;
    });
    this.classificationsLoaded = true;
  }

  public void setByteBuffer(final ByteBuffer byteBuffer) {
    this.byteBuffer = byteBuffer;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setHeader(final LasPointCloudHeader header) {
    this.header = header;
    this.fileGpsTime = getCurrentGpsTime();
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
  public String toHtml() {
    try (
      final StringWriter out = new StringWriter();
      HtmlWriter writer = new HtmlWriter(out, false)) {
      writer.element(HtmlElem.HTML, this::writeHtml);
      return out.toString();
    }
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
    addToMap(map, "url", this.resource.getUri());
    addToMap(map, "header", this.header);
    return map;
  }

  public void writeHtml(final HtmlWriter writer) {
    writer.divClass("las");
    writer.attribute(HtmlAttr.CLASS, "las");
    this.header.writeHtml(writer);
    if (this.classificationsLoaded) {
      writer.h2("Classifications");
      writer.table();
      final DecimalFormat numberFormat = new DecimalFormat("#,###");
      for (short classification = 0; classification < this.classificationCounts.length; classification++) {
        final long count = this.classificationCounts[classification];
        if (count > 0) {
          final String lasClassification = LasClassification.CLASSIFICATIONS
            .getOrDefault(classification, "Custom");
          final String label = classification + " - " + lasClassification;
          writer.tableRowLabelValue(label, numberFormat.format(count));
        }
      }
      writer.endTag();
    }
    writer.endTag();
  }

}
