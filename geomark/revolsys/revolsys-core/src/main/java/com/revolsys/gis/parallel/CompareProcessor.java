package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.revolsys.geometry.algorithm.linematch.LineMatchGraph;
import com.revolsys.geometry.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.geometry.filter.LineIntersectsFilter;
import com.revolsys.geometry.index.PointRecordMap;
import com.revolsys.geometry.index.RecordSpatialIndex;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.RecordLog;
import com.revolsys.record.Records;
import com.revolsys.record.filter.RecordGeometryFilter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.count.LabelCountMap;
import com.revolsys.util.count.LabelCounters;

public class CompareProcessor extends AbstractMergeProcess {

  private final boolean cleanDuplicatePoints = true;

  private LabelCounters duplicateOtherStatistics = new LabelCountMap("Duplicate Other");

  private LabelCounters duplicateSourceStatistics = new LabelCountMap("Duplicate Source");

  private Function<Record, Predicate<Record>> equalFilterFactory;

  private LabelCounters equalStatistics = new LabelCountMap("Equal");

  private Predicate<Record> excludeFilter;

  private LabelCounters excludeNotEqualOtherStatistics = new LabelCountMap(
    "Exclude Not Equal Other");

  private LabelCounters excludeNotEqualSourceStatistics = new LabelCountMap(
    "Exclude Not Equal Source");

  private String label;

  private boolean logNotEqualSource = true;

  private LabelCounters notEqualOtherStatistics = new LabelCountMap("Not Equal Other");

  private LabelCounters notEqualSourceStatistics = new LabelCountMap("Not Equal Source");

  private RecordSpatialIndex<Record> otherIndex;

  private PointRecordMap otherPointMap = new PointRecordMap();

  private Set<Record> sourceObjects = new LinkedHashSet<>();

  private final PointRecordMap sourcePointMap = new PointRecordMap();

  @Override
  protected void addOtherObject(final Record record) {
    final Geometry geometry = record.getGeometry();
    if (geometry instanceof Point) {
      boolean add = true;
      if (this.cleanDuplicatePoints) {
        final List<Record> objects = this.otherPointMap.getRecords(record);
        if (!objects.isEmpty()) {
          final Predicate<Record> filter = this.equalFilterFactory.apply(record);
          add = !Predicates.matches(objects, filter);
        }
        if (add) {
          this.otherPointMap.addRecord(record);
        } else {
          this.duplicateOtherStatistics.addCount(record);
        }
      }
    } else if (geometry instanceof LineString) {
      this.otherIndex.addRecord(record);
    }
  }

  @Override
  protected void addSourceObject(final Record object) {
    final Geometry geometry = object.getGeometry();
    if (geometry instanceof Point) {
      boolean add = true;
      if (this.cleanDuplicatePoints) {
        final List<Record> objects = this.sourcePointMap.getRecords(object);
        if (!objects.isEmpty()) {
          final Predicate<Record> filter = this.equalFilterFactory.apply(object);
          add = !Predicates.matches(objects, filter);
        }
      }
      if (add) {
        this.sourcePointMap.addRecord(object);
      } else {
        this.duplicateSourceStatistics.addCount(object);
      }
    } else if (geometry instanceof LineString) {
      this.sourceObjects.add(object);
    }
  }

  public LabelCounters getDuplicateOtherStatistics() {
    return this.duplicateOtherStatistics;
  }

  public LabelCounters getDuplicateSourceStatistics() {
    return this.duplicateSourceStatistics;
  }

  public Function<Record, Predicate<Record>> getEqualFilterFactory() {
    return this.equalFilterFactory;
  }

  public LabelCounters getEqualStatistics() {
    return this.equalStatistics;
  }

  public Predicate<Record> getExcludeFilter() {
    return this.excludeFilter;
  }

  public LabelCounters getExcludeNotEqualOtherStatistics() {
    return this.excludeNotEqualOtherStatistics;
  }

  public LabelCounters getExcludeNotEqualSourceStatistics() {
    return this.excludeNotEqualSourceStatistics;
  }

  public String getLabel() {
    return this.label;
  }

