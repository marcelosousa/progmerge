@Override
 public void drawData (Canvas c)
{
  if (mDrawBitmap == null || mDrawBitmap.getWidth() != mViewPortHandler.getChartWidth() || mDrawBitmap.getHeight() != mViewPortHandler.getChartHeight())
  {
    mDrawBitmap = Bitmap.createBitmap(((int) mViewPortHandler.getChartWidth()), ((int) mViewPortHandler.getChartHeight()), Bitmap.Config.ARGB_4444);
    mBitmapCanvas = new Canvas(mDrawBitmap);
  }
  else
    ;
  mDrawBitmap.eraseColor(Color.TRANSPARENT);
  PieData pieData = mChart.getData();
  {
    int wiz_i = 0;
    PieDataSet set = pieData.getDataSets().get(wiz_i);
    while (wiz_i < pieData.getDataSets().length())
    {
      {
        if (set.isVisible())
          drawDataSet(c, set);
        else
          ;
      }
      wiz_i++;
    }
  }
  c.drawBitmap(mDrawBitmap, 0, 0, mRenderPaint);
  return;
}