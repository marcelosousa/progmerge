@Override
 protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate(savedInstanceState);
  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  setContentView(R.layout.activity_scatterchart);
  tvX = (TextView) findViewById(R.id.tvXMax);
  tvY = (TextView) findViewById(R.id.tvYMax);
  mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
  mSeekBarX.setOnSeekBarChangeListener(this);
  mSeekBarY = (SeekBar) findViewById(R.id.seekBar2);
  mSeekBarY.setOnSeekBarChangeListener(this);
  mChart = (ScatterChart) findViewById(R.id.chart1);
  mChart.setDescription("");
  mChart.setOnChartValueSelectedListener(this);
  mChart.setDrawGridBackground(false);
  mChart.setTouchEnabled(true);
  mChart.setMaxHighlightDistance(50.0F);
  mChart.setDragEnabled(true);
  mChart.setScaleEnabled(true);
  mChart.setMaxVisibleValueCount(200);
  mChart.setPinchZoom(true);
  mSeekBarX.setProgress(45);
  mSeekBarY.setProgress(100);
  Legend l = mChart.getLegend();
  l.setPosition(LegendPosition.RIGHT_OF_CHART);
  l.setTypeface(mTfLight);
  YAxis yl = mChart.getAxisLeft();
  yl.setTypeface(mTfLight);
  yl.setAxisMinValue(0.0F);
  mChart.getAxisRight().setEnabled(false);
  XAxis xl = mChart.getXAxis();
  xl.setTypeface(mTfLight);
  xl.setDrawGridLines(false);
  return;
}