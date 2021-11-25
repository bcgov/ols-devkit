package com.revolsys.record.io.format.esri.gdb.xml.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.MergePolicyType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelCardinality;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelClassKey;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelKeyRole;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelKeyType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.RelNotification;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.SplitPolicyType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.WorkspaceType;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.record.io.format.xml.XmlProcessor;
import com.revolsys.spring.resource.ByteArrayResource;
import com.revolsys.spring.resource.Resource;

public class EsriGdbXmlParser extends XmlProcessor implements EsriGeodatabaseXmlConstants {

  private static final Map<String, Class<?>> TAG_NAME_CLASS_MAP = new HashMap<>();

  static {
    TAG_NAME_CLASS_MAP.put(CHILDREN.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(SUBTYPE.getLocalPart(), Subtype.class);
    TAG_NAME_CLASS_MAP.put(FIELD_INFOS.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(SUBTYPE_FIELD_INFO.getLocalPart(), SubtypeFieldInfo.class);
    TAG_NAME_CLASS_MAP.put(RELATIONSHIP_CLASS_NAMES.getLocalPart(), ArrayList.class);

    TAG_NAME_CLASS_MAP.put(CODED_VALUE.getLocalPart(), CodedValue.class);
    TAG_NAME_CLASS_MAP.put(CODED_VALUE_DOMAIN.getLocalPart(), CodedValueDomain.class);
    TAG_NAME_CLASS_MAP.put(CODED_VALUES.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(DATA_ELEMENT.getLocalPart(), DataElement.class);
    TAG_NAME_CLASS_MAP.put(DATASET_DEFINITIONS.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(DE_DATASET.getLocalPart(), DEDataset.class);
    TAG_NAME_CLASS_MAP.put(DE_GEO_DATASET.getLocalPart(), DEGeoDataset.class);
    TAG_NAME_CLASS_MAP.put(DE_FEATURE_DATASET.getLocalPart(), DEFeatureDataset.class);
    TAG_NAME_CLASS_MAP.put(DE_FEATURE_CLASS.getLocalPart(), DEFeatureClass.class);
    TAG_NAME_CLASS_MAP.put(DE_TABLE.getLocalPart(), DETable.class);
    TAG_NAME_CLASS_MAP.put(DOMAIN.getLocalPart(), Domain.class);
    TAG_NAME_CLASS_MAP.put(DOMAINS.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(ENVELOPE.getLocalPart(), Envelope.class);
    TAG_NAME_CLASS_MAP.put(ENVELOPE_N.getLocalPart(), EnvelopeN.class);
    TAG_NAME_CLASS_MAP.put(SPATIAL_REFERENCE.getLocalPart(), SpatialReference.class);
    TAG_NAME_CLASS_MAP.put(FIELD_ARRAY.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(FIELDS.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(FIELD.getLocalPart(), Field.class);
    TAG_NAME_CLASS_MAP.put(INDEX_ARRAY.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(INDEXES.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(METADATA.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(INDEX.getLocalPart(), Index.class);
    TAG_NAME_CLASS_MAP.put(GEOMETRY_DEF.getLocalPart(), GeometryDef.class);
    TAG_NAME_CLASS_MAP.put(CONTROLLER_MEMBERSHIP.getLocalPart(), GeometryDef.class);
    TAG_NAME_CLASS_MAP.put(PROPERTY_ARRAY.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(PROPERTY_SET.getLocalPart(), null);
    TAG_NAME_CLASS_MAP.put(PROPERTY_SET_PROPERTY.getLocalPart(), PropertySetProperty.class);
    TAG_NAME_CLASS_MAP.put(EXTENT.getLocalPart(), EnvelopeN.class);
    TAG_NAME_CLASS_MAP.put(GEOGRAPHIC_COORDINATE_SYSTEM.getLocalPart(),
      EsriGdbGeographicCoordinateSystem.class);
    TAG_NAME_CLASS_MAP.put(PROJECTED_COORDINATE_SYSTEM.getLocalPart(),
      EsriGdbProjectedCoordinateSystem.class);
    TAG_NAME_CLASS_MAP.put(SUBTYPES.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(SUBTYPES.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(WORKSPACE_DATA.getLocalPart(), ArrayList.class);
    TAG_NAME_CLASS_MAP.put(WORKSPACE_DEFINITION.getLocalPart(), WorkspaceDefinition.class);
    TAG_NAME_CLASS_MAP.put(WORKSPACE.getLocalPart(), Workspace.class);
  }

  @SuppressWarnings("unchecked")
  public static <T> T parse(final Resource resource) {
    if (resource == null) {
      return null;
    } else {
      final EsriGdbXmlParser parser = new EsriGdbXmlParser();
      return (T)parser.process(resource);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T parse(final String text) {
    if (text == null) {
      return null;
    } else {
      final byte[] bytes = text.getBytes();
      final ByteArrayResource resource = new ByteArrayResource(bytes);
      return (T)parse(resource);
    }
  }

  public EsriGdbXmlParser() {
    super("http://www.esri.com/schemas/ArcGIS/10.1", TAG_NAME_CLASS_MAP);
  }

  @Override
  protected void initConverters() {
    super.initConverters();
    registerEnumConverter(FieldType.class);
    registerEnumConverter(MergePolicyType.class);
    registerEnumConverter(RelCardinality.class);
    registerEnumConverter(RelClassKey.class);
    registerEnumConverter(RelKeyRole.class);
    registerEnumConverter(RelKeyType.class);
    registerEnumConverter(RelNotification.class);
    registerEnumConverter(SplitPolicyType.class);
    registerEnumConverter(WorkspaceType.class);
    registerEnumConverter(GeometryType.class);
  }

  public List<ControllerMembership> processControllerMemberships(final StaxReader parser)
    throws XMLStreamException, IOException {
    final List<ControllerMembership> controllerMemberships = new ArrayList<>();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object value = process(parser);
      if (value instanceof ControllerMembership) {
        final ControllerMembership controllerMembership = (ControllerMembership)value;
        controllerMemberships.add(controllerMembership);
      }
    }
    return controllerMemberships;
  }

}
