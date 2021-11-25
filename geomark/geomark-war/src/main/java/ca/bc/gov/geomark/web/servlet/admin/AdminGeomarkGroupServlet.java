package ca.bc.gov.geomark.web.servlet.admin;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.identifier.Identifier;

import ca.bc.gov.geomark.web.domain.GeomarkConstants;
import ca.bc.gov.geomark.web.servlet.BaseServlet;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.gis.postgresql.PostgreSQLRecordStore;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.Property;

@WebServlet(urlPatterns = "/secure/admin/api/geomarkGroups/*", loadOnStartup = 1)
public class AdminGeomarkGroupServlet extends BaseServlet {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final String[] parts = pathParts(request);
    if (parts.length == 1) {
      final String geomarkGroupId = parts[0];
      if (geomarkGroupId.startsWith("gg-")) {
        deleteRecord(response, GEOMARK_GROUP, geomarkGroupId);
        return;
      }
    } else if (parts.length == 3) {
      final String geomarkGroupId = parts[0];
      final String geomarkId = parts[2];
      if (geomarkId.startsWith("gm-") && parts[1].equals("geomarks")
        && geomarkGroupId.startsWith("gg-")) {
        doDeleteGeomarkFromGroup(geomarkGroupId, geomarkId);
        return;
      }
    }
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private void doDeleteGeomarkFromGroup(final String geomarkGroupId, final String geomarkId) {
    try (
      Transaction transaction = this.recordStore.newTransaction();
      RecordWriter writer = this.recordStore.newRecordWriter()) {
      deleteGeomarkFromGroup(writer, geomarkGroupId, geomarkId, -1);
    }
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String[] parts = pathParts(request);
    if (parts.length == 0) {
      doGetList(request, response);
    } else if (parts.length == 1) {
      final String page = parts[0];
      if ("report".equals(page)) {
        doGetReport(request, response);
      } else if (page.startsWith("gg-")) {
        doGetView(response, page);
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } else if (!parts[0].startsWith("gg-")) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else if (parts.length == 2) {
      final String geomarkGroupId = parts[0];
      if (parts[1].equals("geomarks")) {
        doGetGeomarks(request, response, geomarkGroupId);
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void doGetGeomarks(final HttpServletRequest request, final HttpServletResponse response,
    final String geomarkGroupId) throws IOException {
    final RecordDefinition geomarkGroupXref = this.recordStore
      .getRecordDefinition(GeomarkConstants.GEOMARK_GROUP_XREF);
    final Query query = this.recordStore.newQuery(GeomarkConstants.GEOMARK_POLY)
      .select(GEOMARK_ID, WHEN_CREATED, EXPIRY_DATE)
      .and(geomarkGroupXref.equal("GEOMARK_GROUP_ID", geomarkGroupId))
      .orderBy(GEOMARK_ID);
    query.join(geomarkGroupXref).on("GEOMARK_ID", query);

    final String search = request.getParameter("searchTerm");
    if (Property.hasValue(search)) {
      final String likeName = "%" + search + "%";
      query.and(query.newCondition("GEOMARK_ID", Q::like, likeName));
    }

    writeJsonRecordsPage(request, response, query);
  }

  private void doGetList(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(GEOMARK_GROUP);
    final Query query = new Query(recordDefinition);
    query.select(GEOMARK_GROUP_ID, WHEN_CREATED, DESCRIPTION);

    String search = request.getParameter("searchText");
    if (Property.hasValue(search)) {
      search = "%" + search.toLowerCase() + "%";
      query.setWhereCondition(Q.or(//
        Q.iLike(GEOMARK_GROUP_ID, search), //
        Q.iLike(DESCRIPTION, search) //
      ));
    }

    writeJsonRecordsPage(request, response, query);
  }

  private void doGetReport(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final Query query = new Query("/GEOMARK/GEOMARK_REPORT");

    String whereClause = "";
    String search = request.getParameter("searchText");
    if (Property.hasValue(search)) {
      search = search.toLowerCase();
      whereClause += "(lower(GG.\"GEOMARK_GROUP_ID\") LIKE ? OR lower(GG.\"DESCRIPTION\") LIKE ?)";
      query.addParameter("%" + search + "%");
      query.addParameter("%" + search + "%");
      query.addParameter("%" + search + "%");
      query.addParameter("%" + search + "%");
    }
    String geometryCountFunction = "SDO_UTIL.GETNUMELEM(G.GEOMETRY)";
    String vertexCountFunction = "SDO_UTIL.GETNUMVERTICES(G.GEOMETRY)";
    if (this.recordStore instanceof PostgreSQLRecordStore) {
      geometryCountFunction = "ST_NumGeometries(G.GEOMETRY)";
      vertexCountFunction = "ST_NPoints(G.GEOMETRY)";
    }
    String sql = "select GG.\"GEOMARK_GROUP_ID\", \"DESCRIPTION\", COUNT(G.\"GEOMARK_ID\") \"GEOMARK_COUNT\", sum("
      + geometryCountFunction + ") \"GEOMETRY_COUNT\", sum(" + vertexCountFunction
      + ") \"VERTEX_COUNT\"" + " from \"GEOMARK\".\"GMK_GEOMARK_GROUPS\" GG"
      + " join \"GEOMARK\".GMK_GEOMARK_GROUP_XREF\" GX on GG.\"GEOMARK_GROUP_ID\" = GX.\"GEOMARK_GROUP_ID\""
      + " join \"GEOMARK\".GMK_GEOMARK_POLY\" G on G.\"GEOMARK_ID\" = GX.\"GEOMARK_ID\" ";
    if (Property.hasValue(whereClause)) {
      sql += " where " + whereClause;
    }
    sql += " group by GG.\"GEOMARK_GROUP_ID\", \"DESCRIPTION\"" + " union all"
      + " select GG.\"GEOMARK_GROUP_ID\", \"DESCRIPTION\", 0 \"GEOMARK_COUNT\", 0 \"GEOMETRY_COUNT\", 0 \"VERTEX_COUNT\""
      + " from \"GEOMARK\".\"GMK_GEOMARK_GROUPS GG\""
      + " where (not exists (select GX.\"GEOMARK_GROUP_ID\" from \"GEOMARK\".\"GMK_GEOMARK_GROUP_XREF\" GX where GG.\"GEOMARK_GROUP_ID\" = GX.\"GEOMARK_GROUP_ID\"))"

    ;
    if (Property.hasValue(whereClause)) {
      sql += " AND " + whereClause;
    }
    query.setSql(sql);

    final String sort = request.getParameter("sort");
    if (sort != null && Arrays
      .asList(GEOMARK_GROUP_ID, DESCRIPTION, "GEOMARK_COUNT", "GEOMETRY_COUNT", "VERTEX_COUNT")
      .contains(sort)) {
      final boolean ascending = "asc".equals(request.getParameter("order"));
      query.addOrderBy(sort, ascending);
    }

    try (
      RecordReader reader = this.recordStore.getRecords(query)) {
      writeJsonRecords(response, JsonObject.EMPTY, query.getRecordDefinition(), reader);
    }

  }

  private void doGetView(final HttpServletResponse response, final String geomarkGroupId)
    throws IOException, ServletException {
    final Record geomarkGroup = this.recordStore.getRecord(GEOMARK_GROUP, geomarkGroupId);
    if (geomarkGroup == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } else {
      writeJsonRecord(response, geomarkGroup);
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    if ("DELETE".equals(request.getHeader("X-HTTP-Method-Override"))) {
      doDelete(request, response);
      return;
    }
    try (
      Transaction transaction = this.recordStore.newTransaction()) {
      final JsonObject newValues = readJsonMap(request);
      Identifier id = newValues.getIdentifier(GEOMARK_GROUP_ID);
      final String description = newValues.getString(DESCRIPTION);
      if (id == null) {
        final Query query = new Query(GEOMARK_GROUP)//
          .setWhereCondition(Q.equal(DESCRIPTION, description));
        try (
          final RecordReader results = this.recordStore.getRecords(query)) {
          for (@SuppressWarnings("unused")
          final Record otherGroup : results) {
            writeError(response, "Group description already in use by another geomark group");
            return;
          }
        }

        final Record record = this.recordStore.getRecordDefinition(GEOMARK_GROUP).newRecord();
        record.setValues(newValues);
        id = Identifier.newIdentifier("gg-" + newUuid());
        record.setIdentifier(id);
        final String secretKey = "kg-" + newUuid();
        record.setValue(GeomarkConstants.SECRET_KEY, secretKey);
        record.setValue("WHEN_CREATED", new Timestamp(System.currentTimeMillis()));

        this.recordStore.insertRecord(record);

      } else {
        final Record record = this.recordStore.getRecord(GEOMARK_GROUP, id);
        if (record == null) {
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
          final Query query = new Query(GEOMARK_GROUP)//
            .setWhereCondition(Q.equal(DESCRIPTION, description));
          try (
            final RecordReader results = this.recordStore.getRecords(query)) {
            for (final Record otherGroup : results) {
              if (!otherGroup.equalValue(GEOMARK_GROUP_ID, id)) {
                writeError(response, "Group description already in use by another geomark group");
                return;
              }
            }
          }
          record.setValues(newValues);
          this.recordStore.updateRecord(record);
        }
      }
      writeJsonMap(response, new LinkedHashMapEx("saved", true));
    }
  }

  @Override
  protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final String[] parts = pathParts(request);
    if (parts.length == 3) {
      final String geomarkGroupId = parts[0];
      final String geomarkId = parts[2];
      if (geomarkId.startsWith("gm-") && parts[1].equals("geomarks")
        && geomarkGroupId.startsWith("gg-")) {
        doPutAddGeomarkToGroup(response, geomarkGroupId, geomarkId);
        return;
      }
    }
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private void doPutAddGeomarkToGroup(final HttpServletResponse response,
    final String geomarkGroupId, final String geomarkId) throws IOException {
    final String error;
    try (
      Transaction transaction = this.recordStore.newTransaction();
      RecordWriter writer = this.recordStore.newRecordWriter()) {
      final Record group = this.recordStore.getRecord(GEOMARK_GROUP, geomarkGroupId);
      if (group == null) {
        error = "Geomark group #" + geomarkGroupId + " doesn't exist";
      } else {
        if (addGeomarkToGroup(writer, geomarkGroupId, geomarkId)) {
          error = null;
        } else {
          error = "Geomark #" + geomarkId + " doesn't exist";
        }
      }
    }
    if (error == null) {
      writeJsonMap(response, JsonObject.hash("added", true));
    } else {
      writeError(response, error);
    }
  }

  // @RequestMapping(value =
  // "/secure/admin/geomarkGroups/{geomarkGroupId}/geomarks",
  // method = RequestMethod.GET, title = "Geomarks", fieldNames = {
  // GEOMARK_ID_LINK, GEOMARK_INFO_LINK, WHEN_CREATED
  // })
  // @ResponseBody
  // public void groupList(@PathVariable("geomarkGroupId") final String
  // geomarkGroupId)
  // throws IOException {
  // final Record group = this.recordStore.getRecord(GEOMARK_GROUP,
  // geomarkGroupId);
  // if (group == null) {
  // throw new PageNotFoundException("Geomark Group " + geomarkGroupId + " not
  // found");
  // } else {
  // final HttpServletRequest request = HttpServletUtils.getRequest();
  // final String search = request.getParameter("search[value]");
  //
  // final Query query = new Query(GEOMARK_POLY);
  // query.setFieldNames("GEOMARK_ID", "WHEN_CREATED");
  //
  // query.setFromClause(
  // "GEOMARK.GMK_GEOMARK_POLY T JOIN GEOMARK.GMK_GEOMARK_GROUP_XREF X ON
  // T.GEOMARK_ID = X.GEOMARK_ID");
  // final Condition[] conditions = {
  // Q.equal("X.GEOMARK_GROUP_ID", geomarkGroupId)
  // };
  //
  // final And and = new And(conditions);
  //
  // if (Property.hasValue(search)) {
  // final String likeName = "%" + search + "%";
  // and.and(Q.iLike("T.GEOMARK_ID", likeName));
  // }
  // query.setWhereCondition(and);
  //
  // final Map<String, Boolean> orderBy = getDataTableSortOrder(
  // Arrays.asList("GEOMARK_ID", "EXPIRY_DATE", "WHEN_CREATED"), request);
  // query.setOrderBy(orderBy);
  //
  // return newDataTableMap(request, this.recordStore, query, "groupList");
  // }
  // }

}
