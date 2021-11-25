package ca.bc.gov.geomark.web.domain;

import java.util.Date;

import org.jeometry.common.date.Dates;
import org.jeometry.common.io.PathName;

import com.revolsys.record.Record;

/**
 * @author Paul Austin
 */
public interface GeomarkConstants {
  /** Type name for the config property table. */
  PathName CONFIG_PROPERTY = PathName.newPathName("/GEOMARK/GMK_CONFIG_PROPERTIES");

  /** Primary key column name for the config property table. */
  String CONFIG_PROPERTY_ID = "CONFIG_PROPERTY_ID";

  /** Description column name. */
  String DESCRIPTION = "DESCRIPTION";

  /** The SRID for WSG84 geographics (4326). */
  int EPSG_4326 = 4326;

  /** Expiry date for a geomark column name. */
  String EXPIRY_DATE = "EXPIRY_DATE";

  /** Type name for the geomark group table. */
  PathName GEOMARK_GROUP = PathName.newPathName("/GEOMARK/GMK_GEOMARK_GROUPS");

  /** Primary key column name for the geomark group table. */
  String GEOMARK_GROUP_ID = "GEOMARK_GROUP_ID";

  /** Type name for the geomark group xref table. */
  PathName GEOMARK_GROUP_XREF = PathName.newPathName("/GEOMARK/GMK_GEOMARK_GROUP_XREF");

  /** Primary key column name for the geomark table. */
  String GEOMARK_ID = "GEOMARK_ID";

  /** Type name for the geomark table. */
  PathName GEOMARK_POLY = PathName.newPathName("/GEOMARK/GMK_GEOMARK_POLY");

  /** Minimum expiry date for a geomark column name. */
  String MIN_EXPIRY_DATE = "MIN_EXPIRY_DATE";

  /** Config property name column name. */
  String PROPERTY_NAME = "PROPERTY_NAME";

  /** Config property value column name. */
  String PROPERTY_VALUE = "VALUE";

  /** Secret key column name. */
  String SECRET_KEY = "SECRET_KEY";

  /** When created column name. */
  String WHEN_CREATED = "WHEN_CREATED";

  Date MAX_DATE = Dates.getSqlDate("9999-12-31");

  static boolean isExpired(final Record geomark) {
    if (geomark == null) {
      return true;
    } else {
      final Date date = new Date(System.currentTimeMillis());
      final java.util.Date expiryDate = geomark.getValue(EXPIRY_DATE);
      final boolean expired = expiryDate.compareTo(date) < 0;
      return expired;
    }
  }

}
