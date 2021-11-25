package com.revolsys.gis.wms.capabilities;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.util.UrlUtil;

public class Service {
  private final String abstractDescription;

  private final String accessConstraints;

  private ContactInformation contactInformation;

  private final String fees;

  private final List<String> keywords = new ArrayList<>();

  private final String name;

  private final URL onlineResource;

  private final String title;

  public Service(final Element element) {
    final String onlineResourceText = XmlUtil.getFirstElementAttribute(element, "OnlineResource",
      "http://www.w3.org/1999/xlink", "href");
    this.onlineResource = UrlUtil.getUrl(onlineResourceText);
    this.name = XmlUtil.getFirstElementText(element, "Name");
    this.title = XmlUtil.getFirstElementText(element, "Title");
    this.abstractDescription = XmlUtil.getFirstElementText(element, "Abstract");
    this.fees = XmlUtil.getFirstElementText(element, "Fees");
    this.accessConstraints = XmlUtil.getFirstElementText(element, "AccessConstraints");
    XmlUtil.forFirstElement(element, "ContactInformation", childElement -> {
      this.contactInformation = new ContactInformation(childElement);
    });
    XmlUtil.forFirstElement(element, "KeywordList", keywordsElement -> {
      XmlUtil.forEachElement(keywordsElement, "Keyword", (keywordElement) -> {
        final String keyword = keywordElement.getTextContent();
        this.keywords.add(keyword);
      });
    });
  }

  public String getAbstractDescription() {
    return this.abstractDescription;
  }

  public String getAccessConstraints() {
    return this.accessConstraints;
  }

  public ContactInformation getContactInformation() {
    return this.contactInformation;
  }

  public String getFees() {
    return this.fees;
  }

  public List<String> getKeywords() {
    return this.keywords;
  }

  public String getName() {
    return this.name;
  }

  public URL getOnlineResource() {
    return this.onlineResource;
  }

  public String getTitle() {
    return this.title;
  }
}
