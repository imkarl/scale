package cn.jeesoft.scale;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * 刻度尺-竖向
 * @version 0.1 king 2015-08
 */
public class VerticalScaleView extends ScrollView {

    public static final int MIN_WIDTH = 100; // 最小宽度
    public static final int _SCALE_SPACE = 1; // 刻度值间隔

    public static final int _DIRECTION_LEFT = 0; // 刻度绘制方向
    public static final int _DIRECTION_RIGHT = 1; // 刻度绘制方向

    private DrawView mDrawView;
    private OnScaleValueListener mOnScaleChangeListener;
    private OnScaleDrawListener mOnScaleDrawListener;
    private List<ScaleItem> mScaleItems = new ArrayList<ScaleItem>();
    private Integer mScaleStart; // 起始刻度值
    private Integer mScaleEnd; // 结束刻度值
    private int mScaleWidth = 10; // 单个刻度的宽度or高度
    private Integer mScaleDefault; // 默认刻度值
    private int mDirection =  _DIRECTION_LEFT;// 刻度绘制方向

    public VerticalScaleView(Context context) {
        super(context);
        init(context);
    }
    public VerticalScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public VerticalScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VerticalScaleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
     * 设置选中刻度值监听
     * @param listener 监听器
     */
    public void setOnScaleChangeListener(OnScaleValueListener listener) {
        this.mOnScaleChangeListener = listener;
    }
    /**
     * 设置刻度绘制监听
     * @param listener 监听器
     */
    public void setOnScaleDrawListener(OnScaleDrawListener listener) {
        this.mOnScaleDrawListener = listener;
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
     * 获取刻度绘制方向
     * @return
     */
    public int getDirection() {
        return mDirection;
    }
    /**
     * 设置刻度绘制方向
     * @param direction
     */
    public void setDirection(int direction) {
        this.mDirection = direction;
    }

    /**
     * 获取刻度间隔宽度
     * @return
     */
    public int getScaleWidth() {
        return mScaleWidth;
    }
    /**
     * 设置刻度间隔宽度
     * @param scaleWidth
     */
    public void setScaleWidth(int scaleWidth) {
        this.mScaleWidth = scaleWidth;
    }

    /**
     * 设置选中刻度索引
     * @param position
     * @param isScroll 是否校准滑动位置
     */
    private void setScaleSelectPosition(int position, boolean isScroll) {
        ScaleItem scaleItem = mScaleItems.get(position);
        final int selectPoint = scaleItem.point - getHeight() / 2;
        if (isScroll) {
            post(new Runnable() {
                @Override
                public void run() {
                    scrollTo(0, selectPoint);
                }
            });
        }

        // 通知监听者
        if (mOnScaleChangeListener != null) {
            mOnScaleChangeListener.onValueChange(scaleItem.value);
        }
    }
    /**
     * 设置选中刻度索引
     * @param position
     */
    public void setScaleSelectPosition(int position) {
        setScaleSelectPosition(position, true);
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
        int y = getScrollY();
        int selectItem = (int) (y / mScaleWidth);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_HOVER_MOVE:
                // 设置选中项
                setScaleSelectPosition(selectItem, false);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
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
            item.point = pointStart + (i * mScaleWidth);
            item.value = mScaleStart + (i * _SCALE_SPACE);
            mScaleItems.add(item);
        }

        // 计算刻度尺总高度
        int drawHeight = ((mScaleEnd - mScaleStart) / _SCALE_SPACE * mScaleWidth) + height;
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
                // 是否已绘制
                boolean isDraw = false;
                if (mOnScaleDrawListener != null) {
                    // 自定义绘制方法
                    isDraw = mOnScaleDrawListener.onDrawScale(canvas, item.value, item.point, mDirection);
                }

                if (!isDraw) {
                    // 默认实现的绘制方法
                    if (mDirection == _DIRECTION_LEFT) {
                        if (item.value % 10 == 0) {
                            // 大刻度
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                            canvas.drawLine(0, item.point, getWidth() * 0.5F, item.point, paint);
                            canvas.drawText(getValueShow(item.value), (getWidth() * 0.5F), item.point + mScaleWidth, paint);
                        } else if (item.value % 5 == 0) {
                            // 中间刻度
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                            canvas.drawLine(0, item.point, getWidth() / 3, item.point, paint);
                        } else {
                            // 普通小刻度
                            paint.setTypeface(Typeface.DEFAULT);
                            canvas.drawLine(0, item.point, getWidth() / 4, item.point, paint);
                        }
                    } else {
                        if (item.value % 10 == 0) {
                            // 大刻度
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                            canvas.drawLine(getWidth() * 0.5F, item.point, getWidth(), item.point, paint);
                            canvas.drawText(getValueShow(item.value), (getWidth() * 0.15F), item.point + mScaleWidth, paint);
                        } else if (item.value % 5 == 0) {
                            // 中间刻度
                            paint.setTypeface(Typeface.DEFAULT_BOLD);
                            canvas.drawLine(getWidth() * 2 / 3, item.point, getWidth(), item.point, paint);
                        } else {
                            // 普通小刻度
                            paint.setTypeface(Typeface.DEFAULT);
                            canvas.drawLine(getWidth() * 3 / 4, item.point, getWidth(), item.point, paint);
                        }
                    }
                }
            }
        }

        /**
         * 获取要显示的刻度值
         * @param value 刻度值
         * @return 要显示的内容
         */
        private String getValueShow(int value) {
            String text = null;
            if (mOnScaleChangeListener != null) {
                text = mOnScaleChangeListener.getValueText(value);
            }
            if (TextUtils.isEmpty(text)) {
                text = String.valueOf(value);
            }
            return text;
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
