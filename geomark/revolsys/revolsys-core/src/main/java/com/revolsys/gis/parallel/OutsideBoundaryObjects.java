package com.revolsys.gis.parallel;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;

public class OutsideBoundaryObjects {
  private Geometry boundary;

  private Set<Record> objects = new LinkedHashSet<>();

  private Geometry preparedBoundary;

  public boolean addObject(final Record object) {
    return this.objects.add(object);
  }

  public boolean boundaryContains(final Geometry geometry) {
    return geometry == null || this.boundary == null || this.preparedBoundary.contains(geometry);
  }

  public boolean boundaryContains(final Record object) {
    final Geometry geometry = object.getGeometry();
    return boundaryContains(geometry);
  }

  public void clear() {
    this.objects = new LinkedHashSet<>();
  }

  public void expandBoundary(final Geometry geometry) {
    if (this.boundary == null) {
      setBoundary(geometry);
    } else {
      setBoundary(this.boundary.union(geometry));
    }
  }

  public Set<Record> getAndClearObjects() {
    final Set<Record> objects = this.objects;
    Logs.info(this, "Outside boundary objects size=" + this.objects.size());
    clear();
    return objects;
  }

  public Geometry getBoundary() {
    return this.boundary;
  }

  public Set<Record> getObjects() {
    return this.objects;
  }

  public boolean removeObject(final Record object) {
    return this.objects.remove(object);
  }

  public void setBoundary(final Geometry boundary) {
    this.boundary = boundary;
    this.preparedBoundary = boundary.prepare();
  }

  public void setObjects(final Set<Record> objects) {
    this.objects = objects;
  }
}
