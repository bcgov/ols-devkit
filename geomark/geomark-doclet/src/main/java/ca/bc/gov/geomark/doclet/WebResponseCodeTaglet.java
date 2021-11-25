package ca.bc.gov.geomark.doclet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import com.sun.source.doctree.DocTree;

import jdk.javadoc.doclet.Taglet;

public class WebResponseCodeTaglet implements Taglet {

  @Override
  public Set<Location> getAllowedLocations() {
    return Collections.singleton(Location.METHOD);
  }

  @Override
  public String getName() {
    return "web.response.code";
  }

  @Override
  public boolean isInlineTag() {
    return false;
  }

  @Override
  public String toString(final List<? extends DocTree> tags, final Element element) {
    return null;
  }
}
