package com.revolsys.jdbc.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.number.Numbers;

import com.revolsys.io.PathUtil;
import com.revolsys.record.Record;
import com.revolsys.record.property.ShortNameProperty;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public abstract class JdbcDdlWriter implements Cloneable {
  private PrintWriter out;

  protected boolean primaryKeyOnColumn = false;

  protected boolean quoteColumns = true;

  public JdbcDdlWriter() {
  }

  public JdbcDdlWriter(final PrintWriter out) {
    this.out = out;
  }

  @Override
  public JdbcDdlWriter clone() {
    try {
      return (JdbcDdlWriter)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public JdbcDdlWriter clone(final File file) {
    final JdbcDdlWriter clone = clone();
    clone.setOut(file);
    return clone;
  }

  public void close() {
    this.out.flush();
    this.out.close();
  }

  public PrintWriter getOut() {
    return this.out;
  }

  public String getSequenceName(final RecordDefinition recordDefinition) {
    throw new UnsupportedOperationException();
  }

  public String getTableAlias(final RecordDefinition recordDefinition) {
    final String shortName = ShortNameProperty.getShortName(recordDefinition);
    if (shortName == null) {
      final String path = recordDefinition.getPath();
      return PathUtil.getName(path);
    } else {
      return shortName;
    }
  }

  public void println() {
    this.out.println();
  }

  public void setOut(final File file) {
    try {
      final FileWriter writer = new FileWriter(file);
      this.out = new PrintWriter(writer);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setOut(final PrintWriter out) {
    this.out = out;
  }

  public void writeAddForeignKeyConstraint(final RecordDefinition recordDefinition,
    final String fieldName, final RecordDefinition referencedRecordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String referencedTypeName = referencedRecordDefinition.getPath();
    final String referencedFieldName = referencedRecordDefinition.getIdFieldName();
    final String constraintName = getTableAlias(recordDefinition) + "_"
      + getTableAlias(referencedRecordDefinition) + "_FK";
    writeAddForeignKeyConstraint(typePath, constraintName, fieldName, referencedTypeName,
      referencedFieldName);
  }

  public void writeAddForeignKeyConstraint(final RecordDefinition recordDefinition,
    final String fieldName, final String referenceTablePrefix,
    final RecordDefinition referencedRecordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String referencedTypeName = referencedRecordDefinition.getPath();
    final String referencedFieldName = referencedRecordDefinition.getIdFieldName();
    final String constraintName = getTableAlias(recordDefinition) + "_" + referenceTablePrefix + "_"
      + getTableAlias(referencedRecordDefinition) + "_FK";
    writeAddForeignKeyConstraint(typePath, constraintName, fieldName, referencedTypeName,
      referencedFieldName);
  }

  public void writeAddForeignKeyConstraint(final String typePath, final String constraintName,
    final String fieldName, final String referencedTypeName, final String referencedFieldName) {
    this.out.print("ALTER TABLE ");
    writeTableName(typePath);
    this.out.print(" ADD CONSTRAINT ");
    this.out.print(constraintName);
    this.out.print(" FOREIGN KEY (");
    this.out.print(fieldName);
    this.out.print(") REFERENCES ");
    writeTableName(referencedTypeName);
    this.out.print(" (");
    this.out.print(referencedFieldName);
    this.out.println(");");
  }

  public void writeAddPrimaryKeyConstraint(final RecordDefinition recordDefinition) {
    final String idFieldName = recordDefinition.getIdFieldName();
    if (idFieldName != null) {
      final String typePath = recordDefinition.getPath();
      final String constraintName = getTableAlias(recordDefinition) + "_PK";
      writeAddPrimaryKeyConstraint(typePath, constraintName, idFieldName);
    }
  }

  public void writeAddPrimaryKeyConstraint(final String typePath, final String constraintName,
    final String columnName) {
    this.out.print("ALTER TABLE ");
    writeTableName(typePath);
    this.out.print(" ADD CONSTRAINT ");
    this.out.print(constraintName);
    this.out.print(" PRIMARY KEY (");
    this.out.print(columnName);
    this.out.println(");");
  }

  public abstract void writeColumnDataType(final FieldDefinition field);

  public void writeCreateSchema(final String schemaName) {
  }

  public String writeCreateSequence(final RecordDefinition recordDefinition) {
    final String sequenceName = getSequenceName(recordDefinition);
    writeCreateSequence(sequenceName);
    return sequenceName;
  }

  public void writeCreateSequence(final String sequenceName) {
    this.out.print("CREATE SEQUENCE ");
    this.out.print(sequenceName);
    this.out.println(";");
  }

  public void writeCreateTable(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final PrintWriter out = this.out;
    out.println();
    out.print("CREATE TABLE ");
    writeTableName(typePath);
    out.println(" (");
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      final FieldDefinition field = recordDefinition.getField(i);
      if (i > 0) {
        out.println(",");
      }
      final String name = field.getName();
      out.print("  ");
      if (this.quoteColumns) {
        out.print('"');
      }
      out.print(name);
      if (this.quoteColumns) {
        out.print('"');
      }
      for (int j = name.length(); j < 32; j++) {
        out.print(' ');
      }
      writeColumnDataType(field);
      if (field.isRequired()) {
        out.print(" NOT NULL");
      }
      if (this.primaryKeyOnColumn) {
        if (recordDefinition.isIdField(name)) {
          writePrimaryKeyFieldContstaint(out);
        }
      }
    }
    out.println();
    out.println(");");

    if (!this.primaryKeyOnColumn) {
      writeAddPrimaryKeyConstraint(recordDefinition);
    }

    writeGeometryRecordDefinition(recordDefinition);

    final FieldDefinition idField = recordDefinition.getIdField();
    if (idField != null) {
      if (Number.class.isAssignableFrom(idField.getDataType().getJavaClass())) {
        writeCreateSequence(recordDefinition);
      }
    }
  }

  public void writeCreateView(final String typePath, final String queryTypeName,
    final List<String> columnNames) {
    this.out.println();
    this.out.print("CREATE VIEW ");
    writeTableName(typePath);
    this.out.println(" AS ( ");
    this.out.println("  SELECT ");
    this.out.print("  ");
    this.out.println(Strings.toString(",\n  ", columnNames));
    this.out.print("  FROM ");
    writeTableName(queryTypeName);
    this.out.println();
    this.out.println(");");
  }

  public abstract void writeGeometryRecordDefinition(final RecordDefinition recordDefinition);

  public void writeGrant(final String typePath, final String username, final boolean select,
    final boolean insert, final boolean update, final boolean delete) {

    this.out.print("GRANT ");
    final List<String> perms = new ArrayList<>();
    if (select) {
      perms.add("SELECT");
    }
    if (insert) {
      perms.add("INSERT");
    }
    if (update) {
      perms.add("UPDATE");
    }
    if (delete) {
      perms.add("DELETE");
    }
    this.out.print(Strings.toString(", ", perms));
    this.out.print(" ON ");
    writeTableName(typePath);
    this.out.print(" TO ");
    this.out.print(username);
    this.out.println(";");

  }

  public void writeInsert(final Record row) {
    final RecordDefinition recordDefinition = row.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    this.out.print("INSERT INTO ");
    writeTableName(typePath);
    this.out.print(" (");
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      if (i > 0) {
        this.out.print(", ");
      }
      this.out.print(recordDefinition.getFieldName(i));
    }
    this.out.print(" ) VALUES (");
    for (int i = 0; i < recordDefinition.getFieldCount(); i++) {
      if (i > 0) {
        this.out.print(", ");
      }
      final Object value = row.getValue(i);
      if (value == null) {
        this.out.print("NULL");
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        this.out.print(Numbers.toString(number));
      } else {
        this.out.print("'");
        this.out.print(value.toString().replaceAll("'", "''"));
        this.out.print("'");
      }
    }
    this.out.println(");");

  }

  public void writeInserts(final List<Record> rows) {
    for (final Record row : rows) {
      writeInsert(row);
    }

  }

  protected void writePrimaryKeyFieldContstaint(final PrintWriter out) {
    out.print(" PRIMARY KEY");
  }

  public void writeResetSequence(final RecordDefinition recordDefinition,
    final List<Record> values) {
    throw new UnsupportedOperationException();
  }

  public void writeTableName(final String typePath) {
    final String schemaName = PathUtil.getPath(typePath).substring(1);
    final String tableName = PathUtil.getName(typePath);
    writeTableName(schemaName, tableName);
  }

  public void writeTableName(final String schemaName, final String tableName) {
    if (Property.hasValue(schemaName)) {
      this.out.print(schemaName);
      this.out.print('.');
    }
    this.out.print(tableName);
  }
}
