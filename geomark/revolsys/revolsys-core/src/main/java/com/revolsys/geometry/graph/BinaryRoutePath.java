package com.revolsys.geometry.graph;

import java.util.Comparator;

public class BinaryRoutePath {

  public static final Comparator<BinaryRoutePath> COMPARATOR = (a, b) -> {
    int compare = Integer.compare(a.x, b.x);
    if (compare == 0) {
      compare = Integer.compare(a.y, b.y);
      if (compare == 0) {
        final int minEdgeCount = Math.min(a.edgeCount, b.edgeCount);
        for (int i = 0; i < minEdgeCount; i++) {
          final int f1 = a.edgeFlags[i] * 0xff;
          final int f2 = b.edgeFlags[i] * 0xff;
          compare = Integer.compare(f1, f2);
          if (compare != 0) {
            return compare;
          }
        }
        compare = Integer.compare(a.edgeCount, b.edgeCount);
      }
    }
    return compare;
  };

  private final int x;

  private final int y;

  private final short edgeCount;

  private byte[] edgeFlags;

  public BinaryRoutePath(final byte[] bytes) {
    this.x = (bytes[0] << 24) + ((bytes[1] & 0xff) << 16) + ((bytes[2] & 0xff) << 8)
      + ((bytes[3] & 0xff) << 0);
    this.y = (bytes[4] << 24) + ((bytes[5] & 0xff) << 16) + ((bytes[6] & 0xff) << 8)
      + ((bytes[7] & 0xff) << 0);
    this.edgeCount = (short)((bytes[8] << 8) + ((bytes[9] & 0xff) << 0));
    this.edgeFlags = new byte[bytes.length - 10];
    System.arraycopy(bytes, 10, this.edgeFlags, 0, this.edgeFlags.length);
  }

  public BinaryRoutePath(final int x, final int y) {
    this(x, y, (short)0);
  }

  public BinaryRoutePath(final int x, final int y, final boolean setFlag) {
    this.x = x;
    this.y = y;
    this.edgeCount = 1;
    if (setFlag) {
      this.edgeFlags = new byte[] {
        (byte)(1 << 7)
      };
    } else {
      this.edgeFlags = new byte[] {
        0
      };
    }
  }

  private BinaryRoutePath(final int x, final int y, final int edgeCount) {
    this(x, y, (short)edgeCount, new byte[(int)Math.ceil(edgeCount / 8.0)]);
  }

  private BinaryRoutePath(final int x, final int y, final short edgeCount, final byte[] edgeFlags) {
    this.x = x;
    this.y = y;
    this.edgeCount = edgeCount;
    this.edgeFlags = edgeFlags;
  }

  public BinaryRoutePath appendEdge(final boolean setFlag) {
    final int newEdgeIndex = this.edgeCount;
    final BinaryRoutePath newPath = new BinaryRoutePath(this.x, this.y, this.edgeCount + 1);
    System.arraycopy(this.edgeFlags, 0, newPath.edgeFlags, 0, this.edgeFlags.length);
    if (setFlag) {
      final int byteIndex = newEdgeIndex / 8;
      final int bitIndex = newEdgeIndex % 8;
      final int flagIndex = 7 - bitIndex;
      newPath.edgeFlags[byteIndex] |= 1 << flagIndex;
    }
    return newPath;
  }

  public boolean equalsBytes(final byte[] bytes) {

    final int x = (bytes[0] << 24) + ((bytes[1] & 0xff) << 16) + ((bytes[2] & 0xff) << 8)
      + ((bytes[3] & 0xff) << 0);
    final int y = (bytes[4] << 24) + ((bytes[5] & 0xff) << 16) + ((bytes[6] & 0xff) << 8)
      + ((bytes[7] & 0xff) << 0);
    final short edgeCount = (short)((bytes[8] << 8) + ((bytes[9] & 0xff) << 0));

    if (x != this.x) {
      return false;
    } else if (y != this.y) {
      return false;
    } else if (edgeCount != this.edgeCount) {
      return false;
    } else {
      for (int i = 0; i < this.edgeFlags.length; i++) {
        final byte newValue = this.edgeFlags[i];
        final byte oldValue = bytes[10 + i];
        if (newValue != oldValue) {
          return false;
        }
      }
    }
    return true;
  }

  public int getEdgeCount() {
    return this.edgeCount;
  }

  public int getId() {
    return this.x;
  }

  public boolean startsWith(final BinaryRoutePath route2) {
    return false;
  }

  public byte[] toBytes() {
    final byte[] bytes = new byte[10 + this.edgeFlags.length];
    bytes[0] = (byte)(this.x >> 24);
    bytes[1] = (byte)(this.x >> 16);
    bytes[2] = (byte)(this.x >> 8);
    bytes[3] = (byte)this.x;
    bytes[4] = (byte)(this.y >> 24);
    bytes[5] = (byte)(this.y >> 16);
    bytes[6] = (byte)(this.y >> 8);
    bytes[7] = (byte)this.y;
    bytes[8] = (byte)(this.edgeCount >> 8);
    bytes[9] = (byte)this.edgeCount;
    System.arraycopy(this.edgeFlags, 0, bytes, 10, this.edgeFlags.length);
    return bytes;
  }

  @Override
  public String toString() {
    final StringBuilder text = new StringBuilder();
    text.append(this.x);
    text.append('_');
    text.append(this.y);
    int bitIndex = 0;
    for (final byte flags : this.edgeFlags) {
      text.append('-');
      for (int i = 7; i >= 0; i--) {
        if (bitIndex < this.edgeCount) {
          final int flag = flags & 1 << i;
          if (flag == 0) {
            text.append('0');
          } else {
            text.append('1');
          }
          bitIndex++;
        }
      }

    }
    return text.toString();
  }
}
