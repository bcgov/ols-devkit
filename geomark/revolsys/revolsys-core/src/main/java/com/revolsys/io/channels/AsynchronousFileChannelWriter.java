package com.revolsys.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousFileChannel;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.EndOfFileException;

public class AsynchronousFileChannelWriter extends AbstractChannelWriter implements BaseCloseable {

  AsynchronousFileChannel channel;

  private long filePosition;

  /**
   * <p>Create a new ChannelWriter with buffer of 8192 bytes.<p>
   *
   * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
   *
   * @param channel The channel.
   */
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel) {
    this(channel, 8192);
  }

  /**
   * <p>Create a new ChannelWriter with buffer of 8192 bytes.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   */
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel,
    final boolean closeChannel) {
    this(channel, closeChannel, 8192);
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   * @param buffer The temporary buffer used to write to the channel.
   */
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel,
    final boolean closeChannel, final ByteBuffer buffer) {
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
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel,
    final boolean closeChannel, final int capacity) {
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
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel,
    final boolean closeChannel, final int capacity, final ByteOrder byteOrder) {
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
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel,
    final ByteBuffer buffer) {
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
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel, final int capacity) {
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
  public AsynchronousFileChannelWriter(final AsynchronousFileChannel channel, final int capacity,
    final ByteOrder byteOrder) {
    this(channel, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  @Override
  protected void closeDo() {
    final AsynchronousFileChannel channel = this.channel;
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

  @Override
  protected void write() {
    try {
      final ByteBuffer buffer = this.buffer;
      final AsynchronousFileChannel channel = this.channel;
      if (channel != null) {
        buffer.flip();
        final int size = buffer.remaining();
        int totalWritten = 0;
        while (totalWritten < size) {
          final int written = channel.write(buffer, this.filePosition).get();
          if (written == -1) {
            throw new EndOfFileException();
          } else {
            this.filePosition += written;
          }
          totalWritten += written;
        }
        buffer.clear();
        this.available = this.capacity;
      }
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }
}
