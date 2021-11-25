package com.revolsys.geometry.graph.process;

import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.util.ObjectProcessor;

public class GraphProcessor extends BaseInOutProcess<Record, Record> {
  private RecordGraph graph;

  private GeometryFactory precisionModel;

  private List<ObjectProcessor<RecordGraph>> processors = new ArrayList<>();

  public GeometryFactory getPrecisionModel() {
    return this.precisionModel;
  }

  public List<ObjectProcessor<RecordGraph>> getProcessors() {
    return this.processors;
  }

  @Override
  protected void initializeDo() {
    this.graph = new RecordGraph();
    if (this.precisionModel != null) {
      this.graph.setPrecisionModel(this.precisionModel);
    }
  }

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    if (out != null) {
      processGraph();
      for (final Edge<Record> edge : this.graph.getEdges()) {
        final Record object = edge.getObject();
        out.write(object);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      this.graph.addEdge(object, line);
    } else {
      if (out != null) {
        out.write(object);
      }
    }
  }

  private void processGraph() {
    if (this.graph != null) {
      for (final ObjectProcessor<RecordGraph> processor : this.processors) {
        Logs.info(this, processor.getClass().getName());
        processor.process(this.graph);
      }
    }
  }

  public void setPrecisionModel(final GeometryFactory precisionModel) {
    this.precisionModel = precisionModel;
  }

  public void setProcessors(final List<ObjectProcessor<RecordGraph>> processors) {
    this.processors = processors;
  }
}
