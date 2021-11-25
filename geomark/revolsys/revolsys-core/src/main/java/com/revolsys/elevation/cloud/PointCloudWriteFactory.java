package com.revolsys.elevation.cloud;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloudWriter;
import com.revolsys.io.IoFactory;
import com.revolsys.spring.resource.Resource;

public interface PointCloudWriteFactory extends IoFactory {
  LasPointCloudWriter newWriter(PointCloud<?> pointCloud, final Resource resource,
    MapEx properties);

  boolean writePointCloud(PointCloud<?> pointCloud, Resource resource, MapEx properties);
}
