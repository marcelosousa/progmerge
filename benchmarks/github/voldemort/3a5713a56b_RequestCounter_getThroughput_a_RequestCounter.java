{
  Accumulator oldv = getValidAccumulator();
  double elapsed = (System.currentTimeMillis() - oldv.startTimeMS) / (double) Time.MS_PER_SECOND;
  if (elapsed > 0.0F)
  {
    return (float) (oldv.count / elapsed);
  }
  else
  {
    return 0.0F;
  }
}