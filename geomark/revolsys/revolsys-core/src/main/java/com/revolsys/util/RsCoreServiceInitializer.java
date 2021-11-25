package com.revolsys.util;

import java.util.Map;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.data.type.ObjectDataType;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.CustomRectangularMapGrid;
import com.revolsys.gis.wms.WmsClient;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.raster.commonsimaging.CommonsImagingServiceInitializer;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.mapguide.MapGuideWebService;
import com.revolsys.record.io.format.scaledint.ScaledIntegerPointCloud;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStore;

public class RsCoreServiceInitializer implements ServiceInitializer {
  @Override
  public void initializeService() {
    DataTypes.registerDataTypes(RsCoreDataTypes.class);
    DataTypes.registerDataTypes(GeometryDataTypes.class);
    ObjectDataType.setToObjectFunction(value -> {
      if (value instanceof Map<?, ?>) {
        @SuppressWarnings("unchecked")
        final Map<String, ? extends Object> map = (Map<String, ? extends Object>)value;
        final String type = MapObjectFactory.getType(map);
        if (type != null) {
          final Object object = MapObjectFactory.toObject(map);
          if (object != null) {
            return object;
          }
        }
      }
      return value;
    });

    MapObjectFactoryRegistry.newFactory("geometryFactory", "Geometry Factory", config -> {
      return GeometryFactory.newGeometryFactory(config);
    });

    MapObjectFactoryRegistry.newFactory("field", "Data Record Field", config -> {
      return FieldDefinition.newFieldDefinition(config);
    });

    MapObjectFactoryRegistry.newFactory("recordDefinition", "Data Record Definition",
      config -> RecordDefinitionImpl.newRecordDefinition(config));

    MapObjectFactoryRegistry.newFactory("recordStore", "Record Store",
      config -> RecordStore.newRecordStoreInitialized(config));

    MapObjectFactoryRegistry.newFactory("codeTable", "Code Table",
      config -> CodeTable.newCodeTable(config));

    MapObjectFactoryRegistry.newFactory("recordReaderFactoryFile",
      "Factory to create a RecordReader from a file", config -> {
        return RecordReaderFactory.newRecordReaderSupplier(config);
      });

    MapObjectFactoryRegistry.newFactory("customRectangularMapGrid", "Custom Rectangular Map Grid",
      (config) -> {
        return new CustomRectangularMapGrid(config);
      });

    MapObjectFactoryRegistry.newFactory("arcGisRestServer", "Arc GIS REST Server", (config) -> {
      return ArcGisRestCatalog.newArcGisRestCatalog(config);
    });

    MapObjectFactoryRegistry.newFactory("arcGisRestServerLayerRecordReaderFactory",
      "Factory to create a RecordReader from a ArcGis Server later", (config) -> {
        return ArcGisRestCatalog.newRecordReaderFactory(config);
      });

    MapObjectFactoryRegistry.newFactory("mapGuideWebServer", "Map Guide Web Server", (config) -> {
      return MapGuideWebService.newMapGuideWebService(config);
    });

    MapObjectFactoryRegistry.newFactory("ogcWmsServer", "OGC WMS Server", (config) -> {
      return WmsClient.newOgcWmsClient(config);
    });

    ioFactory();
  }

  private void ioFactory() {
    IoFactoryRegistry.addFactory(new ScaledIntegerPointCloud());
    GriddedElevationModel.serviceInit();
    TriangulatedIrregularNetwork.serviceInit();
    CommonsImagingServiceInitializer.serviceInit();
  }

  @Override
  public int priority() {
    return 0;
  }
}
