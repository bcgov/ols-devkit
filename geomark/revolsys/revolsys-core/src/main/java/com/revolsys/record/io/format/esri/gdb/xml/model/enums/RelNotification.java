package com.revolsys.record.io.format.esri.gdb.xml.model.enums;

public enum RelNotification {
  /**
   * Backward - messages are sent only from destination objects to source
   * objects
   */
  esriRelNotificationBackward,

  /** Both - messages are sent in both directions */
  esriRelNotificationBoth,

  /**
   * Forward - messages are sent only from origin objects to destination objects
   */
  esriRelNotificationForward,

  /** None - no messages are sent */
  esriRelNotificationNone;
}
