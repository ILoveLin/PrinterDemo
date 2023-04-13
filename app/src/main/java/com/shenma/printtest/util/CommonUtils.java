package com.shenma.printtest.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.shenma.printtest.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.stream.Stream;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/17 17:07
 * desc：
 */
public class CommonUtils {


    /**
     * A4纸像素分辨率转换方法：
     *
     * （像素=规格×当前分辨率/2.54）
     *
     * 以下是我们常用的规格尺寸：
     *
     * 当分辨率为72像素/英寸时，A842×595；
     *
     * 当分辨率为100像素/英寸时，A11694纸像素的长度和宽度×826；
     *
     * 当分辨率为120像素/英寸时，A纸像素的长度和宽度分别为2105×1487；
     *
     * 当分辨率为150像素/英寸时，A1754长度和宽度分别为1754×1240；
     *
     * 当分辨率为300像素/英寸时，A纸像素的长度和宽度分别为3508×2479；
     *
     * 1、595*842
     * 2、794*1123
     * 3、1487*2105
     * 4、1240*1754
     * 5、2480*3508
     * 6、992*1403  (这个是我实际使用的)
     *
     */


    /**
     * itext7计算方法
     * 具体坐标值
     * 打印的A4纸的宽高值相差2.38倍
     * <p>
     * <p>
     * <p>
     * 像素与毫米的转换
     * 转换还需要知道另一个参数：DPI（每英寸多少点）
     * 象素数 / DPI = 英寸数
     * 英寸数 * 25.4 = 毫米数
     *
     * @param data window 传进来的像素 ,(96像素/英寸dpi     每像素≈0.013889英寸)  window下面是96
     */


    public static float getNum(float data) {
//     * 当分辨率为72像素/英寸时，595x842；
//        public static PageSize A0 = new PageSize(2384, 3370);
//        public static PageSize A1 = new PageSize(1684, 2384);
//        public static PageSize A2 = new PageSize(1190, 1684);
//        public static PageSize A3 = new PageSize(842, 1190);
//        public static PageSize A4 = new PageSize(595, 842);//* 当分辨率为72像素/英寸时，595x842；
//        public static PageSize A5 = new PageSize(420, 595);
//        public static PageSize A6 = new PageSize(298, 420);
//        public static PageSize A7 = new PageSize(210, 298);
//        public static PageSize A8 = new PageSize(148, 210);
//        public static PageSize A9 = new PageSize(105, 547);
//        public static PageSize A10 = new PageSize(74, 105);
//
//        public static PageSize B0 = new PageSize(2834, 4008);
//        public static PageSize B1 = new PageSize(2004, 2834);
//        public static PageSize B2 = new PageSize(1417, 2004);
//        public static PageSize B3 = new PageSize(1000, 1417);
//        public static PageSize B4 = new PageSize(708, 1000);
//        public static PageSize B5 = new PageSize(498, 708);
//        public static PageSize B6 = new PageSize(354, 498);
//        public static PageSize B7 = new PageSize(249, 354);
//        public static PageSize B8 = new PageSize(175, 249);
//        public static PageSize B9 = new PageSize(124, 175);
//        public static PageSize B10 = new PageSize(88, 124);
//
//        public static PageSize LETTER = new PageSize(612, 792);
//        public static PageSize LEGAL = new PageSize(612, 1008);
//        public static PageSize TABLOID = new PageSize(792, 1224);
//        public static PageSize LEDGER = new PageSize(1224, 792);
//        public static PageSize EXECUTIVE = new PageSize(522, 756);
        //window像素,求得英寸数inch
        float inch = data / 96;
        //像素
        float v = inch * 72;

        return (float) Math.round(v);
    }


    /**
     * @param data 1,对left right  top 和bottom 都安装  x y 各自比例缩放
     *             2,然后对    宽和高也按照比例缩放
     *             <p>
     *             left  Right 相当于 对宽进行缩放
     * @return
     */
    public static float getScaleLeft2Right(float data, PageSize mPageSize) {
        /**
         * 更具当前报告类型做像素值的转变
         */


        float width = mPageSize.getWidth();
        float height = mPageSize.getHeight();

        Log.e("CommonUtils:", "getScaleLeft2Right===width===" + width);
        Log.e("CommonUtils:", "getScaleLeft2Right===height===" + height);

        //window像素
        float inch = data / 96;
        //英寸数
        float v = inch * 72;

        //计算得出android需要的像素然后再转比例
//        Rectangle: 842.0x1190.0    A3,报告模板   只移动X 和 Y
//                        1.145
//        Rectangle: 595.0x842.0     A4,报告模板
//                      1.194
//        Rectangle: 498.0x708.0     B5,报告模板
//
//        A4  A3 B5类型缩放方法
//        x  y  等比例缩放
//                然后宽度x比例缩放
//

        //用A4 宽高/A3宽高
        float mData = 0;
        if (width == 842 && height == 1190) { //A3
            mData = division2float(842, 595) * v;
            Log.e("CommonUtils:", "getScaleLeft2Right===mData===" + mData);

        } else if (width == 595 && height == 842) { //A4
            mData = v;
            Log.e("CommonUtils:", "getScaleLeft2Right===mData===" + mData);

        } else if (width == 498 && height == 708) { //A5
            mData = v * division2float(708, 842);
            Log.e("CommonUtils:", "getScaleLeft2Right===mData===" + mData);

        } else {
            mData = v;
        }

        return (float) Math.round(mData);
    }