  public LabelCounters getNotEqualOtherStatistics() {
    return this.notEqualOtherStatistics;
  }

  public LabelCounters getNotEqualSourceStatistics() {
    return this.notEqualSourceStatistics;
  }

  @Override
  protected void init(final RecordDefinition recordDefinition) {
    super.init(recordDefinition);
    this.otherIndex = RecordSpatialIndex.quadTree(recordDefinition.getGeometryFactory());
  }

  public boolean isLogNotEqualSource() {
    return this.logNotEqualSource;
  }

  private void logError(final Record object, final String message, final boolean source) {
    if (this.excludeFilter == null || !this.excludeFilter.test(object)) {
      if (source) {
        this.notEqualSourceStatistics.addCount(object);
      } else {
        this.notEqualOtherStatistics.addCount(object);
      }
      RecordLog.error(getClass(), message, object);
    } else {
      if (source) {
        this.excludeNotEqualSourceStatistics.addCount(object);
      } else {
        this.excludeNotEqualOtherStatistics.addCount(object);
      }
    }
  }

  private void processExactLineMatch(final Record sourceObject) {
    final LineString sourceLine = sourceObject.getGeometry();
    final LineEqualIgnoreDirectionFilter lineEqualFilter = new LineEqualIgnoreDirectionFilter(
      sourceLine, 3);
    final Predicate<Record> geometryFilter = new RecordGeometryFilter<>(lineEqualFilter);
    final Predicate<Record> equalFilter = this.equalFilterFactory.apply(sourceObject);
    final Predicate<Record> filter = equalFilter.and(geometryFilter);

    final Record otherObject = this.otherIndex.queryFirst(sourceObject, filter);
    if (otherObject != null) {
      this.equalStatistics.addCount(sourceObject);
      removeObject(sourceObject);
      removeOtherObject(otherObject);
    }
  }

  private void processExactLineMatches() {
    for (final Record object : new ArrayList<>(this.sourceObjects)) {
      processExactLineMatch(object);
    }
  }

  private void processExactPointMatch(final Record sourceObject) {
    final Predicate<Record> equalFilter = this.equalFilterFactory.apply(sourceObject);
    final Record otherObject = this.otherPointMap.getFirstMatch(sourceObject, equalFilter);
    if (otherObject != null) {
      final Point sourcePoint = sourceObject.getGeometry();
      final double sourceZ = sourcePoint.getZ();

      final Point otherPoint = otherObject.getGeometry();
      final double otherZ = otherPoint.getZ();

      if (sourceZ == otherZ || Double.isNaN(sourceZ) && Double.isNaN(otherZ)) {
        this.equalStatistics.addCount(sourceObject);
        removeObject(sourceObject);
        removeOtherObject(otherObject);
      }
    }
  }

  private void processExactPointMatches() {
    for (final Record object : new ArrayList<>(this.sourcePointMap.getAll())) {
      processExactPointMatch(object);
    }
  }

  @Override
  protected void processObjects(final RecordDefinition recordDefinition,
    final Channel<Record> out) {
    if (this.otherIndex.getSize() + this.otherPointMap.size() == 0) {
      if (this.logNotEqualSource) {
        for (final Record object : this.sourceObjects) {
          logError(object, "Source missing in Other", true);
        }
      }
    } else {
      processExactPointMatches();
      processExactLineMatches();
      processPartialMatches();
    }
    for (final Record object : this.otherIndex.getItems()) {
      logError(object, "Other missing in Source", false);
    }
    for (final Record record : this.otherPointMap.getAll()) {
      logError(record, "Other missing in Source", false);
    }
    if (this.logNotEqualSource) {
      for (final Record object : this.sourceObjects) {
        logError(object, "Source missing in Other", true);
      }
    }
    this.sourceObjects.clear();
    this.otherIndex = null;
    this.otherPointMap.clear();
  }

