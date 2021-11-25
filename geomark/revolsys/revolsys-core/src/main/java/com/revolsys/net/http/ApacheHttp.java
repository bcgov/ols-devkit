package com.revolsys.net.http;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;

public class ApacheHttp {

  public static final ContentType XML = ContentType.create("application/xml",
    StandardCharsets.UTF_8);

  public static void execute(final HttpUriRequest request, final Consumer<HttpResponse> action) {
    try (
      final CloseableHttpClient httpClient = newClient()) {
      final HttpResponse response = getResponse(httpClient, request);
      action.accept(response);
    } catch (final ApacheHttpException e) {
      throw e;
    } catch (final Exception e) {
      throw Exceptions.wrap(request.getURI().toString(), e);
    }
  }

  public static <V> V execute(final HttpUriRequest request,
    final Function<HttpResponse, V> action) {
    try (
      final CloseableHttpClient httpClient = newClient()) {
      final HttpResponse response = getResponse(httpClient, request);
      try {
        return action.apply(response);
      } catch (final Exception e) {
        throw Exceptions.wrap(request.getURI().toString() + "\n" + e.getMessage(), e);
      }
    } catch (final ApacheHttpException e) {
      throw e;
    } catch (final Exception e) {
      throw Exceptions.wrap(request.getURI().toString(), e);
    }
  }

  public static void execute(final RequestBuilder requestBuilder,
    final Consumer<HttpResponse> action) {
    final HttpUriRequest request = requestBuilder.build();
    execute(request, action);
  }

  public static <V> V execute(final RequestBuilder requestBuilder,
    final Function<HttpResponse, V> action) {
    final HttpUriRequest request = requestBuilder.build();
    return execute(request, action);
  }

  public static InputStream getInputStream(final HttpUriRequest request) {
    final CloseableHttpClient httpClient = newClient();
    try {
      final HttpResponse response = getResponse(httpClient, request);
      final HttpEntity entity = response.getEntity();
      return new ApacheEntityInputStream(httpClient, entity);
    } catch (final ApacheHttpException e) {
      FileUtil.closeSilent(httpClient);
      throw e;
    } catch (final Exception e) {
      FileUtil.closeSilent(httpClient);
      throw Exceptions.wrap(request.getURI().toString(), e);
    }
  }

  public static InputStream getInputStream(final RequestBuilder requestBuilder) {
    final HttpUriRequest request = requestBuilder.build();
    return getInputStream(request);
  }

  public static JsonObject getJson(final HttpResponse response) {
    final HttpEntity entity = response.getEntity();
    try (
      InputStream in = entity.getContent()) {
      return JsonParser.read(in);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public static JsonObject getJson(final RequestBuilder requestBuilder) {
    final Function<HttpResponse, JsonObject> function = ApacheHttp::getJson;
    return execute(requestBuilder, function);
  }

  public static HttpResponse getResponse(final CloseableHttpClient httpClient,
    final HttpUriRequest request) {
    try {
      final HttpResponse response = httpClient.execute(request);
      final StatusLine statusLine = response.getStatusLine();
      final int statusCode = statusLine.getStatusCode();
      if (statusCode >= 200 && statusCode <= 299) {
        return response;
      } else {
        throw ApacheHttpException.create(request, response);
      }
    } catch (final ApacheHttpException e) {
      throw e;
    } catch (final Exception e) {
      throw Exceptions.wrap(request.getURI().toString(), e);
    }
  }

  public static HttpResponse getResponse(final CloseableHttpClient httpClient,
    final RequestBuilder requestBuilder) {
    final HttpUriRequest request = requestBuilder.build();
    return getResponse(httpClient, request);
  }

  public static String getString(final HttpResponse response) {
    final HttpEntity entity = response.getEntity();
    try (
      InputStream in = entity.getContent()) {
      return FileUtil.getString(in);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public static String getString(final RequestBuilder requestBuilder) {
    final Function<HttpResponse, String> function = ApacheHttp::getString;
    return execute(requestBuilder, function);
  }

  public static CloseableHttpClient newClient() {
    return HttpClientBuilder//
      .create()
      .build();
  }

  public static RequestBuilder setJsonBody(final RequestBuilder requestBuilder,
    final JsonObject body) {
    final String jsonString = body.toJsonString();
    final StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    requestBuilder.setEntity(entity);
    return requestBuilder;
  }

}
