package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;

import com.revolsys.util.CaseConverter;

public abstract class AbstractMultiValueCodeTable extends AbstractCodeTable {

  private List<Identifier> identifiers = new ArrayList<>();

  private Map<Identifier, List<Object>> idValueCache = new LinkedHashMap<>();

  private Map<List<Object>, Identifier> valueIdCache = new LinkedHashMap<>();

  public AbstractMultiValueCodeTable() {
  }

  public AbstractMultiValueCodeTable(final boolean capitalizeWords) {
    this.capitalizeWords = capitalizeWords;
  }

  protected synchronized void addValue(final Identifier id, final List<Object> values) {
    if (id instanceof Number) {
      final Number number = (Number)id;
      updateMaxId(number);
    }

    this.identifiers.add(id);
    this.idValueCache.put(id, values);
    addValueId(id, values);

    addIdentifier(id);
  }

  protected void addValue(final Identifier id, final Object... values) {
    final List<Object> valueList = Arrays.asList(values);
    addValue(id, valueList);
  }

  protected void addValueId(final Identifier id, final List<Object> values) {
    this.valueIdCache.put(values, id);
    this.valueIdCache.put(getNormalizedValues(values), id);
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
  public AbstractMultiValueCodeTable clone() {
    final AbstractMultiValueCodeTable clone = (AbstractMultiValueCodeTable)super.clone();
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

  protected Identifier getIdByValue(final List<Object> valueList) {
    processValues(valueList);
    Identifier id = this.valueIdCache.get(valueList);
    if (id == null) {
      final List<Object> normalizedValues = getNormalizedValues(valueList);
      id = this.valueIdCache.get(normalizedValues);
    }
    return id;
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    return getIdentifier(values, true);
  }

  protected Identifier getIdentifier(final List<Object> values, final boolean loadMissing) {
    refreshIfNeeded();
    if (values.size() == 1) {
      final Object id = values.get(0);
      final Identifier identifier = getIdentifierInternal(id);
      if (identifier != null) {
        return identifier;
      }
    }

    processValues(values);
    Identifier id = getIdByValue(values);
    if (id == null && loadMissing && isLoadMissingCodes() && !isLoading()) {
      synchronized (this) {
        id = loadId(values, true);
        if (id != null && !this.idValueCache.containsKey(id)) {
          addValue(id, values);
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
    Identifier id = this.valueIdCache.get(values);
    if (id == null) {
      synchronized (this) {
        id = loadId(values, false);
        return this.valueIdCache.get(values);
      }
    }
    return id;
  }

  private List<Object> getNormalizedValues(final List<Object> values) {
    final List<Object> normalizedValues = new ArrayList<>();
    for (final Object value : values) {
      final Object normalizedValue = getNormalizedValue(value);
      normalizedValues.add(normalizedValue);
    }
    return normalizedValues;
  }

  protected List<Object> getValueById(Object id) {
    if (this.valueIdCache.containsKey(Collections.singletonList(id))) {
      if (id instanceof SingleIdentifier) {
        final SingleIdentifier identifier = (SingleIdentifier)id;
        return Collections.singletonList(identifier.getValue(0));
      } else {
        return Collections.singletonList(id);
      }
    } else {
      List<Object> values = this.idValueCache.get(id);
      if (values == null) {
        String lowerId = id.toString();
        if (this.stringIdMap.containsKey(lowerId)) {
          id = this.stringIdMap.get(lowerId);
          values = this.idValueCache.get(id);
        } else {
          if (!this.caseSensitive) {
            lowerId = lowerId.toLowerCase();
          }
          if (this.stringIdMap.containsKey(lowerId)) {
            id = this.stringIdMap.get(lowerId);
            values = this.idValueCache.get(id);
          }
        }
      }
      return values;
    }
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    if (id != null) {
      List<Object> values = getValueById(id);
      if (values == null) {
        synchronized (this) {
          values = loadValues(id);
          if (values != null && !isLoadAll()) {
            addValue(id, values);
          }
        }
      }
      if (values != null) {
        return Collections.unmodifiableList(values);
      }

    }
    return null;

  }

  protected boolean isLoadMissingCodes() {
    return false;
  }

  protected Identifier loadId(final List<Object> values, final boolean createId) {
    return null;
  }

  protected List<Object> loadValues(final Object id) {
    return null;
  }

  private void processValues(final List<Object> valueList) {
    if (isCapitalizeWords()) {
      for (int i = 0; i < valueList.size(); i++) {
        final Object value = valueList.get(i);
        if (value != null) {
          final String newValue = CaseConverter.toCapitalizedWords(value.toString());
          valueList.set(i, newValue);
        }
      }
    }
  }

  @Override
  public synchronized void refresh() {
    super.refresh();
    this.identifiers.clear();
    this.idValueCache.clear();
    this.valueIdCache.clear();
  }
}
