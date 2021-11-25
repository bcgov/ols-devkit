package org.jeometry.coordinatesystem.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.data.identifier.Code;
import org.jeometry.coordinatesystem.io.EpsgCsWktWriter;
import org.jeometry.coordinatesystem.io.EsriCsWktWriter;
import org.jeometry.coordinatesystem.model.systems.EsriCoordinateSystems;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.util.Md5;

public interface CoordinateSystem extends HorizontalCoordinateSystemProxy, Code {

  CoordinateSystem clone();

  boolean equalsExact(CoordinateSystem coordinateSystem);

  Area getArea();

  Authority getAuthority();

  List<Axis> getAxis();

  @SuppressWarnings("unchecked")
  @Override
  default <C> C getCode() {
    final Integer id = getCoordinateSystemId();
    return (C)id;
  }

  CoordinatesOperation getCoordinatesOperation(CoordinateSystem coordinateSystem);

  @SuppressWarnings("unchecked")
  @Override
  default <C extends CoordinateSystem> C getCoordinateSystem() {
    return (C)this;
  }

  CoordinateSystemType getCoordinateSystemType();

  @Override
  default String getDescription() {
    return getCoordinateSystemName();
  }

  @Override
  default <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return null;
  }

  @Override
  default Integer getInteger(final int index) {
    if (index == 0) {
      return getCoordinateSystemId();
    } else {
      throw new ArrayIndexOutOfBoundsException(index);
    }
  }

  Unit<Length> getLengthUnit();

  LinearUnit getLinearUnit();

  <Q extends Quantity<Q>> Unit<Q> getUnit();

  boolean isDeprecated();

  boolean isSame(CoordinateSystem coordinateSystem);

  default byte[] md5Digest() {
    final MessageDigest digest = Md5.getMessageDigest();
    updateDigest(digest);
    return digest.digest();
  }

  default String toEpsgWkt() {
    try (
      StringWriter stringWriter = new StringWriter()) {
      final PrintWriter out = new PrintWriter(stringWriter);
      EpsgCsWktWriter.write(out, this);
      return stringWriter.toString();
    } catch (final IOException e) {
      return null;
    }
  }

  default String toEsriWktCs() {
    try (
      StringWriter stringWriter = new StringWriter()) {
      writeEsriWktCs(stringWriter, -1);
      return stringWriter.toString();
    } catch (final IOException e) {
      return null;
    }
  }

  default String toEsriWktCsFormatted() {
    try (
      StringWriter stringWriter = new StringWriter()) {
      writeEsriWktCs(stringWriter, 0);
      return stringWriter.toString();
    } catch (final IOException e) {
      return null;
    }
  }

  default void updateDigest(final MessageDigest digest) {
    Md5.update(digest, toString());
  }

  default boolean writeEsriWktCs(final Writer writer, final int indentLevel) {
    final int coordinateSystemId = getCoordinateSystemId();
    final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems
      .getCoordinateSystem(coordinateSystemId);
    if (esriCoordinateSystem == null) {
      EsriCsWktWriter.write(writer, this, indentLevel);
    } else {
      EsriCsWktWriter.write(writer, esriCoordinateSystem, indentLevel);
    }
    return true;
  }
}
