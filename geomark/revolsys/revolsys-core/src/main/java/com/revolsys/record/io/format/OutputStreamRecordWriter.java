package com.revolsys.record.io.format;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.DelegatingWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public class OutputStreamRecordWriter extends DelegatingWriter<Record> implements RecordWriter {
  private final OutputStream out;

  private File tempFile;

  public OutputStreamRecordWriter(final RecordDefinitionProxy recordDefinition,
    final String baseName, final String fileExtension, final OutputStream out) {
    this.out = out;
    try {
      this.tempFile = FileUtil.newTempFile(baseName, "." + fileExtension);
      this.tempFile.delete();
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create temporary file", e);
    }
    try {
      final Resource tempResource = new PathResource(this.tempFile);
      final RecordWriter recordWriter = RecordWriter.newRecordWriter(recordDefinition,
        tempResource);
      setWriter(recordWriter);
    } catch (RuntimeException | Error e) {
      this.tempFile.delete();
      throw e;
    }
  }

  @Override
  public void close() {
    try {
      super.close();
    } finally {
      try {
        try {
          FileUtil.copy(this.tempFile, this.out);
        } finally {
          this.out.close();
        }
      } catch (final IOException e) {
        throw new RuntimeException("Unable to copy file", e);
      } finally {
        if (!FileUtil.deleteDirectory(this.tempFile)) {
          Logs.error(this, "Unable to delete:" + this.tempFile);
        }
      }
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    final RecordWriter writer = getWriter();
    return writer.getRecordDefinition();
  }

  @Override
  public RecordWriter getWriter() {
    return (RecordWriter)super.getWriter();
  }

  @Override
  public boolean isIndent() {
    final RecordWriter writer = getWriter();
    return writer.isIndent();
  }

  @Override
  public boolean isWriteCodeValues() {
    final RecordWriter writer = getWriter();
    return writer.isWriteCodeValues();
  }

  @Override
  public boolean isWriteNulls() {
    final RecordWriter writer = getWriter();
    return writer.isWriteNulls();
  }

  @Override
  public void setIndent(final boolean indent) {
    final RecordWriter writer = getWriter();
    writer.setIndent(indent);
  }

  @Override
  public void setWriteCodeValues(final boolean writeCodeValues) {
    final RecordWriter writer = getWriter();
    writer.setWriteCodeValues(writeCodeValues);
  }

  @Override
  public void setWriteNulls(final boolean writeNulls) {
    final RecordWriter writer = getWriter();
    writer.setWriteNulls(writeNulls);
  }
}
