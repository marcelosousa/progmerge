{
  Map<ByteArray, List<Versioned<byte[]>>> proxyKeyValues = proxyGetAll(keys, stealInfoList);
  if (!isReadOnly)
  {
    for (Map.Entry<ByteArray, List<Versioned<byte[]>>> keyValuePair : proxyKeyValues.entrySet()) {
                                                                                                   for (Versioned<byte[]> proxyValue : keyValuePair.getValue()) {
                                                                                                                                                                  try
                                                                                                                                                                  {
                                                                                                                                                                    getInnerStore().put(keyValuePair.getKey(), proxyValue);
                                                                                                                                                                  }
                                                                                                                                                                  catch (ObsoleteVersionException e)
                                                                                                                                                                  {
                                                                                                                                                                  }
                                                                                                                                                                }
                                                                                                 }
  }
  return proxyKeyValues;
}