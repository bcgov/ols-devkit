package com.revolsys.elevation.cloud.las.zip;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudWriterFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.spring.resource.Resource;

public class LasZipPointCloudFactory extends AbstractIoFactory
  implements PointCloudReadFactory, LasPointCloudWriterFactory {

  public LasZipPointCloudFactory() {
    super("LASzip Point Cloud");
    addMediaTypeAndFileExtension("application/vnd.laz", "laz");
    addFileExtension("laz.gz");
    addFileExtension("laz.zip");
  }

  @SuppressWarnings("unchecked")
  @Override
  public <P extends Point, PC extends PointCloud<P>> PC newPointCloud(final Resource resource,
    final MapEx properties) {
    final LasPointCloud pointCloud = new LasPointCloud(resource, properties);
    if (pointCloud.isExists()) {
      return (PC)pointCloud;
    } else {
      return null;
    }
  }
}
