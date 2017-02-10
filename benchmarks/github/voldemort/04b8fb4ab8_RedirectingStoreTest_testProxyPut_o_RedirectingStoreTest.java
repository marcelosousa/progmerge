class RedirectingStoreTest{ 
 void testProxyPut() {
  HashMap<ByteArray, byte[]> entryMap = ServerTestUtils.createRandomKeyValuePairs(TEST_VALUES_SIZE);
  Store<ByteArray, byte[]> store = server1.getStoreRepository().getStorageEngine(testStoreName);
  for (Entry<ByteArray, byte[]> entry : entryMap.entrySet()) {
                                                               store.put(entry.getKey(), Versioned.value(entry.getValue(), new VectorClock().incremented(0, System.currentTimeMillis())));
                                                             }
  server0.getMetadataStore().put(MetadataStore.CLUSTER_KEY, targetCluster);
  server1.getMetadataStore().put(MetadataStore.CLUSTER_KEY, targetCluster);
  incrementVersionAndPut(server0.getMetadataStore(), MetadataStore.SERVER_STATE_KEY, MetadataStore.VoldemortState.REBALANCING_MASTER_SERVER);
  incrementVersionAndPut(server0.getMetadataStore(), MetadataStore.REBALANCING_STEAL_INFO, new RebalancePartitionsInfo(0, 1, Arrays.asList(1), new ArrayList<Integer>(0), Arrays.asList(testStoreName), 0));
  checkPutEntries(entryMap, server0, testStoreName, Arrays.asList(1));
}
}