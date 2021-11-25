package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

public class Dimension {
  private final String name;

  private final String units;

  private final String unitSymbol;

  public Dimension(final Element dimensionElement) {
    this.name = dimensionElement.getAttribute("name");
    this.units = dimensionElement.getAttribute("units");
    this.unitSymbol = dimensionElement.getAttribute("unitSymbol");
  }

  public String getName() {
    return this.name;
  }

  public String getUnits() {
    return this.units;
  }

  public String getUnitSymbol() {
    return this.unitSymbol;
  }

}
