package com.revolsys.net.http.apache5;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;

public class ApacheHttp {

  public static final ContentType XML = ContentType.create("application/xml",
    StandardCharsets.UTF_8);

  public static void execute(final ClassicHttpRequest request,
    final Consumer<ClassicHttpResponse> action) {
    try (
      final CloseableHttpClient httpClient = newClient()) {
      final ClassicHttpResponse response = getResponse(httpClient, request);
      action.accept(response);
    } catch (final ApacheHttpException e) {
      throw e;
    } catch (final Exception e) {
      throw Exceptions.wrap(getUri(request), e);
    }
  }

  public static <V> V execute(final ClassicHttpRequest request,
    final Function<ClassicHttpResponse, V> action) {
    try (
      final CloseableHttpClient httpClient = newClient()) {
      final ClassicHttpResponse response = getResponse(httpClient, request);
      try {
        return action.apply(response);
      } catch (final Exception e) {
        throw Exceptions.wrap(request.getUri().toString() + "\n" + e.getMessage(), e);
      }
    } catch (final ApacheHttpException e) {
      throw e;
    } catch (final Exception e) {
      throw Exceptions.wrap(getUri(request), e);
    }
  }

  public static void execute(final ClassicRequestBuilder requestBuilder,
    final Consumer<ClassicHttpResponse> action) {
    final ClassicHttpRequest request = requestBuilder.build();
    execute(request, action);
  }

  public static <V> V execute(final ClassicRequestBuilder requestBuilder,
    final Function<ClassicHttpResponse, V> action) {
    final ClassicHttpRequest request = requestBuilder.build();
    return execute(request, action);
  }

  public static InputStream getInputStream(final ClassicHttpRequest request) {
    final CloseableHttpClient httpClient = newClient();
    try {
      final ClassicHttpResponse response = getResponse(httpClient, request);
      final HttpEntity entity = response.getEntity();
      return new ApacheEntityInputStream(httpClient, entity);
    } catch (final ApacheHttpException e) {
      FileUtil.closeSilent(httpClient);
      throw e;
    } catch (final Exception e) {
      FileUtil.closeSilent(httpClient);
      throw Exceptions.wrap(getUri(request), e);
    }
  }

  public static InputStream getInputStream(final ClassicRequestBuilder requestBuilder) {
    final ClassicHttpRequest request = requestBuilder.build();
    return getInputStream(request);
  }

  public static JsonObject getJson(final ClassicHttpResponse response) {
    final HttpEntity entity = response.getEntity();
    try (
      InputStream in = entity.getContent()) {
      return JsonParser.read(in);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public static JsonObject getJson(final ClassicRequestBuilder requestBuilder) {
    final Function<ClassicHttpResponse, JsonObject> function = ApacheHttp::getJson;
    return execute(requestBuilder, function);
  }

  public static ClassicHttpResponse getResponse(final CloseableHttpClient httpClient,
    final ClassicHttpRequest request) {
    try {
      final ClassicHttpResponse response = httpClient.execute(request);
      final int statusCode = response.getCode();
      if (statusCode >= 200 && statusCode <= 299) {
        return response;
      } else {
        throw ApacheHttpException.create(request, response);
      }
    } catch (final ApacheHttpException e) {
      throw e;
    } catch (final Exception e) {
      throw Exceptions.wrap(getUri(request), e);
    }
  }

  public static ClassicHttpResponse getResponse(final CloseableHttpClient httpClient,
    final ClassicRequestBuilder requestBuilder) {
    final ClassicHttpRequest request = requestBuilder.build();
    return getResponse(httpClient, request);
  }

  public static String getString(final ClassicHttpResponse response) {
    final HttpEntity entity = response.getEntity();
    try (
      InputStream in = entity.getContent()) {
      return FileUtil.getString(in);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public static String getString(final ClassicRequestBuilder requestBuilder) {
    final Function<ClassicHttpResponse, String> function = ApacheHttp::getString;
    return execute(requestBuilder, function);
  }

  public static String getUri(final ClassicHttpRequest request) {
    try {
      return request.getUri().toString();
    } catch (final URISyntaxException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static CloseableHttpClient newClient() {
    return HttpClientBuilder//
      .create()
      .build();
  }

  public static ClassicRequestBuilder setJsonBody(final ClassicRequestBuilder requestBuilder,
    final JsonObject body) {
    final String jsonString = body.toJsonString();
    final StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    requestBuilder.setEntity(entity);
    return requestBuilder;
  }

}
