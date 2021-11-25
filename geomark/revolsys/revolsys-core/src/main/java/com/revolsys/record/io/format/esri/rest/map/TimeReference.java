package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.properties.BaseObjectWithProperties;

public class TimeReference extends BaseObjectWithProperties {
  private boolean respectsDaylightSaving;

  private String timeZone;

  public String getTimeZone() {
    return this.timeZone;
  }

  public boolean isRespectsDaylightSaving() {
    return this.respectsDaylightSaving;
  }

  public void setRespectsDaylightSaving(final boolean respectsDaylightSaving) {
    this.respectsDaylightSaving = respectsDaylightSaving;
  }

  public void setTimeZone(final String timeZone) {
    this.timeZone = timeZone;
  }

}
