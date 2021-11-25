package com.revolsys.elevation.gridded.img;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.Maps;

class ImgFieldTypeDictionary {

  private final Map<String, List<ImgField>> STANDARD_TYPES = Maps
    .<String, List<ImgField>> buildHash()
    .add("Edsc_Table", Arrays.asList(//
      new ImgField('l', "numrows")//
    )) //
    .add("Edsc_Column", Arrays.asList(//
      new ImgField('l', "numrows"), //
      new ImgField('L', "columnDataPtr"), //
      new ImgField('e', "dataType", Arrays.asList("integer", "real", "complex", "string")), //
      new ImgField('l', "maxNumChars")//
    ))
    .add("Eprj_Size", Arrays.asList(//
      new ImgField('d', "width"), //
      new ImgField('d', "height")//
    ))
    .add("Eprj_Coordinate", Arrays.asList(//
      new ImgField('d', "x"), //
      new ImgField('d', "y") //
    )) //
    .add("Eprj_MapInfo", Arrays.asList(//
      new ImgField('p', 'c', "proName"), //
      ImgField.newPointer('*', "Eprj_Coordinate", "upperLeftCenter"), //
      ImgField.newPointer('*', "Eprj_Coordinate", "lowerRightCenter"), //
      ImgField.newPointer('*', "Eprj_Size", "pixelSize"), //
      new ImgField('p', 'c', "units") //
    )) //
    //
    .add("Eimg_StatisticsParameters830", Arrays.asList(//
      ImgField.newPointer('p', "Emif_String", "LayerNames"), //
      new ImgField('*', 'b', "ExcludedValues"), //
      ImgField.newObject("Emif_String", "AOIname"), //
      new ImgField('l', "SkipFactorX"), //
      new ImgField('l', "SkipFactorY"), //
      ImgField.newPointer('*', "Edsc_BinFunction", "BinFunction") //
    )) //
    .add("Esta_Statistics", Arrays.asList(//
      new ImgField('d', "minimum"), //
      new ImgField('d', "maximum"), //
      new ImgField('d', "mean"), //
      new ImgField('d', "median"), //
      new ImgField('d', "mode"), //
      new ImgField('d', "stddev") //
    )) //
    .add("Edsc_BinFunction", Arrays.asList(//
      new ImgField('l', "numBins"), //
      new ImgField('e', "binFunctionType", "direct", "linear", "logarithmic", "explicit"), //
      new ImgField('d', "minLimit"), //
      new ImgField('d', "maxLimit"), //
      new ImgField('*', 'b', "binLimits") //
    )) //
    .add("Eimg_NonInitializedValue", Arrays.asList( //
      new ImgField('*', 'b', "valueBD") //
    )) //
    .add("Eprj_ProParameters", Arrays.asList(//
      new ImgField('e', "proType", "EPRJ_INTERNAL", "EPRJ_EXTERNAL"), //
      new ImgField('l', "proNumber"), //
      new ImgField('p', 'c', "proExeName"), //
      new ImgField('p', 'c', "proName"), //
      new ImgField('l', "proZone"), //
      new ImgField('p', 'd', "proParams"), //
      ImgField.newPointer('*', "Eprj_Spheroid", "proSpheroid") //
    )) //
    //
    .add("Eprj_Datum", Arrays.asList(//
      new ImgField('p', 'c', "datumname"), //
      new ImgField('e', "type", "EPRJ_DATUM_PARAMETRIC", "EPRJ_DATUM_GRID",
        "EPRJ_DATUM_REGRESSION"), //
      new ImgField('p', 'd', "params"), //
      new ImgField('p', 'c', "gridname") //
    )) //
    //
    .add("Eprj_Spheroid", Arrays.asList(//
      new ImgField('p', "csphereName"), //
      new ImgField('d', "a"), //
      new ImgField('d', "b"), //
      new ImgField('d', "eSquared"), //
      new ImgField('d', "radius") //
    )) //
    .getMap();

  private final List<ImgFieldType> types;

  public ImgFieldTypeDictionary(final List<ImgFieldType> types) {
    this.types = types;
  }

  public ImgFieldType getFieldType(final String name) {
    for (final ImgFieldType type : this.types) {
      if (type.equalsTypeName(name)) {
        return type;
      }
    }
    if (!"root".equals(name)) {
      final List<ImgField> fields = this.STANDARD_TYPES.get(name);
      if (fields != null) {
        final ImgFieldType newType = new ImgFieldType(name, fields);
        this.types.add(newType);
        return newType;
      }
    }
    return null;
  }
}
