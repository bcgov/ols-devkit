/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.record.io.format.shp;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.GeometryDataTypes;

public class ShapefileConstants {
  public static final Map<Integer, DataType> DATA_TYPE_MAP = new HashMap<>();

  public static final String DESCRIPTION = "Shapefile (ESRI)";

  public static final int FILE_CODE = 9994;

  public static final String FILE_EXTENSION = "shp";

  public static final int INNER_RING = 3;

  public static final String MIME_TYPE = "application/x-shp";

  public static final int MULTI_PATCH_SHAPE = 31;

  public static final int MULTI_POINT_M_SHAPE = 28;

  public static final int MULTI_POINT_SHAPE = 8;

  public static final int MULTI_POINT_Z_SHAPE = 20;

  public static final int MULTI_POINT_ZM_SHAPE = 18;

  public static final int NULL_SHAPE = 0;

  public static final int OUTER_RING = 2;

  public static final int POINT_M_SHAPE = 21;

  public static final int POINT_SHAPE = 1;

  /** Only used for FGDB. */
  public static final int POINT_Z_SHAPE = 9;

  public static final int POINT_ZM_SHAPE = 11;

  public static final int POLYGON_M_SHAPE = 25;

  public static final int POLYGON_SHAPE = 5;

  public static final int POLYGON_Z_SHAPE = 19;

  public static final int POLYGON_ZM_SHAPE = 15;

  public static final int POLYLINE_M_SHAPE = 23;

  public static final int POLYLINE_SHAPE = 3;

  public static final int POLYLINE_Z_SHAPE = 10;

  public static final int POLYLINE_ZM_SHAPE = 13;

  public static final int UNKNOWN_SHAPE = -1;

  public static final int VERSION = 1000;

  static {
    DATA_TYPE_MAP.put(POINT_M_SHAPE, GeometryDataTypes.POINT);
    DATA_TYPE_MAP.put(POINT_Z_SHAPE, GeometryDataTypes.POINT);
    DATA_TYPE_MAP.put(POINT_SHAPE, GeometryDataTypes.POINT);
    DATA_TYPE_MAP.put(POLYLINE_M_SHAPE, GeometryDataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put(POLYLINE_ZM_SHAPE, GeometryDataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put(POLYLINE_SHAPE, GeometryDataTypes.MULTI_LINE_STRING);
    DATA_TYPE_MAP.put(POLYGON_M_SHAPE, GeometryDataTypes.POLYGON);
    DATA_TYPE_MAP.put(POLYGON_ZM_SHAPE, GeometryDataTypes.POLYGON);
    DATA_TYPE_MAP.put(POLYGON_SHAPE, GeometryDataTypes.POLYGON);
    DATA_TYPE_MAP.put(MULTI_POINT_M_SHAPE, GeometryDataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put(MULTI_POINT_ZM_SHAPE, GeometryDataTypes.MULTI_POINT);
    DATA_TYPE_MAP.put(MULTI_POINT_SHAPE, GeometryDataTypes.MULTI_POINT);
  }

}
