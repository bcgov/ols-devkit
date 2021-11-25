package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;

import com.revolsys.collection.map.MapEx;
import com.revolsys.util.CaseConverter;

public abstract class AbstractSingleValueCodeTable extends AbstractCodeTable {

  private List<Identifier> identifiers = new ArrayList<>();

  private Map<Identifier, Object> idValueCache = new LinkedHashMap<>();

  private Map<Object, Identifier> valueIdCache = new LinkedHashMap<>();

  public AbstractSingleValueCodeTable() {
  }

  public AbstractSingleValueCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected synchronized AbstractSingleValueCodeTable addValue(final Identifier id,
    final Object value) {
    if (id instanceof Number) {
      final Number number = (Number)id;
      updateMaxId(number);
    }

    this.identifiers.add(id);
    this.idValueCache.put(id, value);
    addValueId(id, value);

    addIdentifier(id);
    return this;
  }

  protected void addValueId(final Identifier id, final Object value) {
    this.valueIdCache.put(value, id);
    this.valueIdCache.put(getNormalizedValue(value), id);
  }

  protected synchronized void addValues(final Map<Identifier, List<Object>> valueMap) {
    for (final Entry<Identifier, List<Object>> entry : valueMap.entrySet()) {
      final Identifier id = entry.getKey();
      final List<Object> values = entry.getValue();
      addValue(id, values);
    }
  }

  @Override
  protected int calculateValueFieldLength() {
    int length = 0;
    for (final Object value : this.idValueCache.values()) {
      final int valueLength = value.toString().length();
      if (valueLength > length) {
        length = valueLength;
      }
    }
    return length;
  }

  @Override
  public AbstractSingleValueCodeTable clone() {
    final AbstractSingleValueCodeTable clone = (AbstractSingleValueCodeTable)super.clone();
    clone.identifiers = new ArrayList<>(this.identifiers);
    clone.idValueCache = new LinkedHashMap<>(this.idValueCache);
    clone.valueIdCache = new LinkedHashMap<>(this.valueIdCache);
    return clone;
  }

  @Override
  public void close() {
    super.close();
    this.identifiers.clear();
    this.idValueCache.clear();
    this.valueIdCache.clear();
  }

  protected Identifier getIdByValue(Object value) {
    value = processValue(value);
    Identifier id = this.valueIdCache.get(value);
    if (id == null) {
      final Object normalizedValue = getNormalizedValue(value);
      id = this.valueIdCache.get(normalizedValue);
    }
    return id;
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      return getIdentifier(value);
    } else {
      return null;
    }
  }

  @Override
  public Identifier getIdentifier(final Object value) {
    return getIdentifier(value, true);
  }

  @Override
  public Identifier getIdentifier(final Object... values) {
    if (values != null && values.length == 1) {
      final Object value = values[0];
      return getIdentifier(value);
    } else {
      return null;
    }
  }

  public Identifier getIdentifier(Object value, final boolean loadMissing) {
    refreshIfNeeded();
    final Identifier identifier = super.getIdentifierInternal(value);
    if (identifier != null) {
      return identifier;
    }

    value = processValue(value);
    Identifier id = getIdByValue(value);
    if (id == null && loadMissing && isLoadMissingCodes() && !isLoading()) {
      synchronized (this) {
        id = loadId(value, true);
        if (id != null && !this.idValueCache.containsKey(id)) {
          addValue(id, value);
        }
      }
    }
    return id;
  }

  @Override
  public List<Identifier> getIdentifiers() {
    refreshIfNeeded();
    return Collections.unmodifiableList(this.identifiers);
  }

  @Override
  public Identifier getIdExact(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      return getIdExact(value);
    }
    return null;
  }

  @Override
  public Identifier getIdExact(final Object... values) {
    if (values != null && values.length == 1) {
      final Object value = values[0];
      return super.getIdExact(value);
    } else {
      return null;
    }
  }

  @Override
  public Identifier getIdExact(final Object value) {
    Identifier id = this.valueIdCache.get(value);
    if (id == null) {
      synchronized (this) {
        id = loadId(value, false);
        return this.valueIdCache.get(value);
      }
    }
    return id;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Object id) {
    return (V)getValueById(id);
  }

  protected Object getValueById(Object id) {
    if (id == null) {
      return null;
    }
    if (this.valueIdCache.containsKey(id)) {
      if (id instanceof SingleIdentifier) {
        final SingleIdentifier identifier = (SingleIdentifier)id;
        return identifier.getValue(0);
      } else {
        return id;
      }
    } else {
      Object value = this.idValueCache.get(id);
      if (value == null) {
        String lowerId = id.toString();
        if (this.stringIdMap.containsKey(lowerId)) {
          id = this.stringIdMap.get(lowerId);
          value = this.idValueCache.get(id);
        } else {
          if (!this.caseSensitive) {
            lowerId = lowerId.toLowerCase();
          }
          if (this.stringIdMap.containsKey(lowerId)) {
            id = this.stringIdMap.get(lowerId);
            value = this.idValueCache.get(id);
          }
        }
      }
      return value;
    }
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    if (id != null) {
      Object value = getValueById(id);
      if (value == null) {
        synchronized (this) {
          value = loadValues(id);
          if (value != null && !isLoadAll()) {
            addValue(id, value);
          }
        }
      }
      if (value != null) {
        return Collections.singletonList(value);
      }

    }
    return null;

  }

  protected boolean isLoadMissingCodes() {
    return false;
  }

  protected Identifier loadId(final Object value, final boolean createId) {
    return null;
  }

  protected Object loadValues(final Object id) {
    return null;
  }

  private Object processValue(final Object value) {
    if (isCapitalizeWords()) {
      if (value != null) {
        return CaseConverter.toCapitalizedWords(value.toString());
      }
    }
    return value;
  }

  @Override
  public synchronized void refresh() {
    this.valueFieldLength = -1;
    super.refresh();
    this.identifiers.clear();
    this.idValueCache.clear();
    this.valueIdCache.clear();
  }

  public void setValues(final MapEx values) {
    for (final String key : values.keySet()) {
      final Object value = values.get(key);
      final Identifier id = Identifier.newIdentifier(key);
      addValue(id, value);
    }
  }
}
