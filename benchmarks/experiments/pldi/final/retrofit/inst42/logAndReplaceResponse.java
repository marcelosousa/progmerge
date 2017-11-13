static final String IDLE_THREAD_NAME = THREAD_PREFIX + "Idle";
private static final int LOG_CHUNK_SIZE = 4000;
static final String THREAD_PREFIX = "Retrofit-";
private final Executor callbackExecutor;
private final Client.Provider clientProvider;
private final Converter converter;
private final ErrorHandler errorHandler;
private final Executor httpExecutor;
private final Log log;
private volatile LogLevel logLevel;
private final Profiler profiler;
private final RequestInterceptor requestInterceptor;
private final Server server;
private Response logAndReplaceResponse (String url, Response response, long elapsedTime) throws IOException
{
  log.log(String.format("<--- HTTP %s %s (%sms)", response.getStatus(), url, elapsedTime));
  if (logLevel.ordinal() >= LogLevel.HEADERS.ordinal())
  {
    {
      int wiz_i = 0;
      Header header = response.getHeaders().get(wiz_i);
      while (wiz_i < response.getHeaders().length())
      {
        {
          log.log(header.toString());
        }
        wiz_i++;
      }
    }
    long bodySize = 0;
    TypedInput body = response.getBody();
    if (body != null)
    {
      bodySize = body.length();
      if (logLevel.ordinal() >= LogLevel.FULL.ordinal())
      {
        if (!response.getHeaders().isEmpty())
        {
          log.log("");
        }
        else
          ;
        if (!(body instanceof TypedByteArray))
        {
          response = Utils.readBodyToBytesIfNecessary(response);
          body = response.getBody();
        }
        else
          ;
        byte[] bodyBytes = ((TypedByteArray) body).getBytes();
        bodySize = bodyBytes.length;
        String bodyMime = body.mimeType();
        String bodyCharset = MimeUtil.parseCharset(bodyMime);
        log.log(new String(bodyBytes, bodyCharset));
      }
      else
        ;
    }
    else
      ;
    log.log(String.format("<--- END HTTP (%s-byte body)", bodySize));
  }
  else
    ;
  return response;
}
