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

    /**
     * 选中刻度值改变监听器
     */
    public static interface OnScaleChangeListener {
        public void onValueChange(int value);
    }

    public static final int MIN_WIDTH = 100; // 最小宽度
    public static final int _SCALE_WIDTH = 10; // 单个刻度的宽度or高度
    public static final int _SCALE_SPACE = 1; // 刻度值间隔

    private DrawView mDrawView;
    private OnScaleChangeListener mOnScaleChangeListener;
    private List<ScaleItem> mScaleItems = new ArrayList<ScaleItem>();
    private Integer mScaleStart; // 起始刻度值
    private Integer mScaleEnd; // 结束刻度值
    private Integer mScaleDefault; // 默认刻度值

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

    /**
     * 设置选中刻度值改变监听
     * @param listener 监听器
     */
    public void setOnScaleChangeListener(OnScaleChangeListener listener) {
        this.mOnScaleChangeListener = listener;
    }

    /**
     * 设置刻度值的范围
     * @param start 起始起始刻度值
     * @param end 结束刻度值
     */
    public void setScaleRange(int start, int end) {
        this.mScaleStart = start;
        this.mScaleEnd = end;
        if (mScaleEnd < mScaleStart) {
            throw new IllegalArgumentException("end不能小于start的值");
        }
    }
    /**
     * 设置默认刻度值
     * @param def 刻度值
     */
    public void setScaleDefault(int def) {
        this.mScaleDefault = def;
        if (mScaleDefault < mScaleStart) {
            throw new IllegalArgumentException("def不能小于start的值");
        } else if (mScaleDefault > mScaleEnd) {
            throw new IllegalArgumentException("def不能大于end的值");
        }
    }

    /**
     * 设置选中刻度索引
     * @param position
     */
    public void setScaleSelectPosition(int position) {
        ScaleItem scaleItem = mScaleItems.get(position);
        final int selectPoint = scaleItem.point - getHeight() / 2;
        post(new Runnable() {
            @Override
            public void run() {
                scrollTo(0, selectPoint);
            }
        });

        // 通知监听者
        if (mOnScaleChangeListener != null) {
            mOnScaleChangeListener.onValueChange(scaleItem.value);
        }
    }

    /**
     * 设置选中刻度值
     * @param value
     */
    public void setScaleSelectValue(int value) {
        if (value < mScaleStart) {
            value = mScaleStart;
        } else if (value > mScaleEnd) {
            value = mScaleEnd;
        }
        setScaleSelectPosition((value - mScaleStart) / _SCALE_SPACE);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // 滑动结束，设置选中项
                int y = getScrollY();
                int selectItem = (int) (y / _SCALE_WIDTH);

                // 校准选中位置
                setScaleSelectPosition(selectItem);
                return false;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed && mScaleItems.size()==0) {
            computeScaleItems(left, top, right, bottom);
        }
    }

    /**
     * 计算出所有的刻度坐标
     */
    private void computeScaleItems(int left, int top, int right, int bottom) {
        int height = bottom - top;
        int pointStart = height / 2;

        // 计算每个刻度的坐标
        int itemCount = (mScaleEnd - mScaleStart) / _SCALE_SPACE;
        for (int i = 0; i <= itemCount; i++) {
            ScaleItem item = new ScaleItem();
            item.point = pointStart + (i * _SCALE_WIDTH);
            item.value = mScaleStart + (i * _SCALE_SPACE);
            mScaleItems.add(item);
        }

        // 计算刻度尺总高度
        int drawHeight = ((mScaleEnd - mScaleStart) / _SCALE_SPACE * _SCALE_WIDTH) + height;
        mDrawView.getLayoutParams().height = drawHeight;
        mDrawView.setMinimumHeight(drawHeight);

        // 设置默认选中位置
        setScaleSelectValue(mScaleDefault==null ? 0 : mScaleDefault);
    }

    /**
     * 绘制刻度尺的View
     */
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

            // 将所有刻度绘制出来
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
    }

    /**
     * 刻度（包含坐标、刻度值）
     */
    private static class ScaleItem {
        // 坐标
        int point;
        // 刻度值
        int value;

        @Override
        public String toString() {
            return "["+value+"="+point+"]";
        }
    }

}
