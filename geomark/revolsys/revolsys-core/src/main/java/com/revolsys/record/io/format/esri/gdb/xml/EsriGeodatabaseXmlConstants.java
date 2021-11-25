package com.revolsys.record.io.format.esri.gdb.xml;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

public interface EsriGeodatabaseXmlConstants {

  public static final String _NAMESPACE_PREFIX = "esri";

  String _NAMESPACE_URI = "http://www.esri.com/schemas/ArcGIS/10.1";

  QName ALIAS_NAME = new QName(_NAMESPACE_URI, "AliasName", _NAMESPACE_PREFIX);

  QName AREA_FIELD_NAME = new QName(_NAMESPACE_URI, "AreaFieldName", _NAMESPACE_PREFIX);

  QName ARRAY_OF_CODED_VALUE = new QName(_NAMESPACE_URI, "ArrayOfCodedValue", _NAMESPACE_PREFIX);

  QName ARRAY_OF_CONTROLLER_MEMBERSHIP = new QName(_NAMESPACE_URI, "ArrayOfControllerMembership",
    _NAMESPACE_PREFIX);

  QName ARRAY_OF_DATA_ELEMENT = new QName(_NAMESPACE_URI, "ArrayOfDataElement", _NAMESPACE_PREFIX);

  QName ARRAY_OF_DOMAIN = new QName(_NAMESPACE_URI, "ArrayOfDomain", _NAMESPACE_PREFIX);

  QName ARRAY_OF_FIELD = new QName(_NAMESPACE_URI, "ArrayOfField", _NAMESPACE_PREFIX);

  QName ARRAY_OF_INDEX = new QName(_NAMESPACE_URI, "ArrayOfIndex", _NAMESPACE_PREFIX);

  QName ARRAY_OF_PROPERTY_SET_PROPERTY = new QName(_NAMESPACE_URI, "ArrayOfPropertySetProperty",
    _NAMESPACE_PREFIX);

  QName AVG_NUM_POINTS = new QName(_NAMESPACE_URI, "AvgNumPoints", _NAMESPACE_PREFIX);

  QName CAN_VERSION = new QName(_NAMESPACE_URI, "CanVersion", _NAMESPACE_PREFIX);

  QName CATALOG_PATH = new QName(_NAMESPACE_URI, "CatalogPath", _NAMESPACE_PREFIX);

  QName CHILDREN = new QName(_NAMESPACE_URI, "Children", _NAMESPACE_PREFIX);

  QName CHILDREN_EXPANDED = new QName(_NAMESPACE_URI, "ChildrenExpanded", _NAMESPACE_PREFIX);

  QName CLSID = new QName(_NAMESPACE_URI, "CLSID", _NAMESPACE_PREFIX);

  QName CODE = new QName(_NAMESPACE_URI, "Code", _NAMESPACE_PREFIX);

  QName CODED_VALUE = new QName(_NAMESPACE_URI, "CodedValue", _NAMESPACE_PREFIX);

  QName CODED_VALUE_DOMAIN = new QName(_NAMESPACE_URI, "CodedValueDomain", _NAMESPACE_PREFIX);

  QName CODED_VALUES = new QName(_NAMESPACE_URI, "CodedValues", _NAMESPACE_PREFIX);

  QName CONFIGURATION_KEYWORD = new QName(_NAMESPACE_URI, "ConfigurationKeyword",
    _NAMESPACE_PREFIX);

  QName CONTROLLER_MEMBERSHIP = new QName(_NAMESPACE_URI, "ControllerMembership",
    _NAMESPACE_PREFIX);

  QName CONTROLLER_MEMBERSHIPS = new QName(_NAMESPACE_URI, "ControllerMemberships",
    _NAMESPACE_PREFIX);

  String CONTROLLER_MEMBERSHIPS_TYPE = "esri:ArrayOfControllerMembership";

  QName Data = new QName(_NAMESPACE_URI, "Data", _NAMESPACE_PREFIX);

  QName DATA = new QName(_NAMESPACE_URI, "Data", _NAMESPACE_PREFIX);

  QName DATA_ELEMENT = new QName(_NAMESPACE_URI, "DataElement", _NAMESPACE_PREFIX);

