package cn.jeesoft.scale;

import android.app.Activity;
import android.graphics.Color;
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

        // 刻度尺
        ScaleView scaleView = new ScaleView(this);
        scaleView.setId(100);
        layout.addView(scaleView, 150, 500);
        ((RelativeLayout.LayoutParams)scaleView.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);

        // 指示器
        View indexView = new View(this);
        scaleView.setId(101);
        indexView.setBackgroundColor(Color.RED);
        layout.addView(indexView, 30, 10);
        ((RelativeLayout.LayoutParams)indexView.getLayoutParams()).addRule(RelativeLayout.CENTER_VERTICAL);
        ((RelativeLayout.LayoutParams)indexView.getLayoutParams()).addRule(RelativeLayout.LEFT_OF, scaleView.getId());

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
        scaleView.setOnScaleChangeListener(new ScaleView.OnScaleChangeListener() {
            @Override
            public void onValueChange(int value) {
                textView.setText(String.valueOf(value));
            }
        });

        setContentView(layout);

    }
}
