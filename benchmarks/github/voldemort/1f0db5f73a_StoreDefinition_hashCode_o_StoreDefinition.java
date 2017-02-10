class StoreDefinition{ 
 void hashCode() {
  return Objects.hashCode(getName(), getType(), getKeySerializer(), getValueSerializer(), getRoutingPolicy(), getRoutingStrategyType(), getReplicationFactor(), getRequiredReads(), getRequiredWrites(), getPreferredReads(), getPreferredWrites(), getViewTargetStoreName(), (getValueTransformation() == null ? null : getValueTransformation().getClass()), (getZoneReplicationFactor() == null ? null : getZoneReplicationFactor().getClass()), getZoneCountReads(), getZoneCountWrites(), getRetentionDays(), getRetentionScanThrottleRate());
}
}