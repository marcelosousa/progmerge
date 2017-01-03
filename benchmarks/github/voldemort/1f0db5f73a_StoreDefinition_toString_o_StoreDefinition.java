{
  return "StoreDefinition(name = " + getName() + ", type = " + getType() + ", key-serializer = " + getKeySerializer() + ", value-serializer = " + getValueSerializer() + ", routing = " + getRoutingPolicy() + ", routing-strategy = " + getRoutingStrategyType() + ", replication = " + getReplicationFactor() + ", required-reads = " + getRequiredReads() + ", preferred-reads = " + getPreferredReads() + ", required-writes = " + getRequiredWrites() + ", preferred-writes = " + getPreferredWrites() + ", view-target = " + getViewTargetStoreName() + ", value-transformation = " + getValueTransformation() + ", retention-days = " + getRetentionDays() + ", throttle-rate = " + getRetentionScanThrottleRate() + ", zone-count-reads = " + getZoneCountReads() + ", zone-count-writes = " + getZoneCountWrites() + ")";
}