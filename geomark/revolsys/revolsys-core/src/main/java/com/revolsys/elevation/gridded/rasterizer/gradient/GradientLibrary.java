package com.revolsys.elevation.gridded.rasterizer.gradient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;

public class GradientLibrary {

  private static GradientLibrary defaultLibrary;

  public static MultiStopLinearGradient getDefaultGradient(final String gradientId) {
    final GradientLibrary library = getDefaultLibrary();
    return library.getGradient(gradientId);
  }

  public static GradientLibrary getDefaultLibrary() {
    if (defaultLibrary == null) {
      final Resource resource = Resource
        .getResource("classpath:com/revolsys/elevation/gridded/rasterizer/gradient/");
      defaultLibrary = new GradientLibrary(resource);
    }
    return defaultLibrary;
  }

  private final List<MultiStopLinearGradient> gradients = new ArrayList<>();

  private final Map<String, MultiStopLinearGradient> gradientById = new HashMap<>();

  public GradientLibrary(final Resource baseResource) {
    final Resource configResource = baseResource.newChildResource("gradient.json");
    final MapEx config = Json.toMap(configResource);
    final List<String> gradientIds = config.getValue("gradients", Collections.emptyList());
    for (final String gradientId : gradientIds) {
      final Resource gradientResource = baseResource.newChildResource(gradientId + ".pg");
      final MultiStopLinearGradient gradient = new MultiStopLinearGradient(gradientResource);
      this.gradients.add(gradient);
      this.gradientById.put(gradientId, gradient);
    }
  }

  public MultiStopLinearGradient getGradient(final String gradientId) {
    return this.gradientById.get(gradientId);
  }
}
