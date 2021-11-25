package org.jeometry.coordinatesystem.model;

import java.util.Map;

public class CoordinateOperation {
  private final int id;

  private final CoordinateOperationMethod method;

  private final String name;

  private final byte type;

  private final int sourceCrsCode;

  private final int targetCrsCode;

  private final String transformationVersion;

  private final int variant;

  private final Area area;

  private final double accuracy;

  private final boolean deprecated;

  private final Map<ParameterName, ParameterValue> parameterValues;

  public CoordinateOperation(final int id, final CoordinateOperationMethod method,
    final String name, final byte type, final int sourceCrsCode, final int targetCrsCode,
    final String transformationVersion, final int variant, final Area area, final double accuracy,
    final Map<ParameterName, ParameterValue> parameterValues, final boolean deprecated) {
    this.id = id;
    this.method = method;
    this.name = name;
    this.type = type;
    this.sourceCrsCode = sourceCrsCode;
    this.targetCrsCode = targetCrsCode;
    this.transformationVersion = transformationVersion;
    this.variant = variant;
    this.area = area;
    this.accuracy = accuracy;
    this.parameterValues = parameterValues;
    this.deprecated = deprecated;
  }

  public double getAccuracy() {
    return this.accuracy;
  }

  public Area getArea() {
    return this.area;
  }

  public int getId() {
    return this.id;
  }

  public CoordinateOperationMethod getMethod() {
    return this.method;
  }

  public String getName() {
    return this.name;
  }

  public Map<ParameterName, ParameterValue> getParameterValues() {
    return this.parameterValues;
  }

  public int getSourceCrsCode() {
    return this.sourceCrsCode;
  }

  public int getTargetCrsCode() {
    return this.targetCrsCode;
  }

  public String getTransformationVersion() {
    return this.transformationVersion;
  }

  public byte getType() {
    return this.type;
  }

  public int getVariant() {
    return this.variant;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