    public static float getScaleTop2Bottom(float data, PageSize mPageSize) {
        /**
         * 更具当前报告类型做像素值的转变
         */

//        A4  A3 B5类型缩放方法
//        x  y  等比例缩放
//                然后宽度x比例缩放
//
        float width = mPageSize.getWidth();
        float height = mPageSize.getHeight();
        Log.e("CommonUtils:", "getScaleTop2Bottom===width===" + width);
        Log.e("CommonUtils:", "getScaleTop2Bottom===height===" + height);

        //window像素
//        float inch = data / 96;
        float inch = division2float(data, 96);
        //英寸数
        float v = inch * 72;
        //计算得出android需要的像素然后再转比例

        //用A4 宽高/A3宽高
        float mData = 0;
        if (width == 842 && height == 1190) { //A3

            mData = division2float(1190, 842) * v;
            Log.e("CommonUtils:", "getScaleTop2Bottom======" + mData);


        } else if (width == 595 && height == 842) { //A4

            mData = v;
            Log.e("CommonUtils:", "getScaleTop2Bottom======" + mData);


        } else if (width == 498 && height == 708) { //A5

            mData = v * division2float(708, 842);
            Log.e("CommonUtils:", "getScaleTop2Bottom======" + mData);

        } else {
            mData = v * (1f);
        }

        return (float) Math.round(mData);
    }

    /**
     * @param data 传入wind bottom的值 我们进行换算
     *             因为setFixedPosition api 角标是从左下角0起始位置   window 左上角为起始位置
     * @return
     */
    public static float getScaleFixTopNum(float data, PageSize mPageSize) {

//        1024像素=14.222英寸=36.12厘米         (72像素/英寸dpi     每像素≈0.013889英寸)
        //window像素         //英寸数
//        float inch = data / 96;
        float inch = division2float(data, 96);
        float v = inch * 72;
        float width = mPageSize.getWidth();
        float height = mPageSize.getHeight();
        Log.e("CommonUtils:", "getScaleFixTopNum===width===" + width);
        Log.e("CommonUtils:", "getScaleFixTopNum===height===" + height);

        //用A4 宽高/A3宽高   计算高的缩放
        float mData = 0F;
        if (width == 842 && height == 1190) { //A3
            mData = division2float(1190, 842) * v;
            Log.e("CommonUtils:", "getScaleFixTopNum======" + mData);

        } else if (width == 595 && height == 842) { //A4
            mData = v;
            Log.e("CommonUtils:", "getScaleFixTopNum======" + mData);
        } else if (width == 498 && height == 708) { //B5
            mData = division2float(708, 842) * v;
            Log.e("CommonUtils:", "getScaleFixTopNum======" + mData);

        } else {
            mData = v * (1f);
        }

        //像素
        float v1 = height - (float) Math.round(mData);
        return v1;
    }


//    public static float getZoomXY(float data, PageSize mPageSize) {
//        /**
//         * 更具当前报告类型做像素值的转变
//         */
////        Rectangle: 842.0x1190.0    A3,报告模板   只移动X 和 Y
////                        1.145
////        Rectangle: 595.0x842.0     A4,报告模板
////                      1.194
////        Rectangle: 498.0x708.0     B5,报告模板
////
////        A4  A3 B5类型缩放方法
////        x  y  等比例缩放
////                然后宽度x比例缩放
////
//
//
//        float width = mPageSize.getWidth();
//        float height = mPageSize.getHeight();
//
//
//        //window像素
//        float inch = data / 96;
//        //英寸数
//        float v = inch * 72;
//
//        //计算得出android需要的像素然后再转比例
//
////        Rectangle: 842.0x1190.0    A3,报告模板   只移动X 和 Y
////                   1.145  1.413
////        Rectangle: 595.0x842.0     A4,报告模板
////
////        Rectangle: 498.0x708.0     B5,报告模板
////                  0.836  0.84
//        float mData = 0;
//        if (width == 842 && height == 1190) { //A3
//            mData = v / (0.7f);
//
//        } else if (width == 595 && height == 842) { //A4
//            mData = v * (1f);
//
//        } else if (width == 498 && height == 498) { //A5
//            mData = v * (1.1892f);
//        } else {
//            mData = v * (1f);
//        }
//
//        return (float) Math.round(mData);
//    }


