package com.revolsys.record.io.format.xlsx;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorkbookPart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.jeometry.common.logging.Logs;
import org.xlsx4j.exceptions.Xlsx4jException;
import org.xlsx4j.sml.CTRElt;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTSst;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.Sheet;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordReader;
import com.revolsys.spring.resource.Resource;

public class XlsxRecordReader extends AbstractRecordReader {
  public static int getColumnIndex(final Cell cell) {
    final String cellReference = cell.getR();
    if (cellReference == null) {
      return -1;
    } else {
      int columnIndex = 0;
      for (int i = 0; i < cellReference.length(); i++) {
        final char character = cellReference.charAt(i);
        if (character >= 'A' && character <= 'Z') {
          columnIndex *= 26;
          columnIndex += character - 'A' + 1;
        } else {
          return columnIndex - 1;
        }
      }
      return columnIndex - 1;
    }
  }

  private Resource resource;

  private List<Row> rows = Collections.emptyList();

  private int rowIndex = 0;

  private List<CTRst> sharedStringList = Collections.emptyList();

  private String tabName;

  public XlsxRecordReader(final Resource resource) {
    this(resource, ArrayRecord.FACTORY);
  }

  public XlsxRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory) {
    super(recordFactory);
    this.resource = resource;
  }

  public XlsxRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, final MapEx properties) {
    this(resource, recordFactory);
    setProperties(properties);
  }

  @Override
  protected void closeDo() {
    super.closeDo();
    this.resource = null;
    this.rows = Collections.emptyList();
    this.sharedStringList = Collections.emptyList();
  }

  @Override
  protected Record getNext() {
    final List<String> row = readNextRow();
    if (row != null && row.size() > 0) {
      return parseRecord(row);
    } else {
      throw new NoSuchElementException();
    }
  }

  protected String getText(final CTRst sharedString) {
    final CTXstringWhitespace text = sharedString.getT();
    if (text == null) {
      final List<CTRElt> r = sharedString.getR();
      if (r != null) {
        final StringBuilder t = new StringBuilder();
        for (final CTRElt e : r) {
          t.append(e.getT().getValue());
        }
        return t.toString();
      }
      return "";
    } else {
      return text.getValue();
    }
  }

  private WorksheetPart getWorksheetPart(final WorkbookPart workbook) throws Xlsx4jException {
    if (this.tabName == null) {
      return workbook.getWorksheet(0);
    } else {
      int i = 0;
      for (final Sheet sheet : workbook.getJaxbElement().getSheets().getSheet()) {
        if (sheet.getName().equals(this.tabName)) {
          return workbook.getWorksheet(i);
        }
        i++;
      }
      return null;
    }
  }

  @Override
  protected void initDo() {
    super.initDo();
    try (
      InputStream in = this.resource.newBufferedInputStream()) {

      final SpreadsheetMLPackage spreadsheetPackage = (SpreadsheetMLPackage)OpcPackage.load(in);
      final DocPropsCustomPart customProperties = spreadsheetPackage.getDocPropsCustomPart();
      if (customProperties != null) {
        int srid = 0;
        try {
          srid = Integer.parseInt(customProperties.getProperty("srid").getLpwstr());
        } catch (final Throwable e) {
        }
        int axisCount = 2;
        try {
          axisCount = Integer.parseInt(customProperties.getProperty("axisCount").getLpwstr());
          if (axisCount > 4) {
            axisCount = 2;
          }
        } catch (final Throwable e) {
        }
        double scaleXy = 0;
        try {
          scaleXy = Double.parseDouble(customProperties.getProperty("scaleXy").getLpwstr());
        } catch (final Throwable e) {
        }
        double scaleZ = 0;
        try {
          scaleZ = Double.parseDouble(customProperties.getProperty("scaleZ").getLpwstr());
        } catch (final Throwable e) {
        }
        final GeometryFactory geometryFactory = GeometryFactory.fixed(srid, axisCount, scaleXy,
          scaleXy, scaleZ);
        setGeometryFactory(geometryFactory);
      }
      final WorkbookPart workbook = spreadsheetPackage.getWorkbookPart();
      final SharedStrings sharedStrings = workbook.getSharedStrings();
      if (sharedStrings != null) {
        final CTSst contents = sharedStrings.getContents();
        this.sharedStringList = contents.getSi();
      }
      final WorksheetPart worksheetPart = getWorksheetPart(workbook);

      if (worksheetPart != null) {
        final Worksheet worksheet = worksheetPart.getContents();
        final SheetData sheetData = worksheet.getSheetData();
        this.rows = sheetData.getRow();
        final List<String> line = readNextRow();
        final String baseName = this.resource.getBaseName();
        newRecordDefinition(baseName, line);
      }
    } catch (final IOException | Docx4JException | Xlsx4jException e) {
      Logs.error(this, "Unable to open " + this.resource, e);
    } catch (final NoSuchElementException e) {
    }
  }

  @Override
  protected GeometryFactory loadGeometryFactory() {
    return GeometryFactory.floating2d(this.resource);
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate
   *         entry.
   * @throws IOException if bad things happen during the read
   */
  private List<String> readNextRow() {
    if (this.rowIndex < this.rows.size()) {
      final List<String> values = new ArrayList<>();
      final Row row = this.rows.get(this.rowIndex);
      final List<Cell> cells = row.getC();
      for (final Cell cell : cells) {
        String value = null;
        final String cellValue = cell.getV();

        final STCellType cellType = cell.getT();
        switch (cellType) {
          case S:
            final int stringIndex = Integer.parseInt(cellValue);
            final CTRst sharedString = this.sharedStringList.get(stringIndex);
            value = getText(sharedString);
          break;
          default:
            if (cellValue == null) {
              final CTRst is = cell.getIs();
              if (is != null) {
                value = is.getT().getValue();
              }
            } else {
              value = cellValue;
            }
          break;
        }
        final int columnIndex = getColumnIndex(cell);
        if (columnIndex == -1) {
          values.add(value);
        } else {
          while (values.size() < columnIndex) {
            values.add(null);
          }
          values.add(columnIndex, value);
        }
      }
      this.rowIndex++;
      return values;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void setTabName(final String tabName) {
    this.tabName = tabName;
  }
}
