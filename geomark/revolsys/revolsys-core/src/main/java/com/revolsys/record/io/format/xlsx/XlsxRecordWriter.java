package com.revolsys.record.io.format.xlsx;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.docx4j.jaxb.NamespacePrefixMapperUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DocPropsCustomPart;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.CTAutoFilter;
import org.xlsx4j.sml.CTRst;
import org.xlsx4j.sml.CTTable;
import org.xlsx4j.sml.CTTableColumn;
import org.xlsx4j.sml.CTTableColumns;
import org.xlsx4j.sml.CTTableStyleInfo;
import org.xlsx4j.sml.CTXstringWhitespace;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Col;
import org.xlsx4j.sml.Cols;
import org.xlsx4j.sml.ObjectFactory;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;
import org.xlsx4j.sml.SheetData;
import org.xlsx4j.sml.Worksheet;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;

public class XlsxRecordWriter extends AbstractRecordWriter {
  private static final ObjectFactory smlObjectFactory = Context.getsmlObjectFactory();

  static {
    // This method is not thread safe, so call it before we need to use it, to
    // ensure initialization.
    try {
      NamespacePrefixMapperUtils.getPrefixMapper();
    } catch (final JAXBException e) {
      Logs.error(XlsxRecordWriter.class, "Unable to initialized DOCx4j", e);
    }
  }

  public static String getRef(long columnIndex, final int rowIndex) {
    columnIndex--;
    final StringBuilder ref = new StringBuilder();
    do {
      final long index = columnIndex % 26;
      ref.append((char)('A' + index));
      columnIndex = columnIndex / 26;

    } while (columnIndex > 0);
    ref.append(rowIndex);
    return ref.toString();
  }

  private OutputStream out;

  private SpreadsheetMLPackage spreadsheetPackage;

  private WorksheetPart sheet;

  private SheetData sheetData;

  private List<Row> sheetRows = Collections.emptyList();

  public XlsxRecordWriter(final RecordDefinitionProxy recordDefinition, final OutputStream out) {
    super(recordDefinition);
    try {
      this.out = new BufferedOutputStream(out);
      this.spreadsheetPackage = SpreadsheetMLPackage.createPackage();
      final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
      if (geometryFactory != null) {
        final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
        if (coordinateSystemId > 0) {
          this.spreadsheetPackage.addDocPropsCustomPart();
          final DocPropsCustomPart customProperties = this.spreadsheetPackage
            .getDocPropsCustomPart();
          customProperties.setProperty("srid", Integer.toString(coordinateSystemId));
          final int axisCount = geometryFactory.getAxisCount();
          customProperties.setProperty("axisCount", Integer.toString(axisCount));

          final double scaleXY = geometryFactory.getScaleXY();
          if (scaleXY > 0) {
            customProperties.setProperty("scaleXy", Doubles.toString(scaleXY));
          }
          if (axisCount > 2) {
            final double scaleZ = geometryFactory.getScaleZ();
            if (scaleZ > 0) {
              customProperties.setProperty("scaleZ", Doubles.toString(scaleZ));
            }
          }
        }
      }
      String name = this.recordDefinition.getName();
      if (name == null) {
        name = "Sheet1";
      } else {
        name = name.trim();
        if (name.length() == 0) {
          name = "Sheet1";
        } else if (name.length() > 30) {
          name = name.substring(0, 30);
        }
      }
      final PartName spreadsheetPartName = new PartName("/xl/worksheets/sheet1.xml");
      this.sheet = this.spreadsheetPackage.createWorksheetPart(spreadsheetPartName, name, 1);
      final Worksheet worksheet = this.sheet.getContents();
      // final CTTableParts tableParts = smlObjectFactory.createCTTableParts();
      // tableParts.setCount(1L);
      // final CTTablePart tablePart = smlObjectFactory.createCTTablePart();
      // tablePart.setId("rId1");
      // tableParts.getTablePart().add(tablePart);
      // worksheet.setTableParts(tableParts);

      this.sheetData = worksheet.getSheetData();
      this.sheetRows = this.sheetData.getRow();

      addHeaderRow(worksheet, this.recordDefinition);

    } catch (final Docx4JException | JAXBException e) {
      throw Exceptions.wrap(e);
    }
  }

  public XlsxRecordWriter(final RecordDefinitionProxy recordDefinition, final Resource resource) {
    this(recordDefinition, resource.newOutputStream());
    setResource(resource);
  }

