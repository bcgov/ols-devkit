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
package com.revolsys.record.io.format.saif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.saif.util.PathCache;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.record.schema.RecordDefinitionFactoryImpl;
import com.revolsys.spring.resource.ClassPathResource;
import com.revolsys.spring.resource.Resource;

/**
 * <p>
 * The SaifReader.
 * </p>
 *
 * @author Paul Austin
 * @see SaifWriter
 */
public class SaifReader extends AbstractReader<Record>
  implements Iterator<Record>, RecordDefinitionFactory, RecordReader {

  /** The current data object that was read. */
  private Record currentRecord;

  /** The schema definition declared in the SAIF archive. */
  private RecordDefinitionFactory declaredRecordDefinitionFactory;

  /** List of type names to exclude from reading. */
  private final Set<String> excludeTypeNames = new LinkedHashSet<>();

  /** The list of exported objects. */
  private Record exportedObjects;

  private RecordFactory factory = ArrayRecord.FACTORY;

  /** The SAIF archive file. */
  private File file;

  /** Mapping between file names and type names. */
  private final Map<String, String> fileNameTypeNameMap = new HashMap<>();

  /** The global metatdata for the archive. */
  private Record globalMetadata;

  /** Flag indicating if the iterator has more objects. */
  private boolean hasNext;

  /** The list of imported objects. */
  private Record importedObjects;

  /** List of type names to include for reading. */
  private final Set<String> includeTypeNames = new LinkedHashSet<>();

  /** The list of internally referenced objects. */
  private Record internallyReferencedObjects;

  /** Flag indicating if a new data object should be read. */
  private boolean loadNewObject = true;

  private boolean opened = false;

  /** The iterator for the current object set. */
  private OsnReader osnReader;

  /** The schema definition that will be set on each data object. */
  private RecordDefinitionFactory recordDefinitionFactory;

  /** The directory the SAIF archive is extracted to. */
  private File saifArchiveDirectory;

  private int srid = EpsgId.nad83Utm(10);

  /** Mapping between type names and file names. */
  private final Map<String, String> typePathFileNameMap = new HashMap<>();

  /** The iterator of object subsets for the archive. */
  private Iterator<String> typePathIterator;

  private List<String> typePaths;

  /** The zip file. */
  private ZipFile zipFile;

  public SaifReader() {
  }

  /**
   * Construct a new new SaifReader to read the SAIF archive from the specified file .
   * If the file is a directory, then in must contain an expanded SAIF archive,
   * otherwise the file must be a compressed SAIF archive (.zip or.saf).
   *
   * @param file The SAIF archive file to read.
   */
  public SaifReader(final File file) {
    setFile(file);
  }

  public SaifReader(final Resource resource) {
    setFile(Resource.getFileOrCreateTempFile(resource));
  }

  /**
   * Construct a new new SaifReader to read the SAIF archive from the specified file
   * name. If the file is a directory, then in must contain an expanded SAIF
   * archive, otherwise the file must be a compressed SAIF archive (.zip
   * or.saf).
   *
   * @param fileName The name of the SAIF archive file to read.
   */
  public SaifReader(final String fileName) {
    this(new File(fileName));
  }

  /**
   * Close the SAIF archive.
   */
  @Override
  public void close() {
    if (Logs.isDebugEnabled(this)) {
      Logs.debug(this, "Closing SAIF archive '" + this.file.getAbsolutePath() + "'");
    }
    closeCurrentReader();
    if (!this.file.isDirectory() && this.saifArchiveDirectory != null) {
      if (Logs.isDebugEnabled(this)) {
        Logs.debug(this, "  Deleting temporary files");
      }
      FileUtil.deleteDirectory(this.saifArchiveDirectory);
    }
    if (Logs.isDebugEnabled(this)) {
      Logs.debug(this, "  Finished closing file");
    }
  }

  private void closeCurrentReader() {
    if (this.osnReader != null) {
      this.osnReader.close();
      this.osnReader = null;
    }
  }

  /**
   * Get the schema definition declared in the SAIF archive.
   *
   * @return The schema definition.
   */
  public RecordDefinitionFactory getDeclaredRecordDefinitionFactory() {
    return this.declaredRecordDefinitionFactory;
  }

  /**
   * Get the list of exported objects for the SAIF archive.
   *
   * @return The exported objects.
   */
  public Record getExportedObjects() {
    return this.exportedObjects;
  }

  /**
   * @return the factory
   */
  public RecordFactory getFactory() {
    return this.factory;
  }

  public File getFile() {
    return this.file;
  }

  private String getFileName(final String typePath) {
    return this.typePathFileNameMap.get(typePath);
  }

  /**
   * Get the global metatdata for the SAIF archive.
   *
   * @return The global metadata.
   */
  public Record getGlobalMetadata() {
    if (this.globalMetadata == null) {
      try {
        loadGlobalMetadata();
      } catch (final IOException e) {
        throw new RuntimeException("Unable to load globmeta.osn: " + e.getMessage());
      }
    }
    return this.globalMetadata;
  }

  @Override
  public int getHorizontalCoordinateSystemId() {
    return this.srid;
  }

  /**
   * Get the list of imported objects for the SAIF archive.
   *
   * @return The imported objects.
   */
  public Record getImportedObjects() {
    if (this.importedObjects == null) {
      try {
        loadImportedObjects();
      } catch (final IOException e) {
        throw new RuntimeException("Unable to load imports.dir: " + e.getMessage());
      }
    }
    return this.importedObjects;
  }

  private InputStream getInputStream(final String fileName) throws IOException {
    if (this.zipFile != null) {
      final ZipEntry entry = this.zipFile.getEntry(fileName);
      return this.zipFile.getInputStream(entry);
    } else {
      return new FileInputStream(new File(this.saifArchiveDirectory, fileName));
    }
  }

  /**
   * Get the list of internally referenced objects for the SAIF archive.
   *
   * @return The internally referenced objects.
   */
  public Record getInternallyReferencedObjects() {
    if (this.internallyReferencedObjects == null) {
      try {
        loadInternallyReferencedObjects();
      } catch (final IOException e) {
        throw new RuntimeException("Unable to load internal.dir: " + e.getMessage());
      }
    }
    return this.internallyReferencedObjects;
  }

  private <D extends Record> OsnReader getOsnReader(
    final RecordDefinitionFactory recordDefinitionFactory, final RecordFactory factory,
    final String className) throws IOException {
    String fileName = this.typePathFileNameMap.get(className);
    if (fileName == null) {
      fileName = PathUtil.getName(className);
    }
    OsnReader reader;
    if (this.zipFile != null) {
      reader = new OsnReader(recordDefinitionFactory, this.zipFile, fileName, this.srid);
    } else {
      reader = new OsnReader(recordDefinitionFactory, this.saifArchiveDirectory, fileName,
        this.srid);
    }
    reader.setFactory(factory);
    return reader;
  }

  public OsnReader getOsnReader(final String className) throws IOException {
    return getOsnReader(className, this.factory);
  }

  public <D extends Record> OsnReader getOsnReader(final String className,
    final RecordFactory factory) throws IOException {
    final RecordDefinitionFactory recordDefinitionFactory = this.recordDefinitionFactory;
    return getOsnReader(recordDefinitionFactory, factory, className);

  }

  @Override
  public RecordDefinition getRecordDefinition() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return this.recordDefinitionFactory.getRecordDefinition(typePath);
  }

  /**
   * Get the schema definition that will be set on each data object.
   *
   * @return The schema definition.
   */
  public RecordDefinitionFactory getRecordDefinitionFactory() {
    return this.recordDefinitionFactory;
  }

  private String getTypeName(final String fileName) {
    return this.fileNameTypeNameMap.get(fileName);
  }

  /**
   * @return the typePathObjectSetMap
   */
  public Map<String, String> getTypeNameFileNameMap() {
    return this.typePathFileNameMap;
  }

  public List<String> getTypeNames() {
    return this.typePaths;
  }

  private boolean hasData(final String typePath) {
    final String fileName = getFileName(typePath);
    if (fileName == null) {
      return false;
    } else if (this.zipFile != null) {
      return this.zipFile.getEntry(fileName) != null;
    } else {
      return new File(this.saifArchiveDirectory, fileName).exists();
    }
  }

  /**
   * Check to see if the reader has more data objects to be read.
   *
   * @return True if the reader has more data objects to be read.
   */
  @Override
  public boolean hasNext() {
    if (this.loadNewObject) {
      return loadNextRecord();
    }
    return this.hasNext;
  }

  @Override
  public Iterator<Record> iterator() {
    open();
    return this;
  }

  /**
   * Load the exported objects for the SAIF archive.
   *
   * @throws IOException If there was an I/O error.
   */
  @SuppressWarnings("unchecked")
  private void loadExportedObjects() throws IOException {
    final boolean setNames = this.includeTypeNames.isEmpty();
    final ClassPathResource resource = new ClassPathResource("com/revolsys/io/saif/saifzip.csn");
    final RecordDefinitionFactory schema = new SaifSchemaReader().loadSchema(resource);
    final OsnReader reader = getOsnReader(schema, this.factory, "/exports.dir");
    try {
      final Map<String, String> names = new TreeMap<>();
      if (reader.hasNext()) {
        this.exportedObjects = reader.next();
        final Set<Record> handles = (Set<Record>)this.exportedObjects.getValue("handles");
        for (final Record exportedObject : handles) {
          final String fileName = (String)exportedObject.getValue("objectSubset");
          if (fileName != null && !fileName.equals("globmeta.osn")) {
            String typePath = getTypeName(fileName);
            if (typePath == null) {
              final String name = (String)exportedObject.getValue("type");
              typePath = PathCache.getName(name);
              if (!this.fileNameTypeNameMap.containsKey(fileName)) {
                this.fileNameTypeNameMap.put(fileName, typePath);
                this.typePathFileNameMap.put(typePath, fileName);
              }
            }

            if (setNames && !fileName.equals("metdat00.osn") && !fileName.equals("refsys00.osn")) {
              names.put(typePath.toString(), typePath);
            }
          }
        }
        if (setNames) {
          this.typePaths = new ArrayList<>(names.values());
        } else {
          this.typePaths = new ArrayList<>(this.includeTypeNames);
        }
        this.typePaths.removeAll(this.excludeTypeNames);
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the global metatdata for the SAIF archive.
   *
   * @throws IOException If there was an I/O error.
   */
  private void loadGlobalMetadata() throws IOException {
    final OsnReader reader = getOsnReader("/globmeta.osn", this.factory);
    try {
      if (reader.hasNext()) {
        this.globalMetadata = this.osnReader.next();
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the imported objects for the SAIF archive.
   *
   * @throws IOException If there was an I/O error.
   */
  private void loadImportedObjects() throws IOException {
    final OsnReader reader = getOsnReader("/imports.dir", this.factory);
    try {
      if (reader.hasNext()) {
        this.importedObjects = this.osnReader.next();
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the internally referenced objects for the SAIF archive.
   *
   * @throws IOException If there was an I/O error.
   */
  private void loadInternallyReferencedObjects() throws IOException {
    final OsnReader reader = getOsnReader("/internal.dir", this.factory);
    try {
      if (reader.hasNext()) {
        this.internallyReferencedObjects = this.osnReader.next();
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Load the next data object from the archive. A new subset will be loaded if
   * required or if there was an error reading from one of the subsets.
   *
   * @return True if an object was loaded.
   */
  private boolean loadNextRecord() {
    boolean useCurrentFile = true;
    if (this.osnReader == null) {
      useCurrentFile = false;
    } else if (!this.osnReader.hasNext()) {
      useCurrentFile = false;
    }
    if (!useCurrentFile) {
      if (!openNextObjectSet()) {
        this.currentRecord = null;
        this.hasNext = false;
        return false;
      }
    }
    do {
      try {
        this.currentRecord = this.osnReader.next();
        this.hasNext = true;
        this.loadNewObject = false;
        return true;
      } catch (final Throwable e) {
        Logs.error(this, e.getMessage(), e);
      }
    } while (openNextObjectSet());
    this.currentRecord = null;
    this.hasNext = false;
    return false;
  }

  /**
   * Load the schema from the SAIF archive.
   *
   * @throws IOException If there was an I/O error.
   */
  private void loadSchema() throws IOException {
    final SaifSchemaReader parser = new SaifSchemaReader();

    final InputStream in = getInputStream("clasdefs.csn");
    try {
      this.declaredRecordDefinitionFactory = parser.loadSchema("clasdefs.csn", in);
    } finally {
      FileUtil.closeSilent(in);
    }
    if (this.recordDefinitionFactory == null) {
      setRecordDefinitionFactory(this.declaredRecordDefinitionFactory);
    }
  }

  private void loadSrid() throws IOException {
    final OsnReader reader = getOsnReader("/refsys00.osn", this.factory);
    try {
      if (reader.hasNext()) {
        final Record spatialReferencing = reader.next();
        final Record coordinateSystem = spatialReferencing.getValue("coordSystem");
        if (coordinateSystem.getRecordDefinition().getPath().equals("/UTM")) {
          final Number srid = coordinateSystem.getValue("zone");
          setSrid(EpsgId.nad83Utm(srid.intValue()));
        }
      }
    } finally {
      reader.close();
    }
  }

  /**
   * Get the next data object read by this reader. .
   *
   * @return The next Record.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  @Override
  public Record next() {
    if (hasNext()) {
      this.loadNewObject = true;
      return this.currentRecord;
    } else {
      throw new NoSuchElementException();
    }
  }

  /**
   * Open a SAIF archive, extracting compressed archives to a temporary
   * directory.
   */
  @Override
  public void open() {
    if (!this.opened) {
      this.opened = true;
      try {
        if (Logs.isDebugEnabled(this)) {
          Logs.debug(this, "Opening SAIF archive '" + this.file.getCanonicalPath() + "'");
        }
        if (this.file.isDirectory()) {
          this.saifArchiveDirectory = this.file;
        } else if (!this.file.exists()) {
          throw new IllegalArgumentException("SAIF file " + this.file + " does not exist");
        } else {
          this.zipFile = new ZipFile(this.file);
        }
        if (Logs.isDebugEnabled(this)) {
          Logs.debug(this, "  Finished opening archive");
        }
        loadSchema();
        loadExportedObjects();
        loadSrid();
        final GeometryFactory geometryFactory = GeometryFactory.fixed3d(this.srid, 1.0, 1.0, 1.0);

        for (final RecordDefinition recordDefinition : ((RecordDefinitionFactoryImpl)this.recordDefinitionFactory)
          .getRecordDefinitions()) {
          final FieldDefinition geometryField = recordDefinition.getGeometryField();
          if (geometryField != null) {
            geometryField.setGeometryFactory(geometryFactory);
          }
        }
      } catch (final IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  /**
   * Open the iterator for the next object set.
   *
   * @return True if an object set iterator was loaded.
   */
  private boolean openNextObjectSet() {
    try {
      closeCurrentReader();
      if (this.typePathIterator == null) {
        this.typePathIterator = this.typePaths.iterator();
      }
      if (this.typePathIterator.hasNext()) {
        do {
          final String typePath = this.typePathIterator.next();
          if (hasData(typePath)) {
            this.osnReader = getOsnReader(typePath, this.factory);
            this.osnReader.setFactory(this.factory);
            if (this.osnReader.hasNext()) {
              return true;
            }
          }
        } while (this.typePathIterator.hasNext());
      }
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    this.osnReader = null;
    return false;
  }

  protected Record readObject(final String className, final RecordFactory factory)
    throws IOException {
    final OsnReader reader = getOsnReader(className, factory);
    try {
      final Record object = reader.next();
      return object;
    } finally {
      reader.close();
    }
  }

  /**
   * Removing SAIF objects is not supported.
   *
   * @throws UnsupportedOperationException
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing SAIF objects is not supported");
  }

  /**
   * Set the schema definition declared in the SAIF archive.
   *
   * @param declaredSchema The schema definition.
   */
  public void setDeclaredRecordDefinitionFactory(
    final RecordDefinitionFactory declaredRecordDefinitionFactory) {
    this.declaredRecordDefinitionFactory = declaredRecordDefinitionFactory;
  }

  /**
   * @param excludeTypeNames the excludeTypeNames to set
   */
  public void setExcludeTypeNames(final Collection<String> excludeTypeNames) {
    this.excludeTypeNames.clear();
    for (final String typePath : excludeTypeNames) {
      this.excludeTypeNames.add(String.valueOf(typePath));
    }
  }

  /**
   * @param factory the factory to set
   */
  public void setFactory(final RecordFactory factory) {
    this.factory = factory;
  }

  public void setFile(final File file) {
    this.file = file;
  }

  /**
   * @param includeTypeNames the includeTypeNames to set
   */
  public void setIncludeTypeNames(final Collection<String> includeTypeNames) {
    this.includeTypeNames.clear();
    for (final String typePath : includeTypeNames) {
      this.includeTypeNames.add(String.valueOf(typePath));
    }
  }

  /**
   * Set the schema definition that will be set on each data object.
   *
   * @param schema The schema definition.
   */
  public void setRecordDefinitionFactory(final RecordDefinitionFactory recordDefinitionFactory) {
    if (recordDefinitionFactory != null) {
      this.recordDefinitionFactory = recordDefinitionFactory;
    } else {
      this.recordDefinitionFactory = this.declaredRecordDefinitionFactory;
    }

  }

  public void setSrid(final int srid) {
    this.srid = srid;
  }

  /**
   * @param typePathObjectSetMap the typePathObjectSetMap to set
   */
  public void setTypeNameFileNameMap(final Map<String, String> typePathObjectSetMap) {
    this.typePathFileNameMap.clear();
    this.fileNameTypeNameMap.clear();
    for (final Entry<String, String> entry : typePathObjectSetMap.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      this.typePathFileNameMap.put(key, value);
      this.fileNameTypeNameMap.put(value, key);
    }
  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }
}
