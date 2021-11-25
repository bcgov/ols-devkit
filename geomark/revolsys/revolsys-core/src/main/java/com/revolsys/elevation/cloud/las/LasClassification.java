package com.revolsys.elevation.cloud.las;

import java.util.Map;

import com.revolsys.collection.map.Maps;

public interface LasClassification {

  short BUILDING = (short)6;

  short DEFAULT = (short)0;

  short GROUND = (short)2;

  short HIGH_VEGITATION = (short)5;

  short LOW_POINT_NOISE = (short)7;

  short LOW_VEGITATION = (short)3;

  short MEDIUM_VEGITATION = (short)4;

  short MODEL_KEY_POINT = (short)8;

  short OVERLAP_POINTS = (short)12;

  short UNCLASSIFIED = (short)1;

  short WATER = (short)9;

  Map<Short, String> CLASSIFICATIONS = Maps.<Short, String> buildLinkedHash()
    .add(DEFAULT, "Created, never classified") //
    .add(UNCLASSIFIED, "Unclassified") //
    .add(GROUND, "Ground") //
    .add(LOW_VEGITATION, "Low Vegitation") //
    .add(MEDIUM_VEGITATION, "Medium Vegitation") //
    .add(HIGH_VEGITATION, "High Vegitation") //
    .add(BUILDING, "Building") //
    .add(LOW_POINT_NOISE, "Low Point (noise)") //
    .add(MODEL_KEY_POINT, "Model Key-point (mass point)") //
    .add(WATER, "Water") //
    .add(OVERLAP_POINTS, "Overlap Points") //
    .getMap();

}
