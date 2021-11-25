/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jeometry.common.logging.Logs;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.revolsys.io.FileUtil;
import com.revolsys.parallel.process.ProcessNetwork;

public class ProcessorPipelineTool {
  private static final String EXCLUDE_PATTERN = "exclude";

  private static final String EXCLUDE_PATTERN_OPTION = "x";

  private static final String LOG_DIRECTORY = "logDirectory";

  private static final String LOG_DIRECTORY_OPTION = "l";

  private static final String OUTPUT_DIRECTORY = "outputDirectory";

  private static final String OUTPUT_DIRECTORY_OPTION = "o";

  private static final String SCRIPT = "script";

  private static final String SCRIPT_OPTION = "s";

  private static final String SOURCE_DIRECTORY = "sourceDirectory";

  private static final String SOURCE_DIRECTORY_OPTION = "V";

  private static final String SOURCE_FILE_EXTENSION_OPTION = "e";

  private static final String SOURCE_FLE_EXTENSION = "sourceFileExtension";

  private static Throwable getBeanExceptionCause(final BeanCreationException e) {
    Throwable cause = e.getCause();
    while (cause instanceof BeanCreationException || cause instanceof MethodInvocationException
      || cause instanceof PropertyAccessException) {
      final Throwable newCause = cause.getCause();
      if (newCause != null) {
        cause = newCause;
      }
    }
    return cause;
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    final ProcessorPipelineTool app = new ProcessorPipelineTool();
    app.start(args);
  }

  private CommandLine commandLine;

  private String excludePattern;

  private File logDirectory;

  private final Options options = new Options();

  private File scriptFile;

  private File sourceDirectory;

  private File sourceFile;

  private String sourceFileExtension;

  private File targetDirectory;

  private File targetFile;

  public ProcessorPipelineTool() {
    newOptions();
  }

  private void newOptions() {
    final Option script = new Option(SCRIPT_OPTION, SCRIPT, true,
      "the script file that defines the processor pipeline");
    script.setRequired(true);
    this.options.addOption(script);

    final Option sourceDirectory = new Option(SOURCE_DIRECTORY_OPTION, SOURCE_DIRECTORY, true,
      "the location of the source files to process");
    sourceDirectory.setRequired(false);
    this.options.addOption(sourceDirectory);

    final Option sourceFileExtension = new Option(SOURCE_FILE_EXTENSION_OPTION,
      SOURCE_FLE_EXTENSION, true, "the file extension of the source files (e.g. .saf)");
    sourceFileExtension.setRequired(false);
    this.options.addOption(sourceFileExtension);

    final Option outputDirectory = new Option(OUTPUT_DIRECTORY_OPTION, OUTPUT_DIRECTORY, true,
      "the directory to write processed files to");
    outputDirectory.setRequired(false);
    this.options.addOption(outputDirectory);

    final Option logDirectory = new Option(LOG_DIRECTORY_OPTION, LOG_DIRECTORY, true,
      "the directory to write log files to");
    logDirectory.setRequired(false);
    this.options.addOption(logDirectory);

    final Option excludePattern = new Option(EXCLUDE_PATTERN_OPTION, EXCLUDE_PATTERN, true,
      "exclude files matching a regular expression (e.g. '.*_back.zip");
    excludePattern.setRequired(false);
    this.options.addOption(excludePattern);

    final Option property = new Option("D", "property=value", true, "use value for given property");
    property.setValueSeparator('=');
    this.options.addOption(property);
  }

