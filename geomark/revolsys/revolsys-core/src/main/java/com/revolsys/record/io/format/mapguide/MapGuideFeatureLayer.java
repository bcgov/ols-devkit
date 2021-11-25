package com.revolsys.record.io.format.mapguide;

import java.util.Map;

import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.webservice.WebServiceFeatureLayer;
import com.revolsys.webservice.WebServiceResource;

public class MapGuideFeatureLayer implements WebServiceFeatureLayer {
  public static MapGuideFeatureLayer getFeatureLayer(final String serverUrl,
    final PathName pathName) {
    final MapGuideWebService webService = new MapGuideWebService(serverUrl);
    return webService.getWebServiceResource(pathName, MapGuideFeatureLayer.class);
  }

  public static MapGuideFeatureLayer getFeatureLayer(final String serverUrl, final String path) {
    final PathName pathName = PathName.newPathName(path);
    return getFeatureLayer(serverUrl, pathName);
  }

  private final MapGuideWebService webService;

  private final FeatureSource featureSource;

  private final RecordDefinition recordDefinition;

  public MapGuideFeatureLayer(final FeatureSource featureSource,
    final RecordDefinition recordDefinition) {
    this.featureSource = featureSource;
    this.webService = featureSource.getWebService();
    this.recordDefinition = recordDefinition;
  }

  public FeatureSource getFeatureSource() {
    return this.featureSource;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getParent() {
    return (R)this.featureSource;
  }

  @Override
  public PathName getPathName() {
    return this.recordDefinition.getPathName();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public RecordReader getRecordReader(final Query query) {
    final RecordFactory<?> recordFactory = query.getRecordFactory();
    final MapEx queryParameters = new LinkedHashMapEx();
    final String resourceId = this.featureSource.getResourceId();
    queryParameters.put("RESOURCEID", resourceId);
    final String name = this.recordDefinition.getName();
    queryParameters.put("CLASSNAME", name);
    int offset = 0;
    int limit = Integer.MAX_VALUE;
    if (query != null) {
      offset = query.getOffset();
      limit = query.getLimit();
    }
    return new MapGuideServerFeatureIterator(this, queryParameters, offset, limit, recordFactory);
  }

  @Override
  public <V extends Record> RecordReader getRecordReader(final RecordFactory<V> recordFactory,
    final BoundingBox boundingBox) {
    // TODO Auto-generated method stub
    return null;
  }

  public Resource getResource(final Map<String, Object> queryParameters) {
    final MapGuideWebService webService = getWebService();
    return webService.getResource("SELECTFEATURES", "text/xml", queryParameters);
  }

  @Override
  public MapGuideWebService getWebService() {
    return this.webService;
  }

  @Override
  public Query newQuery() {
    return new MapGuideFeatureLayerQuery(this);
  }

  @Override
  public String toString() {
    return getName();
  }
}
