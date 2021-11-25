package ca.bc.gov.geomark.web.servlet.admin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.date.Dates;
import org.jeometry.common.logging.Logs;

import ca.bc.gov.geomark.web.domain.GeomarkConfig;
import ca.bc.gov.geomark.web.domain.GeomarkConstants;
import ca.bc.gov.geomark.web.servlet.BaseServlet;

import com.revolsys.jdbc.io.JdbcRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Property;

@WebServlet(urlPatterns = "/secure/admin/api/geomarks/*", loadOnStartup = 1)
public class AdminGeomarkServlet extends BaseServlet {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private void addSearchFilter(final HttpServletRequest request, final Query query) {
    String search = request.getParameter("searchText");
    if (Property.hasValue(search)) {
      search = "%" + search.toLowerCase() + "%";
      query.and(Q.iLike(GEOMARK_ID, search));
    }
  }

  /**
   * Delete all the geomarks which are older than the  GeomarkConfig maxGeomarkAgeDays
   * and do not have a subscription.
   */
  public void deleteExpiredGeomarks() {
    final Calendar yearExpiryCalendar = new GregorianCalendar(TimeZone.getTimeZone("PST"));
    yearExpiryCalendar.add(Calendar.YEAR, -1);
    yearExpiryCalendar.add(Calendar.MONTH, -1);
    yearExpiryCalendar.set(Calendar.DAY_OF_MONTH, 1);

    final Date yearExpiryDate = new java.sql.Date(yearExpiryCalendar.getTimeInMillis());
    if (this.recordStore instanceof JdbcRecordStore) {
      final JdbcRecordStore jdbcRecordStore = (JdbcRecordStore)this.recordStore;
      try (
        Transaction transaction = jdbcRecordStore.newTransaction()) {
        final int deleteCount = this.recordStore
          .deleteRecords(new Query(GEOMARK_POLY, Q.lessThan(EXPIRY_DATE, yearExpiryDate)));
        if (deleteCount == 0) {
          Logs.info(this, "No expired geomarks to delete");
        } else {
          Logs.info(this,
            "DELETED " + deleteCount + " expired geomarks that were expired more than a year ago");
        }

        final Date today = Dates.getSqlDate();
        final int updateCount = jdbcRecordStore.executeUpdate(
          "UPDATE GEOMARK.GMK_GEOMARK_POLY SET GEOMETRY = NULL WHERE EXPIRY_DATE < ? AND GEOMETRY IS NOT NULL",
          today);
        if (updateCount == 0) {
          Logs.info(this, "No geomarks to mark as expired");
        } else {
          Logs.info(this, "Updated " + updateCount + " geomarks to be expired");
        }
      } catch (final Throwable e) {
        Logs.error("Unable to clean expired Geomarks", e);
      }
    }
  }

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final String[] parts = pathParts(request);
    if (parts.length == 3) {
      final String geomarkId = parts[0];
      final String geomarkGroupId = parts[2];
      if (geomarkId.startsWith("gm-") && parts[1].equals("groups")
        && geomarkGroupId.startsWith("gg-")) {
        int expiryDays = -1;
        final int maxExpiryDays = GeomarkConfig.getConfig().getInt("maxGeomarkAgeDays");
        final Condition filter = Q.and(Q.equal(GeomarkConstants.GEOMARK_ID, geomarkId),
          Q.equal(GeomarkConstants.GEOMARK_GROUP_ID, geomarkGroupId));
        final Query query = new Query(GeomarkConstants.GEOMARK_GROUP_XREF, filter);
        this.recordStore.deleteRecords(query);
        final Record geomark = this.recordStore.getRecord(GeomarkConstants.GEOMARK_POLY, geomarkId);
        if (geomark != null) {
          java.util.Date minExpiry = geomark.getValue(GeomarkConstants.MIN_EXPIRY_DATE);
          if (expiryDays > 0) {
            expiryDays = Math.min(maxExpiryDays, expiryDays);
            final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("PST"));
            cal.add(Calendar.DATE, expiryDays);
            final java.sql.Date newMinExpiry = new java.sql.Date(cal.getTimeInMillis());
            final int compare = newMinExpiry.compareTo(minExpiry);
            if (compare > 0) {
              minExpiry = newMinExpiry;
            }
          }
          geomark.setValue(GeomarkConstants.MIN_EXPIRY_DATE, minExpiry);
          if (!isInAGeomarkGroup(this.recordStore, geomarkId)) {
            geomark.setValue(GeomarkConstants.EXPIRY_DATE, minExpiry);
          }
          this.recordStore.updateRecord(geomark);
        }
        return;
      }
    }
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String[] parts = pathParts(request);
    if (parts.length == 0) {
      doGetList(request, response);
    } else if (parts.length == 1) {
      final String page = parts[0];
      if (page.equals("expired")) {
        doGetExpired(request, response);
      } else if (page.equals("temporary")) {
        doGetTemporary(request, response);
      } else if (!page.startsWith("gm-")) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      } else {
        doGetGeomarkView(response, page);
      }
    } else if (!parts[0].startsWith("gm-")) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else if (parts.length == 2) {
      final String geomarkId = parts[0];
      if (parts[1].equals("groups")) {
        doGetGroups(request, response, geomarkId);
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void doGetExpired(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    final List<List<Object>> sorting = Collections.singletonList(Arrays.<Object> asList(2, "desc"));
    parameters.put("order", sorting);

    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(GEOMARK_POLY);
    final Query query = new Query(recordDefinition);
    query.select(GEOMARK_ID, WHEN_CREATED, EXPIRY_DATE);
    final java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
    query.setWhereCondition(Q.lessThan(EXPIRY_DATE, today));

    addSearchFilter(request, query);
    writeJsonRecordsPage(request, response, query);
  }

  private void doGetGeomarkView(final HttpServletResponse response, final String geomarkId)
    throws IOException, ServletException {
    final Record geomark = this.recordStore.getRecord(GEOMARK_POLY, geomarkId);
    if (GeomarkConstants.isExpired(geomark)) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else {
      writeJsonRecord(response, geomark);
    }
  }

  private void doGetGroups(final HttpServletRequest request, final HttpServletResponse response,
    final String geomarkId) throws IOException {
    final RecordDefinition geomarkGroupXref = this.recordStore
      .getRecordDefinition(GeomarkConstants.GEOMARK_GROUP_XREF);
    final Query query = this.recordStore.newQuery(GeomarkConstants.GEOMARK_GROUP)
      .select(GEOMARK_GROUP_ID, WHEN_CREATED, DESCRIPTION)
      .and(geomarkGroupXref.equal("GEOMARK_ID", geomarkId))
      .orderBy(DESCRIPTION);
    query.join(geomarkGroupXref).on("GEOMARK_GROUP_ID", query);

    final String search = request.getParameter("searchTerm");
    if (Property.hasValue(search)) {
      final String likeName = "%" + search + "%";
      query.and(new Or(query.newCondition("GEOMARK_GROUP_ID", Q::like, likeName),
        query.newCondition("DESCRIPTION", Q::like, likeName)));
    }

    writeJsonRecordsPage(request, response, query);
  }

  private void doGetList(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(GEOMARK_POLY);
    final Query query = new Query(recordDefinition);
    query.select(GEOMARK_ID, WHEN_CREATED, EXPIRY_DATE);
    final Date today = Dates.getSqlDate();
    query.setWhereCondition(Q.greaterThanEqual(EXPIRY_DATE, today));

    addSearchFilter(request, query);
    writeJsonRecordsPage(request, response, query);
  }

  /**
   * The admin list geomark groups for geomark page.
   *
   * @param geomarkId The geomark identifier.
   * @return The page view.
   * @throws IOException If an I/O exception occurred.
   */
  private void doGetTemporary(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(GEOMARK_POLY);
    final Query query = new Query(recordDefinition);
    query.select(GEOMARK_ID, WHEN_CREATED, EXPIRY_DATE);
    final Date today = Dates.getSqlDate();
    query.setWhereCondition(
      Q.and(Q.greaterThanEqual(EXPIRY_DATE, today), Q.lessThan(EXPIRY_DATE, MAX_DATE)));

    addSearchFilter(request, query);

    writeJsonRecordsPage(request, response, query);
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    if ("DELETE".equals(request.getHeader("X-HTTP-Method-Override"))) {
      doDelete(request, response);
      return;
    }
    super.doPost(request, response);
  }

}
