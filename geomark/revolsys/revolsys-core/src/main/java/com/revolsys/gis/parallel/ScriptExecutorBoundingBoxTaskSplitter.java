package com.revolsys.gis.parallel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.parallel.channel.ChannelInput;
import com.revolsys.parallel.channel.ChannelOutput;
import com.revolsys.parallel.tools.ScriptExecutorRunnable;
import com.revolsys.record.Record;

public class ScriptExecutorBoundingBoxTaskSplitter extends BoundingBoxTaskSplitter {

  private Map<String, Object> attributes = new LinkedHashMap<>();

  private Map<String, Object> beans = new LinkedHashMap<>();

  private Map<String, ChannelInput<?>> inChannels = new LinkedHashMap<>();

  private Map<String, ChannelOutput<?>> outChannels = new LinkedHashMap<>();

  private OutsideBoundaryObjects outsideBoundaryObjects = new OutsideBoundaryObjects();

  private String scriptName;

  @Override
  public void execute(final BoundingBox boundingBox) {
    this.outsideBoundaryObjects.expandBoundary(boundingBox.toGeometry());

    final ScriptExecutorRunnable executor = new ScriptExecutorRunnable(this.scriptName,
      this.attributes);
    executor.setLogScriptInfo(isLogScriptInfo());
    executor.addBean("boundingBox", boundingBox);
    final Set<Record> objects = this.outsideBoundaryObjects.getAndClearObjects();
    executor.addBean("outsideBoundaryObjects", objects);
    executor.addBeans(this.beans);
    executor.addBeans(this.inChannels);
    executor.addBeans(this.outChannels);
    executor.run();
  }

  public Map<String, Object> getBeans() {
    return this.beans;
  }

  public Map<String, Object> getFields() {
    return this.attributes;
  }

  public Map<String, ChannelInput<?>> getInChannels() {
    return this.inChannels;
  }

  public Map<String, ChannelOutput<?>> getOutChannels() {
    return this.outChannels;
  }

  public OutsideBoundaryObjects getOutsideBoundaryObjects() {
    return this.outsideBoundaryObjects;
  }

  public String getScriptName() {
    return this.scriptName;
  }

  @Override
  protected void postRun() {
    super.postRun();
    for (final ChannelInput<?> in : this.inChannels.values()) {
      in.readDisconnect();
    }
    for (final ChannelOutput<?> out : this.outChannels.values()) {
      out.writeDisconnect();
    }

  }

  @Override
  protected void preRun() {
    super.preRun();
    for (final ChannelInput<?> in : this.inChannels.values()) {
      in.readConnect();
    }
    for (final ChannelOutput<?> out : this.outChannels.values()) {
      out.writeConnect();
    }
  }

  public void setAttributes(final Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setBeans(final Map<String, Object> beans) {
    this.beans = beans;
  }

  public void setInChannels(final Map<String, ChannelInput<?>> inChannels) {
    this.inChannels = inChannels;
  }

  public void setOutChannels(final Map<String, ChannelOutput<?>> outChannels) {
    this.outChannels = outChannels;
  }

  public void setOutsideBoundaryObjects(final OutsideBoundaryObjects outsideBoundaryObjects) {
    this.outsideBoundaryObjects = outsideBoundaryObjects;
  }

  public void setScriptName(final String scriptName) {
    this.scriptName = scriptName;
  }

}
