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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.io.ZipUtil;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.saif.util.ObjectSetUtil;
import com.revolsys.record.io.format.saif.util.OsnConverterRegistry;
import com.revolsys.record.io.format.saif.util.OsnSerializer;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

/**
 * <p>
 * The SaifWriter.
 * </p>
 *
 * @author Paul Austin
 * @see SaifReader
 */
public class SaifWriter extends AbstractRecordWriter {
  private static final String GLOBAL_METADATA = "/GlobalMetadata";

  private RecordDefinition annotatedSpatialDataSetType;

  private final Map<String, String> compositeTypeNames = new HashMap<>();

  protected OsnConverterRegistry converters = new OsnConverterRegistry();

  private final Set<String> exportedTypes = new LinkedHashSet<>();

  private final Map<String, Map<String, Object>> exports = new TreeMap<>();

  private File file;

  private boolean indentEnabled = false;

  private boolean initialized;

  private int maxSubsetSize = Integer.MAX_VALUE;

  private final Map<String, String> objectIdentifiers = new HashMap<>();

  private final Map<String, String> objectSetNames = new HashMap<>();

  private RecordDefinitionFactory recordDefinitionFactory;

  private List<Resource> schemaFileNames;

  private String schemaResource;

  private final Map<String, OsnSerializer> serializers = new HashMap<>();

  private RecordDefinition spatialDataSetType;

  private File tempDirectory;

  public SaifWriter() {
    super(null);
  }

  public SaifWriter(final File file) throws IOException {
    this();
    setFile(file);
  }

  public SaifWriter(final File file, final RecordDefinitionFactory recordDefinitionFactory)
    throws IOException {
    this(file);
    setRecordDefinitionFactory(recordDefinitionFactory);
  }

  public SaifWriter(final String fileName) throws IOException {
    this(new File(fileName));
  }

  public void addCompositeTypeName(final String typePath, final String compositeTypeName) {
    this.compositeTypeNames.put(String.valueOf(typePath), compositeTypeName);
  }

  protected void addExport(final String typePath, final String compositeType,
    final String objectSubset) {
    if (!this.exports.containsKey(typePath)) {
      final Map<String, Object> export = new HashMap<>();
      this.exports.put(typePath, export);
      final String referenceId = getObjectIdentifier(typePath);
      export.put("referenceId", referenceId);
      export.put("compositeType", compositeType);
      export.put("objectSubset", objectSubset);
    }
  }

  @Override
  public synchronized void close() {
    if (this.tempDirectory != null) {
      try {
        writeExports();
        writeMissingDirObject("InternallyReferencedObjects", "internal.dir");
        writeMissingDirObject("ImportedObjects", "imports.dir");
        writeMissingGlobalMetadata();
        for (final OsnSerializer serializer : this.serializers.values()) {
          try {
            serializer.close();
          } catch (final Throwable e) {
            Logs.error(this, e.getMessage(), e);
          }
        }
        if (!this.file.isDirectory()) {
          ZipUtil.zipDirectory(this.file, this.tempDirectory);
        }
      } catch (final RuntimeException e) {
        this.file.delete();
        throw e;
      } catch (final Error e) {
        this.file.delete();
        throw e;
      } catch (final IOException e) {
        Logs.error(this, "  Unable to compress SAIF archive: " + e.getMessage(), e);
        e.printStackTrace();
      } finally {
        if (!this.file.isDirectory()) {
          FileUtil.deleteDirectory(this.tempDirectory);
        }
        this.tempDirectory = null;
      }
    }
  }

  @Override
  public void flush() {
  }

  private RecordDefinition getCompositeType(final String typePath) {
    String compositeTypeName = this.compositeTypeNames.get(typePath);
    if (compositeTypeName == null) {
      compositeTypeName = typePath + "Composite";
    }
    final RecordDefinition compisteType = this.recordDefinitionFactory
      .getRecordDefinition(String.valueOf(compositeTypeName));
    return compisteType;
  }

  public File getFile() {
    return this.file;
  }

  public int getMaxSubsetSize() {
    return this.maxSubsetSize;
  }

  public String getObjectIdentifier(final String typePath) {
    String objectIdentifier = this.objectIdentifiers.get(typePath);
    if (objectIdentifier == null) {
      objectIdentifier = PathUtil.getName(typePath);
      this.objectIdentifiers.put(typePath, objectIdentifier);
    }
    return objectIdentifier;
  }

  /**
   * @return the objectIdentifiers
   */
  public Map<String, String> getObjectIdentifiers() {
    return this.objectIdentifiers;
  }

