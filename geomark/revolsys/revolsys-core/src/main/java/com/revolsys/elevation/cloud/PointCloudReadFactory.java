package com.revolsys.elevation.cloud;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface PointCloudReadFactory extends ReadIoFactory {

  // TODO figure out how to do this with decent memory usage
  // @Override
  // default GriddedElevationModel newGriddedElevationModel(final Resource
  // resource,
  // final Map<String, ? extends Object> properties) {
  // final PointCloud<?> pointCloud = openPointCloud(resource);
  // if (pointCloud == null) {
  // return null;
  // } else {
  // return pointCloud.newGriddedElevationModel(properties);
  // }
  // }

  <P extends Point, PC extends PointCloud<P>> PC newPointCloud(Resource resource, MapEx properties);
}
