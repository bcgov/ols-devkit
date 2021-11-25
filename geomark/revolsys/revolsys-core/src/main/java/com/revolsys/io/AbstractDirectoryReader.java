/*
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
package com.revolsys.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.filter.ExtensionFilenameFilter;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public abstract class AbstractDirectoryReader<T> extends AbstractReader<T> implements Iterator<T> {
  /** The list of base file names to read. */
  private final List<String> baseFileNames = new ArrayList<>();

  /** The current directory being processed. */
  private File currentFile;

  /** The current iterator. */
  private Iterator<T> currentIterator;

  /** The reader for the current directory being processed. */
  private Reader<T> currentReader;

  /** The directory of data to read from. */
  private File directory;

  /** The filter used to select files from the directory. */
  private FilenameFilter fileNameFilter;

  /** The files to be read by this reader. */
  private List<File> files;

  /** Flag indicating if the reader has more objects to be read. */
  private boolean hasNext = true;

  /** The files to be read by this reader. */
  private Iterator<Entry<File, Reader<T>>> readerIterator;

  private final Map<File, Reader<T>> readers = new LinkedHashMap<>();

  /**
   * Construct a new AbstractDirectoryReader.
   */
  public AbstractDirectoryReader() {
  }

  /**
   * Close the reader.
   */
  @Override
  public void close() {
    if (this.currentReader != null) {
      this.currentReader.close();
    }
  }

  /**
   * Get the list of base file names to read.
   *
   * @return The list of base file names to read.
   */
  public List<String> getBaseFileNames() {
    return this.baseFileNames;
  }

  /**
   * Get the directory containing the files to read.
   *
   * @return The directory containing the files to read.
   */
  public File getDirectory() {
    return this.directory;
  }

  /**
   * Get the filter used to select files from the directory.
   *
   * @return The filter used to select files from the directory.
   */
  public FilenameFilter getFileNameFilter() {
    return this.fileNameFilter;
  }

  /**
   * Get the files that are to be read by this reader. This must be overwritten
   * in sub classes to return the files in the working directory that are to be
   * read by instances of the write returned by {@link #newReader(File)}.
   *
   * @return The list of files.
   */
  protected List<File> getFiles() {
    final File[] files;
    if (this.fileNameFilter == null) {
      files = this.directory.listFiles();
    } else {
      files = this.directory.listFiles(this.fileNameFilter);
    }
    List<File> fileList;
    if (this.baseFileNames.isEmpty()) {
      Arrays.sort(files);
      fileList = Arrays.asList(files);
    } else {
      fileList = new ArrayList<>();
      final Map<String, File> fileBaseNameMap = new HashMap<>();
      for (final File file : files) {
        final String baseName = FileUtil.getBaseName(file).toUpperCase();
        fileBaseNameMap.put(baseName, file);
      }
      for (final String baseName : this.baseFileNames) {
        final File file = fileBaseNameMap.get(baseName);
        if (file != null) {
          fileList.add(file);
        }
      }
    }
    return fileList;
  }

  /**
   * Check to see if the reader has more data objects to be read.
   *
   * @return True if the reader has more data objects to be read.
   */
  @Override
  public boolean hasNext() {
    while (this.hasNext && (this.currentIterator == null || !this.currentIterator.hasNext())) {
      if (this.readerIterator.hasNext()) {
        if (this.currentReader != null) {
          try {
            this.currentReader.close();
          } catch (final Throwable t) {
            Logs.warn(this, t.getMessage(), t);
          }
        }
        final Entry<File, Reader<T>> entry = this.readerIterator.next();
        this.currentFile = entry.getKey();
        try {
          this.currentReader = entry.getValue();
          this.currentIterator = this.currentReader.iterator();
          this.hasNext = this.currentIterator.hasNext();
        } catch (final Throwable e) {
          this.hasNext = false;
          Logs.error(this, e.getMessage(), e);
        }
      } else {
        this.hasNext = false;
      }
    }
    return this.hasNext;
  }

  /**
   * Get the iterator.
   *
   * @return The iterator.
   */
  @Override
  public Iterator<T> iterator() {
    return this;
  }

  /**
   * Construct a new new {@link Reader} to read the file.
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  protected abstract Reader<T> newReader(Resource file);

  /**
   * Get the next data object read by this reader.
   *
   * @return The next Record.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  @Override
  public T next() {
    if (hasNext()) {
      final T record = this.currentIterator.next();
      return record;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  @PostConstruct
  public void open() {
    for (final File file : getFiles()) {
      final PathResource resource = new PathResource(file);
      final Reader<T> reader = newReader(resource);
      reader.open();
      if (reader != null) {
        this.readers.put(file, reader);
      }
    }
    this.readerIterator = this.readers.entrySet().iterator();
    hasNext();
  }

  /**
   * Removing data objects is not supported.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * Set the list of base file names to read.
   *
   * @param baseFileNames The list of base file names to read.
   */
  public void setBaseFileNames(final Collection<String> baseFileNames) {
    this.baseFileNames.clear();
    for (final String name : baseFileNames) {
      this.baseFileNames.add(name.toUpperCase());
    }
  }

  /**
   * Set the directory containing the files to read.
   *
   * @param directory The directory containing the files to read.
   */
  public void setDirectory(final File directory) {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("File must exist and be a directory " + directory);
    } else {
      this.directory = directory;
      this.files = getFiles();

    }
  }

  public void setFileExtensions(final Collection<String> fileExtensions) {
    this.fileNameFilter = new ExtensionFilenameFilter(fileExtensions);
  }

  public void setFileExtensions(final String... fileExtensions) {
    setFileExtensions(Arrays.asList(fileExtensions));
  }

  /**
   * Set the filter used to select files from the directory.
   *
   * @param fileNameFilter The filter used to select files from the directory.
   */
  public void setFileNameFilter(final FilenameFilter fileNameFilter) {
    this.fileNameFilter = fileNameFilter;
  }

}