  /**
   * Get the object set name (file name) within a SAIF archive file name for the
   * specified type name. The null value will be returned if a object set name
   * has not been set for that type name.
   *
   * @param typePath The type name.
   * @return The object set name for the type name.
   */
  public String getObjectSetName(final String typePath) {
    return this.objectSetNames.get(typePath);
  }

  public Map<String, String> getObjectSetNames() {
    return this.objectSetNames;
  }

  private String getObjectSubsetName(final String typePath) {
    String objectSubsetName = getObjectSetName(typePath);
    if (objectSubsetName == null) {
      objectSubsetName = PathUtil.getName(typePath);
      if (objectSubsetName.length() > 6) {
        objectSubsetName = objectSubsetName.substring(0, 6);
      }
      objectSubsetName += "00.osn";
      this.objectSetNames.put(typePath, objectSubsetName);
    }
    return objectSubsetName;
  }

  public String getSchemaResource() {
    return this.schemaResource;
  }

  private OsnSerializer getSerializer(final String typePath) throws IOException {
    OsnSerializer serializer = this.serializers.get(typePath);
    if (serializer == null) {
      initialize();
      try {
        final RecordDefinition compositeType = getCompositeType(typePath);
        if (compositeType != null) {
          final String objectSubsetName = getObjectSubsetName(typePath);
          if (this.maxSubsetSize != Long.MAX_VALUE) {
            FileUtil.deleteFiles(this.tempDirectory,
              ObjectSetUtil.getObjectSubsetPrefix(objectSubsetName) + "...osn");
            serializer = newSerializer(typePath, new File(this.tempDirectory, objectSubsetName),
              this.maxSubsetSize);
          } else {
            serializer = newSerializer(typePath, new File(this.tempDirectory, objectSubsetName),
              Long.MAX_VALUE);
          }
          if (compositeType.isInstanceOf(this.annotatedSpatialDataSetType)) {
            serializer.startObject(compositeType.getPath());
            serializer.fieldName("objectIdentifier");
            final String objectIdentifier = getObjectIdentifier(typePath);
            serializer.attributeValue(objectIdentifier);
            serializer.endLine();
            serializer.serializeIndent();
            serializer.fieldName("annotationComponents");
            serializer.startCollection("Set");
          } else if (compositeType.isInstanceOf(this.spatialDataSetType)) {
            serializer.startObject(compositeType.getPath());
            serializer.fieldName("objectIdentifier");
            final String objectIdentifier = getObjectIdentifier(typePath);
            serializer.attributeValue(objectIdentifier);
            serializer.endLine();
            serializer.serializeIndent();
            serializer.fieldName("geoComponents");
            serializer.startCollection("Set");
          }
          addExport(typePath, compositeType.getPath(), objectSubsetName);
          this.serializers.put(typePath, serializer);
        } else if (typePath.equals("/ImportedObjects")) {
          serializer = newSerializer("/ImportedObject", new File(this.tempDirectory, "imports.dir"),
            Long.MAX_VALUE);
          this.serializers.put(typePath, serializer);
        } else if (PathUtil.getName(typePath).endsWith("InternallyReferencedObjects")) {
          serializer = newSerializer("/InternallyReferencedObject",
            new File(this.tempDirectory, "internal.dir"), Long.MAX_VALUE);
          this.serializers.put(typePath, serializer);
        } else if (PathUtil.getName(typePath).endsWith("GlobalMetadata")) {
          serializer = newSerializer(GLOBAL_METADATA, new File(this.tempDirectory, "globmeta.osn"),
            Long.MAX_VALUE);
          addExport(typePath, typePath, "globmeta.osn");
          this.serializers.put(typePath, serializer);
        }
      } catch (final IOException e) {
        Logs.error(this, "Unable to create serializer: " + e.getMessage(), e);
      }
    }
    return serializer;
  }

  public File getTempDirectory() {
    return this.tempDirectory;
  }

