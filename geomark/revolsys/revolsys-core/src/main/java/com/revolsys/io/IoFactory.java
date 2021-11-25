package com.revolsys.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jeometry.common.exception.Exceptions;
import org.jeometry.coordinatesystem.model.CoordinateSystem;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.file.Paths;
import com.revolsys.io.filter.FileNameExtensionFilter;
import com.revolsys.record.Available;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.spring.resource.GzipResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.util.UrlUtil;

public interface IoFactory extends Available {
  @SuppressWarnings("unchecked")
  static <C extends IoFactory> List<C> factories(final Class<C> factoryClass) {
    return Lists.<C> toArray((Set<C>)IoFactoryRegistry.factoriesByClass.get(factoryClass));
  }

  static List<IoFactory> factoriesByFileExtension(String fileExtension) {
    if (fileExtension != null) {
      fileExtension = fileExtension.toLowerCase();
      final Set<IoFactory> factories = IoFactoryRegistry.factoriesByFileExtension
        .get(fileExtension);
      if (factories != null) {
        return Lists.toArray(factories);
      }
    }
    return Collections.emptyList();
  }

  /**
  * Get the {@link IoFactory} for the given source. The source can be one of the following
  * classes.
  *
  * <ul>
  *   <li>{@link PathUtil}</li>
  *   <li>{@link File}</li>
  *   <li>{@link Resource}</li>
  * </ul>
  * @param factoryClass The class or interface to get the factory for.
  * @param source The source to create the factory for.
  * @return The factory.
  * @throws IllegalArgumentException If the source is not a supported class.
  */
  static <C extends IoFactory> C factory(final Class<C> factoryClass, final Object source) {
    final String fileName = fileName(source);
    return factoryByFileName(factoryClass, fileName);
  }

  @SuppressWarnings("unchecked")
  static <F extends IoFactory> F factoryByFileExtension(final Class<F> factoryClass,
    String fileExtension) {
    if (fileExtension != null) {
      fileExtension = fileExtension.toLowerCase();
      if (Property.hasValue(fileExtension)) {
        return (F)Maps.getMap(IoFactoryRegistry.factoryByClassAndFileExtension, factoryClass,
          fileExtension);
      }
    }
    return null;
  }

