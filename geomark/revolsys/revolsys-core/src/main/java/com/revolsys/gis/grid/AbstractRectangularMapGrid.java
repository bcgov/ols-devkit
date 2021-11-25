package com.revolsys.gis.grid;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;

public abstract class AbstractRectangularMapGrid extends BaseObjectWithProperties
  implements RectangularMapGrid {
  private RecordDefinition recordDefinition;

  private String name;

  @Override
  public String getName() {
    if (this.name == null) {
      return getClass().getName();
    } else {
      return this.name;
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      this.recordDefinition = new RecordDefinitionBuilder(this.name)//
        .addField("name", DataTypes.STRING)//
        .addField("formattedName", DataTypes.STRING)//
        .addField("polygon", GeometryDataTypes.POLYGON)//
        .getRecordDefinition();
    }
    return this.recordDefinition;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