  String DATA_ELEMENT_FEATURE_CLASS = "esri:DEFeatureClass";

  String DATA_ELEMENT_TABLE = "esri:DETable";

  String DATA_RECORD_SET = "esri:RecordSet";

  QName DATASET_DATA = new QName(_NAMESPACE_URI, "DatasetData", _NAMESPACE_PREFIX);

  String DATASET_DATA_TABLE_DATA = "esri:TableData";

  QName DATASET_DEFINITIONS = new QName(_NAMESPACE_URI, "DatasetDefinitions", _NAMESPACE_PREFIX);

  String DATASET_DEFINITIONS_TYPE = "esri:ArrayOfDataElement";

  QName DATASET_NAME = new QName(_NAMESPACE_URI, "DatasetName", _NAMESPACE_PREFIX);

  QName DATASET_TYPE = new QName(_NAMESPACE_URI, "DatasetType", _NAMESPACE_PREFIX);

  String DATASET_TYPE_FEATURE_CLASS = "esriDTFeatureClass";

  String DATASET_TYPE_TABLE = "esriDTTable";

  QName DE_DATASET = new QName(_NAMESPACE_URI, "DEDataset", _NAMESPACE_PREFIX);

  QName DE_FEATURE_CLASS = new QName(_NAMESPACE_URI, "DEFeatureClass", _NAMESPACE_PREFIX);

  QName DE_FEATURE_DATASET = new QName(_NAMESPACE_URI, "DEFeatureDataset", _NAMESPACE_PREFIX);

  QName DE_GEO_DATASET = new QName(_NAMESPACE_URI, "DEGeoDataset", _NAMESPACE_PREFIX);

  QName DE_TABLE = new QName(_NAMESPACE_URI, "DETable", _NAMESPACE_PREFIX);

  QName DEFAULT_SUBTYPE_CODE = new QName(_NAMESPACE_URI, "DefaultSubtypeCode", _NAMESPACE_PREFIX);

  QName DEFAULT_VALUE = new QName(_NAMESPACE_URI, "DefaultValue", _NAMESPACE_PREFIX);

  QName DESCRIPTION = new QName(_NAMESPACE_URI, "Description", _NAMESPACE_PREFIX);

  QName DOMAIN = new QName(_NAMESPACE_URI, "Domain", _NAMESPACE_PREFIX);

  QName DOMAIN_FIXED = new QName(_NAMESPACE_URI, "DomainFixed", _NAMESPACE_PREFIX);

  QName DOMAIN_NAME = new QName(_NAMESPACE_URI, "DomainName", _NAMESPACE_PREFIX);

  QName DOMAINS = new QName(_NAMESPACE_URI, "Domains", _NAMESPACE_PREFIX);

  String DOMAINS_TYPE = "esri:ArrayOfDomain";

  QName DSID = new QName(_NAMESPACE_URI, "DSID", _NAMESPACE_PREFIX);

  QName EDIATBLE = new QName(_NAMESPACE_URI, "Editable", _NAMESPACE_PREFIX);

  QName ENVELOPE = new QName(_NAMESPACE_URI, "Envelope", _NAMESPACE_PREFIX);

  QName ENVELOPE_N = new QName(_NAMESPACE_URI, "EnvelopeN", _NAMESPACE_PREFIX);

  String ENVELOPE_N_TYPE = "esri:EnvelopeN";

  String ESRI_OBJECT_ID_FIELD_NAME = "ESRI_OBJECT_ID_FIELD_NAME";

  QName EXTCLSID = new QName(_NAMESPACE_URI, "EXTCLSID", _NAMESPACE_PREFIX);

  QName EXTENSION_PROPERTIES = new QName(_NAMESPACE_URI, "ExtensionProperties", _NAMESPACE_PREFIX);

  QName EXTENT = new QName(_NAMESPACE_URI, "Extent", _NAMESPACE_PREFIX);

  QName FEATURE_TYPE = new QName(_NAMESPACE_URI, "FeatureType", _NAMESPACE_PREFIX);

  String FEATURE_TYPE_SIMPLE = "esriFTSimple";

  QName FIELD = new QName(_NAMESPACE_URI, "Field", _NAMESPACE_PREFIX);

  QName FIELD_ARRAY = new QName(_NAMESPACE_URI, "FieldArray", _NAMESPACE_PREFIX);

