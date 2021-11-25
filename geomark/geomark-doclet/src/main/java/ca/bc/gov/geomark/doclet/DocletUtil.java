package ca.bc.gov.geomark.doclet;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.javadoc.WildcardType;

public class DocletUtil {

  private static final Map<String, String> PACKAGE_URLS = new LinkedHashMap<>();

  static {
    addPackageUrl("java.",
      "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java.base/");
    addPackageUrl("com.revolsys.jts.", "https://tsusiatsoftware.net/jts/javadoc/");
  }

  public static void addPackageUrl(final String packagePrefix, final String url) {
    PACKAGE_URLS.put(packagePrefix, url);
  }

  public static void anchor(final XmlWriter writer, final String name, final String title) {
    writer.startTag(HtmlElem.A);
    writer.attribute(HtmlAttr.NAME, name);
    writer.text(title);
    writer.endTag(HtmlElem.A);
  }

  public static void contentContainer(final XmlWriter writer, final String firstColClass) {
    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "container-fluid");

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "row");

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, firstColClass);
  }

  public static void copyFiles(final String destDir) {
    for (final String name : Arrays.asList("bootstrap-custom.css", "javadoc.css", "javadoc.js",
      "javadoc.js")) {
      FileUtil.copy(DocletUtil.class.getResourceAsStream("/ca/bc/gov/geomark/doclet/" + name),
        new File(destDir, name));
    }
  }

  public static String description(final ClassDoc containingClass, final Tag doc) {
    final Tag[] tags = doc.inlineTags();
    return description(containingClass, tags);
  }

  public static String description(final ClassDoc containingClass, final Tag[] tags) {
    final StringBuilder text = new StringBuilder();
    if (tags != null && tags.length > 0) {
      for (final Tag tag : tags) {
        final String kind = tag.kind();
        if (tag instanceof SeeTag) {
          final SeeTag seeTag = (SeeTag)tag;
          seeTag(text, containingClass, seeTag);
        } else if ("Text".equals(kind)) {
          text.append(tag.text());
        }
      }
    }
    return text.toString();
  }

  public static void description(final XmlWriter writer, final ClassDoc containingClass,
    final Doc doc) {
    final Tag[] tags = doc.inlineTags();
    description(writer, containingClass, tags);
  }

  public static void description(final XmlWriter writer, final ClassDoc containingClass,
    final Tag[] tags) {
    if (tags != null && tags.length > 0) {
      for (final Tag tag : tags) {
        final String kind = tag.kind();
        if (tag instanceof SeeTag) {
          final SeeTag seeTag = (SeeTag)tag;
          seeTag(writer, containingClass, seeTag);
        } else if ("Text".equals(kind)) {
          writer.write(tag.text());
        }
      }
    }
  }

  public static void descriptionTd(final XmlWriter writer, final ClassDoc containingClass,
    final Map<String, Tag[]> descriptions, final String name) {
    writer.startTag(HtmlElem.TD);
    writer.attribute(HtmlAttr.CLASS, "description");
    final Tag[] description = descriptions.get(name);
    description(writer, containingClass, description);
    writer.endTagLn(HtmlElem.TD);
  }

  public static void documentationReturn(final XmlWriter writer, final MethodDoc method) {
    final Type type = method.returnType();
    if (type != null && !"void".equals(type.qualifiedTypeName())) {
      Tag[] descriptionTags = null;
      for (final Tag tag : method.tags()) {
        if (tag.name().equals("@return")) {
          descriptionTags = tag.inlineTags();
        }
      }
      writer.startTag(HtmlElem.DIV);
      writer.startTag(HtmlElem.STRONG);
      writer.text("Return");
      writer.endTag(HtmlElem.STRONG);
      writer.endTagLn(HtmlElem.DIV);

      typeNameLink(writer, type);
      writer.text(" ");
      description(writer, method.containingClass(), descriptionTags);
    }
  }

  public static void endContentContainer(final XmlWriter writer) {
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.DIV);
  }

  public static AnnotationDesc getAnnotation(final AnnotationDesc[] annotations,
    final String name) {
    for (final AnnotationDesc annotation : annotations) {
      final AnnotationTypeDoc annotationType = annotation.annotationType();
      final String annotationName = qualifiedName(annotationType);
      if (name.equals(annotationName)) {
        return annotation;
      }
    }
    return null;
  }

  public static AnnotationDesc getAnnotation(final ProgramElementDoc doc, final String name) {
    final AnnotationDesc[] annotations = doc.annotations();
    return getAnnotation(annotations, name);
  }

  public static String getExternalUrl(final String qualifiedTypeName) {
    for (final Entry<String, String> entry : PACKAGE_URLS.entrySet()) {
      final String packagePrefix = entry.getKey();
      if (qualifiedTypeName.startsWith(packagePrefix)) {
        final String baseUrl = entry.getValue();
        final String url = baseUrl + qualifiedTypeName.replaceAll("\\.", "/")
          + ".html?is-external=true";
        return url;
      }
    }
    return null;
  }

  public static Map<String, Tag[]> getParameterDescriptions(final ExecutableMemberDoc method) {
    final Map<String, Tag[]> descriptions = new HashMap<>();
    for (final ParamTag tag : method.paramTags()) {
      final String parameterName = tag.parameterName();
      final Tag[] commentTags = tag.inlineTags();
      descriptions.put(parameterName, commentTags);
    }
    return descriptions;
  }

  public static boolean hasAnnotation(final AnnotationDesc[] annotations, final String name) {
    final AnnotationDesc annotation = getAnnotation(annotations, name);
    return annotation != null;
  }

  public static boolean hasAnnotation(final ProgramElementDoc doc, final String name) {
    final AnnotationDesc annotation = getAnnotation(doc, name);
    return annotation != null;
  }

  public static void headOld(final XmlWriter writer, final String docTitle) {
    writer.startTag(HtmlElem.HEAD);
    writer.element(HtmlElem.TITLE, docTitle);
    for (final String url : Arrays.asList(
      "https://code.jquery.com/ui/1.11.2/themes/cupertino/jquery-ui.css",
      "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/prettify.min.css",
      "https://cdn.datatables.net/1.10.6/css/jquery.dataTables.min.css", "javadoc.css")) {
      HtmlUtil.serializeCss(writer, url);

    }
    for (final String url : Arrays.asList("https://code.jquery.com/jquery-1.12.1.min.js",
      "https://code.jquery.com/ui/1.11.4/jquery-ui.min.js",
      "https://cdn.datatables.net/1.10.11/js/jquery.dataTables.min.js", "javadoc.js")) {
      HtmlUtil.serializeScriptLink(writer, url);
    }
    writer.endTagLn(HtmlElem.HEAD);
  }

  public static void htmlFoot(final XmlWriter writer) {
    HtmlUtil.serializeScriptLink(writer,
      "https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js",
      "https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js",
      "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js",
      "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/prettify.min.js");
    writer.startTag(HtmlElem.SCRIPT);
    writer.textLn("$(function() {");
    writer.textLn("  prettyPrint();");
    writer.textLn("});");
    writer.endTag(HtmlElem.SCRIPT);

    writer.endTagLn(HtmlElem.BODY);
    writer.endTagLn(HtmlElem.HTML);
    writer.endDocument();
  }

  public static void htmlHead(final XmlWriter writer, final String docTitle,
    final Collection<String> customCssUrls) {
    writer.docType("<!DOCTYPE html>");
    writer.startTag(HtmlElem.HTML);
    writer.attribute(HtmlAttr.LANG, "en");
    writer.newLine();

    writer.startTagLn(HtmlElem.HEAD);

    writer.startTag(HtmlElem.META);
    writer.attribute(HtmlAttr.CHARSET, "utf-8");
    writer.endTagLn(HtmlElem.META);

    writer.startTag(HtmlElem.META);
    writer.attribute(HtmlAttr.HTTP_EQUIV, "X-UA-Compatible");
    writer.attribute(HtmlAttr.CONTENT, "IE=edge");
    writer.endTagLn(HtmlElem.META);

    writer.startTag(HtmlElem.META);
    writer.attribute(HtmlAttr.NAME, "viewport");
    writer.attribute(HtmlAttr.CONTENT, "width=device-width, initial-scale=1");
    writer.endTagLn(HtmlElem.META);

    writer.elementLn(HtmlElem.TITLE, docTitle);

    HtmlUtil.serializeCss(writer,
      "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css",
      "https://cdnjs.cloudflare.com/ajax/libs/prettify/r298/prettify.min.css",
      "bootstrap-custom.css");
    if (Property.hasValue(customCssUrls)) {
      HtmlUtil.serializeCss(writer, customCssUrls);
    }
    HtmlUtil.serializeStyle(writer, "body{padding-top:60px}\n"
      + "*[id]:before {display:block;content:' ';margin-top:-75px;height:75px;visibility:hidden;}");
    writer.endTagLn(HtmlElem.HEAD);

    writer.startTag(HtmlElem.BODY);
    writer.attribute("data-spy", "scroll");
    writer.attribute("data-target", "#navMain");
    writer.attribute("data-offset", "60");
    writer.newLine();
  }

  public static boolean isTypeIncluded(final Type type) {
    final ClassDoc classDoc = type.asClassDoc();
    final ClassDoc annotationDoc = type.asAnnotationTypeDoc();
    final boolean included = annotationDoc != null && annotationDoc.isIncluded()
      || classDoc != null && classDoc.isIncluded();
    return included;
  }

  public static void label(final StringBuilder text, final String label, final boolean code) {
    if (code) {
      text.append("<code>");
    }
    text(text, label);
    if (code) {
      text.append("</code>");
    }
  }

  public static void label(final XmlWriter writer, final String label, final boolean code) {
    if (code) {
      writer.startTag(HtmlElem.CODE);
    }
    writer.text(label);
    if (code) {
      writer.endTagLn(HtmlElem.CODE);
    }
  }

  public static void link(final StringBuilder text, final String url, final String label,
    final boolean code) {
    final boolean hasUrl = Property.hasValue(url);
    if (hasUrl) {
      text.append("<a href=\"");
      text.append(url);
      text.append("\">");
    }
    label(text, label, code);
    if (hasUrl) {
      text.append("</a>");
    }
  }

  public static void link(final XmlWriter writer, final String url, final String label,
    final boolean code) {
    final boolean hasUrl = Property.hasValue(url);
    if (hasUrl) {
      writer.startTag(HtmlElem.A);
      writer.attribute(HtmlAttr.HREF, url);
    }
    label(writer, label, code);
    if (hasUrl) {
      writer.endTag(HtmlElem.A);
    }
  }

  public static void navbarEnd(final XmlWriter writer) {
    writer.endTagLn(HtmlElem.UL);
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.NAV);

  }

  public static void navbarStart(final XmlWriter writer, final String title) {
    writer.startTag(HtmlElem.NAV);
    writer.attribute(HtmlAttr.ID, "navMain");
    writer.attribute(HtmlAttr.CLASS, "navbar navbar-default navbar-fixed-top");
    writer.newLine();

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "container");
    writer.newLine();

    {
      writer.startTag(HtmlElem.DIV);
      writer.attribute(HtmlAttr.CLASS, "navbar-header");
      writer.newLine();
      {
        writer.startTag(HtmlElem.BUTTON);
        writer.attribute(HtmlAttr.TYPE, "button");
        writer.attribute(HtmlAttr.CLASS, "navbar-toggle collapsed");
        writer.attribute("data-toggle", "collapse");
        writer.attribute("data-target", "#navbar");
        writer.attribute("aria-expanded", "false");
        writer.attribute("aria-controls", "navbar");
        writer.newLine();

        HtmlUtil.serializeSpan(writer, "sr-only", "Toggle navigation");

        for (int i = 0; i < 3; i++) {
          writer.startTag(HtmlElem.SPAN);
          writer.attribute(HtmlAttr.CLASS, "icon-bar");
          writer.text("");
          writer.endTag(HtmlElem.SPAN);
        }
        writer.endTagLn(HtmlElem.BUTTON);
      }
      {
        writer.startTag(HtmlElem.DIV);
        writer.attribute(HtmlAttr.CLASS, "navbar-brand");
        writer.startTag(HtmlElem.A);
        writer.attribute(HtmlAttr.HREF, "#");
        HtmlUtil.serializeSpan(writer, "navbar-brand-title", title);
        writer.endTag(HtmlElem.A);
        writer.endTag(HtmlElem.DIV);
      }
      writer.endTagLn(HtmlElem.DIV);
    }
    {
      writer.startTag(HtmlElem.DIV);
      writer.attribute(HtmlAttr.ID, "navbar");
      writer.attribute(HtmlAttr.CLASS, "navbar-collapse collapse");
      writer.attribute("aria-expanded", "false");
      writer.newLine();

      writer.startTag(HtmlElem.UL);
      writer.attribute(HtmlAttr.CLASS, "nav navbar-nav");

    }
  }

  public static void navDropdownEnd(final XmlWriter writer) {
    writer.endTagLn(HtmlElem.UL);
    writer.endTagLn(HtmlElem.LI);
  }

  public static void navDropdownStart(final XmlWriter writer, final String title, String url,
    final boolean subMenu) {
    writer.startTag(HtmlElem.LI);
    if (subMenu) {
      writer.attribute(HtmlAttr.CLASS, "dropdown-submenu");
    } else {
      writer.attribute(HtmlAttr.CLASS, "dropdown");
    }

    writer.startTag(HtmlElem.A);
    if (url.startsWith("#")) {
      url = "#" + url.substring(1).replaceAll("[^a-zA-Z0-9_]", "_");
    }
    if (subMenu) {
      writer.attribute(HtmlAttr.HREF, url);
    } else {
      writer.attribute(HtmlAttr.HREF, "#");
      writer.attribute(HtmlAttr.CLASS, "dropdown-toggle");
      writer.attribute("data-toggle", "dropdown");
      writer.attribute(HtmlAttr.ROLE, "button");
      writer.attribute("aria-expanded", "false");
    }
    writer.text(title);
    if (!subMenu) {
      writer.startTag(HtmlElem.SPAN);
      writer.attribute(HtmlAttr.CLASS, "caret");
      writer.text("");
      writer.endTag(HtmlElem.SPAN);
    }
    writer.endTag(HtmlElem.A);

    writer.startTag(HtmlElem.UL);
    writer.attribute(HtmlAttr.CLASS, "dropdown-menu");
    writer.attribute(HtmlAttr.ROLE, "menu");
    writer.newLine();
    if (!subMenu) {
      navMenuItem(writer, title, url);
      writer.startTag(HtmlElem.LI);
      writer.attribute(HtmlAttr.CLASS, "divider");
      writer.endTagLn(HtmlElem.LI);
    }
  }

  public static void navMenuItem(final XmlWriter writer, final String title, String url) {
    writer.startTag(HtmlElem.LI);

    writer.startTag(HtmlElem.A);
    if (url.startsWith("#")) {
      url = "#" + url.substring(1).replaceAll("[^a-zA-Z0-9_]", "_");
    }
    writer.attribute(HtmlAttr.HREF, url);
    writer.text(title);
    writer.endTag(HtmlElem.A);

    writer.endTagLn(HtmlElem.LI);
  }

  public static int optionLength(String optionName) {
    optionName = optionName.toLowerCase();
    if (optionName.equals("-d")) {
      return 2;
    } else if (optionName.equals("-doctitle")) {
      return 2;
    } else if (optionName.equals("-customcssurl")) {
      return 2;
    }
    return 0;
  }

  public static void panelEnd(final XmlWriter writer) {
    writer.endTagLn(HtmlElem.DIV);
    writer.endTagLn(HtmlElem.DIV);
  }

  public static void panelStart(final XmlWriter writer, final String panelClass,
    final QName headerElement, final String id, final String titlePrefix, final String title,
    final String titleSuffix) {
    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "panel " + panelClass);
    writer.newLine();

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "panel-heading");
    writer.newLine();

    String simpleId = null;
    if (Property.hasValue(id)) {
      simpleId = id.replaceAll("[^a-zA-Z0-9_]", "_");
      if (!id.equals(simpleId)) {
        writer.startTag(HtmlElem.A);
        writer.attribute(HtmlAttr.ID, id);
        writer.text("");
        writer.endTag(HtmlElem.A);
      }
    }
    writer.startTag(headerElement);
    writer.attribute(HtmlAttr.CLASS, "panel-title");

    if (Property.hasValue(id)) {
      writer.attribute(HtmlAttr.ID, simpleId);
    }
    if (Property.hasValue(titlePrefix)) {
      writer.element(HtmlElem.SMALL, titlePrefix);
      writer.text(" ");
    }
    writer.text(title);
    if (Property.hasValue(titleSuffix)) {
      writer.text(" ");
      writer.element(HtmlElem.SMALL, titleSuffix);
    }
    writer.endTagLn(headerElement);

    writer.endTagLn(HtmlElem.DIV);

    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "panel-body");
    writer.newLine();
  }

  public static String qualifiedName(final ProgramElementDoc element) {
    final String packageName = element.containingPackage().name();
    return packageName + "." + element.name();
  }

  public static String replaceDocRootDir(final String text) {
    int i = text.indexOf("{@");
    if (i < 0) {
      return text;
    } else {
      final String lowerText = text.toLowerCase();
      i = lowerText.indexOf("{@docroot}", i);
      if (i < 0) {
        return text;
      } else {
        final StringBuffer stringbuffer = new StringBuffer();
        int k = 0;
        do {
          final int j = lowerText.indexOf("{@docroot}", k);
          if (j < 0) {
            stringbuffer.append(text.substring(k));
            break;
          }
          stringbuffer.append(text.substring(k, j));
          k = j + 10;
          stringbuffer.append("./");
          if ("./".length() > 0 && k < text.length() && text.charAt(k) != '/') {
            stringbuffer.append("/");
          }
        } while (true);
        return stringbuffer.toString();
      }
    }
  }

  public static void seeTag(final StringBuilder text, final ClassDoc containingClass,
    final SeeTag seeTag) {
    final String name = seeTag.name();
    if (name.startsWith("@link") || name.equals("@see")) {
      final boolean code = !name.equalsIgnoreCase("@linkplain");
      String label = seeTag.label();

      final StringBuffer stringbuffer = new StringBuffer();

      final String seeTagText = replaceDocRootDir(seeTag.text());
      if (seeTagText.startsWith("<") || seeTagText.startsWith("\"")) {
        stringbuffer.append(seeTagText);
        text.append(seeTagText);
      } else {
        final ClassDoc referencedClass = seeTag.referencedClass();
        final MemberDoc referencedMember = seeTag.referencedMember();
        String referencedMemberName = seeTag.referencedMemberName();
        if (referencedClass == null) {
          final PackageDoc packagedoc = seeTag.referencedPackage();
          if (packagedoc != null && packagedoc.isIncluded()) {
            final String packageName = packagedoc.name();
            if (!Property.hasValue(label)) {
              label = packageName;
            }
            link(text, "#" + packageName, label, code);
          } else {
            // TODO link to external package or class
            // String s9 = getCrossPackageLink(referencedClassName);
            // String s8;
            // if (s9 != null)
            // stringbuffer.append(getHyperLink(s9, "", s1.length() != 0 ? s1
            // : s3, false));
            // else if ((s8 = getCrossClassLink(referencedClassName,
            // referencedMemberName, s1, false, "", !plainLink)) != null) {
            // stringbuffer.append(s8);
            // } else {
            // configuration.getDocletSpecificMsg().warning(seeTag.position(),
            // "doclet.see.class_or_package_not_found", name, s2);
            // stringbuffer.append(s1.length() != 0 ? s1 : s3);
            // }
          }
        } else {
          String url = null;
          final String className = referencedClass.qualifiedName();
          if (referencedClass.isIncluded()) {
            url = "#" + className;
          } else {
            url = getExternalUrl(className);
            if (!Property.hasValue(url)) {
              label = className;
            }
          }
          if (referencedMember != null) {
            if (referencedMember instanceof ExecutableMemberDoc) {
              if (referencedMemberName.indexOf('(') < 0) {
                final ExecutableMemberDoc executableDoc = (ExecutableMemberDoc)referencedMember;
                referencedMemberName = referencedMemberName + executableDoc.signature();
              }
              if (Property.hasValue(referencedMemberName)) {
                label = referencedMemberName;
              } else {
                label = seeTagText;
              }
            }
            if (referencedClass.isIncluded()) {
              url += "." + referencedMemberName;
            } else if (Property.hasValue(url)) {
              url += "#" + referencedMemberName;
            } else {
              label = referencedMember.toString();
            }
          }
          if (!Property.hasValue(label)) {
            label = referencedClass.name();
          }
          link(text, url, label, code);
        }
      }
    }
  }

  public static void seeTag(final XmlWriter writer, final ClassDoc containingClass,
    final SeeTag seeTag) {
    final String name = seeTag.name();
    if (name.startsWith("@link") || name.equals("@see")) {
      final boolean code = !name.equalsIgnoreCase("@linkplain");
      String label = seeTag.label();

      final StringBuffer stringbuffer = new StringBuffer();

      final String seeTagText = replaceDocRootDir(seeTag.text());
      if (seeTagText.startsWith("<") || seeTagText.startsWith("\"")) {
        stringbuffer.append(seeTagText);
        writer.write(seeTagText);
      } else {
        final ClassDoc referencedClass = seeTag.referencedClass();
        final MemberDoc referencedMember = seeTag.referencedMember();
        String referencedMemberName = seeTag.referencedMemberName();
        if (referencedClass == null) {
          final PackageDoc packagedoc = seeTag.referencedPackage();
          if (packagedoc != null && packagedoc.isIncluded()) {
            final String packageName = packagedoc.name();
            if (!Property.hasValue(label)) {
              label = packageName;
            }
            link(writer, "#" + packageName, label, code);
          } else {
            // TODO link to external package or class
            // String s9 = getCrossPackageLink(referencedClassName);
            // String s8;
            // if (s9 != null)
            // stringbuffer.append(getHyperLink(s9, "", s1.length() != 0 ? s1
            // : s3, false));
            // else if ((s8 = getCrossClassLink(referencedClassName,
            // referencedMemberName, s1, false, "", !plainLink)) != null) {
            // stringbuffer.append(s8);
            // } else {
            // configuration.getDocletSpecificMsg().warning(seeTag.position(),
            // "doclet.see.class_or_package_not_found", name, s2);
            // stringbuffer.append(s1.length() != 0 ? s1 : s3);
            // }
          }
        } else {
          String url = null;
          final String className = referencedClass.qualifiedName();
          if (referencedClass.isIncluded()) {
            url = "#" + className;
          } else {
            url = getExternalUrl(className);
            if (!Property.hasValue(url)) {
              label = className;
            }
          }
          if (referencedMember != null) {
            if (referencedMember instanceof ExecutableMemberDoc) {
              if (referencedMemberName.indexOf('(') < 0) {
                final ExecutableMemberDoc executableDoc = (ExecutableMemberDoc)referencedMember;
                referencedMemberName = referencedMemberName + executableDoc.signature();
              }
              if (Property.hasValue(referencedMemberName)) {
                label = referencedMemberName;
              } else {
                label = seeTagText;
              }
            }
            if (referencedClass.isIncluded()) {
              url += "." + referencedMemberName;
            } else if (Property.hasValue(url)) {
              url += "#" + referencedMemberName;
            } else {
              label = referencedMember.toString();
            }
          }
          if (!Property.hasValue(label)) {
            label = referencedClass.name();
          }
          link(writer, url, label, code);
        }
      }
    }
  }

  public static void tagWithAnchor(final XmlWriter writer, final QName tag, final String name,
    final String title) {
    writer.startTag(tag);
    writer.attribute(HtmlAttr.CLASS, "title");
    writer.startTag(HtmlElem.A);
    writer.attribute(HtmlAttr.NAME, name);
    writer.text(title);
    writer.endTag(HtmlElem.A);
    writer.endTagLn(tag);
  }

  public static void text(final StringBuilder text, final String string) {
    int index = 0;
    final int lastIndex = string.length();
    String escapeString = null;
    for (int i = index; i < lastIndex; i++) {
      final char ch = string.charAt(i);
      switch (ch) {
        case '&':
          escapeString = "&amp;";
        break;
        case '<':
          escapeString = "&lt;";
        break;
        case '>':
          escapeString = "&gt;";
        break;
        case 9:
        case 10:
        case 13:
        // Accept these control characters
        break;
        default:
          // Reject all other control characters
          if (ch < 32) {
            throw new IllegalStateException(
              "character " + Integer.toString(ch) + " is not allowed in output");
          }
        break;
      }
      if (escapeString != null) {
        if (i > index) {
          text.append(string, index, i - index);
        }
        text.append(escapeString);
        escapeString = null;
        index = i + 1;
      }
    }
    if (lastIndex > index) {
      text.append(string, index, lastIndex - index);
    }
  }

  public static void title(final XmlWriter writer, final QName element, final String title) {
    writer.startTag(element);
    writer.startTag(HtmlElem.SPAN);
    writer.attribute(HtmlAttr.CLASS, "label label-primary");
    writer.text(title);
    writer.endTag(HtmlElem.SPAN);
    writer.endTagLn(element);
  }

  public static void title(final XmlWriter writer, final String name, final String title) {
    writer.startTag(HtmlElem.DIV);
    writer.attribute(HtmlAttr.CLASS, "title");
    anchor(writer, name, title);
    writer.endTagLn(HtmlElem.DIV);
  }

  public static void typeName(final XmlWriter writer, final Type type) {
    String typeName;
    final String qualifiedTypeName = type.qualifiedTypeName();
    if (isTypeIncluded(type) || getExternalUrl(qualifiedTypeName) != null) {
      typeName = type.typeName();
    } else {
      typeName = qualifiedTypeName;
    }
    writer.text(typeName);
    writer.text(type.dimension());
  }

  public static void typeNameLink(final XmlWriter writer, final Type type) {
    if (type instanceof WildcardType) {
      final WildcardType wildCard = (WildcardType)type;
      writer.text("?");
      final Type[] extendsBounds = wildCard.extendsBounds();
      if (extendsBounds.length > 0) {
        writer.text(" extends ");
        for (int i = 0; i < extendsBounds.length; i++) {
          if (i > 0) {
            writer.text(", ");
          }
          final Type extendsType = extendsBounds[i];
          typeNameLink(writer, extendsType);
        }
      }
    } else {
      final String qualifiedTypeName = type.qualifiedTypeName();
      final String externalLink = getExternalUrl(qualifiedTypeName);

      final boolean included = isTypeIncluded(type);

      if (externalLink != null) {
        HtmlUtil.serializeA(writer, "", externalLink, type.typeName());
      } else if (included) {
        final String url = "#" + qualifiedTypeName;
        HtmlUtil.serializeA(writer, "", url, type.typeName());
      } else {
        writer.text(qualifiedTypeName);
      }
      if (type instanceof ParameterizedType) {
        final ParameterizedType parameterizedType = (ParameterizedType)type;
        final Type[] typeArguments = parameterizedType.typeArguments();
        if (typeArguments.length > 0) {
          writer.text("<");
          for (int i = 0; i < typeArguments.length; i++) {
            if (i > 0) {
              writer.text(", ");
            }
            final Type typeParameter = typeArguments[i];
            typeNameLink(writer, typeParameter);
          }
          writer.text(">");
        }
      }
    }
    writer.text(type.dimension());
  }

  public static boolean validOptions(final String options[][],
    final DocErrorReporter docerrorreporter) {
    for (final String[] option : options) {
      final String argName = option[0].toLowerCase();
      if (argName.equals("-d")) {
        final String destDir = option[1];
        final File file = new File(destDir);
        if (!file.exists()) {
          docerrorreporter.printNotice("Create directory" + destDir);
          file.mkdirs();
        }
        if (!file.isDirectory()) {
          docerrorreporter.printError("Destination not a directory" + file.getPath());
          return false;
        } else if (!file.canWrite()) {
          docerrorreporter.printError("Destination directory not writable " + file.getPath());
          return false;
        }
      }
    }
    return true;
  }
}
