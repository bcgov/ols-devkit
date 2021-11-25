package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.util.Property;

public class RectangularMapGridFactory {
  public static final Map<String, String> gridClassNamesByName = new LinkedHashMap<>();

  public static final List<String> gridNames;

  static {
    addGrid("NTS 1:1 000 000", Nts1000000RectangularMapGrid.class);
    addGrid("NTS 1:500 000", Nts500000RectangularMapGrid.class);
    addGrid("NTS 1:250 000", Nts250000RectangularMapGrid.class);
    addGrid("NTS 1:125 000", Nts125000RectangularMapGrid.class);
    addGrid("NTS 1:50 000", Nts50000RectangularMapGrid.class);
    addGrid("NTS 1:25 000", Nts25000RectangularMapGrid.class);
    addGrid("BCGS 1:20 000", Bcgs20000RectangularMapGrid.class);
    addGrid("BCGS 1:10 000", Bcgs10000RectangularMapGrid.class);
    addGrid("BCGS 1:5 000", Bcgs5000RectangularMapGrid.class);
    addGrid("BCGS 1:2 500", Bcgs2500RectangularMapGrid.class);
    addGrid("BCGS 1:2 000", Bcgs2000RectangularMapGrid.class);
    addGrid("BCGS 1:1 250", Bcgs1250RectangularMapGrid.class);
    addGrid("BCGS 1:1 000", Bcgs1000RectangularMapGrid.class);
    addGrid("BCGS 1:500", Bcgs500RectangularMapGrid.class);
    addGrid("MTO", MtoRectangularMapGrid.class);
    gridNames = Collections.unmodifiableList(new ArrayList<>(gridClassNamesByName.keySet()));
  }

  private static void addGrid(final String name,
    final Class<? extends RectangularMapGrid> gridClass) {
    final String className = gridClass.getName();
    gridClassNamesByName.put(name, className);

  }

  public static RectangularMapGrid getGrid(final String name) {
    try {
      final String className = gridClassNamesByName.get(name);
      if (Property.hasValue(className)) {
        return (RectangularMapGrid)Class.forName(className).newInstance();
      }
    } catch (final Throwable e) {
      Logs.error(RectangularMapGridFactory.class, "Unable to create grid for " + name, e);
    }
    return null;
  }

  public static RectangularMapGrid getGrid(final String name, final int inverseScale) {
    if (name.equals("NTS")) {
      switch (inverseScale) {
        case 1000000:
          return new Nts1000000RectangularMapGrid();
        case 500000:
          return new Nts500000RectangularMapGrid();
        case 250000:
          return new Nts250000RectangularMapGrid();
        case 125000:
          return new Nts125000RectangularMapGrid();
        case 50000:
          return new Nts50000RectangularMapGrid();
        case 25000:
          return new Nts25000RectangularMapGrid();
        default:
          return null;
      }
    } else if (name.equals("BCGS")) {
      switch (inverseScale) {
        case 20000:
          return new Bcgs20000RectangularMapGrid();
        case 10000:
          return new Bcgs10000RectangularMapGrid();
        case 5000:
          return new Bcgs5000RectangularMapGrid();
        case 2500:
          return new Bcgs2500RectangularMapGrid();
        case 2000:
          return new Bcgs2000RectangularMapGrid();
        case 1250:
          return new Bcgs1250RectangularMapGrid();
        case 1000:
          return new Bcgs1000RectangularMapGrid();
        case 500:
          return new Bcgs500RectangularMapGrid();
        default:
          return null;
      }
    } else if (name.equals("MTO")) {
      switch (inverseScale) {
        case 0:
          return new MtoRectangularMapGrid();
        default:
          return null;
      }
    }
    return null;
  }

  public static Collection<String> getGridNames() {
    return gridNames;
  }
}
