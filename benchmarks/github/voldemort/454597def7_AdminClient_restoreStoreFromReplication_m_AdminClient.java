{
  logger.info(("Restoring data for store:" + storeDef.getName()));
  RoutingStrategyFactory factory = new RoutingStrategyFactory();
  RoutingStrategy strategy = factory.updateRoutingStrategy(storeDef, cluster);
  Map<Integer, List<Integer>> restoreMapping = getReplicationMapping(cluster, restoringNodeId, strategy);
  for (final Entry<Integer, List<Integer>> replicationEntry : restoreMapping.entrySet()) {
                                                                                           final int donorNodeId = replicationEntry.getKey();
                                                                                           executorService.submit(new Runnable()
                                                                                                                  {
                                                                                                                    public void run ()
                                                                                                                    {
                                                                                                                      try
                                                                                                                      {
                                                                                                                        logger.info(("restoring data for store " + storeDef.getName() + " at node " + restoringNodeId + " from node " + replicationEntry.getKey() + " partitions:" + replicationEntry.getValue()));
                                                                                                                        int migrateAsyncId = migratePartitions(donorNodeId, restoringNodeId, storeDef.getName(), replicationEntry.getValue(), null);
                                                                                                                        waitForCompletion(restoringNodeId, migrateAsyncId, adminClientConfig.getRestoreDataTimeout(), TimeUnit.SECONDS);
                                                                                                                        logger.info(("restoring data for store:" + storeDef.getName() + " from node " + donorNodeId + " completed."));
                                                                                                                      }
                                                                                                                      catch (Exception e)
                                                                                                                      {
                                                                                                                        logger.error(("restore operation for store " + storeDef.getName() + "from node " + donorNodeId + " failed."), e);
                                                                                                                      }
                                                                                                                    }
                                                                                                                  });
                                                                                         }
}