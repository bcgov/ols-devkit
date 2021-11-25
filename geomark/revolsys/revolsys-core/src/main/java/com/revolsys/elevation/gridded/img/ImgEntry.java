package com.revolsys.elevation.gridded.img;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonObjectHash;

class ImgEntry implements MapEx, MapSerializer {

  private final long filePosition;

  private final ImgGriddedElevationReader reader;

  private final ImgEntry parent;

  private final ImgEntry previous;

  private long nextPos;

  private ImgEntry next;

  private final long childPosition;

  private ImgEntry child;

  private final String name;

  private final String typeName;

  private final ImgFieldType type;

  private final long dataPosition;

  private MapEx fieldValues;

  public ImgEntry(final ImgGriddedElevationReader reader, final int position) {
    this(reader, position, null, null);
  }

  ImgEntry(final ImgGriddedElevationReader reader, final long position, final ImgEntry parent,
    final ImgEntry previous) {
    this.reader = reader;
    this.reader.seek(position);
    this.filePosition = position;
    this.parent = parent;
    this.previous = previous;

    this.nextPos = reader.readInt();
    reader.readInt();
    reader.readInt();
    this.childPosition = reader.readInt();
    this.dataPosition = reader.readInt();
    @SuppressWarnings("unused")
    final int dataSize = reader.readInt();

    this.name = reader.readString0(64);
    this.typeName = reader.readString0(32);
    this.type = reader.findType(this.typeName);
  }

  @Override
  public MapEx clone() {
    try {
      return (MapEx)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return getFieldValues().entrySet();
  }

  public boolean equalsName(final String name) {
    return name.equals(this.name);
  }

  public boolean equalsType(final String typeName) {
    return typeName.equals(this.typeName);
  }

  public ImgEntry getChild() {
    if (this.child == null && this.childPosition != 0) {
      this.child = new ImgEntry(this.reader, this.childPosition, this, null);
    }

    return this.child;
  }

  private MapEx getFieldValues() {
    if (this.type == null) {
      return MapEx.EMPTY;
    } else if (this.fieldValues == null) {
      if (this.dataPosition > 0) {
        this.reader.seek(this.dataPosition);
      } else {
        this.reader.seek(this.filePosition);
      }
      this.fieldValues = this.type.readFieldValues(this.reader);
    }
    return this.fieldValues;
  }

  public long getFilePosition() {
    return this.filePosition;
  }

  public int GetIntField(final String fieldPath) {
    return getInteger(fieldPath);
  }

  public String getName() {
    return this.name;
  }

  public ImgEntry getNamedChild(final String pszName) {
    for (ImgEntry entry = getChild(); entry != null; entry = entry.getNext()) {
      if (entry.equalsName(pszName)) {
        return entry;
      }
    }
    return null;
  }

  public ImgEntry getNext() {
    if (this.next == null && this.nextPos != 0) {
      ImgEntry past;

      for (past = this; past != null && past.filePosition != this.nextPos; past = past.previous) {
      }

      if (past != null) {
        this.nextPos = 0;
        throw new IllegalArgumentException(
          String.format("Corrupt (looping) entry in %s, ignoring some entries after .", this.name));
      }

      return new ImgEntry(this.reader, this.nextPos, this.parent, this);
    }

    return this.next;
  }

  // ImgEntry( String dictionary,
  // String typeName,
  // int dataSize,
  // GByte pabyDataIn ) {
  // this.isMIFObject = true;
  //// Initialize Entry
  // memset(szName, 0, sizeof(szName));
  //
  //// Create a dummy HFAInfo_t.
  // psHFA = static_cast<HFAInfo_t *>(CPLCalloc(sizeof(HFAInfo_t), 1));
  //
  // psHFA.eAccess = HFA_ReadOnly;
  // psHFA.bTreeDirty = false;
  // psHFA.poRoot = this;
  //
  // psHFA.poDictionary = new HFADictionary(dictionary);
  //
  //// Work out the type for this MIFObject.
  // memset(szType, 0, sizeof(szType));
  // snprintf(szType, sizeof(szType), "%s", pszTypeName);
  //
  // type = psHFA.poDictionary.FindType(szType);
  //
  // nDataSize = nDataSizeIn;
  // pabyData = pabyDataIn;
  // }

  public String getTypeName() {
    return this.typeName;
  }

  @Override
  public <T> T getValue(final CharSequence name) {
    final MapEx fieldValues = getFieldValues();
    return fieldValues.getValue(name);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = new JsonObjectHash();
    map.add("name", this.name);
    map.add("fieldType", this.typeName);
    final MapEx values = getFieldValues();
    if (!values.isEmpty()) {
      addToMap(map, "values", values);
    }
    final List<MapEx> children = new ArrayList<>();
    for (ImgEntry entry = getChild(); entry != null; entry = entry.getNext()) {
      children.add(entry.toMap());
    }
    if (!children.isEmpty()) {
      addToMap(map, "children", children);
    }
    return map;
  }

  @Override
  public String toString() {
    return this.name + ": " + this.typeName + "\n" + getFieldValues();
  }
}
