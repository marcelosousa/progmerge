{
  StoreUtils.assertValidKey(key);
  GetClientRequest clientRequest = new GetClientRequest(storeName, requestFormat, requestRoutingType, key);
  requestAsync(clientRequest, callback, timeoutMs, "get");
}