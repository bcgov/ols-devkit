package com.revolsys.raster.io.format.tiff;

import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.channels.ChannelWriter;
import com.revolsys.raster.io.format.tiff.code.TiffBaselineTag;
import com.revolsys.raster.io.format.tiff.code.TiffCompression;
import com.revolsys.spring.resource.Resource;

public class TiffFileBuilder implements BaseCloseable {
  private boolean bigTiff = false;

  private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

  private final List<TiffDirectoryBuilder> directories = new ArrayList<>();

  private final Resource resource;

  private long size = 0;

  private final ChannelWriter out;

  public TiffFileBuilder(final Path file) {
    this.resource = Resource.getResource(file);
    this.out = this.resource.newChannelWriter();
  }

  void addDirectory(final TiffDirectoryBuilder directory) {
    if (!this.directories.isEmpty()) {
      final TiffDirectoryBuilder lastDirectory = this.directories.get(this.directories.size() - 1);
      lastDirectory.setNextDirectory(directory);
    }
    this.directories.add(directory);
  }

  @Override
  public void close() {
    writeHeader(this.out);
    this.out.close();
  }

  public ByteOrder getByteOrder() {
    return this.byteOrder;
  }

  public List<TiffDirectoryBuilder> getDirectories() {
    return Collections.unmodifiableList(this.directories);
  }

  public int getDirectoryCount() {
    return this.directories.size();
  }

  public Resource getResource() {
    return this.resource;
  }

  public boolean isBigTiff() {
    return this.bigTiff;
  }

  public <B extends TiffDirectoryBuilder> B newImage(final Function<TiffFileBuilder, B> constructor,
    final int width, final int height) {
    final B directory = constructor.apply(this);
    directory //
      .setImageWidth(width) //
      .setImageHeight(height) //
      .setTileSize(512) //
      .setCompression(TiffCompression.NONE) //
      .setUnsignedShort(TiffBaselineTag.PlanarConfiguration, 1) //
      .setUnsignedShort(TiffBaselineTag.XResolution, 72) //
      .setUnsignedShort(TiffBaselineTag.YResolution, 72) //
      .setUnsignedShort(TiffBaselineTag.ResolutionUnit, 2) //
      .setUnsignedShort(TiffBaselineTag.Orientation, 1) //
    ;
    return directory;
  }

  public <B extends TiffDirectoryBuilder> List<B> newImageWithOverviews(
    final Function<TiffFileBuilder, B> constructor, int width, int height) {
    final List<B> directories = new ArrayList<>();
    boolean running = true;
    while (running) {
      final B directory = newImage(constructor, width, height);
      if (!directories.isEmpty()) {
        directory.setUnsignedInt(TiffBaselineTag.NewSubfileType, 1);
      }
      directories.add(directory);
      if (width <= 512 && height <= 512) {
        running = false;
      } else {
        width = (int)Math.ceil(width / 2.0);
        height = (int)Math.ceil(height / 2.0);
      }
    }
    return directories;
  }

  long reserveSpace(final int size) {
    final long offset = this.size;
    this.size += size;
    return offset;
  }

  public void setBigTiff(final boolean bigTiff) {
    this.bigTiff = bigTiff;
  }

  public void setByteOrder(final ByteOrder byteOrder) {
    this.byteOrder = byteOrder;
  }

  private void writeDirectories(final ChannelWriter out) {
    for (final TiffDirectoryBuilder directory : this.directories) {
      long offset = directory.getOffset();
      if (offset == -1 && directory.getIndex() == 0) {
        reserveSpace(4);
      }
      if (offset == -1 || directory.isSizeIncreased()) {
        offset = reserveSpace(directory.getSize());
      }
      writeOffsetOrCount(out, offset);
      out.seek(offset);
      directory.writeDirectory(out);
    }
    writeOffsetOrCount(out, 0);
  }

  private void writeDirectoryEntries(final ChannelWriter out) {
    for (final TiffDirectoryBuilder directory : this.directories) {
      directory.writeEntries(out);
    }
  }

  private void writeHeader(final ChannelWriter out) {
    out.seek(0);
    if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
      out.putByte((byte)'I'); // Intel
      out.putByte((byte)'I');
    } else { // BIG_ENDIAN
      out.putByte((byte)'M'); // Motorola
      out.putByte((byte)'M');
    }
    if (isBigTiff()) {
      out.putShort((short)43); // magic
      out.putShort((short)8); // offset byte size
      out.putShort((short)0); // reserved
    } else {
      out.putShort((short)42); // magic
    }
    if (this.size == 0) {
      this.size = out.position();
    }

    writeDirectories(out);
    writeDirectoryEntries(out);
  }

  public void writeOffsetOrCount(final ChannelWriter out, final long value) {
    if (isBigTiff()) {
      out.putUnsignedLong(value);
    } else {
      out.putUnsignedInt(value);
    }
  }
}