  private void addCellInlineString(final List<Cell> cells, String value) {
    if (value == null) {
      value = "";
    }
    final CTXstringWhitespace cellContext = smlObjectFactory.createCTXstringWhitespace();
    cellContext.setValue(value);

    final CTRst cellString = new CTRst();
    cellString.setT(cellContext);

    final Cell cell = smlObjectFactory.createCell();
    cell.setT(STCellType.INLINE_STR);
    cell.setIs(cellString);
    cells.add(cell);
  }

  private void addCellNumber(final List<Cell> cells, final String value) {
    final Cell cell = smlObjectFactory.createCell();
    cell.setV(value);
    cells.add(cell);
  }

  private void addHeaderRow(final Worksheet worksheet, final RecordDefinition recordDefinition) {
    final List<Cols> columnGroups = worksheet.getCols();
    final Cols columns = smlObjectFactory.createCols();
    columnGroups.add(columns);

    final Row headerRow = smlObjectFactory.createRow();
    this.sheetRows.add(headerRow);
    final List<Cell> cells = headerRow.getC();

    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String fieldName = field.getName();
      final Col column = smlObjectFactory.createCol();
      columns.getCol().add(column);
      column.setMin(field.getIndex() + 1);
      column.setMax(field.getIndex() + 1);
      column.setBestFit(true);
      final int textLength = Math.min(40,
        Math.max(fieldName.length() + 2, field.getMaxStringLength()));
      column.setWidth(textLength * 1.25);
      addCellInlineString(cells, fieldName);
    }
  }

  /**
   * Closes the underlying reader.
   */
  @Override
  public synchronized void close() {
    if (this.out != null) {
      try {
        final long fieldCount = getFieldCount();
        final String ref = "A1:" + getRef(fieldCount, this.sheetRows.size());
        final TablePart tablePart = new TablePart();
        this.spreadsheetPackage.addTargetPart(tablePart);
        final CTTable table = smlObjectFactory.createCTTable();
        tablePart.setContents(table);
        table.setId(1);
        table.setName("Table1");
        final RecordDefinition recordDefinition = getRecordDefinition();
        table.setDisplayName(recordDefinition.getName());
        table.setRef(ref);

        final CTAutoFilter autoFilter = smlObjectFactory.createCTAutoFilter();
        autoFilter.setRef(ref);
        table.setAutoFilter(autoFilter);

        long columnIndex = 1;
        final CTTableColumns tableColumns = smlObjectFactory.createCTTableColumns();
        tableColumns.setCount(fieldCount);
        table.setTableColumns(tableColumns);
        final List<CTTableColumn> columns = tableColumns.getTableColumn();
        for (final String fieldName : getFieldNames()) {
          final CTTableColumn column = smlObjectFactory.createCTTableColumn();
          column.setId(columnIndex);
          column.setName(fieldName);
          columns.add(column);
          columnIndex++;
        }
        final CTTableStyleInfo tableStyleInfo = smlObjectFactory.createCTTableStyleInfo();
        table.setTableStyleInfo(tableStyleInfo);
        tableStyleInfo.setName("TableStyleMedium14");
        tableStyleInfo.setShowFirstColumn(false);
        tableStyleInfo.setShowLastColumn(false);
        tableStyleInfo.setShowRowStripes(true);
        tableStyleInfo.setShowColumnStripes(false);

        this.sheet.addTargetPart(tablePart, "rId1");

        final Save save = new Save(this.spreadsheetPackage);
        save.save(this.out);
        try {
          this.out.flush();
        } catch (final IOException e) {
        }
      } catch (final Docx4JException e) {
        throw Exceptions.wrap(e);
      } finally {
        FileUtil.closeSilent(this.out);
        this.out = null;
        this.sheet = null;
        this.sheetData = null;
        this.spreadsheetPackage = null;
        this.sheetRows = Collections.emptyList();
      }
    }
  }

  @Override
  public void flush() {
  }

  @Override
  public void write(final Record record) {
    final Row recordRow = smlObjectFactory.createRow();
    this.sheetRows.add(recordRow);
    final List<Cell> cells = recordRow.getC();
    for (final FieldDefinition field : getFieldDefinitions()) {
      final Object value = record.getValue(field);
      final String string = field.toString(value);
      if (value instanceof Number) {
        addCellNumber(cells, string);
      } else {
        addCellInlineString(cells, string);
      }
    }
  }
}
