package com.revolsys.io.channels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.EndOfFileException;
import com.revolsys.io.SeekableByteChannelInputStream;

public class ChannelReader extends AbstractDataReader implements BaseCloseable {

  private final ReadableByteChannel channel;

  public ChannelReader() {
    this((ReadableByteChannel)null);
  }

  public ChannelReader(final InputStream in) {
    this(Channels.newChannel(in));
  }

  public ChannelReader(final ReadableByteChannel channel) {
    this(channel, 8192);
  }

  public ChannelReader(final ReadableByteChannel channel, final ByteBuffer buffer) {
    super(buffer, channel instanceof SeekableByteChannel);
    this.channel = channel;
  }

  public ChannelReader(final ReadableByteChannel channel, final int capacity) {
    this(channel, ByteBuffer.allocateDirect(capacity));
  }

  public ChannelReader(final ReadableByteChannel channel, final int capacity,
    final ByteOrder byteOrder) {
    this(channel, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  @Override
  public void close() {
    if (!isClosed()) {
      try {
        this.channel.close();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      } finally {
        super.close();
      }
    }
  }

  @Override
  public byte[] getBytes(final long offset, final int byteCount) {
    if (isSeekable()) {
      final byte[] bytes = new byte[byteCount];
      try {
        final SeekableByteChannel seekChannel = (SeekableByteChannel)getChannel();
        seekChannel.position(offset);
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final int count = seekChannel.read(buffer);
        if (count == -1) {
          throw new EndOfFileException();
        } else if (count == byteCount) {
          return bytes;
        } else {
          final byte[] subBytes = new byte[count];
          System.arraycopy(bytes, 0, subBytes, 0, count);
          return subBytes;
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      return super.getBytes(offset, byteCount);
    }
  }

  public ReadableByteChannel getChannel() {
    return this.channel;
  }

  @Override
  public InputStream getInputStream(final long offset, final int size) {
    if (isSeekable()) {
      final SeekableByteChannel seekableChannel = (SeekableByteChannel)getChannel();
      return new SeekableByteChannelInputStream(seekableChannel, offset, size);
    } else {
      return super.getInputStream(offset, size);
    }
  }

  @Override
  public long position() {
    if (isSeekable()) {
      final SeekableByteChannel channel = (SeekableByteChannel)getChannel();
      try {
        return channel.position() - getAvailable();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      return super.position();
    }
  }

  public ByteBuffer readByteByteBuffer(final long offset, final int size) {
    final long position = position();
    final ByteBuffer buffer = ByteBuffer.allocateDirect(size);
    seek(offset);
    do {
      try {
        getChannel().read(buffer);
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    } while (buffer.hasRemaining());
    seek(position);
    buffer.flip();
    return buffer;
  }

  @Override
  public int readInternal(final ByteBuffer buffer) throws IOException {
    return getChannel().read(buffer);
  }

  @Override
  public void seek(final long position) {
    try {
      if (isSeekable()) {
        final long currentPosition = position();
        if (position != currentPosition) {
          final SeekableByteChannel channel = (SeekableByteChannel)getChannel();
          channel.position(position);
          afterSeek();
        }
      } else {
        super.seek(position);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public void seekEnd(final long distance) {
    try {
      if (isSeekable()) {
        final SeekableByteChannel channel = (SeekableByteChannel)getChannel();
        final long position = channel.size() - distance;
        seek(position);
      } else {
        throw new IllegalArgumentException("Seek not supported");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
