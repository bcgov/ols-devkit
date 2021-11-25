package com.revolsys.spring.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Map;

import org.jeometry.common.exception.Exceptions;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.revolsys.io.FileUtil;
import com.revolsys.io.channels.ChannelReader;
import com.revolsys.io.channels.HttpChannelReader;
import com.revolsys.io.channels.HttpSeekableByteChannel;
import com.revolsys.io.file.Paths;
import com.revolsys.util.Base64Util;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class UrlResource extends AbstractResource {

  /**
   * Cleaned URL (with normalized path), used for comparisons.
   */
  private final URL cleanedUrl;

  /**
   * Original URI, if available; used for URI and File access.
   */
  private final URI uri;

  /**
   * Original URL, used for actual access.
   */
  private URL url;

  private String username;

  private String password;

  /**
   * Construct a new new UrlResource based on a URL path.
   * <p>Note: The given path needs to be pre-encoded if necessary.
   * @param path a URL path
   * @throws MalformedURLException if the given URL path is not valid
   * @see java.net.URL#URL(String)
   */
  public UrlResource(final CharSequence path) {
    Assert.notNull(path, "Path must not be null");
    try {
      this.uri = null;
      final String urlString = path.toString();
      initUrl(new URL(urlString));
      this.cleanedUrl = getCleanedUrl(this.url, urlString);
    } catch (final Throwable ex) {
      throw Exceptions.wrap(ex);
    }
  }

  public UrlResource(final CharSequence path, final String username, final String password) {
    this(path);
    this.username = username;
    this.password = password;
  }

  public UrlResource(final Resource parent, final CharSequence path) {
    super(parent);
    Assert.notNull(path, "Path must not be null");
    try {
      this.uri = null;
      final String urlString = path.toString();
      initUrl(new URL(urlString));
      this.cleanedUrl = getCleanedUrl(this.url, urlString);
    } catch (final Throwable ex) {
      throw Exceptions.wrap(ex);
    }
  }

  public UrlResource(final Resource parent, final CharSequence path, final String username,
    final String password) {
    this(parent, path);
    this.username = username;
    this.password = password;
  }

  public UrlResource(final Resource parent, final URL url) {
    super(parent);
    Assert.notNull(url, "URL must not be null");
    initUrl(url);
    this.cleanedUrl = getCleanedUrl(this.url, url.toString());
    this.uri = null;
  }

  public UrlResource(final Resource parent, final URL url, final String username,
    final String password) {
    this(parent, url);
    this.username = username;
    this.password = password;
  }

  /**
   * Construct a new new UrlResource based on the given URI object.
   * @param uri a URI
   * @throws MalformedURLException if the given URL path is not valid
   */
  public UrlResource(final URI uri) {
    Assert.notNull(uri, "URI must not be null");
    try {
      this.uri = uri;
      initUrl(uri.toURL());
      this.cleanedUrl = getCleanedUrl(this.url, uri.toString());
    } catch (final Throwable ex) {
      throw Exceptions.wrap(ex);
    }
  }

  /**
   * Construct a new new UrlResource based on the given URL object.
   * @param url a URL
   */
  public UrlResource(final URL url) {
    Assert.notNull(url, "URL must not be null");
    initUrl(url);
    this.cleanedUrl = getCleanedUrl(this.url, url.toString());
    this.uri = null;
  }

  public UrlResource(final URL url, final String username, final String password) {
    this(url);
    this.username = username;
    this.password = password;
  }

  @Override
  public long contentLength() throws IOException {
    final URL url = getURL();
    if (ResourceUtils.isFileURL(url)) {
      // Proceed with file system resolution...
      return getFile().length();
    } else {
      // Try a URL connection content-length header...
      final URLConnection con = url.openConnection();
      customizeConnection(con);
      return con.getContentLength();
    }
  }

  /**
   * This implementation creates a UrlResource, applying the given path
   * relative to the path of the underlying URL of this resource descriptor.
   * @see java.net.URL#URL(java.net.URL, String)
   */
  @Override
  public UrlResource createRelative(String relativePath) {
    try {
      if (relativePath.startsWith("/")) {
        relativePath = relativePath.substring(1);
      }
      final URL url = getURL();
      final URL relativeUrl = UrlUtil.getUrl(url, relativePath);
      final UrlResource relativeResource = new UrlResource(relativeUrl.toString(), this.username,
        this.password);
      return relativeResource;
    } catch (final Throwable e) {
      throw new IllegalArgumentException(
        "Unable to create relative URL " + this + " " + relativePath, e);
    }
  }

  /**
   * Customize the given {@link HttpURLConnection}, obtained in the course of an
   * {@link #exists()}, {@link #contentLength()} or {@link #lastModified()} call.
   * <p>Sets request method "HEAD" by default. Can be overridden in subclasses.
   * @param con the HttpURLConnection to customize
   * @throws IOException if thrown from HttpURLConnection methods
   */
  protected void customizeConnection(final HttpURLConnection con) throws IOException {
    con.setRequestMethod("HEAD");
  }

  /**
   * Customize the given {@link URLConnection}, obtained in the course of an
   * {@link #exists()}, {@link #contentLength()} or {@link #lastModified()} call.
   * <p>Calls {@link ResourceUtils#useCachesIfNecessary(URLConnection)} and
   * delegates to {@link #customizeConnection(HttpURLConnection)} if possible.
   * Can be overridden in subclasses.
   * @param con the URLConnection to customize
   * @throws IOException if thrown from URLConnection methods
   */
  protected void customizeConnection(final URLConnection con) throws IOException {
    if (con instanceof HttpURLConnection) {
      customizeConnection((HttpURLConnection)con);
    }
  }

  /**
   * This implementation compares the underlying URL references.
   */
  @Override
  public boolean equals(final Object obj) {
    return obj == this
      || obj instanceof UrlResource && this.cleanedUrl.equals(((UrlResource)obj).cleanedUrl);
  }

  @Override
  public boolean exists() {
    if (isFolderConnection()) {
      try {
        final File file = getFile();
        if (file == null) {
          return false;
        } else {
          return file.exists();
        }
      } catch (final Throwable e) {
        return false;
      }
    } else {
      try {
        final URL url = getURL();
        if (ResourceUtils.isFileURL(url)) {
          // Proceed with file system resolution...
          return getFile().exists();
        } else {
          // Try a URL connection content-length header...
          final URLConnection con = url.openConnection();
          customizeConnection(con);
          final HttpURLConnection httpCon = con instanceof HttpURLConnection
            ? (HttpURLConnection)con
            : null;
          if (httpCon != null) {
            setAuthorization(this.url, httpCon);
            final int code = httpCon.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
              return true;
            } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
              return false;
            }
          }
          if (con.getContentLength() >= 0) {
            return true;
          }
          if (httpCon != null) {
            // no HTTP OK status, and no content-length header: give up
            httpCon.disconnect();
            return false;
          } else {
            // Fall back to stream existence: can we open the stream?
            try (
              final InputStream is = getInputStream()) {

            } catch (final Throwable e) {
              return false;
            }
            return true;
          }
        }
      } catch (final IOException ex) {
        return false;
      }
    }
  }

  /**
   * Determine a cleaned URL for the given original URL.
   * @param originalUrl the original URL
   * @param originalPath the original URL path
   * @return the cleaned URL
   * @see org.springframework.util.StringUtils#cleanPath
   */
  private URL getCleanedUrl(final URL originalUrl, final String originalPath) {
    try {
      return new URL(StringUtils.cleanPath(originalPath));
    } catch (final MalformedURLException ex) {
      // Cleaned URL path cannot be converted to URL
      // -> take original URL.
      return originalUrl;
    }
  }

  /**
   * This implementation returns a description that includes the URL.
   */
  @Override
  public String getDescription() {
    return this.url.toString();
  }

  /**
   * This implementation returns a File reference for the underlying URL/URI,
   * provided that it refers to a file in the file system.
   * @see org.springframework.util.ResourceUtils#getFile(java.net.URL, String)
   */
  @Override
  public File getFile() {
    try {
      if (isFolderConnection()) {
        return FileUtil.getFile(this.url);
      } else {
        final URL url = getURL();
        if (!"file".equals(url.getProtocol())) {
          throw new FileNotFoundException(getDescription() + " is not a file URL: " + url);
        }
        try {
          final String filePath = ResourceUtils.toURI(url).getSchemeSpecificPart();
          final int queryIndex = filePath.indexOf('?');
          if (queryIndex == -1) {
            return new File(filePath);
          } else {
            final String filePart = filePath.substring(0, queryIndex);
            return new File(filePart);
          }
        } catch (final URISyntaxException ex) {
          // Fallback for URLs that are not valid URIs (should hardly ever
          // happen).
          return new File(url.getFile());
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }

  }

  /**
   * This implementation returns a File reference for the underlying class path
   * resource, provided that it refers to a file in the file system.
   * @see org.springframework.util.ResourceUtils#getFile(java.net.URI, String)
   */
  protected File getFile(final URI uri) throws IOException {
    return ResourceUtils.getFile(uri, getDescription());
  }

  /**
   * This implementation determines the underlying File
   * (or jar file, in case of a resource in a jar/zip).
   */
  @Override
  protected File getFileForLastModifiedCheck() throws IOException {
    final URL url = getURL();
    if (ResourceUtils.isJarURL(url)) {
      final URL actualUrl = ResourceUtils.extractJarFileURL(url);
      return ResourceUtils.getFile(actualUrl, "Jar URL");
    } else {
      return getFile();
    }
  }

  /**
   * This implementation returns the name of the file that this URL refers to.
   */
  @Override
  public String getFilename() {
    return UrlUtil.getFileName(this.url);
  }

  /**
   * This implementation opens an InputStream for the given URL.
   * It sets the "UseCaches" flag to {@code false},
   * mainly to avoid jar file locking on Windows.
   * @see java.net.URL#openConnection()
   * @see java.net.URLConnection#setUseCaches(boolean)
   * @see java.net.URLConnection#getInputStream()
   */
  @Override
  public InputStream getInputStream() {
    try {
      if (isFolderConnection()) {
        final File file = getFile();
        return new FileInputStream(file);
      } else {
        final URLConnection con;
        if ("ftp".equals(this.url.getProtocol()) && this.username != null) {
          final String protocol = this.url.getProtocol();
          final String host = this.url.getHost();
          final int port = this.url.getPort();
          final String path = this.url.getPath();
          final String query = this.url.getQuery();
          final String fragment = this.url.getRef();
          try {
            final String userInfo = this.username.replace(":", "%3A") + ":"
              + this.password.replace(":", "%3A");
            final URI uri = new URI(protocol, userInfo, host, port, path, query, fragment);
            con = uri.toURL().openConnection();
          } catch (final Exception e) {
            throw Exceptions.wrap("Error opening file: " + toString(), e);
          }
        } else {
          con = this.url.openConnection();
          if (con instanceof HttpURLConnection) {
            final HttpURLConnection httpUrlConnection = (HttpURLConnection)con;
            setAuthorization(this.url, httpUrlConnection);
          }
        }
        try {
          return con.getInputStream();
        } catch (final FileNotFoundException e) {
          throw Exceptions.wrap("Error opening file: " + toString(), e);
        } catch (final IOException e) {
          if (con instanceof HttpURLConnection) {
            final HttpURLConnection httpUrlConnection = (HttpURLConnection)con;
            httpUrlConnection.disconnect();
          }
          throw Exceptions.wrap(e);
        }
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public Resource getParent() {
    Resource parent = super.getParent();
    if (parent == null) {
      final URL url = getURL();
      final String parentUrl = UrlUtil.getParentString(url);
      parent = new UrlResource(parentUrl, this.username, this.password);
      setParent(parent);
    }
    return parent;
  }

  public String getPassword() {
    return this.password;
  }

  @Override
  public Path getPath() {
    final URI uri = getUri();
    if (isFolderConnection()) {
      return Paths.getPath(uri);
    } else {
      return Paths.getPath(uri);
    }
  }

  public String getProtocol() {
    return this.url.getProtocol();
  }

  /**
   * This implementation returns the underlying URI directly,
   * if possible.
   */
  @Override
  public URI getURI() throws IOException {
    if (this.uri != null) {
      return this.uri;
    } else {
      return super.getURI();
    }
  }

  /**
   * This implementation returns the underlying URL reference.
   */
  @Override
  public URL getURL() {
    return this.url;
  }

  public String getUsername() {
    return this.username;
  }

  /**
   * This implementation returns the hash code of the underlying URL reference.
   */
  @Override
  public int hashCode() {
    return this.cleanedUrl.hashCode();
  }

  protected void initUrl(final URL url) {
    this.url = url;
    final String userInfo = url.getUserInfo();
    if (userInfo != null) {
      final int colonIndex = userInfo.indexOf(':');
      if (colonIndex == -1) {
        this.username = userInfo;
      } else {
        this.username = userInfo.substring(0, colonIndex);
        this.password = userInfo.substring(colonIndex + 1);
      }
    }
  }

  public boolean isFolderConnection() {
    final URL url = getURL();
    final String protocol = url.getProtocol();
    return "folderConnection".equalsIgnoreCase(protocol);
  }

  @Override
  public boolean isReadable() {
    try {
      final URL url = getURL();
      if (ResourceUtils.isFileURL(url)) {
        // Proceed with file system resolution...
        final File file = getFile();
        return file.canRead() && !file.isDirectory();
      } else {
        return true;
      }
    } catch (final Throwable ex) {
      return false;
    }
  }

  @Override
  public long lastModified() throws IOException {
    final URL url = getURL();
    if (ResourceUtils.isFileURL(url) || ResourceUtils.isJarURL(url)) {
      // Proceed with file system resolution...
      return super.lastModified();
    } else {
      // Try a URL connection last-modified header...
      final URLConnection con = url.openConnection();
      customizeConnection(con);
      return con.getLastModified();
    }
  }

  @Override
  public ChannelReader newChannelReader(final ByteBuffer byteBuffer) {
    if (getProtocol().startsWith("http")) {
      return new HttpChannelReader(this.url);
    } else {
      return super.newChannelReader(byteBuffer);
    }
  }

  @Override
  public UrlResource newChildResource(final CharSequence childPath) {
    return createRelative(childPath.toString());
  }

  @Override
  public ReadableByteChannel newReadableByteChannel() {
    if (getProtocol().startsWith("http")) {
      return new HttpSeekableByteChannel(this.url);
    } else {
      return super.newReadableByteChannel();
    }
  }

  public UrlResource newUrlResource(final Map<String, ? extends Object> parameters) {
    final URL url = getURL();
    final String urlString = UrlUtil.getUrl(url, parameters);
    return new UrlResource(urlString, this.username, this.password);
  }

  public UrlResource newUrlResourceAuthorization(final String username, final String password) {
    return new UrlResource(getURL(), username, password);
  }

  private void setAuthorization(final URL url, final HttpURLConnection connection) {
    if (Property.hasValue(this.username)) {
      final String userpass;
      if (this.password == null) {
        userpass = this.username + ":";
      } else {
        userpass = this.username + ":" + this.password;
      }
      final String basicAuth = "Basic " + Base64Util.encodeToString(userpass);
      connection.setRequestProperty("Authorization", basicAuth);
    } else {
      final String userInfo = url.getUserInfo();
      if (userInfo != null) {
        connection.setRequestProperty("Authorization",
          "Basic " + Base64Util.encodeToString(userInfo));
      }
    }
  }
}
