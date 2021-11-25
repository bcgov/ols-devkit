package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;

public class Style {
  private final String abstractDescription;

  private final List<ImageUrl> legendUrls = new ArrayList<>();

  private final String name;

  private FormatUrl styleSheetUrl;

  private FormatUrl styleUrl;

  private final String title;

  public Style(final Element styleElement) {
    this.name = XmlUtil.getFirstElementText(styleElement, "Name");
    this.title = XmlUtil.getFirstElementText(styleElement, "Title");
    this.abstractDescription = XmlUtil.getFirstElementText(styleElement, "Abstract");
    XmlUtil.forEachElement(styleElement, "LegendURL", (imageUrlElement) -> {
      final ImageUrl imageUrl = new ImageUrl(imageUrlElement);
      this.legendUrls.add(imageUrl);
    });
    XmlUtil.forFirstElement(styleElement, "StyleSheetURL", (urlElement) -> {
      this.styleSheetUrl = new FormatUrl(urlElement);
    });
    XmlUtil.forFirstElement(styleElement, "StyleURL", (urlElement) -> {
      this.styleUrl = new FormatUrl(urlElement);
    });
  }

  public String getAbstractDescription() {
    return this.abstractDescription;
  }

  public List<ImageUrl> getLegendUrls() {
    return this.legendUrls;
  }

  public String getName() {
    return this.name;
  }

  public FormatUrl getStyleSheetUrl() {
    return this.styleSheetUrl;
  }

  public FormatUrl getStyleUrl() {
    return this.styleUrl;
  }

  public String getTitle() {
    return this.title;
  }
}
