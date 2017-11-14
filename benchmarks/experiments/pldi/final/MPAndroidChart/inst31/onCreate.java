private LineChart mChart;
private SeekBar mSeekBarX;
private SeekBar mSeekBarY;
private TextView tvX;
private TextView tvY;
@Override
 protected void onCreate (Bundle savedInstanceState)
{
  super.onCreate(savedInstanceState);
  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  setContentView(R.layout.activity_linechart);
  tvX = (TextView) findViewById(R.id.tvXMax);
  tvY = (TextView) findViewById(R.id.tvYMax);
  mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
  mSeekBarY = (SeekBar) findViewById(R.id.seekBar2);
  mSeekBarX.setProgress(45);
  mSeekBarY.setProgress(100);
  mSeekBarY.setOnSeekBarChangeListener(this);
  mSeekBarX.setOnSeekBarChangeListener(this);
  mChart = (LineChart) findViewById(R.id.chart1);
  mChart.setOnChartGestureListener(this);
  mChart.setOnChartValueSelectedListener(this);
  mChart.setDrawGridBackground(false);
  mChart.setDescription("");
  mChart.setNoDataTextDescription("You need to provide data for the chart.");
  mChart.setTouchEnabled(true);
  mChart.setDragEnabled(true);
  mChart.setScaleEnabled(true);
  mChart.setPinchZoom(true);
  MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
  mChart.setMarkerView(mv);
  LimitLine llXAxis = new LimitLine(10.0F, "Index 10");
  llXAxis.setLineWidth(4.0F);
  llXAxis.enableDashedLine(10.0F, 10.0F, 0.0F);
  llXAxis.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
  llXAxis.setTextSize(10.0F);
  XAxis xAxis = mChart.getXAxis();
  Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
  LimitLine ll1 = new LimitLine(130.0F, "Upper Limit");
  ll1.setLineWidth(4.0F);
  ll1.enableDashedLine(10.0F, 10.0F, 0.0F);
  ll1.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
  ll1.setTextSize(10.0F);
  ll1.setTypeface(tf);
  LimitLine ll2 = new LimitLine(-30.0F, "Lower Limit");
  ll2.setLineWidth(4.0F);
  ll2.enableDashedLine(10.0F, 10.0F, 0.0F);
  ll2.setLabelPosition(LimitLabelPosition.RIGHT_BOTTOM);
  ll2.setTextSize(10.0F);
  ll2.setTypeface(tf);
  YAxis leftAxis = mChart.getAxisLeft();
  leftAxis.removeAllLimitLines();
  leftAxis.addLimitLine(ll1);
  leftAxis.addLimitLine(ll2);
  leftAxis.setAxisMaxValue(220.0F);
  leftAxis.setAxisMinValue((-50.0F));
  leftAxis.enableGridDashedLine(10.0F, 10.0F, 0.0F);
  leftAxis.setDrawZeroLine(false);
  leftAxis.setDrawLimitLinesBehindData(true);
  mChart.getAxisRight().setEnabled(false);
  setData(45, 100);
  mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
  Legend l = mChart.getLegend();
  l.setForm(LegendForm.LINE);
  return;
}
