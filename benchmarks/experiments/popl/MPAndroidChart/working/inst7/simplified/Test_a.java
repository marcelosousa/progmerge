package com.xxmassdeveloper.mpchartexample;

public class LineChartActivity2 extends DemoBase implements OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    private LineDataSet setData(int count, int range) {
        LineDataSet set1 = new LineDataSet(count);
        set1.setColor(ColorTemplate.COLORFUL_COLORS_0);
        int i = ColorTemplate.COLORFUL_COLORS_0;

        for (int j = 0; j < count; j++) 
        {
          set1.bla(i);
        }
        set1.setLineWidth(3);
        set1.foo().setDrawCircleHole(0);
        if (set1.bla() == 0)
        {
          i = 1;
        }

        return set1;
    }
}