  private void initialize() throws IOException {
    if (!this.initialized) {
      this.initialized = true;
      if (this.schemaResource != null) {
        final InputStream in = getClass().getResourceAsStream(this.schemaResource);
        if (in != null) {
          FileUtil.copy(in, new File(this.tempDirectory, "clasdefs.csn"));
        }
      }
      if (this.schemaFileNames != null) {
        try {
          final OutputStream out = new FileOutputStream(
            new File(this.tempDirectory, "clasdefs.csn"));
          try {
            for (final Resource resource : this.schemaFileNames) {
              final InputStream in = resource.getInputStream();
              final SaifSchemaReader reader = new SaifSchemaReader();
              setRecordDefinitionFactory(reader.loadSchemas(this.schemaFileNames));
              try {
                FileUtil.copy(in, out);
              } finally {
                in.close();
              }
            }
          } finally {
            out.close();
          }
        } catch (final IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    }
  }

  public boolean isIndentEnabled() {
    return this.indentEnabled;
  }

  protected OsnSerializer newSerializer(final String typePath, final File file, final long maxSize)
    throws IOException {
    final OsnSerializer serializer = new OsnSerializer(typePath, file, maxSize, this.converters);
    serializer.setIndentEnabled(this.indentEnabled);
    return serializer;
  }

  public void setCompositeTypeNames(final Map<String, String> compositeTypeNames) {
    for (final Entry<String, String> entry : compositeTypeNames.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      addCompositeTypeName(key, value);
    }
  }

  public void setFile(final File file) throws IOException {
    if (!file.isDirectory()) {
      final File parentDir = file.getParentFile();
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }
      String fileName = FileUtil.getFileName(file);
      String filePrefix = fileName;
      final int extensionIndex = fileName.lastIndexOf('.');
      if (extensionIndex != -1) {
        filePrefix = fileName.substring(0, extensionIndex);
        final String extension = fileName.substring(extensionIndex + 1);
        if (!extension.equals(".saf") && !extension.equals(".zip")) {
          fileName = filePrefix + ".saf";
        }
      } else {
        fileName = filePrefix + ".saf";
      }
      this.file = new File(file.getCanonicalFile().getParentFile(), fileName);
      this.tempDirectory = FileUtil.newTempDirectory(filePrefix, ".saf");
      FileUtil.deleteFileOnExit(this.tempDirectory);
    } else {
      this.file = file;
      this.tempDirectory = file;
    }
    initialize();
  }

  public void setIndentEnabled(final boolean indentEnabled) {
    this.indentEnabled = indentEnabled;
  }

  public void setMaxSubsetSize(final int maxSubsetSize) {
    this.maxSubsetSize = maxSubsetSize;
  }

  /**
   * @param objectIdentifiers the objectIdentifiers to set
   */
  public void setObjectIdentifiers(final Map<String, String> objectIdentifiers) {
    for (final Entry<String, String> entry : objectIdentifiers.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      final String qName = String.valueOf(key);
      this.objectIdentifiers.put(qName, value);
    }
  }

  /**
   * Set the full object set name (file name) within a SAIF archive file name
   * for the specified type name. The name must include the .osn (or other)
   * extension (e.g. globmeta.osn). If the file is to be split into multiple
   * object sub sets (for large files) include {$partNum} before the file
   * extension (e.g. roads{$segment}.osn) and the file names will include the
   * object sub set number. If a value is not set the file name will be the
   * first 6 characters of the type name, followed by a object subset number
   * starting at 00 with the .osn suffix (e.g. BreakLines would be
   * breakl00.osn).
   *
   * @param typePath The type name
   * @param subSetName The sub set name for the type name.
   */
  public void setObjectSetName(final String typePath, final String subSetName) {
    this.objectSetNames.put(typePath, subSetName);
  }

  public void setObjectSetNames(final Map<String, String> objectSetNames) {
    for (final Entry<String, String> entry : objectSetNames.entrySet()) {
      final String key = entry.getKey();
      final String value = entry.getValue();
      setObjectSetName(key, value);
    }
  }

  public void setRecordDefinitionFactory(final RecordDefinitionFactory schema) {
    this.recordDefinitionFactory = schema;
    if (schema != null) {
      this.spatialDataSetType = schema.getRecordDefinition("/SpatialDataSet");
      this.annotatedSpatialDataSetType = schema.getRecordDefinition("/AnnotatedSpatialDataSet");
    }
  }

  public void setSchemaFileNames(final List<Resource> schemaFileNames) {
    this.schemaFileNames = schemaFileNames;

  }

  public void setSchemaResource(final String schemaResource) throws IOException {
    this.schemaResource = schemaResource;

  }

  @Override
  public String toString() {
    return this.file.getAbsolutePath();
  }

  @Override
  public void write(final Record object) {
    try {
      final RecordDefinition type = object.getRecordDefinition();
      final OsnSerializer serializer = getSerializer(type.getPath());
      if (serializer != null) {
        serializer.serializeRecord(object);
        if (this.indentEnabled) {
          serializer.endLine();
        }
      } else {
        Logs.error(this, "No serializer for type '" + type.getPath() + "'");
      }
    } catch (final IOException e) {
      Logs.error(this, e.getMessage(), e);
    }
  }

  public void writeExport(final OsnSerializer exportsSerializer, final String referenceId,
    final String compositeTypeName, final String objectSubset) throws IOException {
    exportsSerializer.startObject("ExportedObjectHandle");
    exportsSerializer.attribute("referenceID", referenceId, true);
    exportsSerializer.attribute("type", compositeTypeName, true);
    exportsSerializer.attribute("objectSubset", objectSubset, true);
    exportsSerializer.attribute("offset", new BigDecimal("0"), true);
    exportsSerializer.attribute("sharable", Boolean.FALSE, true);
    exportsSerializer.endObject();
  }

  private void writeExports() throws IOException {
    final File exportsFile = new File(this.tempDirectory, "exports.dir");
    final OsnSerializer exportsSerializer = newSerializer("/ExportedObject", exportsFile,
      Long.MAX_VALUE);
    exportsSerializer.startObject("/ExportedObjects");
    exportsSerializer.fieldName("handles");
    exportsSerializer.startCollection("Set");
    writeExport(exportsSerializer, "GlobalMetadata", "GlobalMetadata", "globmeta.osn");
    for (final Map<String, Object> export : this.exports.values()) {
      final String compositeType = (String)export.get("compositeType");
      final String referenceId = (String)export.get("referenceId");
      final String objectSubset = (String)export.get("objectSubset");
      String compositeTypeName = PathUtil.getName(compositeType);
      final String compositeNamespace = PathUtil.getPath(compositeType).replaceAll("/", "");
      if (Property.hasValue(compositeNamespace)) {
        compositeTypeName += "::" + compositeNamespace;
      }
      writeExport(exportsSerializer, referenceId, compositeTypeName, objectSubset);
    }
    exportsSerializer.close();
  }

  private void writeMissingDirObject(final String typePath, final String fileName)
    throws IOException {
    if (!this.serializers.containsKey(typePath)) {
      final File file = new File(this.tempDirectory, fileName);
      final PrintStream out = new PrintStream(new FileOutputStream(file));
      try {
        out.print(typePath);
        out.print("(handles:Set{})");
      } finally {
        out.close();
      }
    }
  }

  private void writeMissingGlobalMetadata() {
    if (!this.exports.containsKey(GLOBAL_METADATA)) {
      try {
        addExport(GLOBAL_METADATA, GLOBAL_METADATA, "globmeta.osn");
        final File metaFile = new File(this.tempDirectory, "globmeta.osn");
        final OsnSerializer serializer = newSerializer(GLOBAL_METADATA, metaFile, Long.MAX_VALUE);
        serializer.startObject("/GlobalMetadata");
        serializer.attribute("objectIdentifier", "GlobalMetadata", true);

        serializer.fieldName("creationTime");
        serializer.startObject("/TimeStamp");
        final Date creationTimestamp = new Date(System.currentTimeMillis());
        serializer.attribute("year", new BigDecimal(creationTimestamp.getYear() + 1900), true);
        serializer.attribute("month", new BigDecimal(creationTimestamp.getMonth() + 1), true);
        serializer.attribute("day", new BigDecimal(creationTimestamp.getDate()), true);
        serializer.attribute("hour", new BigDecimal(creationTimestamp.getHours()), true);
        serializer.attribute("minute", new BigDecimal(creationTimestamp.getMinutes()), true);
        serializer.attribute("second", new BigDecimal(creationTimestamp.getSeconds()), true);
        serializer.endObject();

        serializer.fieldName("saifProfile");
        serializer.startObject("/Profile");
        serializer.attribute("authority", "Government of British Columbia", true);
        serializer.attribute("idName", "SAIFLite", true);
        serializer.attribute("version", "Release 1.1", true);
        serializer.endObject();

        serializer.attribute("saifRelease", "SAIF 3.2", true);
        serializer.attribute("toolkitVersion", "SAIF Toolkit Version 1.4.0 (May 05, 1997)", true);

        serializer.fieldName("userProfile");
        serializer.startObject("/UserProfile");

        serializer.fieldName("coordDefs");
        serializer.startObject("/LocationalDefinitions");
        serializer.attributeEnum("c1", "real32", true);
        serializer.attributeEnum("c2", "real32", true);
        serializer.attributeEnum("c3", "real32", true);
        serializer.endObject();

        serializer.attribute("organization", new BigDecimal("4"), true);
        serializer.endObject();

        serializer.endObject();
        serializer.close();
      } catch (final IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

}