  String FIELD_ARRAY_TYPE = "esri:ArrayOfField";

  QName FIELD_INFOS = new QName(_NAMESPACE_URI, "FieldInfos", _NAMESPACE_PREFIX);

  QName FIELD_NAME = new QName(_NAMESPACE_URI, "FieldName", _NAMESPACE_PREFIX);

  QName FIELD_TYPE = new QName(_NAMESPACE_URI, "FieldType", _NAMESPACE_PREFIX);

  String FIELD_TYPE_BLOB = "esriFieldTypeBlob";

  String FIELD_TYPE_DATE = "esriFieldTypeDate";

  String FIELD_TYPE_DOUBLE = "esriFieldTypeDouble";

  String FIELD_TYPE_GEOMETRY = "esriFieldTypeGeometry";

  String FIELD_TYPE_GLOBAL_ID = "esriFieldTypeGlobalID";

  String FIELD_TYPE_GUID = "esriFieldTypeGUID";

  String FIELD_TYPE_INTEGER = "esriFieldTypeInteger";

  String FIELD_TYPE_OBJECT_ID = "esriFieldTypeOID";

  String FIELD_TYPE_RASTER = "esriFieldTypeRaster";

  String FIELD_TYPE_SINGLE = "esriFieldTypeSingle";

  String FIELD_TYPE_SMALL_INTEGER = "esriFieldTypeSmallInteger";

  String FIELD_TYPE_STRING = "esriFieldTypeString";

  QName FIELDS = new QName(_NAMESPACE_URI, "Fields", _NAMESPACE_PREFIX);

  String FIELDS_TYPE = "esri:Fields";

  String FILE_EXTENSION = "gdbx";

  String FORMAT_DESCRIPTION = "ESRI Geodatabase (XML)";

  QName FULL_PROPS_RETRIEVED = new QName(_NAMESPACE_URI, "FullPropsRetrieved", _NAMESPACE_PREFIX);

  QName GEOGRAPHIC_COORDINATE_SYSTEM = new QName(_NAMESPACE_URI, "GeographicCoordinateSystem",
    _NAMESPACE_PREFIX);

  String GEOGRAPHIC_COORDINATE_SYSTEM_TYPE = "esri:GeographicCoordinateSystem";

  QName GEOMETRY_DEF = new QName(_NAMESPACE_URI, "GeometryDef", _NAMESPACE_PREFIX);

  String GEOMETRY_DEF_TYPE = "esri:GeometryDef";

  QName GEOMETRY_TYPE = new QName(_NAMESPACE_URI, "GeometryType", _NAMESPACE_PREFIX);

  String GEOMETRY_TYPE_MULTI_PATCH = "esriGeometryMultiPatch";

  String GEOMETRY_TYPE_MULTI_POINT = "esriGeometryMultipoint";

  String GEOMETRY_TYPE_POINT = "esriGeometryPoint";

  String GEOMETRY_TYPE_POLYGON = "esriGeometryPolygon";

  String GEOMETRY_TYPE_POLYLINE = "esriGeometryPolyline";

  QName GLOBAL_ID_FIELD_NAME = new QName(_NAMESPACE_URI, "GlobalIDFieldName", _NAMESPACE_PREFIX);

  QName GRID_SIZE_0 = new QName(_NAMESPACE_URI, "GridSize0", _NAMESPACE_PREFIX);

  QName GRID_SIZE_1 = new QName(_NAMESPACE_URI, "GridSize1", _NAMESPACE_PREFIX);

  QName GRID_SIZE_2 = new QName(_NAMESPACE_URI, "GridSize2", _NAMESPACE_PREFIX);

  QName HAS_GLOBAL_ID = new QName(_NAMESPACE_URI, "HasGlobalID", _NAMESPACE_PREFIX);

  QName HAS_ID = new QName(_NAMESPACE_URI, "HasID", _NAMESPACE_PREFIX);

  QName HAS_M = new QName(_NAMESPACE_URI, "HasM", _NAMESPACE_PREFIX);

  QName HAS_OID = new QName(_NAMESPACE_URI, "HasOID", _NAMESPACE_PREFIX);

