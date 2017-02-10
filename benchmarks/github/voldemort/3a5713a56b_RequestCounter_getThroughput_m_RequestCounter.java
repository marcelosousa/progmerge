{
  Accumulator oldv = getValidAccumulator();
  double elapsed = (time.getMilliseconds() - oldv.startTimeMS) / (double) Time.MS_PER_SECOND;
  if (elapsed > 0.0F)
  {
    return (float) (oldv.count / elapsed);
  }
  else
  {
    return 0.0F;
  }
}