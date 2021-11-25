package com.revolsys.record.schema;

import org.jeometry.common.io.PathName;

import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.util.Property;

public abstract class AbstractRecordStoreSchemaElement extends BaseObjectWithProperties
  implements RecordStoreSchemaElement, Comparable<RecordStoreSchemaElement> {

  private PathName pathName;

  private RecordStoreSchema schema;

  public AbstractRecordStoreSchemaElement() {
    this(null, PathName.ROOT);
  }

  public AbstractRecordStoreSchemaElement(final PathName pathName) {
    this(null, pathName);
  }

  public AbstractRecordStoreSchemaElement(final RecordStoreSchema schema) {
    this.schema = schema;
  }

  public AbstractRecordStoreSchemaElement(final RecordStoreSchema schema, final PathName pathName) {
    this.schema = schema;
    setPathName(pathName);
  }

  public AbstractRecordStoreSchemaElement(final RecordStoreSchema schema, final String path) {
    if (!Property.hasValue(path)) {
      throw new IllegalArgumentException("Path is required");
    }
    setPathName(path);
    this.schema = schema;
  }

  public AbstractRecordStoreSchemaElement(final String path) {
    this(null, path);
  }

  @Override
  public void close() {
    super.close();
    this.schema = null;
  }

  @Override
  public int compareTo(final RecordStoreSchemaElement other) {
    final PathName otherPath = other.getPathName();
    if (otherPath == this.pathName) {
      return 0;
    } else if (this.pathName == null) {
      return 1;
    } else if (otherPath == null) {
      return -1;
    } else {
      return this.pathName.compareTo(otherPath);
    }
  }

  @Override
  public boolean equals(final Object other) {
    return other == this;
  }

  @Override
  public String getName() {
    if (this.pathName == null) {
      return "";
    } else {
      return this.pathName.getName();
    }
  }

  @Override
  public String getPath() {
    if (this.pathName == null) {
      return "";
    } else {
      return this.pathName.getPath();
    }
  }

  @Override
  public PathName getPathName() {
    return this.pathName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends RecordStore> V getRecordStore() {
    final RecordStoreSchema schema = getSchema();
    if (schema == null) {
      return null;
    } else {
      return (V)schema.getRecordStore();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RSS extends RecordStoreSchema> RSS getSchema() {
    return (RSS)this.schema;
  }

  @Override
  public int hashCode() {
    if (this.pathName == null) {
      return super.hashCode();
    } else {
      return this.pathName.hashCode();
    }
  }

  protected void setPathName(final PathName pathName) {
    this.pathName = pathName;
  }

  protected void setPathName(final String path) {
    setPathName(PathName.newPathName(path));
  }

  @Override
  public String toString() {
    return getPath();
  }
}
