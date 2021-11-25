package com.revolsys.log;

import java.io.File;
import java.util.Iterator;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;

public class LogbackUtil {

  static void addAppender(final Class<?> loggerName, final Appender<ILoggingEvent> appender) {
    if (!appender.isStarted()) {
      appender.start();
    }
    final Logger logger = getLogger(loggerName);
    logger.addAppender(appender);
  }

  public static void addAppender(final Class<ILoggingEvent> loggerName, final String pattern) {
    final Logger logger = getLogger(loggerName);

    addConsoleAppender(logger, pattern);
  }

  static void addAppender(final String loggerName, final Appender<ILoggingEvent> appender) {
    if (!appender.isStarted()) {
      appender.start();
    }
    final Logger logger = getLogger(loggerName);
    logger.addAppender(appender);
  }

  public static void addAppender(final String loggerName, final String pattern) {
    final Logger logger = getLogger(loggerName);
    addConsoleAppender(logger, pattern);
  }

  private static ConsoleAppender<ILoggingEvent> addConsoleAppender(final Logger logger,
    final String pattern) {
    final LoggerContext context = logger.getLoggerContext();

    final PatternLayout layout = newLayout(context, pattern);

    final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
    appender.setContext(context);
    appender.setLayout(layout);
    appender.start();

    logger.addAppender(appender);
    return appender;
  }

  public static FileAppender<ILoggingEvent> addFileAppender(final String name, final Logger logger,
    final File logFile, final String pattern, final boolean append) {
    final LoggerContext context = logger.getLoggerContext();
    final PatternLayout layout = newLayout(context, pattern);

    final FileAppender<ILoggingEvent> appender = new FileAppender<>();
    appender.setContext(context);
    appender.setName(name);
    appender.setAppend(append);
    appender.setImmediateFlush(true);
    final String absolutePath = logFile.getAbsolutePath();
    appender.setFile(absolutePath);
    appender.setLayout(layout);
    appender.start();
    logger.addAppender(appender);
    return appender;
  }

  public static void addRootAppender(final Appender<ILoggingEvent> appender) {
    final Logger logger = getRootLogger();
    if (!appender.isStarted()) {
      appender.start();
    }
    logger.addAppender(appender);
  }

  public static ConsoleAppender<ILoggingEvent> addRootAppender(final String pattern) {
    final Logger logger = getRootLogger();
    return addConsoleAppender(logger, pattern);
  }

  public static FileAppender<ILoggingEvent> addRootFileAppender(final File logFile,
    final String pattern, final boolean append) {
    final Logger logger = getRootLogger();
    return addFileAppender(logFile.getName(), logger, logFile, pattern, append);
  }

  public static Object addRootFileAppender(final File logFile, final String pattern,
    final boolean append, final String category, final String... messages) {
    final String name = logFile.getName();
    final Logger logger = getRootLogger();
    final FileAppender<ILoggingEvent> appender = addFileAppender(name, logger, logFile, pattern,
      append);
    for (final String message : messages) {
      final LoggingEvent event = new LoggingEvent();
      event.setLoggerName(category);
      event.setLevel(Level.INFO);
      event.setMessage(message.toString());
      appender.doAppend(event);
    }
    return appender;
  }

  public static Logger getLogger(final Class<?> loggerName) {
    Logger logger;
    if (loggerName == null) {
      logger = getRootLogger();
    } else {
      logger = (Logger)LoggerFactory.getLogger(loggerName);
    }
    return logger;
  }

  public static Logger getLogger(final String name) {
    Logger logger;
    if (name == null) {
      logger = getRootLogger();
    } else {
      logger = (Logger)LoggerFactory.getLogger(name);
    }
    return logger;
  }

  public static Logger getRootLogger() {
    return (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  }

  public static PatternLayout newLayout(final LoggerContext context, final String pattern) {
    final PatternLayout layout = new PatternLayout();
    layout.setContext(context);
    layout.setPattern(pattern);
    layout.start();
    return layout;
  }

  public static void removeAllAppenders() {
    final Logger logger = getRootLogger();
    removeAllAppenders(logger);
  }

  public static void removeAllAppenders(final Logger logger) {
    for (final Iterator<Appender<ILoggingEvent>> iterator = logger.iteratorForAppenders(); iterator
      .hasNext();) {
      final Appender<ILoggingEvent> appender = iterator.next();
      removeAppender(logger, appender);
    }
  }

  public static void removeAppender(final Logger logger, final Appender<ILoggingEvent> appender) {
    if (appender != null) {
      logger.detachAppender(appender);
      appender.stop();
    }
  }

  public static void removeRootAppender(final Appender<ILoggingEvent> appender) {
    final Logger logger = getRootLogger();
    removeAppender(logger, appender);
  }

  @SuppressWarnings("unchecked")
  public static void removeRootAppender(final Object appender) {
    if (appender instanceof Appender) {
      final Appender<ILoggingEvent> a = (Appender<ILoggingEvent>)appender;
      removeRootAppender(a);
    }
  }

  public static void setLevel(final String name, final Level level) {
    final Logger logger = getLogger(name);
    logger.setLevel(level);
  }

  public static void setLevel(final String name, final org.slf4j.event.Level level) {
    setLevel(name, level.toString());
  }

  public static void setLevel(final String name, final String level) {
    final Level level2 = Level.toLevel(level.toUpperCase());
    setLevel(name, level2);
  }

}
