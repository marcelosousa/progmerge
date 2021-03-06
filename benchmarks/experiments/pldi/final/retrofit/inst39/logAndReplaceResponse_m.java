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