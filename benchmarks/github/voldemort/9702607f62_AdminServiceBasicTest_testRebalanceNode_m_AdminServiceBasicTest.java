{
  HashMap<ByteArray, byte[]> entrySet = ServerTestUtils.createRandomKeyValuePairs(TEST_STREAM_KEYS_SIZE);
  List<Integer> fetchAndUpdatePartitionsList = Arrays.asList(0, 2);
  int fetchPartitionKeyCount = 0;
  Store<ByteArray, byte[], byte[]> store = getStore(0, testStoreName);
  for (Entry<ByteArray, byte[]> entry : entrySet.entrySet()) {
                                                               store.put(entry.getKey(), new Versioned<byte[]>(entry.getValue()), null);
                                                               if (isKeyPartition(entry.getKey(), 0, testStoreName, fetchAndUpdatePartitionsList))
                                                               {
                                                                 fetchPartitionKeyCount++;
                                                               }
                                                             }
  List<Integer> rebalancePartitionList = Arrays.asList(1, 3);
  RebalancePartitionsInfo stealInfo = new RebalancePartitionsInfo(1, 0, rebalancePartitionList, rebalancePartitionList, rebalancePartitionList, Arrays.asList(testStoreName), new HashMap<String, String>(), new HashMap<String, String>(), 0);
  int asyncId = adminClient.rebalanceNode(stealInfo);
  assertNotSame("Got a valid rebalanceAsyncId", (-1), asyncId);
  getAdminClient().waitForCompletion(1, asyncId, 120, TimeUnit.SECONDS);
  store = getStore(1, testStoreName);
  for (Entry<ByteArray, byte[]> entry : entrySet.entrySet()) {
                                                               if (isKeyPartition(entry.getKey(), 1, testStoreName, rebalancePartitionList))
                                                               {
                                                                 assertSame("entry should be present at store", 1, store.get(entry.getKey(), null).size());
                                                                 assertEquals("entry value should match", new String(entry.getValue()), new String(store.get(entry.getKey(), null).get(0).getValue()));
                                                               }
                                                             }
}