  QName HAS_SPATIAL_INDEX = new QName(_NAMESPACE_URI, "HasSpatialIndex", _NAMESPACE_PREFIX);

  QName HAS_Z = new QName(_NAMESPACE_URI, "HasZ", _NAMESPACE_PREFIX);

  QName HIGH_PRECISION = new QName(_NAMESPACE_URI, "HighPrecision", _NAMESPACE_PREFIX);

  QName INDEX = new QName(_NAMESPACE_URI, "Index", _NAMESPACE_PREFIX);

  QName INDEX_ARRAY = new QName(_NAMESPACE_URI, "IndexArray", _NAMESPACE_PREFIX);

  String INDEX_ARRAY_TYPE = "esri:ArrayOfIndex";

  String INDEX_TYPE = "esri:Index";

  QName INDEXES = new QName(_NAMESPACE_URI, "Indexes", _NAMESPACE_PREFIX);

  String INDEXES_TYPE = "esri:Indexes";

  QName IS_ASCENDING = new QName(_NAMESPACE_URI, "IsAscending", _NAMESPACE_PREFIX);

  QName IS_NULLABLE = new QName(_NAMESPACE_URI, "IsNullable", _NAMESPACE_PREFIX);

  QName IS_UNIQUE = new QName(_NAMESPACE_URI, "IsUnique", _NAMESPACE_PREFIX);

  QName KEY = new QName(_NAMESPACE_URI, "Key", _NAMESPACE_PREFIX);

  QName LENGTH = new QName(_NAMESPACE_URI, "Length", _NAMESPACE_PREFIX);

  QName LENGTH_FIELD_NAME = new QName(_NAMESPACE_URI, "LengthFieldName", _NAMESPACE_PREFIX);

  QName M = new QName(_NAMESPACE_URI, "M", _NAMESPACE_PREFIX);

  QName M_MAX = new QName(_NAMESPACE_URI, "MMax", _NAMESPACE_PREFIX);

  QName M_MIN = new QName(_NAMESPACE_URI, "MMin", _NAMESPACE_PREFIX);

  QName M_ORIGIN = new QName(_NAMESPACE_URI, "MOrigin", _NAMESPACE_PREFIX);

  QName M_SCALE = new QName(_NAMESPACE_URI, "MScale", _NAMESPACE_PREFIX);

  QName M_TOLERANCE = new QName(_NAMESPACE_URI, "MTolerance", _NAMESPACE_PREFIX);

  String MEDIA_TYPE = "text/xml";

  QName MERGE_POLICY = new QName(_NAMESPACE_URI, "MergePolicy", _NAMESPACE_PREFIX);

  QName METADATA = new QName(_NAMESPACE_URI, "Metadata", _NAMESPACE_PREFIX);

  QName METADATA_RETRIEVED = new QName(_NAMESPACE_URI, "MetadataRetrieved", _NAMESPACE_PREFIX);

  QName MODEL_NAME = new QName(_NAMESPACE_URI, "ModelName", _NAMESPACE_PREFIX);

  QName NAME = new QName(_NAMESPACE_URI, "Name", _NAMESPACE_PREFIX);

  QName NAMES = new QName(_NAMESPACE_URI, "Names", _NAMESPACE_PREFIX);

  String NAMES_TYPE = "esri:Names";

  String NAMESPACE_URI_93 = "http://www.esri.com/schemas/ArcGIS/9.3";

  QName OBJECT_ID_FIELD_NAME = new QName(_NAMESPACE_URI, "OIDFieldName", _NAMESPACE_PREFIX);

  QName OWNER = new QName(_NAMESPACE_URI, "Owner", _NAMESPACE_PREFIX);

  QName PATH = new QName(_NAMESPACE_URI, "Path", _NAMESPACE_PREFIX);

  QName PATH_ARRAY = new QName(_NAMESPACE_URI, "PathArray", _NAMESPACE_PREFIX);

  String PATH_ARRAY_TYPE = "esri:ArrayOfPath";

  String PATH_TYPE = "esri:Path";

  QName POINT = new QName(_NAMESPACE_URI, "Point", _NAMESPACE_PREFIX);

  QName POINT_ARRAY = new QName(_NAMESPACE_URI, "PointArray", _NAMESPACE_PREFIX);

