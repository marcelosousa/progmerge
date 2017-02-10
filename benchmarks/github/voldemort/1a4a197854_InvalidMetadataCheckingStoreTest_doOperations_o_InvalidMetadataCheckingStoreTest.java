class InvalidMetadataCheckingStoreTest{ 
 void doOperations() {
  for (int i = 0 ; i < LOOP_COUNT ;)
  {
    ByteArray key = new ByteArray(ByteUtils.md5(Integer.toString(((int) (Math.random() * Integer.MAX_VALUE))).getBytes()));
    byte[] value = "value".getBytes();
    RoutingStrategy routingStrategy = new RoutingStrategyFactory().updateRoutingStrategy(storeDef, metadata.getCluster());
    if (containsNodeId(routingStrategy.routeRequest(key.get()), nodeId))
    {
      i++;
      switch (i % 3)
      {
        case 0:
          store.get(key);
          break;
        case 1:
          store.delete(key, null);
          break;
        case 2:
          store.put(key, new Versioned<byte[]>(value));
          break;
      }
    }
  }
}
}