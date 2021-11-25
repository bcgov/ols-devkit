package com.revolsys.gis.parallel;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;

public class ConverterProcess extends BaseInOutProcess<Record, Record> {

  private Converter<Record, Record> converter;

  public ConverterProcess() {
  }

  public ConverterProcess(final Converter<Record, Record> converter) {
    this.converter = converter;
  }

  public Converter<Record, Record> getConverter() {
    return this.converter;
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    if (this.converter != null) {
      final Record target = this.converter.convert(object);
      out.write(target);
    }
  }

  public void setConverter(final Converter<Record, Record> converter) {
    this.converter = converter;
  }

}