  String POINT_ARRAY_TYPE = "esri:ArrayOfPoint";

  String POINT_N_TYPE = "esri:PointN";

  String POLYGON_N_TYPE = "esri:PolygonN";

  String POLYLINE_N_TYPE = "esri:PolylineN";

  QName PRECISION = new QName(_NAMESPACE_URI, "Precision", _NAMESPACE_PREFIX);

  QName PROJECTED_COORDINATE_SYSTEM = new QName(_NAMESPACE_URI, "ProjectedCoordinateSystem",
    _NAMESPACE_PREFIX);

  String PROJECTED_COORDINATE_SYSTEM_TYPE = "esri:ProjectedCoordinateSystem";

  QName PROPERTY_ARRAY = new QName(_NAMESPACE_URI, "PropertyArray", _NAMESPACE_PREFIX);

  String PROPERTY_ARRAY_TYPE = "esri:ArrayOfPropertySetProperty";

  QName PROPERTY_SET = new QName(_NAMESPACE_URI, "PropertySet", _NAMESPACE_PREFIX);

  QName PROPERTY_SET_PROPERTY = new QName(_NAMESPACE_URI, "PropertySetProperty", _NAMESPACE_PREFIX);

  String PROPERTY_SET_TYPE = "esri:PropertySet";

  QName RASTER_FIELD_NAME = new QName(_NAMESPACE_URI, "RasterFieldName", _NAMESPACE_PREFIX);

  QName RECORD = new QName(_NAMESPACE_URI, "Record", _NAMESPACE_PREFIX);

  String RECORD_TYPE = "esri:Record";

  QName RECORDS = new QName(_NAMESPACE_URI, "Records", _NAMESPACE_PREFIX);

  String RECORDS_TYPE = "esri:ArrayOfRecord";

  QName RELATIONSHIP_CLASS_NAMES = new QName(_NAMESPACE_URI, "RelationshipClassNames",
    _NAMESPACE_PREFIX);

  QName REQUIRED = new QName(_NAMESPACE_URI, "Required", _NAMESPACE_PREFIX);

  QName RING = new QName(_NAMESPACE_URI, "Ring", _NAMESPACE_PREFIX);

  QName RING_ARRAY = new QName(_NAMESPACE_URI, "RingArray", _NAMESPACE_PREFIX);

  String RING_ARRAY_TYPE = "esri:ArrayOfRing";

  String RING_TYPE = "esri:Ring";

  QName SCALE = new QName(_NAMESPACE_URI, "Scale", _NAMESPACE_PREFIX);

  QName SHAPE_FIELD_NAME = new QName(_NAMESPACE_URI, "ShapeFieldName", _NAMESPACE_PREFIX);

  QName SHAPE_TYPE = new QName(_NAMESPACE_URI, "ShapeType", _NAMESPACE_PREFIX);

  QName SPATIAL_REFERENCE = new QName(_NAMESPACE_URI, "SpatialReference", _NAMESPACE_PREFIX);

  QName SPLIT_POLICY = new QName(_NAMESPACE_URI, "SplitPolicy", _NAMESPACE_PREFIX);

  QName SUBTYPE = new QName(_NAMESPACE_URI, "Subtype", _NAMESPACE_PREFIX);

  QName SUBTYPE_CODE = new QName(_NAMESPACE_URI, "SubtypeCode", _NAMESPACE_PREFIX);

  QName SUBTYPE_FIELD_INFO = new QName(_NAMESPACE_URI, "SubtypeFieldInfo", _NAMESPACE_PREFIX);

  QName SUBTYPE_FIELD_NAME = new QName(_NAMESPACE_URI, "SubtypeFieldName", _NAMESPACE_PREFIX);

  QName SUBTYPE_NAME = new QName(_NAMESPACE_URI, "SubtypePath", _NAMESPACE_PREFIX);

  QName SUBTYPES = new QName(_NAMESPACE_URI, "Subtypes", _NAMESPACE_PREFIX);

  QName TYPE = new QName(_NAMESPACE_URI, "Type", _NAMESPACE_PREFIX);

  QName VALUE = new QName(_NAMESPACE_URI, "Value", _NAMESPACE_PREFIX);

