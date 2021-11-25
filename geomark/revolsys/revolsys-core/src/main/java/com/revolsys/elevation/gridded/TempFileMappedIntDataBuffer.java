package com.revolsys.elevation.gridded;

import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;

public class TempFileMappedIntDataBuffer extends DataBuffer {

  private MappedByteBuffer byteBuffer;

  private IntBuffer intBuffer;

  private final File file;

  private FileChannel channel;

  public TempFileMappedIntDataBuffer(final int size) {
    super(TYPE_INT, size);
    this.file = FileUtil.newTempFile("image", ".raw");
    try {
      this.channel = FileChannel.open(this.file.toPath(), Paths.OPEN_OPTIONS_READ_WRITE_SET,
        Paths.FILE_ATTRIBUTES_NONE);
      this.byteBuffer = this.channel.map(MapMode.READ_WRITE, 0, size * 4);
      this.intBuffer = this.byteBuffer.asIntBuffer();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public TempFileMappedIntDataBuffer(final int width, final int height) {
    this(width * height);
  }

  @Override
  protected void finalize() throws Throwable {
    this.channel.truncate(0);
    this.file.delete();
    super.finalize();
  }

  @Override
  public int getElem(final int bank, final int i) {
    if (bank == 0) {
      return this.intBuffer.get(i);
    } else {
      return 0;
    }
  }

  @Override
  public void setElem(final int bank, final int i, final int value) {
    if (bank == 0) {
      this.intBuffer.put(i, value);
    }
  }
}
