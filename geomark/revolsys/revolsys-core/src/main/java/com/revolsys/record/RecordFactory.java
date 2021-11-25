package com.revolsys.record;

import com.revolsys.record.schema.RecordDefinition;

/**
 * A Record factory
 *
 */
@FunctionalInterface
public interface RecordFactory<R extends Record> {
  /**
   * Construct a new Record using the record definition
   *
   * @param recordDefinition The record definition used to construct the instance.
   * @return The Record instance.
   */
  R newRecord(RecordDefinition recordDefinition);
}
