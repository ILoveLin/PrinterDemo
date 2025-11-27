package com.shenma.printtest.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.shenma.printtest.R;
import com.shenma.printtest.util.LabelBean;
import com.shenma.printtest.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2025/2/13 14:52
 * desc：
 */
public class MedicalReportView extends View {
    private ArrayList<LabelBean> mImageList;
    private ArrayList<LabelBean> mLineList;
    private ArrayList<LabelBean> mTextList;

    private Paint textPaint;
    private Paint backgroundPaint;
    private Bitmap imageBitmap; // 图片预加载

    public MedicalReportView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化Paint对象（避免在onDraw中创建）
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(16 * getResources().getDisplayMetrics().density);


        // 初始化画笔等

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 计算A4尺寸（单位：像素）
        int dpi = getResources().getDisplayMetrics().densityDpi;
        int width = mmToPx(210); // A4宽度210mm     2333
        int height = mmToPx(297); // A4高度297mm    3299
        LogUtils.e("report====width="+width);
        LogUtils.e("report====height="+height);
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        float widthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 210, metrics);
//        float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 297, metrics);
        setMeasuredDimension((int) width, (int) height);
    }

    private int mmToPx(float mm) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //如果设备的DPI是160，1mm大约是6.3像素（因为1英寸=25.4mm，160dpi等于每英寸160像素，所以每毫米160/25.4≈6.3像素）
        return (int) (mm * metrics.xdpi / 25.4f);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制背景
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        LabelBean item = mTextList.get(0);
        LabelBean imageItem = mImageList.get(1           );
        //写字
        canvas.drawText(item.getContent(), mmToPx(Float.parseFloat(item.getLeft())) ,mmToPx(Float.parseFloat(item.getTop()) ), textPaint);
        //画图
        drawImage(canvas, imageItem);

    }

    private void drawImage(Canvas canvas, LabelBean mBean) {
        // 图片缩放绘制（引用<a target="_blank" href="https://www.jb51.net/article/95335.htm" class="hitref" data-title="Android开发之自定义View(视图)用法详解_Android_脚本之家" data-snippet='View类是Android的一个超类,这个类几乎包含了所有的屏幕类型。每一个View都有一个用于绘图的画布,这个画布可以进行任意扩展。在游戏开发中往往需要自定义视图(View),这个画布...' data-url="https://www.jb51.net/article/95335.htm">7</a><a target="_blank" href="https://blog.csdn.net/guolin_blog/article/details/17357967" class="hitref" data-title="Android自定义View的实现方法，带你一步步深入了解View(四) 原创" data-snippet='一些接触Android不久的朋友对自定义View都有一丝畏惧感，总感觉这是一个比较高级的技术，但其实自定义View并不复杂，有时候只需要简单几行代码就可以完成了。' data-url="https://blog.csdn.net/guolin_blog/article/details/17357967">12</a>坐标系处理）

//        RectF targetRect = new RectF(Integer.parseInt(mBean.getLeft()),
//                Integer.parseInt(mBean.getTop()),Integer.parseInt(mBean.getRight()), Integer.parseInt(mBean.getBottom()));

//        Bitmap bitmap = BitmapFactory.decodeFile(getResources().getDrawable(R.drawable.image_01));

        Bitmap bitmap = drawableToBitmap(getResources().getDrawable(R.drawable.image_01));

        float width = Float.parseFloat(mBean.getLeft())-Float.parseFloat(mBean.getRight());
        float height = Float.parseFloat(mBean.getBottom())-Float.parseFloat(mBean.getTop());

//        Rect targetRect = new Rect(
//                mmToPx(Integer.parseInt(mBean.getLeft())),
//                mmToPx(Integer.parseInt(mBean.getTop())),
//                mmToPx(Integer.parseInt(mBean.getRight()) + width),
//                mmToPx(Integer.parseInt(mBean.getBottom())) + (int) height);
        Rect targetRect = new Rect(
                mmToPx(0),
                mmToPx(0),
                mmToPx(Integer.parseInt(mBean.getRight()) + width),
                mmToPx(Integer.parseInt(mBean.getBottom())) + (int) height);
        canvas.drawBitmap(bitmap, null, targetRect, null);

    }

    // 数据注入方法
    public void setList(ArrayList<LabelBean> test,ArrayList<LabelBean> images) {
        this.mTextList = test;
        this.mImageList = images;
        invalidate(); // 触发重绘

    }
    //drawable convert bitmap
    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }


        if ((drawable.getIntrinsicWidth() <= 0) || (drawable.getIntrinsicHeight() <= 0)) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }


        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


}