package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.code.AbstractCodeTable;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.MergePolicyType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.SplitPolicyType;

public class Domain extends AbstractCodeTable implements Cloneable {
  private List<CodedValue> codedValues = new ArrayList<>();

  private Map<Identifier, List<Object>> idValueMap = new HashMap<>();

  private int maxId = 0;

  private Map<String, Identifier> stringIdMap = new HashMap<>();

  private JComponent swingEditor;

  private Map<String, Identifier> valueIdMap = new HashMap<>();

  private String description;

  private String domainName;

  private FieldType fieldType = FieldType.esriFieldTypeSmallInteger;

  private MergePolicyType mergePolicy = MergePolicyType.esriMPTAreaWeighted;

  private String owner;

  private SplitPolicyType splitPolicy = SplitPolicyType.esriSPTDuplicate;

  public Domain() {
  }

  public Domain(final String domainName, final FieldType fieldType, final String description) {
    this.domainName = domainName;
    this.fieldType = fieldType;
    this.description = description;
  }

  public synchronized Domain addCodedValue(final Object code, final String name) {
    final Identifier identifier = Identifier.newIdentifier(code);
    final CodedValue value = new CodedValue(code, name);
    this.codedValues.add(value);
    final List<Object> values = Collections.<Object> singletonList(name);
    this.idValueMap.put(identifier, values);
    this.stringIdMap.put(code.toString(), identifier);
    this.valueIdMap.put(name.toLowerCase(), identifier);
    if (code instanceof Number) {
      final int id = ((Number)code).intValue();
      if (this.maxId < id) {
        this.maxId = id;
      }
    }
    return this;
  }

  public synchronized Domain addCodedValue(final String name) {
    newCodedValue(name);
    return this;
  }

  @Override
  protected int calculateValueFieldLength() {
    int length = 0;
    for (final String value : this.valueIdMap.keySet()) {
      final int valueLength = value.length();
      if (valueLength > length) {
        length = valueLength;
      }
    }
    return length;
  }

  @Override
  public Domain clone() {
    final Domain clone = (Domain)super.clone();
    clone.idValueMap = new HashMap<>();
    clone.stringIdMap = new HashMap<>();
    clone.valueIdMap = new HashMap<>();
    clone.codedValues = new ArrayList<>();
    for (final CodedValue codedValue : this.codedValues) {
      clone.addCodedValue(codedValue.getCode(), codedValue.getName());
    }
    return clone;
  }

  @Override
  public int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (value2 == null) {
      return -1;
    } else {
      final Object codeValue1 = getValue(Identifier.newIdentifier(value1));
      final Object codeValue2 = getValue(Identifier.newIdentifier(value2));
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  public List<CodedValue> getCodedValues() {
    return this.codedValues;
  }

  public String getDescription() {
    return this.description;
  }

  public String getDomainName() {
    return this.domainName;
  }

  @Override
  public List<String> getFieldNameAliases() {
    return Collections.emptyList();
  }

  public FieldType getFieldType() {
    return this.fieldType;
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    if (values.size() == 1) {
      final Object value = values.get(0);
      if (value == null) {
        return null;
      } else if (this.idValueMap.containsKey(value)) {
        return Identifier.newIdentifier(value);
      } else if (this.stringIdMap.containsKey(value.toString())) {
        return this.stringIdMap.get(value.toString());
      } else {
        final String lowerValue = ((String)value).toLowerCase();
        final Identifier id = this.valueIdMap.get(lowerValue);
        return id;
      }
    } else {
      throw new IllegalArgumentException("Expecting only a single value " + values);
    }
  }

  @Override
  public Identifier getIdentifier(final Map<String, ? extends Object> values) {
    final Object name = getName(values);
    return getIdentifier(name);
  }

  @Override
  public List<Identifier> getIdentifiers() {
    return new ArrayList<>(this.idValueMap.keySet());
  }

  @Override
  public String getIdFieldName() {
    return getDomainName() + "_ID";
  }

  @Override
  public Map<String, ? extends Object> getMap(final Identifier id) {
    final Object value = getValue(id);
    return Collections.singletonMap("NAME", value);
  }

  public MergePolicyType getMergePolicy() {
    return this.mergePolicy;
  }

  @Override
  public String getName() {
    return getDomainName();
  }

  public String getName(final Map<String, ? extends Object> values) {
    return (String)values.get("NAME");
  }

  public String getOwner() {
    return this.owner;
  }

  public SplitPolicyType getSplitPolicy() {
    return this.splitPolicy;
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Identifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return null;
    } else {
      final Object value = values.get(0);
      return (V)value;
    }
  }

  @Override
  public <V> V getValue(final Object id) {
    return getValue(Identifier.newIdentifier(id));
  }

  @Override
  public List<String> getValueFieldNames() {
    return Arrays.asList("NAME");
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    if (id == null) {
      return null;
    } else {
      List<Object> values = this.idValueMap.get(id);
      if (values == null) {
        final Identifier objectId = this.stringIdMap.get(id.toString());
        if (objectId == null) {
          return null;
        } else {
          values = this.idValueMap.get(objectId);
        }
      }
      return Collections.unmodifiableList(values);
    }
  }

  @Override
  public boolean isEmpty() {
    return this.idValueMap.isEmpty();
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public boolean isLoading() {
    return false;
  }

  public synchronized Identifier newCodedValue(final String name) {
    Object id;
    switch (getFieldType()) {
      case esriFieldTypeInteger:
        id = (int)++this.maxId;
      break;
      case esriFieldTypeSmallInteger:
        id = (short)++this.maxId;
      break;

      default:
        throw new RuntimeException("Cannot generate code for field type " + getFieldType());
    }
    addCodedValue(id, name);
    return Identifier.newIdentifier(id);
  }

  @Override
  public void refresh() {
  }

  public synchronized void setCodedValues(final List<CodedValue> codedValues) {
    this.codedValues = new ArrayList<>();
    for (final CodedValue codedValue : codedValues) {
      final Object code = codedValue.getCode();
      final String name = codedValue.getName();
      addCodedValue(code, name);

    }
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setDomainName(final String domainName) {
    this.domainName = domainName;
  }

  public void setFieldType(final FieldType fieldType) {
    this.fieldType = fieldType;
  }

  public void setMergePolicy(final MergePolicyType mergePolicy) {
    this.mergePolicy = mergePolicy;
  }

  public void setOwner(final String owner) {
    this.owner = owner;
  }

  public void setSplitPolicy(final SplitPolicyType splitPolicy) {
    this.splitPolicy = splitPolicy;
  }

  @Override
  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }

  @Override
  public String toString() {
    return this.domainName;
  }

}
