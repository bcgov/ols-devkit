package com.revolsys.gis.wms.capabilities;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class ContactInformation {
  private ContactAddress contactAddress;

  private final String contactElectronicMailAddress;

  private final String contactFacsimileTelephone;

  private ContactPersonPrimary contactPersonPrimary;

  private final String contactPosition;

  private final String contactVoiceTelephone;

  public ContactInformation(final Element element) {
    this.contactPosition = XmlUtil.getFirstElementText(element, "ContactPosition");
    this.contactVoiceTelephone = XmlUtil.getFirstElementText(element, "ContactVoiceTelephone");
    this.contactFacsimileTelephone = XmlUtil.getFirstElementText(element,
      "ContactFacsimileTelephone");
    this.contactElectronicMailAddress = XmlUtil.getFirstElementText(element,
      "ContactElectronicMailAddress");

    XmlUtil.forFirstElement(element, "ContactAddress", childElement -> {
      this.contactAddress = new ContactAddress(childElement);
    });
    XmlUtil.forFirstElement(element, "ContactPersonPrimary", childElement -> {
      this.contactPersonPrimary = new ContactPersonPrimary(childElement);
    });
  }

  public ContactAddress getContactAddress() {
    return this.contactAddress;
  }

  public String getContactElectronicMailAddress() {
    return this.contactElectronicMailAddress;
  }

  public String getContactFacsimileTelephone() {
    return this.contactFacsimileTelephone;
  }

  public ContactPersonPrimary getContactPersonPrimary() {
    return this.contactPersonPrimary;
  }

  public String getContactPosition() {
    return this.contactPosition;
  }

  public String getContactVoiceTelephone() {
    return this.contactVoiceTelephone;
  }
}
