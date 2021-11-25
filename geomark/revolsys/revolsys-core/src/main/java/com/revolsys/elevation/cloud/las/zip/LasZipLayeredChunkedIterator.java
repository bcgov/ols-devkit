package com.revolsys.elevation.cloud.las.zip;

import java.util.Iterator;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudIterator;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.io.channels.DataReader;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;

public class LasZipLayeredChunkedIterator extends LasPointCloudIterator {

  private final ArithmeticDecoder decoder;

  private final LasZipItemCodec[] codecs;

  private long chunk_size = Integer.MAX_VALUE;

  private int contextIndex = 0;

  private int number_chunks = Integer.MAX_VALUE;

  private long point_start;

  private long chunk_count;

  private int tabled_chunks;

  private int current_chunk;

  private long[] chunk_starts;

  private int[] chunk_totals;

  public LasZipLayeredChunkedIterator(final LasPointCloud pointCloud, final DataReader reader) {
    super(pointCloud, reader);
    this.decoder = new ArithmeticDecoder();
    final LasZipHeader lasZipHeader = LasZipHeader.getLasZipHeader(pointCloud);
    this.codecs = lasZipHeader.newLazCodecs(this.decoder);

    this.chunk_size = lasZipHeader.getChunkSize();
    this.chunk_count = this.chunk_size;
  }

  @Override
  public boolean hasNext() {
    return this.index < this.pointCount;
  }

  private void init_dec() {
    // maybe read chunk table (only if chunking enabled)

    if (this.number_chunks == Integer.MAX_VALUE) {
      if (!read_chunk_table()) {
        return;
      }
      this.current_chunk = 0;
      if (this.chunk_totals != null) {
        this.chunk_size = this.chunk_totals[1];
      }
    }

    this.point_start = this.reader.position();
  }

  @Override
  public Iterator<LasPoint> iterator() {
    return this;
  }

  private boolean read_chunk_table() {
    // read the 8 bytes that store the location of the chunk table
    long chunk_table_start_position;
    try {
      chunk_table_start_position = this.reader.getLong();
    } catch (final Throwable e) {
      return false;
    }

    // this is where the chunks start
    final long chunks_start = this.reader.position();

    // was compressor interrupted before getting a chance to write the chunk
    // table?
    if (chunk_table_start_position + 8 == chunks_start) {
      // no choice but to fail if adaptive chunking was used
      if (this.chunk_size == Integer.MAX_VALUE) {
        return false;
      }
      // otherwise we build the chunk table as we read the file
      this.number_chunks = 256;
      this.chunk_starts = new long[this.number_chunks + 1];
      this.chunk_starts[0] = chunks_start;
      this.tabled_chunks = 1;
      return true;
    }

    // maybe the stream is not seekable
    if (!this.reader.isSeekable()) {
      // no choice but to fail if adaptive chunking was used
      if (this.chunk_size == Integer.MAX_VALUE) {
        return false;
      }
      // then we cannot seek to the chunk table but won't need it anyways
      this.number_chunks = 0;
      this.tabled_chunks = 0;
      return true;
    }

    if (chunk_table_start_position == -1) {
      // the compressor was writing to a non-seekable stream and wrote the chunk
      // table start at the end
      this.reader.seekEnd(8);
      try {
        chunk_table_start_position = this.reader.getLong();
      } catch (final Throwable e) {
        return false;
      }
    }

    // read the chunk table
    try {
      this.reader.seek(chunk_table_start_position);
      final int version = this.reader.getInt();
      if (version != 0) {
        throw new RuntimeException("Unsupported chunk version: " + version);
      }
      this.number_chunks = this.reader.getInt();
      this.chunk_totals = null;
      this.chunk_starts = null;
      if (this.chunk_size == Integer.MAX_VALUE) {
        this.chunk_totals = new int[this.number_chunks + 1];
      }
      this.chunk_starts = new long[this.number_chunks + 1];
      this.chunk_starts[0] = chunks_start;
      this.tabled_chunks = 1;
      if (this.number_chunks > 0) {
        this.decoder.init(this.reader);
        final ArithmeticCodingInteger ic = new ArithmeticCodingInteger(this.decoder, 32, 2);
        ic.initDecompressor();
        for (int i = 1; i <= this.number_chunks; i++) {
          if (this.chunk_size == Integer.MAX_VALUE) {
            this.chunk_totals[i] = ic.decompress(i > 1 ? this.chunk_totals[i - 1] : 0, 0);
          }
          this.chunk_starts[i] = ic.decompress(i > 1 ? (int)this.chunk_starts[i - 1] : 0, 1);
          this.tabled_chunks++;
        }
        for (int i = 1; i <= this.number_chunks; i++) {
          if (this.chunk_size == Integer.MAX_VALUE) {
            this.chunk_totals[i] += this.chunk_totals[i - 1];
          }
          this.chunk_starts[i] += this.chunk_starts[i - 1];
          if (this.chunk_starts[i] <= this.chunk_starts[i - 1]) {
            throw new RuntimeException("Chunk corrupt");
          }
        }
      }
    } catch (final Throwable e) {
      // something went wrong while reading the chunk table
      this.chunk_totals = null;
      // no choice but to fail if adaptive chunking was used
      if (this.chunk_size == Integer.MAX_VALUE) {
        return false;
      }
      // did we not even read the number of chunks
      if (this.number_chunks == Integer.MAX_VALUE) {
        // then compressor was interrupted before getting a chance to write the
        // chunk table
        this.number_chunks = 256;
        this.chunk_starts = new long[this.number_chunks + 1];
        this.chunk_starts[0] = chunks_start;
        this.tabled_chunks = 1;
      } else {
        // otherwise fix as many additional chunk_starts as possible
        for (int i = 1; i < this.tabled_chunks; i++) {
          this.chunk_starts[i] += this.chunk_starts[i - 1];
        }
      }
    }
    this.reader.seek(chunks_start);
    return true;
  }

