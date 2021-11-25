package com.revolsys.record.io.format.moep;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public final class MoepConstants {
  public static final String ADMIT_INTEGRATION_DATE = "A_DATE";

  public static final String ADMIT_REASON_FOR_CHANGE = "A_REASON";

  public static final String ADMIT_REVISION_KEY = "A_REV_KEY";

  public static final String ADMIT_SOURCE_DATE = "A_SRC_DATE";

  public static final String ADMIT_SPECIFICATIONS_RELEASE = "A_SPEC";

  public static final String ANGLE = "ANGLE";

  public static final String DISPLAY_TYPE = "DISP_TYPE";

  public static final String ELEVATION = "ELEVATION";

  public static final String FEATURE_CODE = "FEAT_CODE";

  public static final String FONT_NAME = "FONT_NAME";

  public static final String FONT_SIZE = "FONT_SIZE";

  public static final String FONT_WEIGHT = "FONTWEIGHT";

  public static final String GEOMETRY = "geometry";

  public static final String MAPSHEET_NAME = "MAPSHEET";

  public static final String ORIGINAL_FILE_TYPE = "FILE_TYPE";

  public static final RecordDefinition RECORD_DEFINITION;

  public static final String RETIRE_INTEGRATION_DATE = "R_DATE";

  public static final String RETIRE_REASON_FOR_CHANGE = "R_REASON";

  public static final String RETIRE_REVISION_KEY = "R_REV_KEY";

  public static final String RETIRE_SOURCE_DATE = "R_SRC_DATE";

  public static final String RETIRE_SPECIFICATIONS_RELEASE = "R_SPEC";

  public static final String TEXT = "TEXT";

  public static final String TEXT_GROUP = "TEXT_GROUP";

  public static final String TEXT_INDEX = "TEXT_INDEX";

  public static final String TYPE_NAME = "/MOEP/Feature";

  static {
    RECORD_DEFINITION = newRecordDefinition(TYPE_NAME);
  }

  public static RecordDefinitionImpl newRecordDefinition(final String typePath) {
    final RecordDefinitionImpl type = new RecordDefinitionImpl(PathName.newPathName(typePath));
    type.addField(FEATURE_CODE, DataTypes.STRING, 10, true);
    type.addField(MAPSHEET_NAME, DataTypes.STRING, 7, false);
    type.addField(DISPLAY_TYPE, DataTypes.STRING, 20, true);
    type.addField(ANGLE, DataTypes.DECIMAL, false);
    type.addField(ELEVATION, DataTypes.DECIMAL, false);
    type.addField(TEXT_GROUP, DataTypes.DECIMAL, false);
    type.addField(TEXT_INDEX, DataTypes.DECIMAL, false);
    type.addField(TEXT, DataTypes.STRING, 200, false);
    type.addField(FONT_NAME, DataTypes.STRING, 10, false);
    type.addField(FONT_SIZE, DataTypes.DECIMAL, false);
    type.addField(FONT_WEIGHT, DataTypes.STRING, 10, false);
    type.addField(ORIGINAL_FILE_TYPE, DataTypes.STRING, 20, false);
    type.addField(ADMIT_SOURCE_DATE, DataTypes.SQL_DATE, false);
    type.addField(ADMIT_REASON_FOR_CHANGE, DataTypes.STRING, 1, false);
    type.addField(ADMIT_INTEGRATION_DATE, DataTypes.SQL_DATE, false);
    type.addField(ADMIT_REVISION_KEY, DataTypes.STRING, 10, false);
    type.addField(ADMIT_SPECIFICATIONS_RELEASE, DataTypes.STRING, 10, false);
    type.addField(RETIRE_SOURCE_DATE, DataTypes.SQL_DATE, false);
    type.addField(RETIRE_REASON_FOR_CHANGE, DataTypes.STRING, 1, false);
    type.addField(RETIRE_INTEGRATION_DATE, DataTypes.SQL_DATE, false);
    type.addField(RETIRE_REVISION_KEY, DataTypes.STRING, 10, false);
    type.addField(RETIRE_SPECIFICATIONS_RELEASE, DataTypes.STRING, 10, false);
    type.addField(GEOMETRY, GeometryDataTypes.GEOMETRY, true);
    type.setGeometryFieldName(GEOMETRY);
    return type;
  }

  private MoepConstants() {
  }
}
