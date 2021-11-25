package ca.bc.gov.geomark.doclet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

public class BaseDoclet {
  public static Map<String, AnnotationTypeDoc> getAnnotations(final PackageDoc packageDoc) {
    final Map<String, AnnotationTypeDoc> annotations = new TreeMap<>();
    for (final AnnotationTypeDoc annotationDoc : packageDoc.annotationTypes()) {
      annotations.put(annotationDoc.name(), annotationDoc);
    }
    return annotations;
  }

  public static Map<String, ClassDoc> getClasses(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> classes = new TreeMap<>();
    for (final ClassDoc classDoc : packageDoc.ordinaryClasses()) {
      classes.put(classDoc.name(), classDoc);
    }
    return classes;
  }

  public static Map<String, ClassDoc> getEnums(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> enums = new TreeMap<>();
    for (final ClassDoc enumDoc : packageDoc.enums()) {
      enums.put(enumDoc.name(), enumDoc);
    }
    return enums;
  }

  public static Map<String, ClassDoc> getInterfaces(final PackageDoc packageDoc) {
    final Map<String, ClassDoc> interfaces = new TreeMap<>();
    for (final ClassDoc classDoc : packageDoc.interfaces()) {
      interfaces.put(classDoc.name(), classDoc);
    }
    return interfaces;
  }

  protected String destDir = ".";

  protected String docTitle;

  protected Set<String> customCssUrls = new LinkedHashSet<>();

  protected final RootDoc root;

  protected XmlWriter writer;

  public BaseDoclet(final RootDoc root) {
    this.root = root;
  }

  public void documentation() {
  }

  protected String getClassId(final ClassDoc classDoc) {
    return DocletUtil.qualifiedName(classDoc);
  }

  protected String getMemberId(final ExecutableMemberDoc member) {
    final StringBuilder id = new StringBuilder();
    final ClassDoc classDoc = member.containingClass();
    final String classId = getClassId(classDoc);
    id.append(classId);
    id.append(".");
    final String memberName = member.name();
    id.append(memberName);
    final Parameter[] parameters = member.parameters();
    for (final Parameter parameter : parameters) {
      id.append("-");
      final Type type = parameter.type();
      String typeName = type.qualifiedTypeName();
      typeName = typeName.replaceAll("^java.lang.", "");
      typeName = typeName.replaceAll("^java.io.", "");
      typeName = typeName.replaceAll("^java.util.", "");
      id.append(typeName);
      id.append(type.dimension());
    }
    return id.toString().replaceAll("[^A-Za-z0-9\\-_.]", "_");
  }

  public void navbar() {
  }

  protected void setOptions(final String[][] options) {
    for (final String[] option : options) {
      final String optionName = option[0];
      if (optionName.equals("-d")) {
        this.destDir = option[1];
      } else if (optionName.equals("-doctitle")) {
        this.docTitle = option[1];
      } else if (optionName.equals("-customcssurl")) {
        this.customCssUrls.add(option[1]);
      }
    }
    try {
      final File dir = new File(this.destDir);
      final File indexFile = new File(dir, "index.html");
      final FileWriter out = new FileWriter(indexFile);
      this.writer = new XmlWriter(out, false);
      this.writer.setIndent(false);
      this.writer.setWriteNewLine(false);
      DocletUtil.copyFiles(this.destDir);
    } catch (final IOException e) {
      throw new IllegalArgumentException(e.fillInStackTrace().getMessage(), e);
    }
  }

  protected void start() {
    try {
      setOptions(this.root.options());

      DocletUtil.htmlHead(this.writer, this.docTitle, this.customCssUrls);

      navbar();

      documentation();

      DocletUtil.htmlFoot(this.writer);
    } finally {
      if (this.writer != null) {
        this.writer.close();
      }
    }
  }
}
