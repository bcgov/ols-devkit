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
package com.revolsys.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.net.UrlProxy;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;

/**
 * The UrlUtil class is a utility class for processing and create URL strings.
 *
 * @author Paul Austin
 */
public final class UrlUtil {

  private static final String TLD = "\\p{Alpha}+";

  private static final String IP4_ADDRESS = "\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}";

  private static final String DOMAIN_PART = "\\p{Alpha}[\\p{Alpha}0-9\\-]*\\.";

  private static final String DOMAIN_NAME = "(?:" + DOMAIN_PART + ")+" + TLD;

  private static final String DOMAIN = "(?:" + IP4_ADDRESS + "|" + DOMAIN_NAME + ")";

  private static final String WORD_CHARACTERS = "a-zA-Z0-9\\+!#$%&'*+-/=?^_`{}|~";

  private static final String LOCAL_PART = "[" + WORD_CHARACTERS + "][" + WORD_CHARACTERS + "\\.]*["
    + WORD_CHARACTERS + "]?";

  private static final String EMAIL_RE = "^(" + LOCAL_PART + ")@(" + DOMAIN + ")$";

  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_RE);

  public static URI appendPath(final URI uri, final String path) {
    final String originalPath = uri.getPath();
    String newPath = appendPath(originalPath, path);
    return uri.resolve(newPath);
  }

  public static String appendPath(final String originalPath, final String path) {
    final int length = path.length();
    final StringBuilder pathBuilder = new StringBuilder(originalPath);
    int startIndex = 0;
    while (startIndex < length) {
      if (path.charAt(startIndex) == '/') {
        if (pathBuilder.charAt(pathBuilder.length() - 1) != '/') {
          pathBuilder.append('/');
        }
        startIndex++;
      } else {
        final int endIndex = path.indexOf('/', startIndex);
        String pathElement;
        if (endIndex == -1) {
          pathElement = path.substring(startIndex);
          startIndex = length;
        } else {
          pathElement = path.substring(startIndex, endIndex);
          startIndex = endIndex;
        }
        if (pathBuilder.length() == 0 || pathBuilder.charAt(pathBuilder.length() - 1) != '/') {
          pathBuilder.append('/');
        }
        final String encoded = UrlUtil.encodePathSegment(pathElement);
        pathBuilder.append(encoded);

      }
    }
    String newPath = pathBuilder.toString();
    return newPath;
  }

  public static void appendQuery(final StringBuilder query,
    final Map<String, ? extends Object> parameters) throws Error {
    if (parameters != null) {
      boolean firstParameter = true;
      for (final Entry<String, ? extends Object> parameter : parameters.entrySet()) {
        final String name = parameter.getKey();
        final Object value = parameter.getValue();
        if (name != null && value != null) {
          if (!firstParameter) {
            query.append('&');
          } else {
            firstParameter = false;
          }
          try {
            if (value instanceof String[]) {
              final String[] values = (String[])value;
              for (int i = 0; i < values.length; i++) {
                query.append(name).append('=').append(URLEncoder.encode(values[i], "US-ASCII"));
                if (i < values.length - 1) {
                  query.append('&');
                }
              }
            } else if (value instanceof Collection) {
              boolean first = true;
              final Collection<?> values = (Collection<?>)value;
              for (final Object childValue : values) {
                if (childValue != null) {
                  if (first == true) {
                    first = false;
                  } else {
                    query.append('&');
                  }
                  query.append(name)
                    .append('=')
                    .append(URLEncoder.encode(childValue.toString(), "US-ASCII"));
                }
              }

            } else {
              query.append(name)
                .append('=')
                .append(URLEncoder.encode(value.toString(), "US-ASCII"));
            }
          } catch (final UnsupportedEncodingException e) {
            throw new Error(e);
          }

        }
      }
    }
  }

  public static void appendQuery(final StringBuilder query, final String name, final Object value) {
    final boolean firstParameter = query.length() == 0;
    if (name != null && value != null) {
      if (!firstParameter) {
        query.append('&');
      }
      try {
        if (value instanceof String[]) {
          final String[] values = (String[])value;
          for (int i = 0; i < values.length; i++) {
            query.append(name).append('=').append(URLEncoder.encode(values[i], "US-ASCII"));
            if (i < values.length - 1) {
              query.append('&');
            }
          }
        } else if (value instanceof Collection) {
          boolean first = true;
          final Collection<?> values = (Collection<?>)value;
          for (final Object childValue : values) {
            if (childValue != null) {
              if (first == true) {
                first = false;
              } else {
                query.append('&');
              }
              query.append(name)
                .append('=')
                .append(URLEncoder.encode(childValue.toString(), "US-ASCII"));
            }
          }

        } else {
          query.append(name).append('=').append(URLEncoder.encode(value.toString(), "US-ASCII"));
        }
      } catch (final UnsupportedEncodingException e) {
        throw new Error(e);
      }

    }
  }

  /**
   * Clean repeated // characters from the URL path.
   *
   * @param url
   * @return
   */
  public static String cleanPath(final String url) {
    return url.replaceAll("/+", "/")
      .replaceAll("^((\\w)+:)/", "$1//")
      .replaceAll("^file://", "file:///");
  }

  public static String encodePathSegment(final String segment) {

    final byte[] bytes = segment.getBytes(StandardCharsets.UTF_8);
    boolean valid = true;
    int i = 0;
    for (; i < segment.length(); i++) {
      final char c = segment.charAt(i);
      if (!isPathChar(c)) {
        valid = false;
        break;
      }
    }
    if (valid) {
      return segment;
    } else {
      final ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
      for (final byte b : bytes) {
        if (isPathChar(b)) {
          baos.write(b);
        } else {
          baos.write('%');
          final char hex1 = Character.toUpperCase(Character.forDigit(b >> 4 & 0xF, 16));
          final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
          baos.write(hex1);
          baos.write(hex2);
        }
      }
      return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
  }

  public static String getContent(final String urlString) {
    try {
      final URL url = UrlUtil.getUrl(urlString);
      final InputStream in = url.openStream();
      return FileUtil.getString(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read " + urlString, e);
    }
  }

  public static String getFileBaseName(final URL url) {
    final String name = getFileName(url);
    final int dotIndex = name.lastIndexOf('.');
    if (dotIndex != -1) {
      return name.substring(0, dotIndex);
    } else {
      return name;
    }
  }

  public static String getFileName(final String url) {
    return getFileName(getUrl(url));
  }

  public static String getFileName(final URI url) {
    final String path = url.getPath();
    final int index = path.lastIndexOf('/');
    if (index != -1) {
      return path.substring(index + 1);
    } else {
      return path;
    }
  }

  public static String getFileName(final URL url) {
    final String path = url.getPath();
    final int index = path.lastIndexOf('/');
    if (index != -1) {
      return path.substring(index + 1);
    } else {
      return path;
    }
  }

  public static InputStream getInputStream(final String urlString) {
    final URL url = getUrl(urlString);
    return getInputStream(url);
  }

  public static InputStream getInputStream(final URL url) {
    try {
      return url.openStream();
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot open stream for: " + url, e);
    }
  }

  public static String getParent(final String urlString) {
    final int index = urlString.lastIndexOf('/');
    if (index != -1) {
      final String parentPath = urlString.substring(0, index);
      return parentPath;
    } else {
      return urlString;
    }
  }

  public static URL getParent(final URL url) {
    final String urlString = url.toString();
    final int index = urlString.lastIndexOf('/');
    if (index == -1) {
      return url;
    } else {
      final String parentPath = urlString.substring(0, index + 1);
      return getUrl(parentPath);
    }
  }

  public static String getParentString(final URL url) {
    final String urlString = url.toString();
    return getParent(urlString);
  }

  /**
   * Construct a new new URL from the baseUrl with the additional query string
   * parameters.
   *
   * @param baseUrl The baseUrl.
   * @param parameters The additional parameters to add to the query string.
   * @return The new URL.
   */
  public static String getQueryString(final Map<String, ? extends Object> parameters) {
    final StringBuilder query = new StringBuilder();
    appendQuery(query, parameters);
    return query.toString();
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getQueryStringMap(final String queryString) {
    final MapEx map = new LinkedHashMapEx();
    if (Property.hasValue(queryString)) {
      for (final String part : queryString.split("\\&")) {
        final int equalsIndex = part.indexOf("=");
        if (equalsIndex > -1) {
          final String name = part.substring(0, equalsIndex);
          final String value = percentDecode(
            part.substring(equalsIndex + 1).replaceAll("\\+", " "));
          if (map.containsKey(name)) {
            final Object existingValue = map.get(name);
            if (existingValue instanceof List) {
              final List<Object> list = (List<Object>)existingValue;
              list.add(value);
            } else {
              final List<Object> list = new ArrayList<>();
              list.add(existingValue);
              list.add(value);
            }
          } else {
            map.put(name, value);
          }
        }
      }
    }
    return map;
  }

  public static URI getUri(final String uri) {
    return URI.create(uri);
  }

  public static URI getUri(final URL url) {
    try {
      return url.toURI();
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException("Unknown URI: " + url, e);
    }
  }

  /**
   * Construct a new new URL from the baseUrl with the additional query string
   * parameters.
   *
   * @param baseUrl The baseUrl.
   * @param parameters The additional parameters to add to the query string.
   * @return The new URL.
   */
  public static String getUrl(final Object baseUrl,
    final Map<String, ? extends Object> parameters) {
    return getUrl(baseUrl.toString(), parameters);
  }

  public static URL getUrl(final String urlString) {
    if (Property.isEmpty(urlString)) {
      return null;
    } else {
      try {
        return new URL(urlString);
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException("Unknown URL", e);
      }
    }
  }

  /**
   * Construct a new new URL from the baseUrl with the additional query string
   * parameters.
   *
   * @param baseUrl The baseUrl.
   * @param parameters The additional parameters to add to the query string.
   * @return The new URL.
   */
  public static String getUrl(String baseUrl, final Map<String, ? extends Object> parameters) {
    final int fragmentIndex = baseUrl.indexOf('#');
    String fragment = null;
    if (fragmentIndex > -1 && fragmentIndex < baseUrl.length() - 1) {
      fragment = baseUrl.substring(fragmentIndex + 1);
      baseUrl = baseUrl.substring(0, fragmentIndex);
    }
    final String query = getQueryString(parameters);

    String url;
    if (query.length() == 0) {
      url = baseUrl;
    } else {
      final int qsIndex = baseUrl.indexOf('?');
      if (qsIndex == baseUrl.length() - 1) {
        url = baseUrl + query;
      } else if (qsIndex > -1) {
        url = baseUrl + '&' + query;
      } else {
        url = baseUrl + '?' + query;
      }
    }
    if (Property.hasValue(fragment)) {
      return url + "#" + fragment;
    } else {
      return url;
    }
  }

  public static URL getUrl(final URL parent, final String child) {
    if (parent == null) {
      return null;
    } else {
      try {
        // final String encodedChild = percentEncode(child);
        final StringBuilder newUrl = new StringBuilder(parent.toExternalForm());
        final String ref = parent.getRef();
        if (ref != null) {
          newUrl.setLength(newUrl.length() - ref.length() - 1);
        }
        final String query = parent.getQuery();
        if (query != null) {
          newUrl.setLength(newUrl.length() - query.length() - 1);
        }
        if (newUrl.charAt(newUrl.length() - 1) != '/') {
          newUrl.append('/');
        }
        newUrl.append(child);
        if (query != null) {
          newUrl.append('?');
          newUrl.append(query);
        }
        if (ref != null) {
          newUrl.append('#');
          newUrl.append(ref);
        }
        return new URL(newUrl.toString());
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException("Cannot create child URL for " + parent + " + " + child);
      }
    }
  }

  public static URL getUrl(final UrlProxy parent, final String child) {
    if (parent == null) {
      return null;
    } else {
      return parent.getUrl(child);
    }
  }

  public static boolean isPathChar(final int c) {
    // a-zA-Z0-9-._~!$&\()*+,;=:@
    return c >= 'a' && c <= 'z' || c >= '@' && c <= 'Z' || c >= '0' && c <= ';'
      || c >= '\'' && c <= '.' || '=' == c || '_' == c || '~' == c || '!' == c || '$' == c
      || '&' == c;
  }

  public static boolean isValidEmail(final String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }

  public static Map<String, String> parseMatrixParams(final String matrixParams) {
    final Map<String, String> params = new LinkedHashMap<>();
    parseMatrixParams(matrixParams, params);
    return params;
  }

  public static void parseMatrixParams(final String matrixParams,
    final Map<String, String> params) {
    for (final String param : matrixParams.split(";")) {
      final String[] paramParts = param.split("=");
      final String key = paramParts[0];
      if (paramParts.length == 1) {
        params.put(key, null);
      } else {
        final String value = paramParts[1];
        params.put(key, value);
      }
    }
  }

  private static String parseName(final String s, final StringBuilder sb) {
    sb.setLength(0);
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      switch (c) {
        case '+':
          sb.append(' ');
        break;
        case '%':
          try {
            sb.append((char)Integer.parseInt(s.substring(i + 1, i + 3), 16));
            i += 2;
          } catch (final NumberFormatException e) {
            // XXX
            // need to be more specific about illegal arg
            throw new IllegalArgumentException();
          } catch (final StringIndexOutOfBoundsException e) {
            final String rest = s.substring(i);
            sb.append(rest);
            if (rest.length() == 2) {
              i++;
            }
          }

        break;
        default:
          sb.append(c);
        break;
      }
    }

    return sb.toString();
  }

  public static Map<String, String[]> parseQueryString(final String s) {

    String valArray[] = null;

    if (s == null) {
      throw new IllegalArgumentException();
    }

    final Map<String, String[]> ht = new LinkedHashMap<>();
    final StringBuilder sb = new StringBuilder();
    final StringTokenizer st = new StringTokenizer(s, "&");
    while (st.hasMoreTokens()) {
      final String pair = st.nextToken();
      final int pos = pair.indexOf('=');
      if (pos == -1) {
        // XXX
        // should give more detail about the illegal argument
        throw new IllegalArgumentException();
      }
      final String key = parseName(pair.substring(0, pos), sb);
      final String val = parseName(pair.substring(pos + 1, pair.length()), sb);
      if (ht.containsKey(key)) {
        final String oldVals[] = ht.get(key);
        valArray = new String[oldVals.length + 1];
        for (int i = 0; i < oldVals.length; i++) {
          valArray[i] = oldVals[i];
        }
        valArray[oldVals.length] = val;
      } else {
        valArray = new String[1];
        valArray[0] = val;
      }
      ht.put(key, valArray);
    }

    return ht;
  }

  public static String percentDecode(final String encodedText) {
    final int len = encodedText.length();
    final StringBuilder decoded = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      char ch = encodedText.charAt(i);
      if (ch == '%') {
        final String hex = encodedText.substring(i + 1, i + 3);
        ch = (char)Integer.parseInt(hex, 16);
        i += 2;
      }
      decoded.append(ch);

    }
    return decoded.toString();
  }

  public static String percentEncode(final String text) {
    final int len = text.length();
    final StringBuilder encoded = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      final char ch = text.charAt(i);
      if (ch >= 'A' && ch <= 'Z' || //
        ch >= 'a' && ch <= 'z' || //
        ch >= '0' && ch <= '9' || //
        ch == '-' || ch == '_' || ch == '.' || ch == '~' || //
        ch == '?' || ch == '/' //
      ) {
        encoded.append(ch);
      } else {
        encoded.append('%');
        if (ch < 0x10) {
          encoded.append('0');
        }
        encoded.append(Integer.toHexString(ch));
      }
    }
    return encoded.toString();
  }

  public static File toFile(final URL url) {
    try {
      final URI uri = url.toURI();
      final Path path = Paths.get(uri);
      return path.toFile();
    } catch (final URISyntaxException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static URI toUri(final Object value) {
    try {
      if (value == null) {
        return null;
      } else if (value instanceof URL) {
        final URL url = (URL)value;
        return url.toURI();
      } else if (value instanceof Resource) {
        final Resource resource = (Resource)value;
        return resource.getURI();
      } else if (value instanceof File) {
        final File file = (File)value;
        return file.toURI();
      } else if (value instanceof Path) {
        final Path path = (Path)value;
        return path.toUri();
      } else {
        final String string = DataTypes.toString(value);
        return URI.create(string);
      }
    } catch (final Throwable e) {
      throw Exceptions.wrap(e);
    }
  }

  public static URL toUrl(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof URL) {
      return (URL)value;
    } else if (value instanceof UrlProxy) {
      final UrlProxy proxy = (UrlProxy)value;
      return proxy.getUrl();
    } else if (value instanceof File) {
      final File file = (File)value;
      return FileUtil.toUrl(file);
    } else if (value instanceof Path) {
      final Path path = (Path)value;
      return com.revolsys.io.file.Paths.toUrl(path);
    } else {
      final String string = DataTypes.toString(value);
      return UrlUtil.getUrl(string);
    }
  }

  /**
   * Construct a new UrlUtil.
   */
  private UrlUtil() {
  }

}
