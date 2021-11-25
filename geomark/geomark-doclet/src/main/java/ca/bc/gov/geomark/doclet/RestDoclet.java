package ca.bc.gov.geomark.doclet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.collection.set.Sets;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class RestDoclet extends BaseDoclet {

  private static Set<String> PARAMETER_IGNORE_CLASS_NAMES = Sets
    .newHash("javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");

  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

  public static int optionLength(final String optionName) {
    return DocletUtil.optionLength(optionName);
  }

  public static boolean start(final RootDoc root) {
    new RestDoclet(root).start();
    return true;
  }

  public static boolean validOptions(final String options[][],
    final DocErrorReporter docerrorreporter) {
    return DocletUtil.validOptions(options, docerrorreporter);
  }

  public RestDoclet(final RootDoc root) {
    super(root);
    this.docTitle = "Geomark Web Service REST API";
    this.customCssUrls.add("../css/rs.css");
  }

  public void addResponseStatusDescription(final Map<String, List<String>> responseCodes,
    final String code, final String description) {
    List<String> descriptions = responseCodes.get(code);
    if (descriptions == null) {
      descriptions = new ArrayList<>();
      responseCodes.put(code, descriptions);
    }
    descriptions.add(description);
  }

  @Override
  public void documentation() {
    DocletUtil.contentContainer(this.writer, "col-md-12");

    this.writer.element(HtmlElem.H1, this.docTitle);
    DocletUtil.description(this.writer, null, this.root);
    for (final PackageDoc packageDoc : this.root.specifiedPackages()) {
      final Map<String, ClassDoc> classes = new TreeMap<>();
      for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
        classes.put(classDoc.name(), classDoc);
      }
      for (final ClassDoc classDoc : classes.values()) {
        documentationClass(classDoc);
      }
    }
    DocletUtil.endContentContainer(this.writer);
  }

  public void documentationClass(final ClassDoc classDoc) {
    if (DocletUtil.hasAnnotation(classDoc, "org.springframework.stereotype.Controller")) {
      final String id = getClassId(classDoc);
      final String name = classDoc.name();
      final String title = CaseConverter.toCapitalizedWords(name);
      DocletUtil.panelStart(this.writer, "panel-default", HtmlElem.H2, id, null, title, null);
      DocletUtil.description(this.writer, classDoc, classDoc);
      for (final MethodDoc methodDoc : classDoc.methods()) {
        documentationMethod(classDoc, methodDoc);
      }
      DocletUtil.panelEnd(this.writer);
    }
  }

  public void documentationMethod(final ClassDoc classDoc, final MethodDoc methodDoc) {
    final AnnotationDesc requestMapping = DocletUtil.getAnnotation(methodDoc,
      "ca.bc.gov.geomark.framework.ui.web.annotation.RequestMapping");
    if (requestMapping != null) {
      final String id = getMethodId(methodDoc);
      final String methodName = methodDoc.name();
      final String title = CaseConverter.toCapitalizedWords(methodName);
      DocletUtil.panelStart(this.writer, "panel-primary", HtmlElem.H3, id, null, title, null);

      DocletUtil.description(this.writer, methodDoc.containingClass(), methodDoc);
      requestMethods(requestMapping);
      uriTemplates(requestMapping);
      uriTemplateParameters(methodDoc);
      parameters(methodDoc);
      responseStatus(methodDoc);

      DocletUtil.panelEnd(this.writer);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getElementValue(final AnnotationDesc annotation, final String name) {
    for (final ElementValuePair pair : annotation.elementValues()) {
      if (pair.element().name().equals(name)) {
        return (T)pair.value().value();
      }
    }
    return null;
  }

  protected String getMethodId(final ExecutableMemberDoc member) {
    final ClassDoc classDoc = member.containingClass();
    final String methodName = member.name();
    final String classId = getClassId(classDoc);
    return classId + "." + methodName;
  }

  @Override
  public void navbar() {
    DocletUtil.navbarStart(this.writer, this.docTitle);
    for (final PackageDoc packageDoc : this.root.specifiedPackages()) {
      final Map<String, ClassDoc> classes = new TreeMap<>();
      for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
        classes.put(classDoc.name(), classDoc);
      }
      for (final ClassDoc classDoc : classes.values()) {
        navMenu(classDoc);
      }
    }
    DocletUtil.navbarEnd(this.writer);
  }

  public void navMenu(final ClassDoc classDoc) {
    final String id = getClassId(classDoc);
    final String name = classDoc.name();
    final String title = CaseConverter.toCapitalizedWords(name);
    DocletUtil.navDropdownStart(this.writer, title, "#" + id, false);
    for (final MethodDoc methodDoc : classDoc.methods()) {
      final AnnotationDesc requestMapping = DocletUtil.getAnnotation(methodDoc,
        "ca.bc.gov.geomark.framework.ui.web.annotation.RequestMapping");
      if (requestMapping != null) {
        navMenu(classDoc, methodDoc);
      }
    }
    DocletUtil.navDropdownEnd(this.writer);
  }

  public void navMenu(final ClassDoc classDoc, final MethodDoc methodDoc) {
    final String name = methodDoc.name();
    final String id = getMethodId(methodDoc);
    final String title = CaseConverter.toCapitalizedWords(name);
    DocletUtil.navMenuItem(this.writer, title, "#" + id);
  }

  private void parameters(final MethodDoc method) {
    final List<Parameter> parameters = new ArrayList<>();
    for (final Parameter parameter : method.parameters()) {
      final AnnotationDesc[] annotations = parameter.annotations();
      if (DocletUtil.hasAnnotation(annotations,
        "org.springframework.web.bind.annotation.RequestParam")
        || DocletUtil.hasAnnotation(annotations,
          "org.springframework.web.bind.annotation.RequestBody")) {
        parameters.add(parameter);
      }
    }
    if (!parameters.isEmpty()) {
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);

      DocletUtil.panelStart(this.writer, "panel-info", HtmlElem.H4, null, null, "Parameters", null);
      this.writer.element(HtmlElem.P, "The resource supports the following parameters. "
        + "For HTTP get requests these must be specified using query string parameters. "
        + "For HTTP POST requests these can be specified using query string, application/x-www-form-urlencoded parameters or multipart/form-data unless otherwise specified. "
        + "Array values [] can be specified by including the parameter multiple times in the request.");

      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "table-responsive");
      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered table-condensed");

      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "Parameter");
      this.writer.element(HtmlElem.TH, "Type");
      this.writer.element(HtmlElem.TH, "Default");
      this.writer.element(HtmlElem.TH, "Required");
      this.writer.element(HtmlElem.TH, "Description");
      this.writer.endTag(HtmlElem.TR);
      this.writer.endTag(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final Parameter parameter : parameters) {
        String typeName = parameter.typeName();
        if (PARAMETER_IGNORE_CLASS_NAMES.contains(typeName)) {
          typeName = typeName.replaceAll("java.util.List<([^>]+)>", "$1\\[\\]");
          typeName = typeName.replaceFirst("^java.lang.", "");
          typeName = typeName.replaceAll("org.springframework.web.multipart.MultipartFile", "File");
          this.writer.startTag(HtmlElem.TR);
          final String name = parameter.name();
          final AnnotationDesc requestParam = DocletUtil.getAnnotation(parameter.annotations(),
            "org.springframework.web.bind.annotation.RequestParam");
          final AnnotationDesc requestBody = DocletUtil.getAnnotation(parameter.annotations(),
            "org.springframework.web.bind.annotation.RequestBody");
          String paramName = name;
          String defaultValue = "-";

          boolean required = true;
          if (requestParam != null) {
            final String value = getElementValue(requestParam, "value");
            if (value != null && !value.trim().equals("")) {
              paramName = value;
            }
            defaultValue = getElementValue(requestParam, "defaultValue");
            if (defaultValue == null) {
              defaultValue = "-";
            }
            required = Boolean.FALSE != (Boolean)getElementValue(requestParam, "required");
          }
          if (requestBody != null) {
            required = true;
            paramName = "HTTP Request body or 'body' parameter";
            typeName = "binary/character data";
          }

          this.writer.startTag(HtmlElem.TD);
          this.writer.startTag(HtmlElem.CODE);
          this.writer.text(paramName);
          this.writer.endTag(HtmlElem.CODE);
          this.writer.endTag(HtmlElem.TD);

          this.writer.startTag(HtmlElem.TD);
          this.writer.startTag(HtmlElem.CODE);
          this.writer.text(typeName);
          this.writer.endTag(HtmlElem.CODE);
          this.writer.endTag(HtmlElem.TD);

          this.writer.element(HtmlElem.TD, defaultValue);
          if (required) {
            this.writer.element(HtmlElem.TD, "Yes");
          } else {
            this.writer.element(HtmlElem.TD, "No");
          }
          DocletUtil.descriptionTd(this.writer, method.containingClass(), descriptions, name);
          this.writer.endTag(HtmlElem.TR);
        }
      }
      this.writer.endTag(HtmlElem.TBODY);

      this.writer.endTag(HtmlElem.TABLE);
      this.writer.endTag(HtmlElem.DIV);
      DocletUtil.panelEnd(this.writer);
    }
  }

  private void requestMethods(final AnnotationDesc requestMapping) {
    final AnnotationValue[] methods = getElementValue(requestMapping, "method");
    if (methods != null && methods.length > 0) {
      DocletUtil.panelStart(this.writer, "panel-info", HtmlElem.H4, null, null,
        "HTTP Request Methods", null);
      this.writer.element(HtmlElem.P,
        "The resource can be accessed using the following HTTP request methods.");
      this.writer.startTag(HtmlElem.UL);
      for (final AnnotationValue value : methods) {
        final FieldDoc method = (FieldDoc)value.value();
        this.writer.element(HtmlElem.LI, method.name());
      }
      this.writer.endTag(HtmlElem.UL);
      DocletUtil.panelEnd(this.writer);
    }
  }

  private void responseStatus(final MethodDoc method) {
    final Map<String, List<String>> responseStatusDescriptions = new TreeMap<>();

    for (final Tag tag : method.tags()) {
      if (tag.name().equals("@web.response.status")) {
        final String text = DocletUtil.description(method.containingClass(), tag);

        final int index = text.indexOf(" ");
        if (index != -1) {
          final String status = text.substring(0, index);
          final String description = text.substring(index + 1).trim();
          addResponseStatusDescription(responseStatusDescriptions, status, description);
        }
      }
    }
    addResponseStatusDescription(responseStatusDescriptions, "500",
      "<p><b>Internal Server Error</b></p>"
        + "<p>This error indicates that there was an unexpected error on the server. "
        + "This is sometimes temporary so try again after a few minutes. "
        + "The problem could also be caused by bad input data so verify all input parameters and files. "
        + "If the problem persists contact the support desk with exact details of the parameters you were using.</p>");
    if (!responseStatusDescriptions.isEmpty()) {
      DocletUtil.panelStart(this.writer, "panel-info", HtmlElem.H4, null, null, "HTTP Status Codes",
        null);
      this.writer.element(HtmlElem.P,
        "The resource will return one of the following status codes. The HTML error page may include an error message. The descriptions of the messages and the cause are described below.");
      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "table-responsive");

      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered table-condensed");

      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "HTTP Status Code");
      this.writer.element(HtmlElem.TH, "Description");
      this.writer.endTag(HtmlElem.TR);
      this.writer.endTag(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final Entry<String, List<String>> entry : responseStatusDescriptions.entrySet()) {
        final String code = entry.getKey();
        for (final String message : entry.getValue()) {
          this.writer.startTag(HtmlElem.TR);
          this.writer.element(HtmlElem.TD, code);
          this.writer.startTag(HtmlElem.TD);
          this.writer.write(message);
          this.writer.endTag(HtmlElem.TD);

          this.writer.endTag(HtmlElem.TR);
        }
      }
      this.writer.endTag(HtmlElem.TBODY);

      this.writer.endTag(HtmlElem.TABLE);
      this.writer.endTag(HtmlElem.DIV);
      DocletUtil.panelEnd(this.writer);
    }
  }

  @Override
  protected void setOptions(final String[][] options) {
    super.setOptions(options);
  }

  private void uriTemplateParameters(final MethodDoc method) {
    final List<Parameter> parameters = new ArrayList<>();
    for (final Parameter parameter : method.parameters()) {
      if (DocletUtil.hasAnnotation(parameter.annotations(),
        "org.springframework.web.bind.annotation.PathVariable")) {
        parameters.add(parameter);
      }
    }
    if (!parameters.isEmpty()) {
      final Map<String, Tag[]> descriptions = DocletUtil.getParameterDescriptions(method);
      DocletUtil.panelStart(this.writer, "panel-info", HtmlElem.H4, null, null,
        "URI Template Parameters", null);
      this.writer.element(HtmlElem.P,
        "The URI templates support the following parameters which must be replaced with values as described below.");
      this.writer.startTag(HtmlElem.DIV);
      this.writer.attribute(HtmlAttr.CLASS, "table-responsive");

      this.writer.startTag(HtmlElem.TABLE);
      this.writer.attribute(HtmlAttr.CLASS, "table table-striped table-bordered table-condensed");

      this.writer.startTag(HtmlElem.THEAD);
      this.writer.startTag(HtmlElem.TR);
      this.writer.element(HtmlElem.TH, "Parameter");
      this.writer.element(HtmlElem.TH, "Type");
      this.writer.element(HtmlElem.TH, "Description");
      this.writer.endTag(HtmlElem.TR);
      this.writer.endTag(HtmlElem.THEAD);

      this.writer.startTag(HtmlElem.TBODY);
      for (final Parameter parameter : parameters) {
        this.writer.startTag(HtmlElem.TR);
        final String name = parameter.name();
        this.writer.element(HtmlElem.TD, "{" + name + "}");
        this.writer.element(HtmlElem.TD, parameter.typeName());
        DocletUtil.descriptionTd(this.writer, method.containingClass(), descriptions, name);

        this.writer.endTag(HtmlElem.TR);
      }
      this.writer.endTag(HtmlElem.TBODY);

      this.writer.endTag(HtmlElem.TABLE);
      this.writer.endTag(HtmlElem.DIV);
      DocletUtil.panelEnd(this.writer);
    }
  }

  private void uriTemplates(final AnnotationDesc requestMapping) {
    final AnnotationValue[] uriTemplates = getElementValue(requestMapping, "value");
    if (uriTemplates.length > 0) {
      DocletUtil.panelStart(this.writer, "panel-info", HtmlElem.H4, null, null, "URI Templates",
        null);
      this.writer.element(HtmlElem.P,
        "The URI templates define the paths that can be appended to the base URL of the service to access this resource.");

      for (final AnnotationValue uriTemplate : uriTemplates) {
        this.writer.element(HtmlElem.PRE, uriTemplate.value());
      }
      DocletUtil.panelEnd(this.writer);
    }
  }

}
