{
  for (final Integer node : from.keySet()) {
                                             timeFunction(new Timed()
                                                          {
                                                            public void apply ()
                                                            {
                                                              adminClient.migratePartitions(node, to, storeName, new ArrayList<Integer>(from.get(node)), null);
                                                            }
                                                          }, 1);
                                           }
}