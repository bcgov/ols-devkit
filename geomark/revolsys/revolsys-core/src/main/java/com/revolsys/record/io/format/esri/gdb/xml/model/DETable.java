package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.util.Property;

public class DETable extends DEDataset {

  private String aliasName;

  private String clsid = "{7A566981-C114-11D2-8A28-006097AFF44E}";

  private List<ControllerMembership> controllerMemberships = new ArrayList<>();

  private String defaultSubtypeCode;

  private String extclsid = "";

  private List<PropertySetProperty> extensionProperties = new ArrayList<>();

  private List<Field> fields = new ArrayList<>();

  private String globalIDFieldName = "";

  private boolean hasOID;

  private List<Index> indexes = new ArrayList<>();

  private String modelName = "";

  private String oidFieldName;

  private String rasterFieldName = "";

  private List<String> relationshipClassNames = new ArrayList<>();

  private String subtypeFieldName;

  private List<Subtype> subtypes = null;

  public DETable() {
    setDatasetType(EsriGeodatabaseXmlConstants.DATASET_TYPE_TABLE);
  }

  public DETable(final String clsid) {
    this.clsid = clsid;
  }

  public void addField(final Field field) {
    this.fields.add(field);
    if (field.getType() == FieldType.esriFieldTypeGlobalID) {
      this.globalIDFieldName = field.getName();
    }
  }

  public void addIndex(final Field field, final boolean unique, final String indexName) {
    final Index index = new Index();
    index.setName(indexName);
    index.setIsUnique(unique);
    index.addField(field);
    addIndex(index);
  }

  public void addIndex(final Index index) {
    this.indexes.add(index);
  }

  public String getAliasName() {
    return this.aliasName;
  }

  public String getCLSID() {
    return this.clsid;
  }

  public List<ControllerMembership> getControllerMemberships() {
    return this.controllerMemberships;
  }

  public String getDefaultSubtypeCode() {
    return this.defaultSubtypeCode;
  }

  public String getEXTCLSID() {
    return this.extclsid;
  }

  public List<PropertySetProperty> getExtensionProperties() {
    return this.extensionProperties;
  }

  public List<Field> getFields() {
    return this.fields;
  }

  public String getGlobalIDFieldName() {
    if (!Property.hasValue(this.globalIDFieldName)) {
      for (final Field field : getFields()) {
        if (field.getType() == FieldType.esriFieldTypeGlobalID) {
          this.globalIDFieldName = field.getName();
        }
      }
    }
    return this.globalIDFieldName;
  }

  public List<Index> getIndexes() {
    return this.indexes;
  }

  public String getModelName() {
    return this.modelName;
  }

  public String getOIDFieldName() {
    return this.oidFieldName;
  }

  public String getRasterFieldName() {
    return this.rasterFieldName;
  }

  public List<String> getRelationshipClassNames() {
    return this.relationshipClassNames;
  }

  public String getSubtypeFieldName() {
    return this.subtypeFieldName;
  }

  public List<Subtype> getSubtypes() {
    return this.subtypes;
  }

  public boolean isHasGlobalID() {
    return Property.hasValue(getGlobalIDFieldName());
  }

  public boolean isHasOID() {
    return this.hasOID;
  }

  public void setAliasName(final String aliasName) {
    this.aliasName = aliasName;
  }

  public void setCLSID(final String clsid) {
    this.clsid = clsid;
  }

  public void setControllerMemberships(final List<ControllerMembership> controllerMemberships) {
    this.controllerMemberships = controllerMemberships;
  }

  public void setDefaultSubtypeCode(final String defaultSubtypeCode) {
    this.defaultSubtypeCode = defaultSubtypeCode;
  }

  public void setEXTCLSID(final String extclsid) {
    this.extclsid = extclsid;
  }

  public void setExtensionProperties(final List<PropertySetProperty> extensionProperties) {
    this.extensionProperties = extensionProperties;
  }

  public void setFields(final List<Field> fields) {
    this.fields = fields;
    for (final Field field : fields) {
      if (field.getType() == FieldType.esriFieldTypeGlobalID) {
        this.globalIDFieldName = field.getName();
      }
    }
  }

  public void setGlobalIDFieldName(final String globalIDFieldName) {
    this.globalIDFieldName = globalIDFieldName;
  }

  public void setHasGlobalID(final boolean hasGlobalID) {
  }

  public void setHasOID(final boolean hasOID) {
    this.hasOID = hasOID;
  }

  public void setIndexes(final List<Index> indexes) {
    this.indexes = indexes;
  }

  public void setModelName(final String modelName) {
    this.modelName = modelName;
  }

  public void setOIDFieldName(final String oidFieldName) {
    this.oidFieldName = oidFieldName;
  }

  public void setRasterFieldName(final String rasterFieldName) {
    this.rasterFieldName = rasterFieldName;
  }

  public void setRelationshipClassNames(final List<String> relationshipClassNames) {
    this.relationshipClassNames = relationshipClassNames;
  }

  public void setSubtypeFieldName(final String subtypeFieldName) {
    this.subtypeFieldName = subtypeFieldName;
  }

  public void setSubtypes(final List<Subtype> subtypes) {
    this.subtypes = subtypes;
  }

}