    /**
     * 文字缩放根据X缩放
     *
     * @param data
     * @return
     */
    public static float getScaleFontSizeNum(float data, PageSize mPageSize) {
        /**
         * 像素与毫米的转换
         * 转换还需要知道另一个参数：DPI（每英寸多少点）
         * 象素数 / DPI = 英寸数
         * 英寸数 * 25.4 = 毫米数
         *
         *
         *
         */
//        float v = Float.parseFloat(data);
        //android 对应字体像素大小
//        float v1 = data / (1440f / 72f);
        float v = division2float(1440, 72);
        float v1 = division2float(data, v);
        float width = mPageSize.getWidth();
        float height = mPageSize.getHeight();

        //计算得出android需要的像素然后再转比例
//        Rectangle: 842.0x1190.0    A3,报告模板   只移动X 和 Y
//                   1.145  1.413
//        Rectangle: 595.0x842.0     A4,报告模板
//
//        Rectangle: 498.0x708.0     B5,报告模板
//                  0.836  0.84

        //用A4 宽高/A3宽高
        float mData = 0;
        if (width == 842 && height == 1190) { //A3
            float i = division2float(842, 595);  //倍数
            mData = i * v1;
            Log.e("CommonUtils:", "getScaleFontSizeNum=====A3=" + mData);


        } else if (width == 595 && height == 842) { //A4
            mData = v1 * (1f);
            Log.e("CommonUtils:", "getScaleFontSizeNum=====A4=" + mData);


        } else if (width == 498 && height == 708) { //B5
            float i = division2float(498, 595); //倍数
            mData = v1 * i;
            Log.e("CommonUtils:", "getScaleFontSizeNum=====B5=" + mData);

        } else {
            mData = v1 * (1f);

        }

        return Math.round(mData);
//        return Math.round(v1);

    }

    public static RelativeLayout.LayoutParams getParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return params;
    }

    public static DeviceRgb getRGBColor(int color) {


        float red = (color >> 16) & 0xFF;
        float green = (color >> 8) & 0xFF;
        float blue = (color >> 0) & 0xFF;
        float alpha = (color >> 24) & 0xFF;
        DeviceRgb text_color = new DeviceRgb(red, green, blue);
        return text_color;


    }

    /**
     * 加载rgba格式颜色
     * 注意格式是 rgba(7, 66, 244, 0.64)
     *
     * @param color
     */
    public static DeviceRgb getColorRgba(int color) {
//16711680  红色   255  蓝色
        try {
            float red = (color >> 16) & 0xFF;
            float green = (color >> 8) & 0xFF;
            float blue = (color >> 0) & 0xFF;
            float alphaF = (color >> 24) & 0xFF;
            if (0 <= red && red <= 255 &&
                    0 <= green && green <= 255 &&
                    0 <= blue && blue <= 255 &&
                    0f <= alphaF && alphaF <= 1f) {
                DeviceRgb text_color = new DeviceRgb(red, green, blue);
                return text_color;

//                return Color.argb((int) (alphaF * 255), red, green, blue);
            }
        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    public static int getColorRgba(String rgba) {
        String substring = rgba.substring(5, rgba.length() - 1);
        String[] split = substring.split(",");
        int argb = 0;
        try {
            int red = Integer.parseInt(split[0].trim());
            int green = Integer.parseInt(split[1].trim());
            int blue = Integer.parseInt(split[2].trim());
            float alphaF = Float.parseFloat(split[3].trim());
            if (0 <= red && red <= 255 &&
                    0 <= green && green <= 255 &&
                    0 <= blue && blue <= 255 &&
                    0f <= alphaF && alphaF <= 1f) {
                return Color.argb((int) (alphaF * 255), red, green, blue);
            }
        } catch (NumberFormatException ignored) {
        }

        return argb;
    }

    /**
     * setFixedPosition  在绝对位置插入文字
     * <p>
     * <p>
     * pageNumber:要设置绝对位置所在的页码
     * left：添加文本的左下角相对原点的x坐标
     * bottom:添加文本的左下角相对原点的y坐标
     * width:添加文本的横向宽度
     * <p>
     * 说明:itext7 的绝对位置坐标体系左下点为起始点
     * 模板里面的坐标体系右上角为七点
     * <p>
     * 所以我们设置绝对坐标的时候需要反着计算
     * <p>
     * 比如主标题 模板:Top:30,Bottom:63, height:33像素    Left:48,Right:708 width:660像素
     * //把win像素转换成72英寸的Android像素后 设置位置
     * float num = mPageHeigth - CommonUtils.getNum(33);  //获取android 具体To
     * mTitleLayout.setFixedPosition(1, CommonUtils.getNum(48), 816-33, CommonUtils.getNum(660));
     * setFixedPosition(1,48,)
     */










    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        Log.e("TAG", "isExternalStorageWritable: " + state);
        return false;
    }

    /**
     * TODO 除法运算，保留小数
     *
     * @param a 被除数
     * @param b 除数
     * @return 商
     * @date 2018-4-17下午2:24:48
     */
    public static float division2float(float a, float b) {
        // TODO 自动生成的方法存根

        DecimalFormat df = new DecimalFormat("0.0000");//设置保留位数
        String format = df.format((float) a / b);
        float v = Float.parseFloat(format);
        return v;

    }


}
