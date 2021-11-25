package com.revolsys.record.io.format.esri.gdb.xml.model;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;

public class CodedValueDomain extends Domain {

  public CodedValueDomain() {
  }

  public CodedValueDomain(final String domainName, final FieldType fieldType,
    final String description) {
    super(domainName, fieldType, description);
  }

  CodedValueDomain newDomain(final CodeTable codeTable) {
    final CodedValueDomain domain = new CodedValueDomain(codeTable.getName(),
      FieldType.esriFieldTypeString, codeTable.getName());
    for (final Identifier code : codeTable.getIdentifiers()) {
      final Object value = codeTable.getValue(code);
      domain.addCodedValue(code.toIdString(), value.toString());
    }
    return domain;
  }
}
