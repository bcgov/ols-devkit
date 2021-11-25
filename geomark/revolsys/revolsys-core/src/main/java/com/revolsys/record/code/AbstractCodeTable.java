package com.revolsys.record.code;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.number.Numbers;

import com.revolsys.io.BaseCloseable;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.record.schema.FieldDefinition;

public abstract class AbstractCodeTable extends BaseObjectWithPropertiesAndChange
  implements BaseCloseable, CodeTable, Cloneable {

  protected boolean capitalizeWords = false;

  protected boolean caseSensitive = false;

  private Map<Identifier, Identifier> idIdCache = new LinkedHashMap<>();

  private long maxId;

  private String name;

  protected Map<String, Identifier> stringIdMap = new HashMap<>();

  protected int valueFieldLength = -1;

  private JComponent swingEditor;

  private FieldDefinition valueFieldDefinition = new FieldDefinition("value", DataTypes.STRING,
    true);

  public AbstractCodeTable() {
  }

  protected void addIdentifier(final Identifier id) {
    this.idIdCache.put(id, id);
    String lowerId = id.toString();
    this.stringIdMap.put(lowerId, id);
    if (!this.caseSensitive) {
      lowerId = lowerId.toLowerCase();
    }
    this.stringIdMap.put(lowerId, id);
  }

  protected abstract int calculateValueFieldLength();

  @Override
  public AbstractCodeTable clone() {
    final AbstractCodeTable clone = (AbstractCodeTable)super.clone();
    clone.idIdCache = new LinkedHashMap<>(this.idIdCache);
    clone.stringIdMap = new LinkedHashMap<>(this.stringIdMap);
    return clone;
  }

  @Override
  public void close() {
    this.idIdCache.clear();
    this.stringIdMap.clear();
    this.swingEditor = null;
  }

  protected Identifier getIdentifierInternal(final Object id) {
    if (id != null) {
      final Identifier cachedId = this.idIdCache.get(id);
      if (cachedId != null) {
        return cachedId;
      } else {
        String lowerId = id.toString();
        if (this.stringIdMap.containsKey(lowerId)) {
          return this.stringIdMap.get(lowerId);
        } else {
          if (!this.caseSensitive) {
            lowerId = lowerId.toLowerCase();
          }
          if (this.stringIdMap.containsKey(lowerId)) {
            return this.stringIdMap.get(lowerId);
          }
        }
      }
    }
    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }

  protected synchronized long getNextId() {
    return ++this.maxId;
  }

  protected Object getNormalizedValue(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return Numbers.toString(number);
    } else {
      return value.toString().toLowerCase();
    }
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  public FieldDefinition getValueFieldDefinition() {
    return this.valueFieldDefinition;
  }

  @Override
  public int getValueFieldLength() {
    if (this.valueFieldLength == -1) {
      final int length = calculateValueFieldLength();
      this.valueFieldLength = length;
    }
    return this.valueFieldLength;
  }

  public boolean isCapitalizeWords() {
    return this.capitalizeWords;
  }

  public boolean isCaseSensitive() {
    return this.caseSensitive;
  }

  @Override
  public boolean isEmpty() {
    return this.idIdCache.isEmpty();
  }

  @Override
  public synchronized void refresh() {
    this.idIdCache.clear();
    this.stringIdMap.clear();
  }

  public void setCapitalizeWords(final boolean capitalizedWords) {
    this.capitalizeWords = capitalizedWords;
  }

  public void setCaseSensitive(final boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }

  public void setValueFieldDefinition(final FieldDefinition valueFieldDefinition) {
    this.valueFieldDefinition = valueFieldDefinition;
  }

  protected void updateMaxId(final Number id) {
    final long longValue = id.longValue();
    if (longValue > this.maxId) {
      this.maxId = longValue;
    }
  }

}
