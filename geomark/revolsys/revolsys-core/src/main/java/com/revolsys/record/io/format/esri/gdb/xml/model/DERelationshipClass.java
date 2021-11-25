package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelCardinality;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelClassKey;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelKeyType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelNotification;

public class DERelationshipClass extends DETable {
  private String backwardPathLabel;

  private RelCardinality cardinality;

  private RelClassKey classKey;

  private List<RelationshipClassKey> destinationClassKeys;

  private List<String> destinationClassNames = new ArrayList<>();

  private String forwardPathLabel;

  private boolean isAttributed;

  private boolean isComposite;

  private boolean isReflexive;

  private RelNotification notification;

  private List<RelationshipClassKey> originClassKeys = new ArrayList<>();

  private List<String> originClassNames = new ArrayList<>();

  private List<RelationshipRule> relationshipRules = new ArrayList<>();

  private RelKeyType reyType;

  public DERelationshipClass() {
    super("");
  }

  public String getBackwardPathLabel() {
    return this.backwardPathLabel;
  }

  public RelCardinality getCardinality() {
    return this.cardinality;
  }

  public RelClassKey getClassKey() {
    return this.classKey;
  }

  public List<RelationshipClassKey> getDestinationClassKeys() {
    return this.destinationClassKeys;
  }

  public List<String> getDestinationClassNames() {
    return this.destinationClassNames;
  }

  public String getForwardPathLabel() {
    return this.forwardPathLabel;
  }

  public RelNotification getNotification() {
    return this.notification;
  }

  public List<RelationshipClassKey> getOriginClassKeys() {
    return this.originClassKeys;
  }

  public List<String> getOriginClassNames() {
    return this.originClassNames;
  }

  public List<RelationshipRule> getRelationshipRules() {
    return this.relationshipRules;
  }

  public RelKeyType getReyType() {
    return this.reyType;
  }

  public boolean isAttributed() {
    return this.isAttributed;
  }

  public boolean isComposite() {
    return this.isComposite;
  }

  public boolean isReflexive() {
    return this.isReflexive;
  }

  public void setAttributed(final boolean isAttributed) {
    this.isAttributed = isAttributed;
  }

  public void setBackwardPathLabel(final String backwardPathLabel) {
    this.backwardPathLabel = backwardPathLabel;
  }

  public void setCardinality(final RelCardinality cardinality) {
    this.cardinality = cardinality;
  }

  public void setClassKey(final RelClassKey classKey) {
    this.classKey = classKey;
  }

  public void setComposite(final boolean isComposite) {
    this.isComposite = isComposite;
  }

  public void setDestinationClassKeys(final List<RelationshipClassKey> destinationClassKeys) {
    this.destinationClassKeys = destinationClassKeys;
  }

  public void setDestinationClassNames(final List<String> destinationClassNames) {
    this.destinationClassNames = destinationClassNames;
  }

  public void setForwardPathLabel(final String forwardPathLabel) {
    this.forwardPathLabel = forwardPathLabel;
  }

  public void setNotification(final RelNotification notification) {
    this.notification = notification;
  }

  public void setOriginClassKeys(final List<RelationshipClassKey> originClassKeys) {
    this.originClassKeys = originClassKeys;
  }

  public void setOriginClassNames(final List<String> originClassNames) {
    this.originClassNames = originClassNames;
  }

  public void setReflexive(final boolean isReflexive) {
    this.isReflexive = isReflexive;
  }

  public void setRelationshipRules(final List<RelationshipRule> relationshipRules) {
    this.relationshipRules = relationshipRules;
  }

  public void setReyType(final RelKeyType reyType) {
    this.reyType = reyType;
  }

}
