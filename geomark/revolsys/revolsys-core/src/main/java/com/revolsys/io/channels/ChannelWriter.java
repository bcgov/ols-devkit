package com.revolsys.io.channels;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.EndOfFileException;
import com.revolsys.spring.resource.Resource;

public class ChannelWriter extends AbstractChannelWriter implements BaseCloseable {
  public static ChannelWriter newChannelWriter(final Object source) {
    return Resource.getResource(source).newChannelWriter();
  }

  /**
   * Create a new {@link ChannelWriter} for the {@link Resource} using the {@link ByteBuffer}.
   * If the resource ends with .zip or .gz it will be wrapped in a zip on gz compressed file.
   *
   * @param resource The resource to write to.
   * @param byteBuffer The byte buffer.
   * @return The channel writer.
   */
  public static ChannelWriter newChannelWriterCompressed(final Resource resource,
    final ByteBuffer byteBuffer) {
    final String fileNameExtension = resource.getFileNameExtension();
    if ("zip".equals(fileNameExtension)) {
      try {
        final OutputStream bufferedOut = resource.newBufferedOutputStream();
        final String fileName = resource.getBaseName();
        final ZipOutputStream zipOut = new ZipOutputStream(bufferedOut);
        final ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        final WritableByteChannel channel = Channels.newChannel(zipOut);
        return new ChannelWriter(channel, true, byteBuffer);
      } catch (final IOException e) {
        throw Exceptions.wrap("Error creating: " + resource, e);
      }
    } else if ("gz".equals(fileNameExtension)) {
      try {
        final OutputStream bufferedOut = resource.newBufferedOutputStream();
        final GZIPOutputStream zipOut = new GZIPOutputStream(bufferedOut);
        final WritableByteChannel channel = Channels.newChannel(zipOut);
        return new ChannelWriter(channel, true, byteBuffer);
      } catch (final IOException e) {
        throw Exceptions.wrap("Error creating: " + resource, e);
      }
    } else {
      return resource.newChannelWriter(byteBuffer);
    }
  }

  WritableByteChannel channel;

  public ChannelWriter(final OutputStream out) {
    this(Channels.newChannel(out));
  }

  /**
   * <p>Create a new ChannelWriter with buffer of 8192 bytes.<p>
   *
   * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
   *
   * @param channel The channel.
   */
  public ChannelWriter(final WritableByteChannel channel) {
    this(channel, 8192);
  }

  /**
   * <p>Create a new ChannelWriter with buffer of 8192 bytes.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel) {
    this(channel, closeChannel, 8192);
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   * @param buffer The temporary buffer used to write to the channel.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel,
    final ByteBuffer buffer) {
    super(closeChannel, buffer);
    this.channel = channel;
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   * @param capacity The size of the temporary buffer.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel,
    final int capacity) {
    this(channel, closeChannel, ByteBuffer.allocateDirect(capacity));
  }

  /**
   * <p>Create a new ChannelWriter.<p>
    *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   * @param capacity The size of the temporary buffer.
   * @param byteOrder The byte order of the buffer.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel,
    final int capacity, final ByteOrder byteOrder) {
    this(channel, closeChannel, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
   *
   * @param channel The channel.
   * @param buffer The temporary buffer used to write to the channel.
   */
  public ChannelWriter(final WritableByteChannel channel, final ByteBuffer buffer) {
    this(channel, false, buffer);
  }

  /**
  * <p>Create a new ChannelWriter.<p>
  *
  * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
  *
  * @param channel The channel.
  * @param capacity The size of the temporary buffer.
  */
  public ChannelWriter(final WritableByteChannel channel, final int capacity) {
    this(channel, ByteBuffer.allocateDirect(capacity));
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
   *
   * @param channel The channel.
   * @param capacity The size of the temporary buffer.
   * @param byteOrder The byte order of the buffer.
   */
  public ChannelWriter(final WritableByteChannel channel, final int capacity,
    final ByteOrder byteOrder) {
    this(channel, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  @Override
  protected void closeDo() {
    final WritableByteChannel channel = this.channel;
    if (channel != null) {
      this.channel = null;
      if (this.closeChannel) {
        try {
          channel.close();
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    }
  }

  public boolean isSeekable() {
    return this.channel instanceof SeekableByteChannel;
  }

  public long position() {
    if (this.channel instanceof SeekableByteChannel) {
      final SeekableByteChannel channel = (SeekableByteChannel)this.channel;
      try {
        return channel.position() + this.buffer.position();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
    return -1;
  }

  public void seek(final long position) {
    flush();
    try {
      if (this.channel instanceof SeekableByteChannel) {
        final SeekableByteChannel channel = (SeekableByteChannel)this.channel;
        channel.position(position);
        this.available = 0;
        this.buffer.clear();
      } else {
        throw new IllegalArgumentException("Not supported");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public void seekEnd(final long distance) {
    try {
      if (this.channel instanceof SeekableByteChannel) {
        final SeekableByteChannel channel = (SeekableByteChannel)this.channel;
        final long position = channel.size() - distance;
        seek(position);
      } else {
        throw new IllegalArgumentException("Not supported");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  protected void write() {
    try {
      final ByteBuffer buffer = this.buffer;
      final WritableByteChannel channel = this.channel;
      if (channel != null) {
        buffer.flip();
        final int size = buffer.remaining();
        int totalWritten = 0;
        while (totalWritten < size) {
          final int written = channel.write(buffer);
          if (written == -1) {
            throw new EndOfFileException();
          }
          totalWritten += written;
        }
        buffer.clear();
        this.available = this.capacity;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
