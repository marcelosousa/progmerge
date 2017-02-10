class PipelineRoutedStore{ 
 void put() {
  StoreUtils.assertValidKey(key);
  PutPipelineData pipelineData = new PutPipelineData();
  if (zoneRoutingEnabled)
    pipelineData.setZonesRequired(storeDef.getZoneCountWrites());
  else
    pipelineData.setZonesRequired(null);
  pipelineData.setStartTimeNs(System.nanoTime());
  pipelineData.setStoreName(name);
  pipelineData.setStats(stats);
  Pipeline pipeline = new Pipeline(Operation.PUT, timeoutConfig.getOperationTimeout(VoldemortOpCode.PUT_OP_CODE), TimeUnit.MILLISECONDS);
  pipeline.setEnableHintedHandoff(isHintedHandoffEnabled());
  HintedHandoff hintedHandoff = null;
  AbstractConfigureNodes<ByteArray, Void, PutPipelineData> configureNodes = putNodeConfiguration(pipelineData, key);
  if (isHintedHandoffEnabled())
    hintedHandoff = new HintedHandoff(failureDetector, slopStores, nonblockingSlopStores, handoffStrategy, pipelineData.getFailedNodes(), timeoutConfig.getOperationTimeout(VoldemortOpCode.PUT_OP_CODE));
  pipeline.addEventAction(Event.STARTED, configureNodes);
  pipeline.addEventAction(Event.CONFIGURED, new PerformSerialPutRequests(pipelineData, (isHintedHandoffEnabled() ? Event.RESPONSES_RECEIVED : Event.COMPLETED), key, transforms, failureDetector, innerStores, storeDef.getRequiredWrites(), versioned, time, Event.MASTER_DETERMINED));
  pipeline.addEventAction(Event.MASTER_DETERMINED, new PerformParallelPutRequests(pipelineData, Event.RESPONSES_RECEIVED, key, transforms, failureDetector, storeDef.getPreferredWrites(), storeDef.getRequiredWrites(), timeoutConfig.getOperationTimeout(VoldemortOpCode.PUT_OP_CODE), nonblockingStores, hintedHandoff));
  if (isHintedHandoffEnabled())
  {
    pipeline.addEventAction(Event.ABORTED, new PerformPutHintedHandoff(pipelineData, Event.ERROR, key, versioned, transforms, hintedHandoff, time));
    pipeline.addEventAction(Event.RESPONSES_RECEIVED, new PerformPutHintedHandoff(pipelineData, Event.HANDOFF_FINISHED, key, versioned, transforms, hintedHandoff, time));
    pipeline.addEventAction(Event.HANDOFF_FINISHED, new IncrementClock(pipelineData, Event.COMPLETED, versioned, time));
  }
  else
    pipeline.addEventAction(Event.RESPONSES_RECEIVED, new IncrementClock(pipelineData, Event.COMPLETED, versioned, time));
  pipeline.addEvent(Event.STARTED);
  if (logger.isDebugEnabled())
  {
    logger.debug(("Operation " + pipeline.getOperation().getSimpleName() + " Key " + ByteUtils.toHexString(key.get())));
  }
  try
  {
    pipeline.execute();
  }
  catch (VoldemortException e)
  {
    stats.reportException(e);
    throw e;
  }
  if (pipelineData.getFatalError() != null)
    throw pipelineData.getFatalError();
}
}