  QName VALUES = new QName(_NAMESPACE_URI, "Values", _NAMESPACE_PREFIX);

  String VALUES_TYPE = "esri:ArrayOfValue";

  QName VERSION = new QName(_NAMESPACE_URI, "Version", _NAMESPACE_PREFIX);

  QName VERSIONED = new QName(_NAMESPACE_URI, "Versioned", _NAMESPACE_PREFIX);

  QName WKID = new QName(_NAMESPACE_URI, "WKID", _NAMESPACE_PREFIX);

  QName WKT = new QName(_NAMESPACE_URI, "WKT", _NAMESPACE_PREFIX);

  QName WORKSPACE = new QName(_NAMESPACE_URI, "Workspace", _NAMESPACE_PREFIX);

  QName WORKSPACE_DATA = new QName(_NAMESPACE_URI, "WorkspaceData", _NAMESPACE_PREFIX);

  String WORKSPACE_DATA_TYPE = "esri:WorkspaceData";

  QName WORKSPACE_DEFINITION = new QName(_NAMESPACE_URI, "WorkspaceDefinition", _NAMESPACE_PREFIX);

  String WORKSPACE_DEFINITION_TYPE = "esri:WorkspaceDefinition";

  QName WORKSPACE_TYPE = new QName(_NAMESPACE_URI, "WorkspaceType", _NAMESPACE_PREFIX);

  QName X = new QName(_NAMESPACE_URI, "X", _NAMESPACE_PREFIX);

  QName X_MAX = new QName(_NAMESPACE_URI, "XMax", _NAMESPACE_PREFIX);

  QName X_MIN = new QName(_NAMESPACE_URI, "XMin", _NAMESPACE_PREFIX);

  QName X_ORIGIN = new QName(_NAMESPACE_URI, "XOrigin", _NAMESPACE_PREFIX);

  QName XML_DOC = new QName(_NAMESPACE_URI, "XmlDoc", _NAMESPACE_PREFIX);

  QName XML_PROPERTY_SET = new QName(_NAMESPACE_URI, "XmlPropertySet", _NAMESPACE_PREFIX);

  String XML_PROPERTY_SET_TYPE = "esri:XmlPropertySet";

  Collection<DataType> XML_SCHEMA_DATA_TYPES = Arrays.asList(DataTypes.ANY_URI,
    DataTypes.BASE64_BINARY, DataTypes.BOOLEAN, DataTypes.BYTE, DataTypes.SQL_DATE,
    DataTypes.DATE_TIME, DataTypes.DECIMAL, DataTypes.DOUBLE, DataTypes.DURATION, DataTypes.FLOAT,
    DataTypes.INT, DataTypes.BIG_INTEGER, DataTypes.LONG, DataTypes.QNAME, DataTypes.SHORT,
    DataTypes.STRING);

  QName XY_SCALE = new QName(_NAMESPACE_URI, "XYScale", _NAMESPACE_PREFIX);

  QName XY_TOLERANCE = new QName(_NAMESPACE_URI, "XYTolerance", _NAMESPACE_PREFIX);

  QName Y = new QName(_NAMESPACE_URI, "Y", _NAMESPACE_PREFIX);

  QName Y_MAX = new QName(_NAMESPACE_URI, "YMax", _NAMESPACE_PREFIX);

  QName Y_MIN = new QName(_NAMESPACE_URI, "YMin", _NAMESPACE_PREFIX);

  QName Y_ORIGIN = new QName(_NAMESPACE_URI, "YOrigin", _NAMESPACE_PREFIX);

  QName Z = new QName(_NAMESPACE_URI, "Z", _NAMESPACE_PREFIX);

  QName Z_MAX = new QName(_NAMESPACE_URI, "ZMax", _NAMESPACE_PREFIX);

  QName Z_MIN = new QName(_NAMESPACE_URI, "ZMin", _NAMESPACE_PREFIX);

  QName Z_ORIGIN = new QName(_NAMESPACE_URI, "ZOrigin", _NAMESPACE_PREFIX);

  QName Z_SCALE = new QName(_NAMESPACE_URI, "ZScale", _NAMESPACE_PREFIX);

  QName Z_TOLERANCE = new QName(_NAMESPACE_URI, "ZTolerance", _NAMESPACE_PREFIX);

}
