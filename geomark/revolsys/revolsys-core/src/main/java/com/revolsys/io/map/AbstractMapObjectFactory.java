package com.revolsys.io.map;

public abstract class AbstractMapObjectFactory implements MapObjectFactory {
  private String description;

  private String typeName;

  public AbstractMapObjectFactory(final String typeName, final String description) {
    this.typeName = typeName;
    this.description = description;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public String getTypeName() {
    return this.typeName;
  }

  protected void setDescription(final String description) {
    this.description = description;
  }

  protected void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
