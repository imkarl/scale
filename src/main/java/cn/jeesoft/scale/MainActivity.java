package cn.jeesoft.scale;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @version 0.1 king 2015-08
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout layout = new RelativeLayout(this);

        // 刻度尺-横向
//        HorizontalScaleView scaleView = new HorizontalScaleView(this);
        // 刻度尺-竖向
        final VerticalScaleView scaleView = new VerticalScaleView(this);
        scaleView.setScaleRange(1900, 2020);
        scaleView.setScaleDefault(2000);
        scaleView.setDirection(VerticalScaleView._DIRECTION_LEFT);
        scaleView.setScaleWidth(20);
        scaleView.setId(100);
        // 横向
//        layout.addView(scaleView, 500, 150);
        // 竖向
        layout.addView(scaleView, 150, 500);
        ((RelativeLayout.LayoutParams)scaleView.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);

        // 指示器
        View indexView = new View(this);
        scaleView.setId(101);
        indexView.setBackgroundColor(Color.RED);
        layout.addView(indexView, 10, 10);
        ((RelativeLayout.LayoutParams)indexView.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
        ((RelativeLayout.LayoutParams)indexView.getLayoutParams()).addRule(RelativeLayout.ABOVE, scaleView.getId());

        // 指示器
        final TextView textView = new TextView(this);
        textView.setId(102);
        textView.setTextColor(Color.RED);
        textView.setTextSize(30);
        textView.setGravity(Gravity.RIGHT);
        layout.addView(textView);
        ((RelativeLayout.LayoutParams)textView.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL);
        ((RelativeLayout.LayoutParams)textView.getLayoutParams()).addRule(RelativeLayout.LEFT_OF, scaleView.getId());

        // 设置监听器
        scaleView.setOnScaleChangeListener(new OnScaleValueListener() {
            @Override
            public void onValueChange(int value) {
                textView.setText(String.valueOf(value));
            }
            @Override
            public String getValueText(int value) {
                return String.valueOf(value);
            }

            /**
             * 指定小数点后的个数
             * @param decimal 要处理显示的数值
             * @param length 小数点后的个数
             * @return 处理后的结果
             */
            private String getDecimal(double decimal, int length) {
                String strLen = ".";
                if (length <= 0) {
                    strLen = "";
                } else {
                    for (int i=0; i<length; i++) {
                        strLen += "0";
                    }
                }
                java.text.DecimalFormat df = new java.text.DecimalFormat("0"+strLen);
                return df.format(decimal);
            }
        });
        scaleView.setOnScaleDrawListener(new OnScaleDrawListener() {
            @Override
            public boolean onDrawScale(Canvas canvas, int value, int point, int direction) {
                if (value % 5 == 0) {
                    // 中间刻度
                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(40);

                    // 大刻度
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    canvas.drawLine(0, point, scaleView.getWidth() * 0.5F, point, paint);
                    canvas.drawText(String.valueOf(value), (scaleView.getWidth() * 0.5F), point + scaleView.getScaleWidth(), paint);
                }
                return false;
            }
        });

        setContentView(layout);

    }


}
