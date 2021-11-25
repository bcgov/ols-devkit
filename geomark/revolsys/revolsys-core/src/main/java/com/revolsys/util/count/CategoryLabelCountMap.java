package com.revolsys.util.count;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathNameProxy;

import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.tsv.Tsv;
import com.revolsys.record.io.format.tsv.TsvWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Strings;

public class CategoryLabelCountMap implements Emptyable {
  private boolean logCounts;

  private String prefix;

  private String labelTitle = "Type";

  private int providerCount = 0;

  private final Map<String, LabelCounters> labelCountMapByCategory = new TreeMap<>();

  public CategoryLabelCountMap() {
  }

  public CategoryLabelCountMap(final Map<String, LabelCounters> counts) {
    if (counts != null) {
      this.labelCountMapByCategory.putAll(counts);
    }
  }

  public CategoryLabelCountMap(final String prefix) {
    this.prefix = prefix;
  }

  public void addCount(final CharSequence category, final CharSequence label) {
    final LabelCounters labelCountMap = getLabelCountMap(category);
    labelCountMap.addCount(label);
  }

  public void addCount(final CharSequence category, final CharSequence label, final long count) {
    final LabelCounters labelCountMap = getLabelCountMap(category);
    labelCountMap.addCount(label, count);
  }

  public void addCount(final CharSequence category, final PathNameProxy pathNameProxy) {
    final LabelCounters labelCountMap = getLabelCountMap(category);
    labelCountMap.addCount(pathNameProxy);

  }

  public void addCount(final CharSequence category, final PathNameProxy pathNameProxy,
    final long count) {
    final LabelCounters labelCountMap = getLabelCountMap(category);
    labelCountMap.addCount(pathNameProxy, count);
  }

  public void addCounts(final CategoryLabelCountMap counts) {
    synchronized (counts) {
      for (final Entry<String, LabelCounters> entry : counts.labelCountMapByCategory.entrySet()) {
        final String category = entry.getKey();
        final LabelCounters labelCountMap = entry.getValue();
        addCounts(category, labelCountMap);
      }
    }
  }

  public void addCounts(final String category, final LabelCounters labelCountMap) {
    final LabelCounters thisLabelCountMap = getLabelCountMap(category);
    thisLabelCountMap.addCounts(labelCountMap);
  }

  public synchronized void addCountsText(final StringBuilder sb) {
    for (final LabelCounters labelCountMap : this.labelCountMapByCategory.values()) {
      labelCountMap.addCountsText(sb);
    }
  }

  public void clear() {
    this.labelCountMapByCategory.clear();
  }

  public void clearCounts(final CharSequence category) {
    final LabelCounters labelCountMap = this.labelCountMapByCategory.get(category);
    if (labelCountMap != null) {
      labelCountMap.clearCounts();
    }
  }

  @PostConstruct
  public synchronized void connect() {
    this.providerCount++;
  }

  @PreDestroy
  public synchronized void disconnect() {
    this.providerCount--;
    if (this.providerCount <= 0) {
      for (final LabelCounters labelCountMap : this.labelCountMapByCategory.values()) {
        labelCountMap.disconnect();
      }
    }
  }

  public synchronized Set<String> getCategories() {
    return this.labelCountMapByCategory.keySet();
  }

  public Long getCount(final CharSequence category, final CharSequence label) {
    final LabelCounters labelCountMap = getLabelCountMap(category);
    if (labelCountMap == null) {
      return null;
    } else {
      return labelCountMap.getCount(label);
    }
  }

  public synchronized String getCountsText() {
    final StringBuilder sb = new StringBuilder();
    addCountsText(sb);
    return sb.toString();
  }

  public synchronized LabelCounters getLabelCountMap(final CharSequence category) {
    if (category == null) {
      return null;
    } else {
      String categoryString = category.toString();
      if (this.prefix != null) {
        categoryString = this.prefix + " " + category;
      }
      LabelCounters labelCountMap = this.labelCountMapByCategory.get(categoryString);
      if (labelCountMap == null) {
        labelCountMap = new LabelCountMap(categoryString);
        labelCountMap.setLogCounts(this.logCounts);
        this.labelCountMapByCategory.put(categoryString, labelCountMap);
      }
      return labelCountMap;
    }
  }

  public String getLabelTitle() {
    return this.labelTitle;
  }

  public String getPrefix() {
    return this.prefix;
  }

  @Override
  public boolean isEmpty() {
    return this.labelCountMapByCategory.isEmpty();
  }

  public synchronized void setLabelCounters(final CharSequence category,
    final LabelCounters labelCountMap) {
    if (category != null) {
      final String categoryName = Strings.toString(" ", this.prefix, category);
      labelCountMap.setLogCounts(this.logCounts);
      this.labelCountMapByCategory.put(categoryName, labelCountMap);
    }
  }

  public CategoryLabelCountMap setLabelTitle(final String labelTitle) {
    this.labelTitle = labelTitle;
    return this;
  }

  public synchronized void setLogCounts(final boolean logCounts) {
    this.logCounts = logCounts;
    for (final LabelCounters labelCountMap : this.labelCountMapByCategory.values()) {
      labelCountMap.setLogCounts(logCounts);
    }
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public String toTsv() {
    return toTsv("CATEGORY", "NAME", "COUNT");
  }

  public String toTsv(final String... titles) {
    final StringWriter out = new StringWriter();
    toTsv(out, titles);
    return out.toString();
  }

  public void toTsv(final Writer out, final String... titles) {
    try (
      TsvWriter tsv = Tsv.plainWriter(out)) {
      tsv.write(Arrays.asList(titles));
      long total = 0;
      for (final Entry<String, LabelCounters> entry : this.labelCountMapByCategory.entrySet()) {
        final String category = entry.getKey();
        final LabelCounters labelCountMap = entry.getValue();
        for (final String label : labelCountMap.getLabels()) {
          final long count = labelCountMap.getCount(label);
          total += count;
          tsv.write(category, label, count);
        }
      }
      tsv.write(null, "Total", total);
    }
  }

  public void writeCounts(final Object target) {
    writeCounts(target, this.labelTitle, this.labelCountMapByCategory.keySet());
  }

  public void writeCounts(final Object target, final String labelTitle,
    final Iterable<String> categoryNames) {
    final RecordDefinitionBuilder recordDefinitionBuilder = new RecordDefinitionBuilder("Counts");
    recordDefinitionBuilder.addField(labelTitle, DataTypes.STRING, 50);
    final Set<String> allLabels = new TreeSet<>();
    final List<String> matchedCategoryNames = new ArrayList<>();
    for (final String categoryName : categoryNames) {
      final LabelCounters labelCountMap = this.labelCountMapByCategory.get(categoryName);
      if (labelCountMap != null) {
        matchedCategoryNames.add(categoryName);
        recordDefinitionBuilder.addField(categoryName, DataTypes.LONG, 10);
        final Set<String> labels = labelCountMap.getLabels();
        allLabels.addAll(labels);
      }
    }
    final RecordDefinition recordDefinition = recordDefinitionBuilder.getRecordDefinition();
    try (
      RecordWriter recordWriter = RecordWriter.newRecordWriter(recordDefinition, target)) {
      final List<Object> row = new ArrayList<>(matchedCategoryNames.size() + 1);
      for (final String label : allLabels) {
        row.clear();
        row.add(label);
        for (final String categoryName : matchedCategoryNames) {
          final Long count = getCount(categoryName, label);
          row.add(count);
        }
        recordWriter.write(row);
      }
    }
  }
}
