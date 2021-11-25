package com.revolsys.raster.io.format.tiff;

import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.DataReader;
import com.revolsys.spring.resource.Resource;

public class TiffDirectoryIterator
  implements BaseCloseable, Iterator<TiffDirectory>, Iterable<TiffDirectory> {

  public static TiffDirectoryIterator newIterator(final Object source) {
    final Resource resource = Resource.getResource(source);
    return new TiffDirectoryIterator(resource);
  }

  private int index;

  private final DataReader in;

  private final Queue<TiffDirectoryEntry> entries = new LinkedList<>();

  private long directoryOffset;

  private final Resource resource;

  private boolean bigTiff;

  private TiffDirectory lastDirectory;

  public TiffDirectoryIterator(final Resource resource) {
    this.resource = resource;
    final DataReader in = resource.newChannelReader();
    this.in = in;
    final byte b1 = in.getByte();
    final byte b2 = in.getByte();
    if (b1 == 'I' && b2 == 'I') {
      in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    } else if (b1 == 'M' && b2 == 'M') {
      in.setByteOrder(ByteOrder.BIG_ENDIAN);
    } else {
      throw new IllegalStateException("Not a valid TIFF file");
    }
    final short magic = in.getShort();
    if (magic == 42) {
      this.bigTiff = false;
    } else if (magic == 43) {
      this.bigTiff = true;
    } else {
      throw new IllegalStateException("Not a valid TIFF file");
    }
    if (this.bigTiff) {
      if (in.getShort() != 8) {
        throw new IllegalStateException("Not a valid TIFF file");
      }
      if (in.getShort() != 0) {
        throw new IllegalStateException("Not a valid TIFF file");
      }
      this.directoryOffset = in.getLong();
    } else {
      this.directoryOffset = in.getUnsignedInt();
    }
  }

  @Override
  public void close() {
    this.entries.clear();
    this.directoryOffset = 0;
    this.in.close();
  }

  @Override
  public boolean hasNext() {
    return this.directoryOffset != 0;
  }

  @Override
  public Iterator<TiffDirectory> iterator() {
    return this;
  }

  @Override
  public TiffDirectory next() {
    if (this.directoryOffset == 0) {
      this.lastDirectory = null;
      throw new NoSuchElementException();
    } else {
      final TiffDirectory directory = new TiffDirectory(this.resource, this.in, this.index++,
        this.directoryOffset, this.bigTiff);
      this.directoryOffset = directory.getNextOffset();
      if (this.lastDirectory != null) {
        this.lastDirectory.setNextDirectory(directory);
      }
      this.lastDirectory = directory;
      return directory;
    }
  }

  @Override
  public String toString() {
    return this.resource.toString();
  }
}
