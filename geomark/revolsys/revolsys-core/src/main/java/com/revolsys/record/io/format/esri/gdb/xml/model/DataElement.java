package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

public class DataElement implements Cloneable {
  private String catalogPath;

  private List<DataElement> children;

  private Boolean childrenExpanded;

  private Boolean fullPropsRetrieved;

  private String metadata;

  private Boolean metadataRetrieved;

  private String name;

  @Override
  public DataElement clone() {
    try {
      final DataElement clone = (DataElement)super.clone();
      if (this.children != null) {
        clone.children = new ArrayList<>();
        for (final DataElement child : this.children) {
          clone.children.add(child.clone());
        }
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public String getCatalogPath() {
    return this.catalogPath;
  }

  public List<DataElement> getChildren() {
    return this.children;
  }

  public Boolean getChildrenExpanded() {
    return this.childrenExpanded;
  }

  public Boolean getFullPropsRetrieved() {
    return this.fullPropsRetrieved;
  }

  public String getMetadata() {
    return this.metadata;
  }

  public Boolean getMetadataRetrieved() {
    return this.metadataRetrieved;
  }

  public String getName() {
    return this.name;
  }

  public String getParentCatalogPath() {
    final String path = getCatalogPath();
    if (path == null) {
      return null;
    } else {
      final int index = path.lastIndexOf('\\');
      if (index == -1 || index == 0) {
        return "\\";
      } else {
        return path.substring(0, index);
      }
    }
  }

  public QName getTypeName() {
    final int slashIndex = this.catalogPath.lastIndexOf('\\');
    if (slashIndex == -1) {
      return new QName(this.catalogPath);
    } else if (slashIndex == 0) {
      return new QName(this.catalogPath.substring(1));
    } else {
      final String namespaceUri = this.catalogPath.substring(1, slashIndex);
      final String localPart = this.catalogPath.substring(slashIndex + 1);
      return new QName(namespaceUri, localPart);
    }
  }

  public void setCatalogPath(final String catalogPath) {
    this.catalogPath = catalogPath;
  }

  public void setChildren(final List<DataElement> children) {
    this.children = children;
  }

  public void setChildrenExpanded(final Boolean childrenExpanded) {
    this.childrenExpanded = childrenExpanded;
  }

  public void setFullPropsRetrieved(final Boolean fullPropsRetrieved) {
    this.fullPropsRetrieved = fullPropsRetrieved;
  }

  public void setMetadata(final String metadata) {
    this.metadata = metadata;
  }

  public void setMetadataRetrieved(final Boolean metadataRetrieved) {
    this.metadataRetrieved = metadataRetrieved;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setTypeName(final QName catalogPath) {
    final String namespaceUri = catalogPath.getNamespaceURI();
    this.name = catalogPath.getLocalPart();
    if (namespaceUri.length() == 0) {
      this.catalogPath = "\\" + this.name;
    } else {
      this.catalogPath = "\\" + namespaceUri + "\\" + this.name;
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
