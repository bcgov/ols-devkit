package com.revolsys.elevation.cloud.las;

import java.util.Arrays;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.util.Pair;

public class LasVariableLengthRecord implements Cloneable {
  private byte[] bytes;

  private final String description;

  private Object value;

  private boolean valueInitialized = false;

  private LasPointCloudHeader header;

  private final Pair<String, Integer> key;

  private boolean extended;

  public LasVariableLengthRecord(final LasPointCloud pointCloud, final boolean extended,
    final Pair<String, Integer> key, final String description, final Object value) {
    this(pointCloud, key, description, value);
    this.extended = true;
  }

  public LasVariableLengthRecord(final LasPointCloud pointCloud, final Pair<String, Integer> key,
    final String description, final Object value) {
    this(pointCloud.getHeader(), key, description);
    final LasVariableLengthRecordConverter converter = LasPointCloudHeader
      .getVariableLengthRecordConverter(this.key);
    if (converter == null) {
      throw new IllegalArgumentException(
        "Setting LAS record type " + this.key + "=" + value + " is not supported");
    }
    this.value = value;
    this.valueInitialized = true;
    this.bytes = converter.objectToBytes(pointCloud, this);
    if (this.bytes == null) {
      throw new IllegalArgumentException(value + " is not valid for " + key);
    }
  }

  public LasVariableLengthRecord(final LasPointCloudHeader lasPointCloudHeader,
    final boolean extended, final String userId, final int recordId, final String description,
    final byte[] bytes) {
    this(lasPointCloudHeader, userId, recordId, description, bytes);
    this.extended = extended;
  }

  private LasVariableLengthRecord(final LasPointCloudHeader header, final Pair<String, Integer> key,
    final String description) {
    this.header = header;
    this.key = key;
    this.description = description;
  }

  private LasVariableLengthRecord(final LasPointCloudHeader header, final String userId,
    final int recordId, final String description) {
    this(header, new Pair<>(userId, recordId), description);
  }

  public LasVariableLengthRecord(final LasPointCloudHeader header, final String userId,
    final int recordId, final String description, final byte[] bytes) {
    this(header, userId, recordId, description);
    this.bytes = bytes;
  }

  public LasVariableLengthRecord(final LasPointCloudHeader header, final String userId,
    final int recordId, final String description, final byte[] bytes, final Object value) {
    this(header, userId, recordId, description);
    this.bytes = bytes;
    this.value = value;
    this.valueInitialized = true;
  }

  @Override
  public LasVariableLengthRecord clone() {
    try {
      return (LasVariableLengthRecord)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  public byte[] getBytes() {
    return this.bytes;
  }

  public String getDescription() {
    return this.description;
  }

  public Pair<String, Integer> getKey() {
    return this.key;
  }

  public int getRecordId() {
    return this.key.getValue2();
  }

  public String getUserId() {
    return this.key.getValue1();
  }

  @SuppressWarnings("unchecked")
  public <V> V getValue() {
    if (!this.valueInitialized) {
      final LasVariableLengthRecordConverter converter = LasPointCloudHeader
        .getVariableLengthRecordConverter(this.key);
      if (converter == null) {
        this.value = this.bytes;
      } else {
        this.value = converter.readObject(this.header, this.bytes);
      }
      this.valueInitialized = true;
    }
    return (V)this.value;
  }

  public int getValueLength() {
    return this.bytes.length;
  }

  public boolean isExtended() {
    return this.extended;
  }

  void setHeader(final LasPointCloudHeader header) {
    this.header = header;
  }

  @Override
  public String toString() {
    String valueString;
    if (this.value == null) {
      valueString = Arrays.toString(this.bytes);
    } else if (this.value.getClass().isArray()) {
      if (this.value instanceof byte[]) {
        valueString = Arrays.toString((byte[])this.value);
      } else if (this.value instanceof boolean[]) {
        valueString = Arrays.toString((boolean[])this.value);
      } else if (this.value instanceof short[]) {
        valueString = Arrays.toString((short[])this.value);
      } else if (this.value instanceof int[]) {
        valueString = Arrays.toString((int[])this.value);
      } else if (this.value instanceof long[]) {
        valueString = Arrays.toString((long[])this.value);
      } else if (this.value instanceof float[]) {
        valueString = Arrays.toString((float[])this.value);
      } else if (this.value instanceof double[]) {
        valueString = Arrays.toString((double[])this.value);
      } else {
        valueString = this.value.toString();
      }
    } else {
      valueString = this.value.toString();
    }
    return this.key + "=" + valueString + " (" + this.description + ")";
  }
}
