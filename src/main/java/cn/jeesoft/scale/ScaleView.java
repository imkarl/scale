package cn.jeesoft.scale;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * 刻度尺
 * @version 0.1 king 2015-08
 */
public class ScaleView extends ScrollView {

    public static final int MIN_WIDTH = 100; // 最小宽度
    public static final int _SCALE_WIDTH = 10; // 单个刻度的宽度or高度
    public static final int _SCALE_START = 140; // 起始刻度值
    public static final int _SCALE_STOP = 300; // 结束刻度值
    public static final int _SCALE_SPACE = 1; // 刻度间隔值

    private static class ScaleItem {
        int point;
        int value;

        @Override
        public String toString() {
            return "["+value+"="+point+"]";
        }
    }

    private DrawView mDrawView;
    private OnScaleChangeListener mOnScaleChangeListener;
    private List<ScaleItem> mScaleItems = new ArrayList<ScaleItem>();

    public ScaleView(Context context) {
        super(context);
        init(context);
    }
    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public ScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScaleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mDrawView = new DrawView(context);
        mDrawView.setMinimumWidth(MIN_WIDTH);
        mDrawView.setMinimumHeight(MIN_WIDTH);
        mDrawView.setBackgroundColor(Color.YELLOW);
        addView(mDrawView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                int y = getScrollY();

                int selectItem = (int) (y / _SCALE_WIDTH);

                // 校准选中位置
                ScaleItem scaleItem = mScaleItems.get(selectItem);
                int selectPoint = scaleItem.point - getHeight() / 2;
                if (selectPoint != y) {
                    scrollTo(0, selectPoint);
                }

                // 通知监听者
                if (mOnScaleChangeListener != null) {
                    mOnScaleChangeListener.onValueChange(scaleItem.value);
                }

                return false;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        this.mOnScaleChangeListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e("View", "onLayout  changed=" + changed + ", top=" + top + ", bottom=" + bottom);

        if (changed && mScaleItems.size()==0) {
            computeScaleItems(left, top, right, bottom);
        }
    }

    private void computeScaleItems(int left, int top, int right, int bottom) {
        int height = bottom - top;
        int pointStart = height / 2;
        // 计算每个刻度的坐标

        int itemCount = (_SCALE_STOP - _SCALE_START) / _SCALE_SPACE;
        for (int i = 0; i <= itemCount; i++) {
            ScaleItem item = new ScaleItem();
            item.point = pointStart + (i * _SCALE_WIDTH);
            item.value = _SCALE_START + (i * _SCALE_SPACE);
            mScaleItems.add(item);
        }

        int drawHeight = ((_SCALE_STOP - _SCALE_START) / _SCALE_SPACE * _SCALE_WIDTH) + height;

        mDrawView.getLayoutParams().height = drawHeight;
        mDrawView.setMinimumHeight(drawHeight);


        // 通知监听者
        if (mOnScaleChangeListener != null) {
            mOnScaleChangeListener.onValueChange(mScaleItems.get(0).value);
        }
    }


    private class DrawView extends View {

        public DrawView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);

            for (ScaleItem item : mScaleItems) {
                if (item.value % 10 == 0) {
                    // 大刻度
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    canvas.drawLine(0, item.point, getWidth() * 0.5F, item.point, paint);
                    canvas.drawText(getDecimal(item.value * 0.01, 2), (getWidth() * 0.5F), item.point+_SCALE_WIDTH, paint);
                } else if (item.value % 5 == 0) {
                    // 中间刻度
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    canvas.drawLine(0, item.point, getWidth() / 3, item.point, paint);
                } else {
                    // 普通小刻度
                    paint.setTypeface(Typeface.DEFAULT);
                    canvas.drawLine(0, item.point, getWidth()/4, item.point, paint);
                }
            }

        }

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
    }

    public static interface OnScaleChangeListener {
        public void onValueChange(int value);
    }

}
