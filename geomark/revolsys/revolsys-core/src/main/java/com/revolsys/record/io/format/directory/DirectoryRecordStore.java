package com.revolsys.record.io.format.directory;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jeometry.common.io.PathName;

import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.io.filter.ExtensionFilenameFilter;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public class DirectoryRecordStore extends AbstractRecordStore {

  private File directory;

  private List<String> fileExtensions;

  private final Map<RecordDefinition, Resource> resourcesByRecordDefinition = new HashMap<>();

  private final Map<Resource, String> typePathByResource = new HashMap<>();

  private final Map<String, RecordWriter> writers = new HashMap<>();

  public DirectoryRecordStore(final File directory, final Collection<String> fileExtensions) {
    this.directory = directory;
    this.fileExtensions = new ArrayList<>(fileExtensions);
    setCreateMissingRecordStore(true);
    setCreateMissingTables(true);
  }

  public DirectoryRecordStore(final File directory, final String... fileExtensions) {
    this(directory, Arrays.asList(fileExtensions));
  }

  public DirectoryRecordStore(final Path directory, final String... fileExtensions) {
    this(directory.toFile(), Arrays.asList(fileExtensions));
  }

  @Override
  public void close() {
    super.close();
    this.directory = null;
    synchronized (this.writers) {
      for (final RecordWriter writer : this.writers.values()) {
        if (writer != null) {
          writer.close();
        }
      }
      this.writers.clear();
    }
  }

  public void closeWriter(final String typeName) {
    final RecordWriter writer;
    synchronized (this.writers) {
      writer = this.writers.remove(typeName);
    }
    FileUtil.closeSilent(writer);
  }

  @Override
  protected RecordDefinition createRecordDefinitionDo(final RecordDefinition recordDefinition) {
    final PathName typePath = recordDefinition.getPathName();
    final PathName schemaPath = typePath.getParent();
    RecordStoreSchema schema = getSchema(schemaPath);
    if (schema == null && isCreateMissingTables()) {
      final RecordStoreSchema rootSchema = getRootSchema();
      schema = rootSchema.newSchema(schemaPath);
    }
    final File schemaDirectory = new File(this.directory, schemaPath.getPath());
    if (!schemaDirectory.exists()) {
      schemaDirectory.mkdirs();
    }
    final RecordDefinitionImpl newRecordDefinition = new RecordDefinitionImpl(schema, typePath);
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final FieldDefinition newField = new FieldDefinition(field);
      newRecordDefinition.addField(newField);
    }
    schema.addElement(newRecordDefinition);
    return newRecordDefinition;
  }

  @Override
  public boolean deleteRecord(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this) {
      throw new UnsupportedOperationException("Deleting records not supported");
    } else {
      return false;
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  public String getFileExtension() {
    return getFileExtensions().get(0);
  }

  public List<String> getFileExtensions() {
    return this.fileExtensions;
  }

  @Override
  public int getRecordCount(final Query query) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RecordReader getRecords(final PathName path) {
    final RecordDefinition recordDefinition = getRecordDefinition(path);
    final Resource resource = getResource(path.toString(), recordDefinition);
    final RecordReader reader = RecordReader.newRecordReader(resource);
    if (reader == null) {
      throw new IllegalArgumentException("Cannot find reader for: " + path);
    } else {
      final String typePath = this.typePathByResource.get(resource);
      reader.setProperty("schema", recordDefinition.getSchema());
      reader.setProperty("typePath", typePath);
      return reader;
    }
  }

  @Override
  public String getRecordStoreType() {
    return "Directory";
  }

  protected Resource getResource(final String path) {
    final RecordDefinition recordDefinition = getRecordDefinition(path);
    return getResource(path, recordDefinition);
  }

  protected Resource getResource(final String path, final RecordDefinition recordDefinition) {
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Table does not exist " + path);
    }
    final Resource resource = this.resourcesByRecordDefinition.get(recordDefinition);
    if (resource == null) {
      throw new IllegalArgumentException("File does not exist for " + path);
    }
    return resource;
  }

  private RecordWriter getWriter(final RecordDefinition recordDefinition) {
    synchronized (this.writers) {
      if (isClosed()) {
        throw new IllegalStateException("Record store is closed");
      } else {
        final String typePath = recordDefinition.getPath();
        RecordWriter writer = this.writers.get(typePath);
        if (writer == null) {
          final String schemaName = PathUtil.getPath(typePath);
          final File subDirectory = FileUtil.getDirectory(getDirectory(), schemaName);
          final String fileExtension = getFileExtension();
          final File file = new File(subDirectory,
            recordDefinition.getName() + "." + fileExtension);
          final Resource resource = new PathResource(file);
          writer = RecordWriter.newRecordWriter(recordDefinition, resource);
          if (writer == null) {
            throw new RuntimeException("Cannot create writer for: " + typePath);
          } else if (writer instanceof ObjectWithProperties) {
            final ObjectWithProperties properties = writer;
            properties.setProperties(getProperties());
          }
          this.writers.put(typePath, writer);
        }
        return writer;
      }
    }
  }

  @Override
  public void initializeDo() {
    super.initializeDo();
    if (!this.directory.exists()) {
      this.directory.mkdirs();
    }
  }

  @Override
  public synchronized void insertRecord(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordWriter writer = getWriter(recordDefinition);
    synchronized (writer) {
      writer.write(record);
    }
    addStatistic("Insert", record);
  }

  protected RecordDefinition loadRecordDefinition(final RecordStoreSchema schema,
    final String schemaName, final Resource resource) {
    try (
      RecordReader recordReader = RecordReader.newRecordReader(resource)) {
      final String typePath = PathUtil.toPath(schemaName, resource.getBaseName());
      recordReader.setProperty("schema", schema);
      recordReader.setProperty("typePath", typePath);
      final RecordDefinition recordDefinition = recordReader.getRecordDefinition();
      if (recordDefinition != null) {
        this.resourcesByRecordDefinition.put(recordDefinition, resource);
        this.typePathByResource.put(resource, typePath);
      }
      return recordDefinition;
    }
  }

  @Override
  public RecordIterator newIterator(final Query query, final Map<String, Object> properties) {
    final PathName path = query.getTablePath();
    final RecordReader reader = getRecords(path);
    reader.setProperties(properties);
    return new RecordReaderQueryIterator(reader, query);
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    return new DirectoryRecordStoreWriter(this);
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinitionProxy recordDefinition) {
    return new DirectoryRecordStoreWriter(this, recordDefinition);
  }

  @Override
  protected Map<PathName, RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final Map<PathName, RecordStoreSchemaElement> elements = new TreeMap<>();
    final String schemaPath = schema.getPath();
    final PathName schemaPathName = schema.getPathName();
    final File subDirectory;
    if (schemaPath.equals("/")) {
      subDirectory = this.directory;
    } else {
      subDirectory = new File(this.directory, schemaPath);
    }
    final FileFilter filter = new ExtensionFilenameFilter(this.fileExtensions);
    final File[] files = subDirectory.listFiles();
    if (files != null) {
      for (final File file : files) {
        if (filter.accept(file)) {
          final PathResource resource = new PathResource(file);
          final RecordDefinition recordDefinition = loadRecordDefinition(schema, schemaPath,
            resource);
          if (recordDefinition != null) {
            final PathName path = recordDefinition.getPathName();
            elements.put(path, recordDefinition);
          }
        } else if (file.isDirectory()) {
          final String name = file.getName();
          final PathName childSchemaPath = schemaPathName.newChild(name);
          RecordStoreSchema childSchema = schema.getSchema(childSchemaPath);
          if (childSchema == null) {
            childSchema = new RecordStoreSchema(schema, childSchemaPath);
          } else {
            if (!childSchema.isInitialized()) {
              childSchema.refresh();
            }
          }
          elements.put(childSchemaPath, childSchema);
        }
      }
    }
    return elements;
  }

  public void setDirectory(final File directory) {
    this.directory = directory;
  }

  protected void setFileExtensions(final List<String> fileExtensions) {
    this.fileExtensions = fileExtensions;
  }

  protected void superDelete(final Record record) {
    super.deleteRecord(record);
  }

  protected void superUpdate(final Record record) {
    super.updateRecord(record);
  }

  @Override
  public String toString() {
    final String fileExtension = getFileExtension();
    return fileExtension + " " + this.directory;
  }

  @Override
  public void updateRecord(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    final RecordStore recordStore = recordDefinition.getRecordStore();
    if (recordStore == this) {
      switch (record.getState()) {
        case DELETED:
        break;
        case PERSISTED:
        break;
        case MODIFIED:
          throw new UnsupportedOperationException();
        default:
          insertRecord(record);
        break;
      }
    } else {
      insertRecord(record);
    }
  }
}