  @Override
  protected LasPoint readNext() {
    try {
      final LasPoint point;

      if (this.chunk_count == this.chunk_size) {
        if (this.point_start != 0) {
          this.current_chunk++;
          // check integrity
          if (this.current_chunk < this.tabled_chunks) {
            final long here = this.reader.position();
            if (this.chunk_starts[this.current_chunk] != here) {
              // previous chunk was corrupt
              this.current_chunk--;
              throw new RuntimeException("LAS file is corrupt");
            }
          }
        }
        init_dec();
        if (this.current_chunk == this.tabled_chunks) // no or incomplete chunk
                                                      // table?
        {
          if (this.current_chunk == this.number_chunks) {
            this.number_chunks += 256;
            final long[] chunkStarts = new long[this.number_chunks + 1];
            System.arraycopy(this.chunk_starts, 0, chunkStarts, 0, this.chunk_starts.length);
            this.chunk_starts = chunkStarts;
          }
          this.chunk_starts[this.tabled_chunks] = this.point_start; // needs
                                                                    // fixing
          this.tabled_chunks++;
        } else if (this.chunk_totals != null) // variable sized chunks?
        {
          this.chunk_size = this.chunk_totals[this.current_chunk + 1]
            - this.chunk_totals[this.current_chunk];
        }
        this.chunk_count = 0;
      }
      this.chunk_count++;

      if (this.chunk_count == 1) {
        point = this.pointFormat.readLasPoint(this.pointCloud, this.reader);

        this.decoder.init(this.reader, false);
        final int count = this.reader.getInt(); // count unused
        for (final LasZipItemCodec codec : this.codecs) {
          codec.readChunkSizes();
        }
        for (final LasZipItemCodec codec : this.codecs) {
          this.contextIndex = codec.init(point, this.contextIndex);
        }
      } else {
        point = this.pointFormat.newLasPoint(this.pointCloud);
        for (final LasZipItemCodec pointDecompressor : this.codecs) {
          this.contextIndex = pointDecompressor.read(point, this.contextIndex);
        }
      }
      return point;
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Exception e) {
      close();
      throw Exceptions.wrap("Error decompressing: " + this.pointCloud.getResource(), e);
    }
  }

}
