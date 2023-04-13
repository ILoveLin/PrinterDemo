package com.shenma.printtest.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/15 10:12
 * desc：
 */
public class ScreenInchUtils {
    private static double mInch = 0;

    public static double getScreenInch(Activity activity) {
        if (mInch != 0.0d) {
            return mInch;
        }

        try {
            int realWidth = 0, realHeight = 0;
            Display display = activity.getWindowManager().getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            if (Build.VERSION.SDK_INT >= 17) {
                Point size = new Point();
                display.getRealSize(size);
                realWidth = size.x;
                realHeight = size.y;
            } else if (Build.VERSION.SDK_INT < 17
                    && Build.VERSION.SDK_INT >= 14) {
                Method mGetRawH = Display.class.getMethod("getRawHeight");
                Method mGetRawW = Display.class.getMethod("getRawWidth");
                realWidth = (Integer) mGetRawW.invoke(display);
                realHeight = (Integer) mGetRawH.invoke(display);
            } else {
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }

            mInch = formatDouble(Math.sqrt((realWidth / metrics.xdpi) * (realWidth / metrics.xdpi) + (realHeight / metrics.ydpi) * (realHeight / metrics.ydpi)), 1);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return mInch;
    }


    /**
     * Double类型保留指定位数的小数，返回double类型（四舍五入）
     * newScale 为指定的位数
     */
    public static double formatDouble(double d, int newScale) {
        BigDecimal bd = new BigDecimal(d);
        return bd.setScale(newScale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private static final int COMPLEX_UNIT_PX = 100;
    private static final int COMPLEX_UNIT_DIP = 101;
    private static final int COMPLEX_UNIT_SP = 102;
    private static final int COMPLEX_UNIT_PT = 103;
    private static final int COMPLEX_UNIT_IN = 104;
    private static final int COMPLEX_UNIT_MM = 105;

    /**
     * TypedValue.applyDimension是一个将各种单位的值转换为像素的方法
     * int w = 20;//单位:mm
     * float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM,w,getResources().getDisplayMetrics());
     * int unit:你想要转换的值的单位
     * float value:你想要转换的值
     * <p>
     * float width:转换成功的值 （单位：px）
     *
     * @param unit
     * @param value
     * @param metrics
     * @return
     */
    public static float applyDimension(int unit, float value,
                                       DisplayMetrics metrics) {
        switch (unit) {
            case COMPLEX_UNIT_PX:
                return value;
            case COMPLEX_UNIT_DIP:
                return value * metrics.density;
            case COMPLEX_UNIT_SP:
                return value * metrics.scaledDensity;
            case COMPLEX_UNIT_PT:
                return value * metrics.xdpi * (1.0f / 72);
            case COMPLEX_UNIT_IN:
                return value * metrics.xdpi;
            case COMPLEX_UNIT_MM:
                return value * metrics.xdpi * (1.0f / 25.4f);
        }
        return 0;

    }

/**
 * *****************************
 */

    /**
     * 获取DPI，图像每英寸长度内的像素点数
     * DPI = 宽 / ((尺寸2 × 宽2) / (宽2 + 高2))1/2 = 长 / ((尺寸2 × 高2) / (宽2 + 高2))1/2
     *
     * @return
     */
    public static float getDPI(Activity mContext) {
        //获取屏幕尺寸
        double screenSize = ScreenInchUtils.getScreenInch(mContext);
        //获取宽高大小
        int widthPx = mContext.getResources().getDisplayMetrics().widthPixels;
        int heightPx = mContext.getResources().getDisplayMetrics().heightPixels;
        float dpi = (float) (widthPx / Math.sqrt((screenSize * screenSize * widthPx * widthPx) / (widthPx * widthPx + heightPx * heightPx)));
        Log.d("MainActivity2", "=====dpi=" + dpi);

        return dpi;
    }

    //毫米转px
    public int mmToPx(int value, Activity mContext) {
        float inch = value / 25.4f;
        int c_value = (int) (inch * getDPI(mContext));
        return c_value;
    }

    // 根据手机的分辨率从 dp 的单位 转成为 px(像素)
    public static int dip2px(Context context, float dpValue) {
        // 获取当前手机的像素密度（1个dp对应几个px）
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f); // 四舍五入取整
    }

    // 根据手机的分辨率从 px(像素) 的单位 转成为 dp
    public static int px2dip(Context context, float pxValue) {
        // 获取当前手机的像素密度（1个dp对应几个px）
        float scale = context.getResources().getDisplayMetrics().density;
        int i = (int) (pxValue / scale + 0.5f);
        Log.d("MainActivity2", "=====dp=" + i);

        return i; // 四舍五入取整
    }

    /**
     * 传入mm转换成dp
     */
    public static int mm2dp(int value, Activity mContext){
        float inch = value / 25.4f;
        float  c_value = (inch * getDPI(mContext));
//        float  c_value = (inch * getDPI(mContext));
        int i = px2dip(mContext, c_value);
        return i;
    }

    /**
     * 获取屏幕的宽度
     */
    public static int getScreenWidth(Context context){
        //从系统服务中获取窗口管理器
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        //从默认显示显示器中获取显示参数保存到dm对象中
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;//返回屏幕的宽度数值
    }

    /**
     * 获取屏幕的高度
     */
    public static  int getScreenHeight(Context context){
        //从系统服务中获取窗口管理器
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        //从默认显示显示器中获取显示参数保存到dm对象中
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;//返回屏幕的高度数值
    }

}