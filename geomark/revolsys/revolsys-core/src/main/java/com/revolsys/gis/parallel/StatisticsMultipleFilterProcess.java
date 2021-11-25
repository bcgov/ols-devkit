package com.revolsys.gis.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.MultipleFilterProcess;
import com.revolsys.record.Record;
import com.revolsys.util.count.LabelCountMap;
import com.revolsys.util.count.LabelCounters;

public class StatisticsMultipleFilterProcess extends MultipleFilterProcess<Record> {

  private final Map<Predicate<Record>, LabelCountMap> statisticsMap = new HashMap<>();

  private String statisticsName;

  private boolean useStatistics;

  @Override
  protected void destroy() {
    super.destroy();
    for (final LabelCounters stats : this.statisticsMap.values()) {
      stats.disconnect();
    }
  }

  /**
   * @return the statisticsName
   */
  public String getStatisticsName() {
    return this.statisticsName;
  }

  /**
   * @return the useStatistics
   */
  public boolean isUseStatistics() {
    return this.useStatistics;
  }

  @Override
  protected boolean processPredicate(final Record object, final Predicate<Record> filter,
    final Channel<Record> filterOut) {
    if (super.processPredicate(object, filter, filterOut)) {
      if (this.useStatistics) {
        LabelCountMap stats = this.statisticsMap.get(filter);
        String name;
        if (stats == null) {
          if (this.statisticsName != null) {
            name = this.statisticsName + " " + filter.toString();
          } else {
            name = filter.toString();
          }
          stats = new LabelCountMap(name);
          stats.connect();
          this.statisticsMap.put(filter, stats);
        }
        stats.addCount(object);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param statisticsName the statisticsName to set
   */
  public void setStatisticsName(final String statisticsName) {
    this.statisticsName = statisticsName;
  }

  /**
   * @param useStatistics the useStatistics to set
   */
  public void setUseStatistics(final boolean useStatistics) {
    this.useStatistics = useStatistics;
  }

}
