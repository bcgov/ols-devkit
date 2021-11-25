package com.revolsys.gis.parallel;

import com.revolsys.io.Writer;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInProcess;
import com.revolsys.record.Record;

public class WriterProcess extends BaseInProcess<Record> {
  private Writer<Record> writer;

  public WriterProcess() {
  }

  public WriterProcess(final Channel<Record> in, final Writer<Record> writer) {
    super(in);
    this.writer = writer;
  }

  public WriterProcess(final Writer<Record> writer) {
    this.writer = writer;
  }

  public WriterProcess(final Writer<Record> writer, final int inBufferSize) {
    super(inBufferSize);
    this.writer = writer;
  }

  /**
   * @return the writer
   */
  public Writer<Record> getWriter() {
    return this.writer;
  }

  @Override
  protected void postRun(final Channel<Record> in) {
    this.writer.close();
  }

  @Override
  protected void process(final Channel<Record> in, final Record record) {
    this.writer.write(record);
  }

  public void setWriter(final Writer<Record> writer) {
    this.writer = writer;
  }

}
