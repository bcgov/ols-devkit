package com.revolsys.elevation.cloud.las;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.elevation.cloud.las.pointformat.LasPointFormat;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.DataReader;

public class LasPointCloudIterator
  implements BaseCloseable, Iterator<LasPoint>, Iterable<LasPoint> {

  protected long index = 0;

  protected long pointCount = 0;

  protected final LasPointFormat pointFormat;

  protected DataReader reader;

  protected final LasPointCloud pointCloud;

  public LasPointCloudIterator(final LasPointCloud pointCloud, final DataReader reader) {
    this.pointCloud = pointCloud;
    this.reader = reader;
    this.pointCount = pointCloud.getPointCount();
    this.pointFormat = pointCloud.getPointFormat();
  }

  @Override
  public void close() {
    this.index = this.pointCount;
    if (this.reader != null) {
      this.reader.close();
    }
    this.reader = null;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  @Override
  public boolean hasNext() {
    return this.index < this.pointCount;
  }

  @Override
  public Iterator<LasPoint> iterator() {
    return this;
  }

  @Override
  public LasPoint next() {
    if (this.index < this.pointCount) {
      final LasPoint point = readNext();
      this.index++;
      return point;
    } else {
      close();
      throw new NoSuchElementException();
    }
  }

  protected LasPoint readNext() {
    return this.pointFormat.readLasPoint(this.pointCloud, this.reader);
  }
}
