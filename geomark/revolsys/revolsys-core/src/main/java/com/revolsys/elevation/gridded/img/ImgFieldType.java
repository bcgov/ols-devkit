package com.revolsys.elevation.gridded.img;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;

class ImgFieldType {
  private List<ImgField> fields = new ArrayList<>();

  private final String typeName;

  public ImgFieldType(final String typeName, final List<ImgField> fields) {
    this.typeName = typeName;
    this.fields = fields;
  }

  public boolean equalsTypeName(final String typeName) {
    if (typeName == null) {
      return false;
    } else {
      return typeName.equals(this.typeName);
    }
  }

  public List<ImgField> getFields() {
    return this.fields;
  }

  public MapEx readFieldValues(final ImgGriddedElevationReader reader) {
    final MapEx fieldValues = new LinkedHashMapEx();
    for (final ImgField field : this.fields) {
      final String name = field.getName();
      final Object value = field.readValue(reader);
      fieldValues.put(name, value);
    }
    return fieldValues;
  }

  @Override
  public String toString() {
    return this.typeName;
  }
}
