class ProtoBuffAdminServiceRequestHandler{ 
 void handleFetchPartitionEntries() {
  try
  {
    String storeName = request.getStore();
    StorageEngine<ByteArray, byte[]> storageEngine = storeRepository.getStorageEngine(storeName);
    if (storageEngine == null)
    {
      throw new VoldemortException("No store named '" + storeName + "'.");
    }
    RoutingStrategy routingStrategy = metadataStore.getRoutingStrategy(storageEngine.getName());
    EventThrottler throttler = new EventThrottler(streamMaxBytesReadPerSec);
    List<Integer> partitionList = request.getPartitionsList();
    boolean fetchValues = request.hasFetchValues() && request.getFetchValues();
    VoldemortFilter filter;
    if (request.hasFilter())
    {
      filter = getFilterFromRequest(request.getFilter());
    }
    else
    {
      filter = new DefaultVoldemortFilter();
    }
    ClosableIterator<Pair<ByteArray, Versioned<byte[]>>> iterator = storageEngine.entries();
    while (iterator.hasNext())
    {
      Pair<ByteArray, Versioned<byte[]>> entry = iterator.next();
      if (validPartition(entry.getFirst().get(), partitionList, routingStrategy) && filter.filter(entry.getFirst(), entry.getSecond()))
      {
        VAdminProto.FetchPartitionEntriesResponse.Builder response = VAdminProto.FetchPartitionEntriesResponse.newBuilder();
        if (fetchValues)
        {
          VAdminProto.PartitionEntry partitionEntry = VAdminProto.PartitionEntry.newBuilder().setKey(ProtoUtils.encodeBytes(entry.getFirst())).setVersioned(ProtoUtils.encodeVersioned(entry.getSecond())).build();
          response.setPartitionEntry(partitionEntry);
        }
        else
        {
          response.setKey(ProtoUtils.encodeBytes(entry.getFirst()));
        }
        Message message = response.build();
        ProtoUtils.writeMessage(outputStream, message);
        if (throttler != null)
        {
          throttler.maybeThrottle(entrySize(entry));
        }
      }
    }
    iterator.close();
    ProtoUtils.writeEndOfStream(outputStream);
  }
  catch (VoldemortException e)
  {
    VAdminProto.FetchPartitionEntriesResponse response = VAdminProto.FetchPartitionEntriesResponse.newBuilder().setError(ProtoUtils.encodeError(errorCodeMapper, e)).build();
    ProtoUtils.writeMessage(outputStream, response);
  }
}
}