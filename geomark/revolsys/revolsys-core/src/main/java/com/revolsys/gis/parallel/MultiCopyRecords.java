package com.revolsys.gis.parallel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.parallel.process.AbstractMultipleProcess;
import com.revolsys.parallel.process.Parallel;
import com.revolsys.parallel.process.Process;
import com.revolsys.parallel.process.ProcessNetwork;
import com.revolsys.parallel.process.Sequential;
import com.revolsys.process.CopyRecords;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class MultiCopyRecords implements Process {
  private String name;

  private Process process;

  private Map<String, Object> processDefinition;

  private ProcessNetwork processNetwork;

  private RecordStore sourceRecordStore;

  private RecordStore targetRecordStore;

  @Override
  public String getBeanName() {
    return this.name;
  }

  @Override
  public ProcessNetwork getProcessNetwork() {
    return this.processNetwork;
  }

  public RecordStore getSourceRecordStore() {
    return this.sourceRecordStore;
  }

  public RecordStore getTargetRecordStore() {
    return this.targetRecordStore;
  }

  @SuppressWarnings("unchecked")
  protected Process newProcess(final Map<String, Object> processDefinition) {
    if (processDefinition == null) {
      return null;
    } else {
      final String type = MapObjectFactory.getType(processDefinition);
      if ("copyRecords".equals(type)) {
        final PathName typePath = PathName.newPathName(processDefinition.get("typePath"));
        if (Property.hasValue(typePath)) {
          final boolean hasSequence = Maps.getBool(processDefinition, "hasSequence");
          final Map<String, Boolean> orderBy = Maps.get(processDefinition, "orderBy",
            Collections.<String, Boolean> emptyMap());
          final CopyRecords copy = new CopyRecords(this.sourceRecordStore, typePath, orderBy,
            this.targetRecordStore, hasSequence);
          return copy;
        } else {
          Logs.error(this, "Parameter 'typePath' required for type='copyRecords'");
        }
      } else if ("sequential".equals(type)) {
        final List<Map<String, Object>> processList = (List<Map<String, Object>>)processDefinition
          .get("processes");
        if (processList == null) {
          Logs.error(this, "Parameter 'processes' required for type='sequential'");
        } else {
          final Sequential processes = new Sequential();
          newProcesses(processes, processList);
          return processes;
        }
      } else if ("parallel".equals(type)) {
        final List<Map<String, Object>> processList = (List<Map<String, Object>>)processDefinition
          .get("processes");
        if (processList == null) {
          Logs.error(this, "Parameter 'processes' required for type='parallel'");
        } else {
          final Parallel processes = new Parallel();
          newProcesses(processes, processList);
          return processes;
        }

      } else {
        Logs.error(this,
          "Parameter type=" + type + " not in 'copyRecords', 'sequential', 'copyRecords'");
      }
      return null;
    }
  }

  private void newProcesses(final AbstractMultipleProcess processes,
    final List<Map<String, Object>> processDefinitions) {
    for (final Map<String, Object> processDefinition : processDefinitions) {
      final Process process = newProcess(processDefinition);
      if (process != null) {
        processes.addProcess(process);
      }
    }
  }

  @Override
  public void run() {
    this.process = newProcess(this.processDefinition);
    if (this.process != null) {
      if (this.processNetwork != null) {
        this.processNetwork.addProcess(this.process);
      } else {
        this.process.run();
      }
    }
  }

  @Override
  public void setBeanName(final String name) {
    this.name = name;
  }

  public void setProcessDefinition(final Map<String, Object> processDefinition) {
    this.processDefinition = processDefinition;
  }

  public void setProcessDefinitionResource(final Resource resource) {
    final Map<String, Object> processDefinition = Json.toMap(resource);
    setProcessDefinition(processDefinition);
  }

  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
    if (processNetwork != null) {
      processNetwork.addProcess(this);
    }
  }

  public void setSourceRecordStore(final RecordStore sourceRecordStore) {
    this.sourceRecordStore = sourceRecordStore;
  }

  public void setTargetRecordStore(final RecordStore targetRecordStore) {
    this.targetRecordStore = targetRecordStore;
  }
}
