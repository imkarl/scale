package cn.jeesoft.scale;

import android.graphics.Canvas;

/**
 * 刻度值监听器
 * @version 0.1 king 2015-08
 */
public interface OnScaleValueListener {

    /**
     * 选中刻度值改变
     * value 当前选中项的值
     */
    public void onValueChange(int value);

    /**
     * 显示选中刻度值
     * value 当前选中项的值
     * @return 要显示的内容
     */
    public String getValueText(int value);

}
