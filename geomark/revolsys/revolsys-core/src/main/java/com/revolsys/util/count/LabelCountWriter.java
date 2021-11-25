package com.revolsys.util.count;

import javax.annotation.PostConstruct;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;

public class LabelCountWriter extends AbstractRecordWriter {
  private LabelCounters counts;

  private Writer<Record> writer;

  public LabelCountWriter() {
    super(null);
  }

  public LabelCountWriter(final Writer<Record> writer) {
    super(null);
    setWriter(writer);
  }

  @Override
  public void close() {
    this.writer.close();
    this.counts.disconnect();
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  /**
   * @return the counts
   */
  public LabelCounters getCounts() {
    return this.counts;
  }

  public Writer<Record> getWriter() {
    return this.writer;
  }

  @PostConstruct
  public void init() {
    if (this.counts == null) {
      setCounts(new LabelCountMap("Write " + this.writer));
    }
    this.counts.connect();
  }

  /**
   * @param labelCountMap the labelCountMap to set
   */
  public LabelCountWriter setCounts(final LabelCounters labelCountMap) {
    this.counts = labelCountMap;
    return this;
  }

  public LabelCountWriter setWriter(final Writer<Record> writer) {
    this.writer = writer;
    return this;
  }

  @Override
  public String toString() {
    return this.writer.toString();
  }

  @Override
  public void write(final Record object) {
    if (object != null) {
      this.writer.write(object);
      this.counts.addCount(object);
    }
  }
}
