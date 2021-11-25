package com.revolsys.elevation.cloud.las;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudWriteFactory;
import com.revolsys.spring.resource.Resource;

public interface LasPointCloudWriterFactory extends PointCloudWriteFactory {

  default LasPointCloudWriter newWriter(final LasPointCloud pointCloud, final Resource resource,
    final MapEx properties) {
    if (pointCloud == null) {
      return null;
    } else {
      return pointCloud.newWriter(resource, properties);
    }
  }

  @Override
  default LasPointCloudWriter newWriter(final PointCloud<?> pointCloud, final Resource resource,
    final MapEx properties) {
    if (pointCloud instanceof LasPointCloud) {
      final LasPointCloud lasPointCloud = (LasPointCloud)pointCloud;
      return newWriter(lasPointCloud, resource, properties);
    }
    return null;
  }

  @Override
  default boolean writePointCloud(final PointCloud<?> pointCloud, final Resource resource,
    final MapEx properties) {
    try (
      LasPointCloudWriter writer = newWriter(pointCloud, resource, properties)) {
      if (writer == null) {
        return false;
      } else {
        final LasPointCloud lasPointCloud = (LasPointCloud)pointCloud;
        lasPointCloud.forEachPoint(writer::writePoint);
        return true;
      }
    }
  }
}
