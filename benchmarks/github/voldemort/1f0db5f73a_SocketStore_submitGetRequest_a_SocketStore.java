class SocketStore{ 
 void submitGetRequest() {
  StoreUtils.assertValidKey(key);
  GetClientRequest clientRequest = new GetClientRequest(storeName, requestFormat, requestRoutingType, key, transforms);
  requestAsync(clientRequest, callback);
}
}