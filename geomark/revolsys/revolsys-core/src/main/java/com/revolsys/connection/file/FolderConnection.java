package com.revolsys.connection.file;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.Parent;
import com.revolsys.connection.AbstractConnection;
import com.revolsys.io.file.Paths;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.Property;

public class FolderConnection extends AbstractConnection<FolderConnection, FolderConnectionRegistry>
  implements Parent<Path> {

  private final Path path;

  public FolderConnection(final FolderConnectionRegistry registry, final String name,
    final Path path) {
    super(registry, name);
    if (path == null) {
      throw new IllegalArgumentException("File must not be null");
    }
    if (!Property.hasValue(getName())) {
      final String fileName = Paths.getFileName(path);
      setName(fileName);
    }
    this.path = path.toAbsolutePath();
  }

  @Override
  public boolean equals(final Object obj) {
    if (super.equals(obj)) {
      if (DataType.equal(getFile(), ((FolderConnection)obj).getFile())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Path> getChildren() {
    final List<Path> paths = Paths.getChildPaths(this.path);
    paths.sort((a, b) -> {
      final String name1 = a.getName(a.getNameCount() - 1).toString().toLowerCase();
      final String name2 = b.getName(b.getNameCount() - 1).toString().toLowerCase();
      return name1.compareTo(name2);
    });
    return paths;
  }

  public File getFile() {
    return this.path.toFile();
  }

  @Override
  public String getIconName() {
    return "folder";
  }

  public Path getPath() {
    return this.path;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addTypeToMap(map, "folderConnection");
    map.put("name", getName());
    map.put("file", this.path.toAbsolutePath().toString());
    return map;
  }
}
