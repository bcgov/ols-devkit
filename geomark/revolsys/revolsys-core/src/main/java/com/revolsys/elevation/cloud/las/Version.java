package com.revolsys.elevation.cloud.las;

import java.io.IOException;

import com.revolsys.io.channels.DataReader;

public class Version implements Comparable<Version> {
  private final short major;

  private final short minor;

  private final int revision;

  public Version(final DataReader reader) throws IOException {
    this.major = reader.getUnsignedByte();
    this.minor = reader.getUnsignedByte();
    this.revision = 0;
  }

  public Version(final int major, final int minor) {
    this.major = (short)major;
    this.minor = (short)minor;
    this.revision = 0;
  }

  public Version(final int major, final int minor, final int revision) {
    this.major = (short)major;
    this.minor = (short)minor;
    this.revision = revision;
  }

  public boolean atLeast(final Version version) {
    return compareTo(version) >= 0;
  }

  @Override
  public int compareTo(final Version version) {
    int compare = Integer.compare(this.major, version.major);
    if (compare == 0) {
      compare = Integer.compare(this.minor, version.minor);
    }
    return compare;
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof Version) {
      final Version version = (Version)other;
      if (this.major == version.major) {
        if (this.minor == version.minor) {
          return true;
        }
      }
    }
    return false;
  }

  public short getMajor() {
    return this.major;
  }

  public short getMinor() {
    return this.minor;
  }

  public int getRevision() {
    return this.revision;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.major;
    result = prime * result + this.minor;
    return result;
  }

  @Override
  public String toString() {
    return this.major + "." + this.minor;
  }
}