  private void processPartialMatch(final Record sourceObject) {
    final Geometry sourceGeometry = sourceObject.getGeometry();
    if (sourceGeometry instanceof LineString) {
      final LineString sourceLine = (LineString)sourceGeometry;

      final LineIntersectsFilter intersectsFilter = new LineIntersectsFilter(sourceLine);
      final Predicate<Record> geometryFilter = new RecordGeometryFilter<>(intersectsFilter);
      final Predicate<Record> equalFilter = this.equalFilterFactory.apply(sourceObject);
      final Predicate<Record> filter = equalFilter.and(geometryFilter);
      final List<Record> otherObjects = this.otherIndex.queryList(sourceGeometry, filter);
      if (!otherObjects.isEmpty()) {
        final LineMatchGraph<Record> graph = new LineMatchGraph<>(sourceObject, sourceLine);
        for (final Record otherObject : otherObjects) {
          final LineString otherLine = otherObject.getGeometry();
          graph.add(otherLine);
        }
        final Lineal nonMatchedLines = graph.getNonMatchedLines(0);
        if (nonMatchedLines.isEmpty()) {
          removeObject(sourceObject);

        } else {
          removeObject(sourceObject);
          if (nonMatchedLines.getGeometryCount() == 1
            && nonMatchedLines.getGeometry(0).getLength() == 1) {
          } else {
            for (int j = 0; j < nonMatchedLines.getGeometryCount(); j++) {
              final Geometry newGeometry = nonMatchedLines.getGeometry(j);
              final Record newObject = Records.copy(sourceObject, newGeometry);
              addSourceObject(newObject);
            }
          }
        }
        for (int i = 0; i < otherObjects.size(); i++) {
          final Record otherObject = otherObjects.get(i);
          final Lineal otherNonMatched = graph.getNonMatchedLines(i + 1, 0);
          for (int j = 0; j < otherNonMatched.getGeometryCount(); j++) {
            final Geometry newGeometry = otherNonMatched.getGeometry(j);
            final Record newOtherObject = Records.copy(otherObject, newGeometry);
            addOtherObject(newOtherObject);
          }
          removeOtherObject(otherObject);
        }
      }
    }
  }

  private void processPartialMatches() {
    for (final Record object : new ArrayList<>(this.sourceObjects)) {
      processPartialMatch(object);
    }
  }

  private void removeObject(final Record object) {
    this.sourceObjects.remove(object);
  }

  private void removeOtherObject(final Record object) {
    final Geometry geometry = object.getGeometry();
    if (geometry instanceof Point) {
      this.otherPointMap.removeRecord(object);
    } else {
      this.otherIndex.removeRecord(object);
    }
  }

  public void setEqualFilterFactory(final Function<Record, Predicate<Record>> equalFilterFactory) {
    this.equalFilterFactory = equalFilterFactory;
  }

  public void setExcludeFilter(final Predicate<Record> excludeFilter) {
    this.excludeFilter = excludeFilter;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setLogNotEqualSource(final boolean logNotEqualSource) {
    this.logNotEqualSource = logNotEqualSource;
  }

  @Override
  protected void setUp() {
    this.equalStatistics.connect();
    this.notEqualSourceStatistics.connect();
    this.notEqualOtherStatistics.connect();
    this.duplicateSourceStatistics.connect();
    this.duplicateOtherStatistics.connect();
    this.excludeNotEqualSourceStatistics.connect();
    this.excludeNotEqualOtherStatistics.connect();
  }

  @Override
  protected void tearDown() {
    this.sourceObjects = null;
    this.sourcePointMap.clear();
    this.otherPointMap = null;
    this.otherIndex = null;
    this.equalStatistics.disconnect();
    this.notEqualSourceStatistics.disconnect();
    this.notEqualOtherStatistics.disconnect();
    this.duplicateSourceStatistics.disconnect();
    this.duplicateOtherStatistics.disconnect();
    this.excludeNotEqualSourceStatistics.disconnect();
    this.excludeNotEqualOtherStatistics.disconnect();
    this.equalStatistics = null;
    this.notEqualSourceStatistics = null;
    this.notEqualOtherStatistics = null;
    this.duplicateSourceStatistics = null;
    this.duplicateOtherStatistics = null;
    this.excludeNotEqualSourceStatistics = null;
    this.excludeNotEqualOtherStatistics = null;
  }
}
