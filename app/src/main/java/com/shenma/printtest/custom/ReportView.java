//package com.shenma.printtest.custom;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.drawable.Drawable;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.util.SparseArray;
//import android.view.View;
//
//import com.itextpdf.layout.element.Text;
//import com.shenma.printtest.R;
//import com.shenma.printtest.util.CommonUtils;
//import com.shenma.printtest.util.LabelBean;
//import com.shenma.printtest.util.SaxHelper2Text;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
///**
// * company：江西神州医疗设备有限公司
// * author： LoveLin
// * time：2025/2/13 9:05
// * desc：
// */
//public class ReportView extends View {
//    //private ReportView reportData;
//    private Context mContext;
//
//    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private final SparseArray<Bitmap> imageCache = new SparseArray<>();
//
//    public ReportView(Context context, ReportView data) throws Exception {
//        super(context);
//        this.mContext = context;
//        initPaint();
//
//    }
//
//    private void initPaint() {
//        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setTextAlign(Paint.Align.LEFT);
//
//        backgroundPaint = new Paint();
//        backgroundPaint.setColor(Color.WHITE);
//
//
//    }
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = mmToPx(210);  // 210mm宽
//        int height = mmToPx(297); // 297mm高
//        setMeasuredDimension(width, height);
//    }
//    private int mmToPx(float mm) {
//        DisplayMetrics metrics = getResources().getDisplayMetrics();
//        return (int) (mm * metrics.xdpi / 25.4f);
//    }
//    @Override
//    protected void onDraw(Canvas canvas) {
//
//
//
//        // 绘制背景
//        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
//
////        // 绘制文字
//        for (int i = 0; i < mReportLabelList.size(); i++) {
//            LabelBean text = mReportLabelList.get(i);
//            if (text.getType().equals("Edit")) {
//                //设置字体  1:加粗,0:正常
//                if (text.getBold().equals("1")) {
//                    if (text.getContent().equals("岁")) {
//                        textPaint.setTextSize(Float.parseFloat(text.getFontSize()));
//                        textPaint.setColor(mContext.getResources().getColor(android.R.color.black));
//                        canvas.drawText(text.getContent(), Float.parseFloat(text.getLeft()),Float.parseFloat(text.getTop()), textPaint);
//                    }
//
//                } else {
//
//                }
//
//            }
//
//
//        }
//
//
////
////        // 绘制图片
//
//        int resId = getResources().getIdentifier(
//                "测试", "drawable", getContext().getPackageName());
//
//        LabelBean mBean = mImageAreaList.get(0);
//        float width = Float.parseFloat(mBean.getLeft())-Float.parseFloat(mBean.getRight());
//        float height = Float.parseFloat(mBean.getBottom())-Float.parseFloat(mBean.getTop());
//        Bitmap bitmap = imageCache.get(resId);
//        if (bitmap == null) {
//            bitmap = decodeSampledBitmap(resId, (int)width, (int)height);
//            imageCache.put(resId, bitmap);
//        }
//
//        canvas.drawBitmap(bitmap,
//                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
//                new Rect(Integer.parseInt(mBean.getLeft()), Integer.parseInt(mBean.getTop()),
//                        Integer.parseInt(mBean.getLeft() + width), Integer.parseInt(mBean.getTop() + height)),
//                null);
//
//    }
//
//
//    private Bitmap decodeSampledBitmap(int resId, int reqWidth, int reqHeight) {
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeResource(getResources(), resId, options);
//
//        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
//        options.inJustDecodeBounds = false;
//
//        return BitmapFactory.decodeResource(getResources(), resId, options);
//    }
//
//    private int calculateInSampleSize(BitmapFactory.Options options,
//                                      int reqWidth, int reqHeight) {
//        final int width = options.outWidth;
//        final int height = options.outHeight;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//            final int halfWidth = width / 2;
//            final int halfHeight = height / 2;
//
//            while ((halfWidth / inSampleSize) >= reqWidth
//                    && (halfHeight / inSampleSize) >= reqHeight) {
//                inSampleSize *= 2;
//            }
//        }
//        return inSampleSize;
//    }
//
//
//
//}