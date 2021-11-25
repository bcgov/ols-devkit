package ca.bc.gov.geomark.web.servlet.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.identifier.Identifier;

import ca.bc.gov.geomark.web.servlet.BaseServlet;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.transaction.Transaction;

@WebServlet(urlPatterns = "/secure/admin/api/configProperties/*", loadOnStartup = 1)
public class AdminConfigPropertyServlet extends BaseServlet {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String path = request.getPathInfo();
    if (path == null || path.length() == 1) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } else if (path.indexOf('/', 1) == -1) {
      final String id = path.substring(1);
      deleteRecord(response, CONFIG_PROPERTY, id);
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws ServletException, IOException {
    final String path = request.getPathInfo();
    if (path == null || path.length() == 1) {
      doGetList(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private void doGetList(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException {
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(CONFIG_PROPERTY);
    final Query query = new Query(recordDefinition);
    query.select(CONFIG_PROPERTY_ID, PROPERTY_NAME, PROPERTY_VALUE);
    writeJsonRecordsPage(request, response, query);
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
      final Identifier id = newValues.getIdentifier(CONFIG_PROPERTY_ID);
      final String propertyName = newValues.getString(PROPERTY_NAME);
      if (id == null) {
        final Query query = new Query(CONFIG_PROPERTY)//
          .setWhereCondition(Q.equal(PROPERTY_NAME, propertyName));
        try (
          final RecordReader results = this.recordStore.getRecords(query)) {
          for (@SuppressWarnings("unused")
          final Record otherProperty : results) {
            writeError(response, "Property Name already in use by another config property");
            return;
          }
        }

        final Record record = this.recordStore.getRecordDefinition(CONFIG_PROPERTY).newRecord();
        record.setValues(newValues);
        this.recordStore.insertRecord(record);

      } else {
        final Record record = this.recordStore.getRecord(CONFIG_PROPERTY, id);
        if (record == null) {
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
          final Query query = new Query(CONFIG_PROPERTY)//
            .setWhereCondition(Q.equal(PROPERTY_NAME, propertyName));
          try (
            final RecordReader results = this.recordStore.getRecords(query)) {
            for (final Record otherProperty : results) {
              if (!otherProperty.equalValue(CONFIG_PROPERTY_ID, id)) {
                writeError(response, "Property Name already in use by another config property");
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
}
