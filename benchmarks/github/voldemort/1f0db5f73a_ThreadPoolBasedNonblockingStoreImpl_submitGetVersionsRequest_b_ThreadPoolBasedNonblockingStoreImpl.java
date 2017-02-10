class ThreadPoolBasedNonblockingStoreImpl{ 
 void submitGetVersionsRequest() {
  submit(new StoreRequest<List<Version>>()
         {
           public List<Version> request (Store<ByteArray, byte[]> store)
           {
             return innerStore.getVersions(key);
           }
         }, callback, timeoutMs, "submit");
}
}