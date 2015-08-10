package cn.jeesoft.scale;

import android.graphics.Canvas;

/**
 * 刻度绘制监听器
 * @version 0.1 king 2015-08
 */
public interface OnScaleDrawListener {

    /**
     * 绘制刻度
     * canvas 画板
     * value 刻度值
     * point 刻度坐标
     * direction 刻度绘制方向
     * @return 是否已处理（true:已处理,false:使用默认方式）
     */
    public boolean onDrawScale(Canvas canvas, int value, int point, int direction);

}
