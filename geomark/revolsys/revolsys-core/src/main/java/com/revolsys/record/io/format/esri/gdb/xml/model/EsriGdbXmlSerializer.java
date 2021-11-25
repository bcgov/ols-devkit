package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.xml.XmlConstants;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.io.format.xml.XsiConstants;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class EsriGdbXmlSerializer implements EsriGeodatabaseXmlConstants {
  public static String toString(final Object object) {
    final StringWriter writer = new StringWriter();
    final EsriGdbXmlSerializer serializer = new EsriGdbXmlSerializer(null, writer);
    serializer.serialize(object);
    writer.flush();
    return writer.toString();
  }

  private final Map<Class<?>, Map<QName, Method>> classPropertyMethodMap = new HashMap<>();

  private final Map<Class<?>, Set<QName>> classPropertyTagNamesMap = new HashMap<>();

  private final Map<Class<?>, QName> classTagNameMap = new HashMap<>();

  private final Map<Class<?>, QName> classTypeNameMap = new LinkedHashMap<>();

  private final Map<Class<?>, QName> classXsiTagNameMap = new HashMap<>();

  private XmlWriter out;

  private final Map<QName, QName> tagNameChildTagNameMap = new HashMap<>();

  private final Map<QName, QName> tagNameListElementTagNameMap = new HashMap<>();

  private final Map<QName, QName> tagNameXsiTagNameMap = new HashMap<>();

  private boolean writeFirstNamespace;

  private boolean writeNamespaces;

  private boolean writeNull;

  private final Set<QName> xsiTypeTypeNames = new HashSet<>();

  private EsriGdbXmlSerializer() {
    addTagNameXsiTagName(METADATA, XML_PROPERTY_SET);
    addTagNameChildTagName(METADATA, XML_DOC);

    addClassProperties(SpatialReference.class, SPATIAL_REFERENCE, null, WKT, X_ORIGIN, Y_ORIGIN,
      XY_SCALE, Z_ORIGIN, Z_SCALE, M_ORIGIN, M_SCALE, XY_TOLERANCE, Z_TOLERANCE, M_TOLERANCE,
      HIGH_PRECISION, WKID);

    addClassProperties(EsriGdbGeographicCoordinateSystem.class, SPATIAL_REFERENCE,
      GEOGRAPHIC_COORDINATE_SYSTEM);

    addClassProperties(EsriGdbProjectedCoordinateSystem.class, SPATIAL_REFERENCE,
      PROJECTED_COORDINATE_SYSTEM);

    addClassProperties(GeometryDef.class, GEOMETRY_DEF, GEOMETRY_DEF, AVG_NUM_POINTS, GEOMETRY_TYPE,
      HAS_M, HAS_Z, SPATIAL_REFERENCE, GRID_SIZE_0, GRID_SIZE_1, GRID_SIZE_2);

    addClassProperties(Domain.class, DOMAIN, null, DOMAIN_NAME, FIELD_TYPE, MERGE_POLICY,
      SPLIT_POLICY, DESCRIPTION, OWNER, CODED_VALUES);

    addClassProperties(CodedValueDomain.class, DOMAIN, CODED_VALUE_DOMAIN);
    addTagNameXsiTagName(CODED_VALUES, ARRAY_OF_CODED_VALUE);

    addClassProperties(CodedValue.class, CODED_VALUE, CODED_VALUE, NAME, CODE);

    addClassProperties(Field.class, FIELD, FIELD, NAME, TYPE, IS_NULLABLE, LENGTH, PRECISION, SCALE,
      REQUIRED, EDIATBLE, DOMAIN_FIXED, GEOMETRY_DEF, ALIAS_NAME, MODEL_NAME, DEFAULT_VALUE,
      DOMAIN);

    addTagNameXsiTagName(FIELD_ARRAY, ARRAY_OF_FIELD);

    addTagNameXsiTagName(FIELDS, FIELDS);
    addTagNameChildTagName(FIELDS, FIELD_ARRAY);

    addClassProperties(Index.class, INDEX, INDEX, NAME, IS_UNIQUE, IS_ASCENDING, FIELDS);

    addTagNameXsiTagName(INDEX_ARRAY, ARRAY_OF_INDEX);

    addTagNameXsiTagName(INDEXES, INDEXES);
    addTagNameChildTagName(INDEXES, INDEX_ARRAY);

    addClassProperties(PropertySetProperty.class, PROPERTY_SET_PROPERTY, null, KEY, VALUE);

    addTagNameXsiTagName(PROPERTY_ARRAY, ARRAY_OF_PROPERTY_SET_PROPERTY);

    addTagNameXsiTagName(EXTENSION_PROPERTIES, PROPERTY_SET);
    addTagNameChildTagName(EXTENSION_PROPERTIES, PROPERTY_ARRAY);

    addTagNameListElementTagName(RELATIONSHIP_CLASS_NAMES, NAME);
    addTagNameXsiTagName(RELATIONSHIP_CLASS_NAMES, NAMES);

    addTagNameListElementTagName(SUBTYPES, SUBTYPE);

    addClassProperties(Subtype.class, SUBTYPE, null, SUBTYPE_NAME, SUBTYPE_CODE, FIELD_INFOS);

    addClassProperties(SubtypeFieldInfo.class, SUBTYPE_FIELD_INFO, null, FIELD_NAME, DOMAIN_NAME,
      DEFAULT_VALUE);

    addClassProperties(EnvelopeN.class, ENVELOPE, ENVELOPE_N, X_MIN, Y_MIN, X_MAX, Y_MAX, Z_MIN,
      Z_MAX, M_MIN, M_MAX, SPATIAL_REFERENCE);
    addTagNameXsiTagName(CONTROLLER_MEMBERSHIPS, ARRAY_OF_CONTROLLER_MEMBERSHIP);

    addClassProperties(DataElement.class, DATA_ELEMENT, DATA_ELEMENT, CATALOG_PATH, NAME,
      CHILDREN_EXPANDED, FULL_PROPS_RETRIEVED, METADATA_RETRIEVED, METADATA, CHILDREN);

    addClassProperties(DEDataset.class, DATA_ELEMENT, DE_DATASET, DATASET_TYPE, DSID, VERSIONED,
      CAN_VERSION, CONFIGURATION_KEYWORD);

    addClassProperties(DEGeoDataset.class, DATA_ELEMENT, DE_GEO_DATASET, EXTENT, SPATIAL_REFERENCE);

    addClassProperties(DEFeatureDataset.class, DATA_ELEMENT, DE_FEATURE_DATASET, EXTENT,
      SPATIAL_REFERENCE);

    addClassProperties(DETable.class, DATA_ELEMENT, DE_TABLE, HAS_OID, OBJECT_ID_FIELD_NAME, FIELDS,
      INDEXES, CLSID, EXTCLSID, RELATIONSHIP_CLASS_NAMES, ALIAS_NAME, MODEL_NAME, HAS_GLOBAL_ID,
      GLOBAL_ID_FIELD_NAME, RASTER_FIELD_NAME, EXTENSION_PROPERTIES, SUBTYPE_FIELD_NAME,
      DEFAULT_SUBTYPE_CODE, SUBTYPES, CONTROLLER_MEMBERSHIPS);

    addClassProperties(DEFeatureClass.class, DATA_ELEMENT, DE_FEATURE_CLASS, FEATURE_TYPE,
      SHAPE_TYPE, SHAPE_FIELD_NAME, HAS_M, HAS_Z, HAS_SPATIAL_INDEX, AREA_FIELD_NAME,
      LENGTH_FIELD_NAME, EXTENT, SPATIAL_REFERENCE);

    addTagNameXsiTagName(DATASET_DEFINITIONS, ARRAY_OF_DATA_ELEMENT);
    addTagNameXsiTagName(DOMAINS, ARRAY_OF_DOMAIN);

    addClassProperties(WorkspaceDefinition.class, WORKSPACE_DEFINITION, WORKSPACE_DEFINITION,
      WORKSPACE_TYPE, VERSION, DOMAINS, DATASET_DEFINITIONS, METADATA);

    addTagNameListElementTagName(WORKSPACE_DATA, DATASET_DATA);
    addTagNameXsiTagName(WORKSPACE_DATA, WORKSPACE_DATA);

    addClassProperties(Workspace.class, WORKSPACE, null, WORKSPACE_DEFINITION, WORKSPACE_DATA);

    this.classTypeNameMap.put(Byte.class, XmlConstants.XS_BYTE);
    this.classTypeNameMap.put(Short.class, XmlConstants.XS_SHORT);
    this.classTypeNameMap.put(Integer.class, XmlConstants.XS_INT);
    this.classTypeNameMap.put(Float.class, XmlConstants.XS_FLOAT);
    this.classTypeNameMap.put(Double.class, XmlConstants.XS_DOUBLE);
    this.classTypeNameMap.put(String.class, XmlConstants.XS_STRING);

    this.xsiTypeTypeNames.add(CODE);
  }

  public EsriGdbXmlSerializer(final String esriNamespaceUri, final Writer out) {
    this(out);
    if (esriNamespaceUri != null) {
      this.out.setNamespaceAlias(_NAMESPACE_URI, esriNamespaceUri);
    }
  }

  public EsriGdbXmlSerializer(final Writer out) {
    this();
    this.out = new XmlWriter(out);
    this.out.setIndent(true);
    this.out.startDocument("UTF-8");
    this.out.setPrefix(XmlConstants.XML_SCHEMA);
    this.out.setPrefix(XsiConstants.TYPE);
    this.writeNamespaces = false;
    this.writeFirstNamespace = true;
  }

  private void addClassProperties(final Class<?> objectClass, final QName tagName,
    final QName xsiTagName, final Collection<QName> propertyNames) {
    this.classTagNameMap.put(objectClass, tagName);
    addClassXsiTagName(objectClass, xsiTagName);
    final Set<QName> allPropertyNames = new LinkedHashSet<>();
    addSuperclassPropertyNames(allPropertyNames, objectClass.getSuperclass());
    allPropertyNames.addAll(propertyNames);
    this.classPropertyTagNamesMap.put(objectClass, allPropertyNames);
  }

  private void addClassProperties(final Class<?> objectClass, final QName tagName,
    final QName xsiTagName, final QName... propertyNames) {
    addClassProperties(objectClass, tagName, xsiTagName, Arrays.asList(propertyNames));
  }

  protected void addClassPropertyMethod(final Class<?> objectClass, final QName propertyName,
    final String methodName) {
    Map<QName, Method> classMethods = this.classPropertyMethodMap.get(objectClass);
    if (classMethods == null) {
      classMethods = new HashMap<>();
      this.classPropertyMethodMap.put(objectClass, classMethods);
    }
    final Method method = JavaBeanUtil.getMethod(EsriGdbXmlSerializer.class, methodName,
      Object.class);
    classMethods.put(propertyName, method);
  }

  private void addClassXsiTagName(final Class<?> objectClass, final QName tagName) {
    if (tagName != null) {
      this.classXsiTagNameMap.put(objectClass, tagName);
    }
  }

  private void addSuperclassPropertyNames(final Set<QName> allPropertyNames,
    final Class<?> objectClass) {
    if (!objectClass.equals(Object.class)) {
      addSuperclassPropertyNames(allPropertyNames, objectClass.getSuperclass());
      final Set<QName> propertyNames = this.classPropertyTagNamesMap.get(objectClass);
      if (propertyNames != null) {
        allPropertyNames.addAll(propertyNames);
      }
    }

  }

  private void addTagNameChildTagName(final QName tagName, final QName xsiTagName) {
    this.tagNameChildTagNameMap.put(tagName, xsiTagName);
  }

  private void addTagNameListElementTagName(final QName tagName, final QName xsiTagName) {
    this.tagNameListElementTagNameMap.put(tagName, xsiTagName);
  }

  private void addTagNameXsiTagName(final QName tagName, final QName xsiTagName) {
    this.tagNameXsiTagNameMap.put(tagName, xsiTagName);
  }

  public void close() {
    this.out.flush();
    this.out.close();
  }

  private void endTag(final QName tagName) {
    final QName childTagName = this.tagNameChildTagNameMap.get(tagName);
    if (childTagName != null) {
      endTag(childTagName);
    }

    this.out.endTag();
  }

  private Method getClassPropertyMethod(final Class<?> objectClass, final QName propertyName) {
    final Map<QName, Method> propertyMethodMap = this.classPropertyMethodMap.get(objectClass);
    if (propertyMethodMap == null) {
      return null;
    } else {
      return propertyMethodMap.get(propertyName);
    }
  }

  public void serialize(final Object object) {
    final Class<? extends Object> objectClass = object.getClass();
    QName tagName = this.classTagNameMap.get(objectClass);
    if (tagName == null) {

      final Package classPackage = objectClass.getPackage();
      final String packageName = classPackage.getName();
      final String className = objectClass.getSimpleName();
      tagName = new QName(packageName, className);
    }
    if (!startTag(tagName)) {
      writeXsiTypeAttribute(tagName, objectClass);
    }
    serializeObjectProperties(tagName, object);
    endTag(tagName);
  }

  @SuppressWarnings("rawtypes")
  private void serializeObjectProperties(final QName tagName, final Object object) {
    if (object != null) {
      final Class<? extends Object> objectClass = object.getClass();
      final Collection<QName> propertyTagNames = this.classPropertyTagNamesMap.get(objectClass);
      if (propertyTagNames == null) {
        if (object instanceof List) {
          final Collection list = (Collection)object;
          if (list.isEmpty()) {
            this.out.closeStartTag();
            this.out.setElementHasContent();
          } else {
            final QName listElementTagName = this.tagNameListElementTagNameMap.get(tagName);
            if (listElementTagName == null) {
              for (final Object value : list) {
                serialize(value);
              }
            } else {
              for (final Object value : list) {
                if (!startTag(listElementTagName)) {
                  writeXsiTypeAttribute(listElementTagName, value);
                }
                serializeObjectProperties(listElementTagName, value);
                endTag(listElementTagName);
              }
            }
          }
        } else {
          String string;
          if (object instanceof Double) {
            final Double value = (Double)object;
            if (Double.isFinite(value)) {
              string = DataTypes.toString(value);
            } else {
              string = "0";
            }
          } else {
            string = DataTypes.toString(object);
          }
          this.out.text(string);
        }
      } else {
        for (final QName propertyTagName : propertyTagNames) {
          String propertyName = propertyTagName.getLocalPart();
          if (propertyName.length() > 1 && Character.isLowerCase(propertyName.charAt(1))) {
            propertyName = CaseConverter.toLowerFirstChar(propertyName);
          }
          final String propertyName1 = propertyName;
          final Object value = Property.getSimple(object, propertyName1);
          if (this.writeNull || value != null) {
            final Method method = getClassPropertyMethod(objectClass, propertyTagName);
            if (method == null) {
              if (!startTag(propertyTagName)) {
                writeXsiTypeAttribute(propertyTagName, value);
              }
              serializeObjectProperties(propertyTagName, value);
              endTag(propertyTagName);
            } else {
              JavaBeanUtil.method(method, this, value);
            }
          }
        }
      }
    }
  }

  private boolean startTag(final QName tagName) {
    if (this.writeNamespaces || this.writeFirstNamespace) {
      this.out.startTag(tagName);
      this.writeFirstNamespace = false;
    } else {
      this.out.startTag(null, tagName.getLocalPart());
    }
    final QName xsiTagName = this.tagNameXsiTagNameMap.get(tagName);
    boolean hasXsi = false;
    if (xsiTagName != null) {
      this.out.xsiTypeAttribute(xsiTagName);
      hasXsi = true;
    }
    final QName childTagName = this.tagNameChildTagNameMap.get(tagName);
    if (childTagName != null) {
      startTag(childTagName);
    }
    return hasXsi;
  }

  private void writeXsiTypeAttribute(final QName tagName,
    final Class<? extends Object> objectClass) {
    QName xsiTagName = this.classXsiTagNameMap.get(objectClass);
    if (xsiTagName == null) {
      if (this.xsiTypeTypeNames.contains(tagName)) {
        xsiTagName = this.classTypeNameMap.get(objectClass);
        if (xsiTagName == null) {
          Logs.error(this, "No xsi:type configuration for class " + objectClass);
        }
      }
    }
    if (xsiTagName != null) {
      this.out.xsiTypeAttribute(xsiTagName);
    }
  }

  private void writeXsiTypeAttribute(final QName tagName, final Object value) {
    if (value != null) {
      final Class<?> valueClass = value.getClass();
      writeXsiTypeAttribute(tagName, valueClass);
    }
  }
}