  @SuppressWarnings("unchecked")
  public boolean processArguments(final String[] args) {
    try {
      final CommandLineParser parser = new PosixParser();
      this.commandLine = parser.parse(this.options, args);
      final List<String> arguments = this.commandLine.getArgList();
      final Option[] options = this.commandLine.getOptions();
      for (final Option option : options) {
        final String shortOpt = option.getOpt();
        if (shortOpt != null && shortOpt.equals("D")) {
          final String argument = arguments.remove(0);
          final String[] values = argument.split("=");
          System.setProperty(values[0], values[1]);
        }

      }
      if (this.commandLine.hasOption(SOURCE_DIRECTORY_OPTION)) {
        this.sourceDirectory = new File(this.commandLine.getOptionValue(SOURCE_DIRECTORY_OPTION));
        if (!this.sourceDirectory.isDirectory()) {
          System.err.println("Source directory '" + this.sourceDirectory.getAbsolutePath()
            + "' does not exist or is not a directory");
          return false;
        }
      }
      if (this.commandLine.hasOption(SOURCE_FILE_EXTENSION_OPTION)) {
        this.sourceFileExtension = this.commandLine.getOptionValue(SOURCE_FILE_EXTENSION_OPTION);
      }
      if (this.commandLine.hasOption(OUTPUT_DIRECTORY_OPTION)) {
        this.targetDirectory = new File(this.commandLine.getOptionValue(OUTPUT_DIRECTORY_OPTION));
        if (!this.targetDirectory.isDirectory()) {
          System.err.println("Target directory '" + this.targetDirectory.getAbsolutePath()
            + "' does not exist or is not a directory");
          return false;
        }
      }
      if (this.commandLine.hasOption(LOG_DIRECTORY_OPTION)) {
        this.logDirectory = new File(this.commandLine.getOptionValue(LOG_DIRECTORY_OPTION));
        if (!this.logDirectory.isDirectory()) {
          System.err.println("Log directory '" + this.logDirectory.getAbsolutePath()
            + "' does not exist or is not a directory");
          return false;
        }
      }
      this.scriptFile = new File(this.commandLine.getOptionValue(SCRIPT_OPTION));
      if (!this.scriptFile.exists()) {
        System.err.println("The script '" + this.scriptFile + "' does not exist");
        return false;
      }
      this.excludePattern = this.commandLine.getOptionValue(EXCLUDE_PATTERN_OPTION);
      if (this.sourceDirectory != null) {
        if (this.targetDirectory == null) {
          System.err.println("A " + OUTPUT_DIRECTORY + " must be specified if " + SOURCE_DIRECTORY
            + " is specified");
          return false;
        }
        if (this.sourceFileExtension == null) {
          System.err.println("A " + SOURCE_FLE_EXTENSION + " must be specified if "
            + SOURCE_DIRECTORY + " is specified");
          return false;
        }
      } else {
        this.sourceFile = new File(arguments.get(0));
        if (!this.sourceFile.exists()) {
          System.err.println("The file '" + this.sourceFile + "' does not exist");
          return false;
        }
        this.targetFile = new File(arguments.get(1));
        // if (targetFile.isDirectory()) {
        // targetFile = new File(targetFile, sourceFile.getName());
        // }
      }
      return true;
    } catch (final MissingOptionException e) {
      System.err.println("Missing " + e.getMessage() + " argument");
      return false;
    } catch (final ParseException e) {
      System.err.println("Unable to process command line arguments: " + e.getMessage());
      return false;
    }
  }

  private void processDirectory(final File sourceDirectory, final File targetDirectory,
    final File logDirectory, final String sourceFileExtension) {
    System.out.println("Processing directory '" + sourceDirectory.getAbsolutePath() + "'");
    final File[] files = sourceDirectory.listFiles();
    for (final File file : files) {
      final String fileName = FileUtil.getFileName(file);
      if (file.isDirectory()) {
        processDirectory(file, new File(targetDirectory, fileName),
          new File(logDirectory, fileName), sourceFileExtension);
      } else if (fileName.endsWith(sourceFileExtension)) {
        processFile(file, new File(targetDirectory, fileName),
          new File(logDirectory, fileName + ".log"));
      }
    }
  }

  private void processFile(final File sourceFile, final File targetFile, final File logFile) {
    final long startTime = System.currentTimeMillis();
    if (this.excludePattern != null) {
      try {
        if (sourceFile.getCanonicalPath().matches(this.excludePattern)) {
          return;
        }
      } catch (final IOException e) {
        Logs.error(this, e.getMessage(), e);
      }
    }

    Logs.info(this, "Processing file '" + sourceFile + "' to '" + targetFile + "'");
    System.out.println("Processing file '" + sourceFile + "' to '" + targetFile + "'");

    System.setProperty("sourceFile", sourceFile.getAbsolutePath());
    System.setProperty("targetFile", targetFile.getAbsolutePath());
    final BeanFactory beans = new FileSystemXmlApplicationContext(
      "file:" + this.scriptFile.getAbsolutePath());
    try {
      final File parentFile = targetFile.getParentFile();
      if (parentFile != null) {
        parentFile.mkdirs();
      }
      final Object bean = beans.getBean("pipeline");
      final ProcessNetwork pipeline = (ProcessNetwork)bean;
      pipeline.startAndWait();
    } catch (final BeanCreationException e) {
      final Throwable cause = getBeanExceptionCause(e);
      cause.printStackTrace();
    }
    final long endTime = System.currentTimeMillis();
    final long time = endTime - startTime;
    long seconds = time / 1000;
    final long minutes = seconds / 60;
    seconds = seconds % 60;
    Logs.info(this, minutes + " minutes " + seconds + " seconds");
    System.out.println(minutes + " minutes " + seconds + " seconds");

  }

  private void run() {
    if (this.sourceFile != null) {
      final String baseName = FileUtil.getFileNamePrefix(this.targetFile);
      if (this.logDirectory == null) {
        final File parentDirectory = this.targetFile.getParentFile();
        if (parentDirectory == null) {
          this.logDirectory = new File(baseName);
        } else {
          this.logDirectory = new File(parentDirectory, baseName);
        }
      }
      this.logDirectory.mkdirs();
      final File logFile = new File(this.logDirectory, baseName + ".log");

      processFile(this.sourceFile, this.targetFile, logFile);
    } else {
      processDirectory(this.sourceDirectory, this.targetDirectory, this.logDirectory,
        this.sourceFileExtension);
    }
  }

  public void start(final String[] args) {
    if (processArguments(args)) {
      run();
    } else {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("processorPipeline", this.options);
    }

  }
}
