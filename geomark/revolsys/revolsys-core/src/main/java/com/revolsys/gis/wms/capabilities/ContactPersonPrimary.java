package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class ContactPersonPrimary {
  private final String contactOrganization;

  private final String contactPerson;

  public ContactPersonPrimary(final Element element) {
    this.contactPerson = XmlUtil.getFirstElementText(element, "ContactPerson");
    this.contactOrganization = XmlUtil.getFirstElementText(element, "ContactOrganization");
  }

  public String getContactOrganization() {
    return this.contactOrganization;
  }

  public String getContactPerson() {
    return this.contactPerson;
  }
}
