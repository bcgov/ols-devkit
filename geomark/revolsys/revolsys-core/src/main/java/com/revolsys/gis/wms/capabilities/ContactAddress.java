package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class ContactAddress {
  private final String address;

  private final String addressType;

  private final String city;

  private final String country;

  private final String postCode;

  private final String stateOrProvince;

  public ContactAddress(final Element element) {
    this.address = XmlUtil.getFirstElementText(element, "Address");
    this.addressType = XmlUtil.getFirstElementText(element, "AddressType");
    this.city = XmlUtil.getFirstElementText(element, "City");
    this.country = XmlUtil.getFirstElementText(element, "StateOrProvince");
    this.postCode = XmlUtil.getFirstElementText(element, "PostCode");
    this.stateOrProvince = XmlUtil.getFirstElementText(element, "Country");
  }

  public String getAddress() {
    return this.address;
  }

  public String getAddressType() {
    return this.addressType;
  }

  public String getCity() {
    return this.city;
  }

  public String getCountry() {
    return this.country;
  }

  public String getPostCode() {
    return this.postCode;
  }

  public String getStateOrProvince() {
    return this.stateOrProvince;
  }
}
