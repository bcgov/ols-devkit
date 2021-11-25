package com.revolsys.util.count;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Set;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathNameProxy;

import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.tsv.Tsv;
import com.revolsys.record.io.format.tsv.TsvWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.util.Counter;

public interface LabelCounters {

  default void addCount(final CharSequence label) {
    addCount(label, 1);
  }

  boolean addCount(CharSequence label, long count);

  default void addCount(final Enum<?> label) {
    addCount(label.name(), 1);
  }

  default void addCount(final PathNameProxy pathNameProxy) {
    if (pathNameProxy != null) {
      final CharSequence label = pathNameProxy.getPathName();
      addCount(label);
    }
  }

  default void addCount(final PathNameProxy pathNameProxy, final long count) {
    final CharSequence label = pathNameProxy.getPathName();
    addCount(label, count);
  }

  default void addCounts(final LabelCounters labelCountMap) {
    synchronized (labelCountMap) {
      for (final String label : labelCountMap.getLabels()) {
        final long count = labelCountMap.getCount(label);
        addCount(label, count);
      }
    }
  }

  void addCountsText(StringBuilder sb);

  void clearCounts();

  void clearCounts(String label);

  void connect();

  void disconnect();

  Long getCount(CharSequence label);

  default long getCount(final CharSequence label, final long defaultValue) {
    final Long value = getCount(label);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  Counter getCounter(CharSequence label);

  Set<String> getLabels();

  String getMessage();

  boolean isEmpty();

  boolean isLogCounts();

  String logCounts();

  default void setCounter(final CharSequence label, final Counter counter) {
    throw new UnsupportedOperationException();
  }

  void setLogCounts(boolean logCounts);

  void setMessage(String message);

  default String toTsv() {
    return toTsv("LABEL", "COUNT");
  }

  default String toTsv(final String... titles) {
    final StringWriter out = new StringWriter();
    toTsv(out, titles);
    return out.toString();
  }

  default void toTsv(final Writer out, final String... titles) {
    try (
      TsvWriter tsv = Tsv.plainWriter(out)) {
      long total = 0;
      tsv.write(Arrays.asList(titles));
      for (final String label : getLabels()) {
        final long count = getCount(label);
        total += count;
        tsv.write(label, count);
      }
      tsv.write("Total", total);
    }
  }

  default void writeCounts(final Object target, final String labelTitle) {
    final RecordDefinitionBuilder recordDefinitionBuilder = new RecordDefinitionBuilder("Counts");
    recordDefinitionBuilder.addField(labelTitle, DataTypes.STRING, 50);
    recordDefinitionBuilder.addField("Count", DataTypes.LONG, 10);
    final RecordDefinition recordDefinition = recordDefinitionBuilder.getRecordDefinition();
    try (
      RecordWriter recordWriter = RecordWriter.newRecordWriter(recordDefinition, target)) {
      for (final String label : getLabels()) {
        final Long count = getCount(label);
        recordWriter.write(label, count);
      }
    }
  }

}
