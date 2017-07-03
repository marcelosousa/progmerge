// Copyright 2012 Square, Inc.
package retrofit.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import retrofit.http.client.Request;
import retrofit.http.mime.FormEncodedTypedOutput;
import retrofit.http.mime.MultipartTypedOutput;
import retrofit.http.mime.TypedOutput;

import static retrofit.http.RestMethodInfo.NO_BODY;

/** Builds HTTP requests from Java method invocations. */
final class RequestBuilder {
  private final Converter converter;

  private RestMethodInfo methodInfo;
  private Object[] args;
  private String apiUrl;
  private List<retrofit.http.client.Header> headers;

  RequestBuilder(Converter converter) {
    this.converter = converter;
  }

  /** Supply cached method metadata info. */
  RequestBuilder methodInfo(RestMethodInfo methodDetails) {
    this.methodInfo = methodDetails;
    return this;
  }

  /** Base API url. */
  RequestBuilder apiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
    return this;
  }

  /** Arguments from method invocation. */
  RequestBuilder args(Object[] args) {
    this.args = args;
    return this;
  }

  /** A list of custom headers. */
  RequestBuilder headers(List<retrofit.http.client.Header> headers) {
    this.headers = headers;
    return this;
  }

  /**
   * Construct a {@link Request} from the supplied information. You <strong>must</strong> call
   * {@link #methodInfo}, {@link #apiUrl}, {@link #args}, and {@link #headers} before invoking this
   * method.
   */
  Request build() throws UnsupportedEncodingException {
    String apiUrl = this.apiUrl;

    StringBuilder url = new StringBuilder(apiUrl);
    if (apiUrl.endsWith("/")) {
      // We enforce relative paths to start with '/'. Prevent a double-slash.
      url.deleteCharAt(url.length() - 1);
    }

    // Append the method relative URL.
    url.append(buildRelativeUrl());

    // Append query parameters, if needed.
    if (methodInfo.hasQueryParams) {
      boolean first = true;
      String requestQuery = methodInfo.requestQuery;
      if (requestQuery != null) {
        url.append(requestQuery);
        first = false;
      }
      String[] requestQueryName = methodInfo.requestQueryName;
      for (int i = 0; i < requestQueryName.length; i++) {
        String query = requestQueryName[i];
        if (query != null) {
          String value = URLEncoder.encode(String.valueOf(args[i]), "UTF-8");
          url.append(first ? '?' : '&').append(query).append('=').append(value);
          first = false;
        }
      }
    }

    List<retrofit.http.client.Header> headers = new ArrayList<retrofit.http.client.Header>();
    if (this.headers != null) {
      headers.addAll(this.headers);
    }
    if (methodInfo.headers != null) {
      headers.addAll(methodInfo.headers);
    }
    // RFC 2616: Field names are case-insensitive
    List<String> lcHeadersToRemove = new ArrayList<String>();
    if (methodInfo.requestParamHeader != null) {
      for (int i = 0; i < methodInfo.requestParamHeader.length; i++) {
        String name = methodInfo.requestParamHeader[i];
        if (name == null) continue;
        Object arg = args[i];
        if (arg != null) {
          headers.add(new retrofit.http.client.Header(name, arg.toString()));
        } else {
          lcHeadersToRemove.add(name.toLowerCase());
        }
      }
    }
    for (Iterator<retrofit.http.client.Header> header = headers.iterator(); header.hasNext();) {
      // RFC 2616: Field names are case-insensitive
      if (lcHeadersToRemove.contains(header.next().getName().toLowerCase()))
        header.remove();
    }

    return new Request(methodInfo.requestMethod, url.toString(), headers, buildBody());
  }

  /** Create the final relative URL by performing parameter replacement. */
  private String buildRelativeUrl() throws UnsupportedEncodingException {
    String replacedPath = methodInfo.requestUrl;
    String[] requestUrlParam = methodInfo.requestUrlParam;
    for (int i = 0; i < requestUrlParam.length; i++) {
      String param = requestUrlParam[i];
      if (param != null) {
        String value = URLEncoder.encode(String.valueOf(args[i]), "UTF-8");
        replacedPath = replacedPath.replace("{" + param + "}", value);
      }
    }
    return replacedPath;
  }

  /** Create the request body using the method info and invocation arguments. */
  private TypedOutput buildBody() {
    switch (methodInfo.requestType) {
      case SIMPLE: {
        int bodyIndex = methodInfo.bodyIndex;
        if (bodyIndex == NO_BODY) {
          return null;
        }
        Object body = args[bodyIndex];
        if (body instanceof TypedOutput) {
          return (TypedOutput) body;
        } else {
          return converter.toBody(body);
        }
      }

      case FORM_ENCODED: {
        FormEncodedTypedOutput body = new FormEncodedTypedOutput();
        String[] requestFormPair = methodInfo.requestFormPair;
        for (int i = 0; i < requestFormPair.length; i++) {
          String name = requestFormPair[i];
          if (name != null) {
            body.addPair(name, String.valueOf(args[i]));
          }
        }
        return body;
      }

      case MULTIPART: {
        MultipartTypedOutput body = new MultipartTypedOutput();
        String[] requestMultipartPart = methodInfo.requestMultipartPart;
        for (int i = 0; i < requestMultipartPart.length; i++) {
          String name = requestMultipartPart[i];
          if (name != null) {
            Object value = args[i];
            if (value instanceof TypedOutput) {
              body.addPart(name, (TypedOutput) value);
            } else {
              body.addPart(name, converter.toBody(value));
            }
          }
        }
        return body;
      }

      default:
        throw new IllegalArgumentException("Unknown request type " + methodInfo.requestType);
    }
  }
}