  static <C extends IoFactory> C factoryByFileName(final Class<C> factoryClass,
    final String fileName) {
    for (final String fileExtension : FileUtil.getFileNameExtensions(fileName)) {
      final C factory = factoryByFileExtension(factoryClass, fileExtension);
      if (factory != null) {
        return factory;
      }
    }
    if (fileName.endsWith(".zip")) {
      final C factory = factoryByFileName(factoryClass,
        fileName.substring(0, fileName.length() - 4));
      if (factory != null && factory.isReadFromZipFileSupported()) {
        return factory;
      }
    }
    if (fileName.endsWith(".gz")) {
      final C factory = factoryByFileName(factoryClass,
        fileName.substring(0, fileName.length() - 3));
      if (factory != null && factory.isReadFromZipFileSupported()) {
        return factory;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <F extends IoFactory> F factoryByMediaType(final Class<F> factoryClass,
    final String mediaType) {
    if (Property.hasValue(mediaType)) {
      if (mediaType.contains("/")) {
        return (F)Maps.getMap(IoFactoryRegistry.factoryByClassAndMediaType, factoryClass,
          mediaType);
      } else {
        return factoryByFileExtension(factoryClass, mediaType);
      }
    }
    return null;
  }

  static String fileExtensionByMediaType(final String mediaType) {
    final RecordWriterFactory writerFactory = factoryByMediaType(RecordWriterFactory.class,
      mediaType);
    if (writerFactory == null) {
      return null;
    } else {
      return writerFactory.getFileExtension(mediaType);
    }
  }

  static List<String> fileExtensions(final Class<? extends IoFactory> factoryClass) {
    return Lists.toArray(IoFactoryRegistry.fileExtensionsByClass.get(factoryClass));
  }

  public static String fileName(final Object source) {
    String fileName = null;
    if (Property.hasValue(source)) {
      if (source instanceof Resource) {
        fileName = ((Resource)source).getFilename();
      } else if (source instanceof Path) {
        fileName = Paths.getFileName((Path)source);
      } else if (source instanceof File) {
        fileName = FileUtil.getFileName((File)source);
      } else if (source instanceof URL) {
        fileName = UrlUtil.getFileName((URL)source);
      } else if (source instanceof URI) {
        fileName = UrlUtil.getFileName((URI)source);
      } else if (source instanceof String) {
        fileName = FileUtil.getFileName((String)source);
      } else {
        throw new IllegalArgumentException(source.getClass() + " is not supported");
      }
    }
    return fileName;
  }

  static String fileNameExtension(final Class<? extends IoFactory> factoryClass,
    final String fileName) {
    String fullFileNameExtension = "";
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(fileName)) {
      fullFileNameExtension = fileNameExtension + fullFileNameExtension;
      final IoFactory factory = factoryByFileExtension(factoryClass, fullFileNameExtension);
      if (factory != null) {
        return fullFileNameExtension;
      }
      fullFileNameExtension = "." + fullFileNameExtension;
    }

    return null;
  }

  static <C extends IoFactory> boolean hasFactory(final Class<C> factoryClass,
    final Object source) {
    final C factory = factory(factoryClass, source);
    return factory != null;
  }

  static <F extends IoFactory> boolean isAvailable(final Class<F> factoryClass,
    final Object source) {
    if (factoryClass != null) {
      final List<String> fileExtensions = fileExtensions(factoryClass);
      if (Property.hasValue(fileExtensions)) {
        try {
          final String fileName = fileName(source);
          for (final String fileExtension : FileUtil.getFileNameExtensions(fileName)) {
            if (Property.hasValue(fileExtension)
              && fileExtensions.contains(fileExtension.toLowerCase())) {
              return true;
            }
          }
        } catch (final IllegalArgumentException e) {
        }
      }
    }
    return false;
  }

  static Map<String, String> mediaTypeByFileExtension() {
    return new HashMap<>(IoFactoryRegistry.mediaTypeByFileExtension);
  }

  static <F extends IoFactory> List<String> mediaTypes(final Class<F> factoryClass) {
    return Lists.toArray(IoFactoryRegistry.mediaTypesByClass.get(factoryClass));
  }

  public static ChannelReader newChannelReader(final Resource resource) {
    final ReadableByteChannel channel = newReadableByteChannel(resource);
    if (channel == null) {
      return null;
    } else {
      return new ChannelReader(channel);
    }
  }

  public static ChannelReader newChannelReader(final Resource resource, final ByteBuffer buffer) {
    final ReadableByteChannel channel = newReadableByteChannel(resource);
    if (channel == null) {
      return null;
    } else {
      return new ChannelReader(channel, buffer);
    }
  }

  public static ChannelReader newChannelReader(final Resource resource, final int bufferSize) {
    final ReadableByteChannel channel = newReadableByteChannel(resource);
    if (channel == null) {
      return null;
    } else {
      return new ChannelReader(channel, bufferSize);
    }
  }

  public static FileNameExtensionFilter newFileFilter(final String description,
    final Collection<String> fileExtensions) {
    final String[] array = fileExtensions.toArray(new String[0]);
    return new FileNameExtensionFilter(description, array);
  }

  public static FileNameExtensionFilter newFileFilter(final String description,
    final String fileExtension) {
    return new FileNameExtensionFilter(description, fileExtension);
  }

  public static List<FileNameExtensionFilter> newFileFilters(final Set<String> allExtensions,
    final Class<? extends IoFactory> factoryClass) {
    final List<FileNameExtensionFilter> filters = new ArrayList<>();
    final List<? extends IoFactory> factories = IoFactory.factories(factoryClass);
    for (final IoFactory factory : factories) {
      final FileNameExtensionFilter filter = factory.newFileFilterAllExtensions();
      filters.add(filter);
      if (allExtensions != null) {
        for (final String fileNameExtension : filter.getExtensions()) {
          allExtensions.add(fileNameExtension);
        }
      }
    }
    sortFilters(filters);
    return filters;
  }

  public static ReadableByteChannel newReadableByteChannel(final Resource resource) {
    final String fileExtension = resource.getFileNameExtension();
    try {
      if (fileExtension.equals("zip")) {
        final ZipInputStream in = resource.newBufferedInputStream(ZipInputStream::new);
        final String baseName = resource.getBaseName();
        for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in
          .getNextEntry()) {
          if (zipEntry.getName().equals(baseName)) {
            return Channels.newChannel(in);
          }
        }
        throw new IllegalArgumentException("Cannot find " + baseName + " in " + resource);
      } else if (fileExtension.equals("gz")) {
        final InputStream in = resource.newBufferedInputStream();
        final GZIPInputStream gzIn = new GZIPInputStream(in);
        return Channels.newChannel(gzIn);
      } else {
        return resource.newReadableByteChannel();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to open: " + resource, e);
    }
  }

  public static void sortFilters(final List<FileNameExtensionFilter> filters) {
    Collections.sort(filters, new Comparator<FileNameExtensionFilter>() {
      @Override
      public int compare(final FileNameExtensionFilter filter1,
        final FileNameExtensionFilter filter2) {
        return filter1.getDescription().compareTo(filter2.getDescription());
      }
    });
  }

  default void addFileFilters(final List<FileNameExtensionFilter> filters) {
    for (final String fileExtension : getFileExtensions()) {
      final String description = getName() + " (" + fileExtension + ")";
      final FileNameExtensionFilter filter = new FileNameExtensionFilter(description,
        fileExtension);
      filters.add(filter);

    }
  }

  default String getFileExtension(final String mediaType) {
    return null;
  }

  default List<String> getFileExtensions() {
    return Collections.emptyList();
  }

  default String getMediaType(final String fileExtension) {
    return null;
  }

  default Set<String> getMediaTypes() {
    return Collections.emptySet();
  }

  String getName();

  default Resource getZipResource(final Object source) {
    Resource resource = Resource.getResource(source);
    if (isReadFromZipFileSupported()) {
      final String filename = resource.getFilename();
      if (filename.endsWith(".zip")) {
        final String baseName = filename.substring(0, filename.length() - 4);
        final String url = "jar:" + resource.getUri() + "!/" + baseName;
        final UrlResource urlResource = new UrlResource(url);
        if (urlResource.exists()) {
          resource = urlResource;
        } else {
          return null;
        }
      } else if (filename.endsWith(".gz")) {
        return new GzipResource(resource);
      } else if (filename.endsWith(getFileExtensions().get(0) + "z")) {
        return new GzipResource(resource);
      }
    }
    return resource;
  }

  default void init() {
  }

  default boolean isCoordinateSystemSupported(final CoordinateSystem coordinateSystem) {
    return true;
  }

  default boolean isReadFromDirectorySupported() {
    return false;
  }

  default boolean isReadFromZipFileSupported() {
    return false;
  }

  default FileNameExtensionFilter newFileFilter() {
    final List<String> fileExtensions = getFileExtensions();
    String description = getName();
    final String fileExtension = fileExtensions.get(0);
    description += " (" + fileExtension + ")";
    return newFileFilter(description, fileExtension);
  }

  default FileNameExtensionFilter newFileFilterAllExtensions() {
    final List<String> fileExtensions = getFileExtensions();
    String description = getName();
    description += " (" + Strings.toString(fileExtensions) + ")";
    return newFileFilter(description, fileExtensions);
  }
}
