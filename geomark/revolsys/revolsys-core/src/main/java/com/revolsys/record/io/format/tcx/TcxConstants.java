package com.revolsys.record.io.format.tcx;

import javax.xml.namespace.QName;

import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.GeometryFactory;

public interface TcxConstants {

  String _ACTIVITY_EXTENSION_NS_URI = "http://www.garmin.com/xmlschemas/ActivityExtension/v2";

  String _NS_URI = "http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2";

  QName ACTIVITY = new QName(_NS_URI, "Activity");

  QName ALTITUDE_METERS = new QName(_NS_URI, "AltitudeMeters");

  QName AVERAGE_HEART_RATE_BPM = new QName(_NS_URI, "AverageHeartRateBpm");

  QName CADENCE = new QName(_NS_URI, "Cadence");

  QName CALORIES = new QName(_NS_URI, "Calories");

  QName DISTANCE_METERS = new QName(_NS_URI, "DistanceMeters");

  QName EXTENSIONS = new QName(_NS_URI, "Extensions");

  GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3d(EpsgId.WGS84);

  QName HEART_RATE_BPM = new QName(_NS_URI, "HeartRateBpm");

  QName INTENSITY = new QName(_NS_URI, "Intensity");

  QName LAP = new QName(_NS_URI, "Lap");

  QName LATITUDE_DEGREES = new QName(_NS_URI, "LatitudeDegrees");

  QName LONGITUDE_DEGREES = new QName(_NS_URI, "LongitudeDegrees");

  QName MAXIMUM_HEART_RATE_BPM = new QName(_NS_URI, "MaximumHeartRateBpm");

  QName MAXIMUM_SPEED = new QName(_NS_URI, "MaximumSpeed");

  QName NOTES = new QName(_NS_URI, "Notes");

  String NS = "tcx";

  QName POSITION = new QName(_NS_URI, "Position");

  QName SPEED = new QName(_ACTIVITY_EXTENSION_NS_URI, "Speed");

  QName TIME = new QName(_NS_URI, "Time");

  QName TOTAL_TIME_SECONDS = new QName(_NS_URI, "TotalTimeSeconds");

  QName TPX = new QName(_ACTIVITY_EXTENSION_NS_URI, "TPX");

  QName TRACK = new QName(_NS_URI, "Track");

  QName TRACKPOINT = new QName(_NS_URI, "Trackpoint");

  QName TRAINING_CENTER_DATABASE = new QName(_NS_URI, "TrainingCenterDatabase");

  QName TRIGGER_METHOD = new QName(_NS_URI, "TriggerMethod");

  QName VALUE = new QName(_NS_URI, "Value");

  QName WATTS = new QName(_ACTIVITY_EXTENSION_NS_URI, "Watts");
}
