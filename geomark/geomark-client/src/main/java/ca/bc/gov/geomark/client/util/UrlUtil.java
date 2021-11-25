package ca.bc.gov.geomark.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class UrlUtil {
  /**
   * Clean repeated // characters from the URL path and trim whitespace.
   *
   * @param url The URL to clean.
   * @return The cleaned URL.
   */
  public static String cleanPath(final String url) {
    return url.replaceAll("/+", "/")
      .replaceAll("^((\\w)+:)/", "$1//")
      .replaceAll("^file://", "file:///")
      .trim();
  }

  /**
   * Get an InputStream for the URL.
   *
   * @param url The URL to connect to.
   * @return The InputStream.
   * @throws RuntimeException If a connection to the URL could not be made.
   */
  public static InputStream getInputStream(final String url) {
    try {
      final URL urlObject = new URL(url);
      return urlObject.openStream();
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to connect to :" + url, e);
    }
  }

  public static String getQueryString(final Map<String, ? extends Object> parameters) throws Error {
    if (parameters == null || parameters.isEmpty()) {
      return "";
    } else {
      final StringBuilder query = new StringBuilder();
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
          boolean first = true;
          if (value instanceof Iterable) {
            final Iterable<?> values = (Iterable<?>)value;
            for (final Object paramValue : values) {
              if (first) {
                first = false;
              } else {
                query.append('&');
              }
              query.append(name).append('=').append(urlEncode(paramValue));
            }
          } else if (value instanceof String[]) {
            final String[] values = (String[])value;
            for (int i = 0; i < values.length; i++) {
              query.append(name).append('=').append(urlEncode(values[i]));
              if (i < values.length - 1) {
                query.append('&');
              }
            }
          } else if (value instanceof List) {
            @SuppressWarnings("rawtypes")
            final List values = (List)value;
            for (int i = 0; i < values.size(); i++) {
              query.append(name).append('=').append(urlEncode(values.get(i)));
              if (i < values.size() - 1) {
                query.append('&');
              }
            }
          } else {
            query.append(name).append('=').append(urlEncode(value));
          }

        }
      }
      return query.toString();
    }
  }

  public static String getUrl(final String baseUrl,
    final Map<String, ? extends Object> parameters) {
    final String query = getQueryString(parameters);
    if (query.length() == 0) {
      return baseUrl;
    } else {
      final int qsIndex = baseUrl.indexOf('?');
      if (qsIndex == baseUrl.length() - 1) {
        return baseUrl + query;
      } else if (qsIndex > -1) {
        return baseUrl + '&' + query;
      } else {
        return baseUrl + '?' + query;
      }
    }
  }

  public static Map<String, Object> postMultipartJsonResponse(final String path,
    final Map<String, Object> parameters) {
    try {
      final String boundary = UUID.randomUUID().toString();

      final URL url = new URL(path + ".json");
      final URLConnection conn = url.openConnection();
      conn.setRequestProperty("content-type", "multipart/form-data; boundary=" + boundary);
      conn.setDoOutput(true);

      try (
        final OutputStream out = conn.getOutputStream();
        final MimeMultipartOutputStream multiOut = new MimeMultipartOutputStream(out, boundary);) {
        multiOut.writeParts(parameters);
      }

      try (
        InputStream in = conn.getInputStream();
        final Reader responseReader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
        return JsonParser.getMap(responseReader);
      } catch (final IOException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    } catch (

    final IllegalArgumentException e) {
      throw e;
    } catch (

    final Throwable e) {
      throw new RuntimeException("Unable to perform request " + path + "?" + parameters, e);
    }
  }

  public static Map<String, Object> postRequestJsonResponse(final String path,
    final Map<String, Object> parameters) {
    final String data = getQueryString(parameters);
    return postRequestJsonResponse(path, data);
  }

  public static Map<String, Object> postRequestJsonResponse(final String path, final String data) {
    try {
      final URL url = new URL(path + ".json");
      final URLConnection conn = url.openConnection();
      conn.setDoOutput(true);

      try (
        final OutputStreamWriter requestWriter = new OutputStreamWriter(conn.getOutputStream())) {
        requestWriter.write(data);
        requestWriter.flush();

        try (
          final Reader responseReader = new InputStreamReader(conn.getInputStream(),
            StandardCharsets.UTF_8)) {
          return JsonParser.getMap(responseReader);
        }
      }
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to perform request " + path + "?" + data, e);
    }
  }

  public static String sign(final String secretKey, final String data) {
    try {
      final SecretKey key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA1");
      final Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(key);
      final byte[] dataBytes = data.getBytes("UTF-8");
      final byte[] digestBytes = mac.doFinal(dataBytes);
      final String signature = Base64.encode(digestBytes);
      return signature;
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to encrypt data " + data, e);
    }
  }

  public static String sign(final String key, final String path, final Object time,
    final Map<String, ? extends Object> parameters) {
    final String data = getQueryString(new TreeMap<String, Object>(parameters));
    final String dataToSign = path + ":" + time + ":" + data;
    final String signature = sign(key, dataToSign);
    return signature;
  }

  public static Map<String, Object> signedPostRequestJsonResponse(final String key,
    final String serverUrl, final String path, final Map<String, Object> parameters) {
    final long time = System.currentTimeMillis();
    final String data = getQueryString(parameters);
    final String dataToSign = path + ":" + time + ":" + data;
    final String signature = sign(key, dataToSign);
    final String requestData = "time=" + time + "&signature=" + urlEncode(signature) + "&" + data;
    return postRequestJsonResponse(serverUrl + path, requestData);
  }

  public static String urlEncode(final Object value) {
    try {
      return URLEncoder.encode(value.toString(), "US-ASCII");
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException("Cannot find US-ASCII encoding", e);
    }
  }
}
