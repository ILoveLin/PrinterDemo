package com.shenma.printtest.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.shenma.printtest.util.LabelBean;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义医用报告View
 * 支持1-9张图片的医用报告渲染
 */
public class MedicalReportView extends View {
    private static final String TAG = "MedicalReportView";
    
    // 纸张尺寸常量 (以像素为单位，基于72 DPI)
    public static final int PAPER_SIZE_A4 = 0;
    public static final int PAPER_SIZE_A5 = 1;
    
    private static final float A4_WIDTH = 595f;
    private static final float A4_HEIGHT = 842f;
    private static final float A5_WIDTH = 420f;
    private static final float A5_HEIGHT = 595f;
    
    private int mPaperSize = PAPER_SIZE_A4;  // 默认A4
    private float mPaperWidth = A4_WIDTH;
    private float mPaperHeight = A4_HEIGHT;
    
    private Paint mPaint;
    private TextPaint mTextPaint;
    private Paint mLinePaint;
    private Paint mImagePaint;
    
    private ArrayList<LabelBean> mReportLabels;
    private ArrayList<LabelBean> mImageAreaLabels;
    
    private Map<String, Bitmap> mImageCache;
    private float mScaleRatio = 1.0f;
    
    private int mViewWidth;
    private int mViewHeight;

    public MedicalReportView(Context context) {
        super(context);
        init();
    }

    public MedicalReportView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MedicalReportView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStrokeWidth(2f);
        
        mImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mImagePaint.setFilterBitmap(true);
        
        mImageCache = new HashMap<>();
        mReportLabels = new ArrayList<>();
        mImageAreaLabels = new ArrayList<>();
    }

    /**
     * 设置报告数据
     */
    public void setReportData(ArrayList<LabelBean> reportLabels, ArrayList<LabelBean> imageAreaLabels) {
        this.mReportLabels = reportLabels;
        this.mImageAreaLabels = imageAreaLabels;
        invalidate();
    }

    /**
     * 设置图片数据
     */
    public void setImageUrl(String order, String imageUrl) {
        // 异步加载图片
        new Thread(() -> {
            try {
                Bitmap bitmap = loadImageFromUrl(imageUrl);
                if (bitmap != null) {
                    mImageCache.put(order, bitmap);
                    postInvalidate();
                }
            } catch (Exception e) {
                Log.e(TAG, "加载图片失败: " + imageUrl, e);
            }
        }).start();
    }

    /**
     * 设置纸张大小
     */
    public void setPaperSize(int paperSize) {
        this.mPaperSize = paperSize;
        
        if (paperSize == PAPER_SIZE_A5) {
            mPaperWidth = A5_WIDTH;
            mPaperHeight = A5_HEIGHT;
        } else {
            mPaperWidth = A4_WIDTH;
            mPaperHeight = A4_HEIGHT;
        }
        
        requestLayout();
        invalidate();
    }
    
    /**
     * 获取当前纸张大小
     */
    public int getPaperSize() {
        return mPaperSize;
    }
    
    /**
     * 获取纸张宽度
     */
    public float getPaperWidth() {
        return mPaperWidth;
    }
    
    /**
     * 获取纸张高度
     */
    public float getPaperHeight() {
        return mPaperHeight;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        
        // 获取屏幕宽度作为参考
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        
        // 计算基准显示宽度（以A4纸张为基准，使用屏幕宽度的90%）
        int baseDisplayWidth = (int) (screenWidth * 0.9f);
        
        // 根据纸张类型计算实际显示宽度
        // A4纸张：使用基准宽度
        // A5纸张：按照A5与A4的宽度比例缩小
        if (mPaperSize == PAPER_SIZE_A5) {
            // A5宽度是A4宽度的 420/595 ≈ 0.706
            float a5ToA4Ratio = A5_WIDTH / A4_WIDTH;
            mViewWidth = (int) (baseDisplayWidth * a5ToA4Ratio);
        } else {
            // A4纸张使用基准宽度
            mViewWidth = baseDisplayWidth;
        }
        
        // 如果是EXACTLY模式且指定的宽度小于计算的宽度，使用指定的宽度
        if (widthMode == MeasureSpec.EXACTLY && widthSize < mViewWidth) {
            mViewWidth = widthSize;
        }
        
        // 根据当前纸张比例计算高度
        float paperRatio = mPaperHeight / mPaperWidth;
        mViewHeight = (int) (mViewWidth * paperRatio);
        
        // 计算缩放比例，保持纸张比例
        mScaleRatio = mViewWidth / mPaperWidth;
        
        Log.d("MedicalReportView", "纸张: " + (mPaperSize == PAPER_SIZE_A5 ? "A5" : "A4") + 
              ", 显示尺寸: " + mViewWidth + "x" + mViewHeight + 
              ", 缩放比例: " + mScaleRatio);
        
        // 设置测量尺寸
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制白色背景
        canvas.drawColor(Color.WHITE);
        
        // 保存画布状态
        canvas.save();
        
        // 应用缩放
        canvas.scale(mScaleRatio, mScaleRatio);
        
        // 绘制报告内容
        drawReportContent(canvas);
        
        // 绘制图片区域
        drawImageArea(canvas);
        
        // 恢复画布状态
        canvas.restore();
    }

    /**
     * 绘制报告内容（文字、线条等）
     */
    private void drawReportContent(Canvas canvas) {
        if (mReportLabels == null || mReportLabels.isEmpty()) {
            return;
        }
        
        for (LabelBean label : mReportLabels) {
            String type = label.getType();
            
            if ("分界线".equals(label.getContent())) {
                drawLine(canvas, label);
            } else if ("徽标".equals(label.getContent())) {
                drawLogo(canvas, label);
            } else if ("图像区域".equals(label.getContent())) {
                // 图像区域单独处理
                continue;
            } else {
                drawText(canvas, label);
            }
        }
    }

    /**
     * 绘制文字
     */
    private void drawText(Canvas canvas, LabelBean label) {
        // 过滤掉"年龄单位"，不进行绘制
        if ("年龄单位".equals(label.getContent())) {
            return;
        }
        
        float left = convertX(Float.parseFloat(label.getLeft()));
        float top = convertY(Float.parseFloat(label.getTop()));
        float right = convertX(Float.parseFloat(label.getRight()));
        float bottom = convertY(Float.parseFloat(label.getBottom()));
        
        float width = right - left;
        float height = bottom - top;
        
        // 设置字体大小
        float fontSize = convertFontSize(Float.parseFloat(label.getFontSize()));
        mTextPaint.setTextSize(fontSize);
        
        // 设置粗体
        if ("1".equals(label.getBold())) {
            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            mTextPaint.setTypeface(Typeface.DEFAULT);
        }
        
        // 设置颜色
        int color = parseColor(label.getColor());
        mTextPaint.setColor(color);
        
        String content = label.getContent();
        
        // 不再绘制特殊背景，保持透明
        // 所有区域使用统一的文字颜色
        
        // 绘制文字
        drawTextInRect(canvas, content, left, top, width, height, label.getAlignment());
    }

    /**
     * 在矩形区域内绘制文字
     */
    private void drawTextInRect(Canvas canvas, String text, float left, float top, float width, float height, String alignment) {
        if (text == null || text.isEmpty()) {
            return;
        }
        
        // 创建StaticLayout用于多行文本
        Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;
        if ("1".equals(alignment)) {
            align = Layout.Alignment.ALIGN_CENTER;
        } else if ("2".equals(alignment)) {
            align = Layout.Alignment.ALIGN_OPPOSITE;
        }
        
        StaticLayout layout = new StaticLayout(text, mTextPaint, (int) width, 
                align, 1.0f, 0.0f, false);
        
        canvas.save();
        canvas.translate(left, top);
        layout.draw(canvas);
        canvas.restore();
    }

    /**
     * 绘制线条
     */
    private void drawLine(Canvas canvas, LabelBean label) {
        float left = convertX(Float.parseFloat(label.getLeft()));
        float top = convertY(Float.parseFloat(label.getTop()));
        float right = convertX(Float.parseFloat(label.getRight()));
        
        if ("1".equals(label.getBold())) {
            mLinePaint.setStrokeWidth(3f);
        } else {
            mLinePaint.setStrokeWidth(1f);
        }
        
        canvas.drawLine(left, top, right, top, mLinePaint);
    }

    /**
     * 绘制Logo
     */
    private void drawLogo(Canvas canvas, LabelBean label) {
        // 可以在这里绘制Logo图片
        float left = convertX(Float.parseFloat(label.getLeft()));
        float top = convertY(Float.parseFloat(label.getTop()));
        float right = convertX(Float.parseFloat(label.getRight()));
        float bottom = convertY(Float.parseFloat(label.getBottom()));
        
        // 如果有Logo图片，从缓存中获取并绘制
        Bitmap logoBitmap = mImageCache.get("logo");
        if (logoBitmap != null) {
            RectF destRect = new RectF(left, top, right, bottom);
            canvas.drawBitmap(logoBitmap, null, destRect, mImagePaint);
        }
    }

    /**
     * 绘制图片区域
     */
    private void drawImageArea(Canvas canvas) {
        if (mImageAreaLabels == null || mImageAreaLabels.isEmpty()) {
            return;
        }
        
        for (LabelBean label : mImageAreaLabels) {
            String type = label.getType();
            
            if ("Image".equals(type)) {
                drawImage(canvas, label);
            } else if ("ImageDesc".equals(type)) {
                drawImageDesc(canvas, label);
            } else if ("ImageSketch".equals(type)) {
                drawImageSketch(canvas, label);
            }
        }
    }

    /**
     * 绘制图片
     */
    private void drawImage(Canvas canvas, LabelBean label) {
        float left = convertX(Float.parseFloat(label.getLeft()));
        float top = convertY(Float.parseFloat(label.getTop()));
        float right = convertX(Float.parseFloat(label.getRight()));
        float bottom = convertY(Float.parseFloat(label.getBottom()));
        
        String order = label.getOrder();
        Bitmap bitmap = mImageCache.get(order);
        
        if (bitmap != null) {
            RectF destRect = new RectF(left, top, right, bottom);
            canvas.drawBitmap(bitmap, null, destRect, mImagePaint);
        } else {
            // 绘制占位框
            Paint placeholderPaint = new Paint();
            placeholderPaint.setColor(Color.LTGRAY);
            placeholderPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(left, top, right, bottom, placeholderPaint);
            
            // 绘制边框
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.GRAY);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2f);
            canvas.drawRect(left, top, right, bottom, borderPaint);
        }
    }

    /**
     * 绘制图片说明
     */
    private void drawImageDesc(Canvas canvas, LabelBean label) {
        float left = convertX(Float.parseFloat(label.getLeft()));
        float top = convertY(Float.parseFloat(label.getTop()));
        float right = convertX(Float.parseFloat(label.getRight()));
        float bottom = convertY(Float.parseFloat(label.getBottom()));
        
        float fontSize = convertFontSize(Float.parseFloat(label.getFontSize()));
        mTextPaint.setTextSize(fontSize);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTypeface(Typeface.DEFAULT);
        
        String content = label.getContent();
        drawTextInRect(canvas, content, left, top, right - left, bottom - top, "1");
    }

    /**
     * 绘制图片标注
     */
    private void drawImageSketch(Canvas canvas, LabelBean label) {
        float left = convertX(Float.parseFloat(label.getLeft()));
        float top = convertY(Float.parseFloat(label.getTop()));
        float right = convertX(Float.parseFloat(label.getRight()));
        float bottom = convertY(Float.parseFloat(label.getBottom()));
        
        // 绘制小图标或标注
        String order = label.getOrder();
        Bitmap sketchBitmap = mImageCache.get("sketch_" + order);
        
        if (sketchBitmap != null) {
            RectF destRect = new RectF(left, top, right, bottom);
            canvas.drawBitmap(sketchBitmap, null, destRect, mImagePaint);
        }
    }

    /**
     * 转换X坐标
     * 1. Windows 96 DPI -> Android 72 DPI
     * 2. 如果是A5纸张，需要额外缩放坐标
     */
    private float convertX(float windowsX) {
        // 第一步：DPI转换 (Windows 96 DPI -> Android 72 DPI)
        float androidX = (windowsX / 96f) * 72f;
        
        // 第二步：如果是A5纸张，需要按比例缩放坐标
        // XML模板中的坐标是基于A4纸张的，需要转换到A5坐标系
        if (mPaperSize == PAPER_SIZE_A5) {
            // A5宽度是A4宽度的 420/595
            float scaleRatio = A5_WIDTH / A4_WIDTH;
            androidX = androidX * scaleRatio;
        }
        
        return androidX;
    }

    /**
     * 转换Y坐标
     * 1. Windows 96 DPI -> Android 72 DPI
     * 2. 如果是A5纸张，需要额外缩放坐标
     */
    private float convertY(float windowsY) {
        // 第一步：DPI转换 (Windows 96 DPI -> Android 72 DPI)
        float androidY = (windowsY / 96f) * 72f;
        
        // 第二步：如果是A5纸张，需要按比例缩放坐标
        // XML模板中的坐标是基于A4纸张的，需要转换到A5坐标系
        if (mPaperSize == PAPER_SIZE_A5) {
            // A5高度是A4高度的 595/842
            float scaleRatio = A5_HEIGHT / A4_HEIGHT;
            androidY = androidY * scaleRatio;
        }
        
        return androidY;
    }

    /**
     * 转换字体大小
     * 1. Windows字体大小 -> Android字体大小
     * 2. 如果是A5纸张，需要按比例缩放字体
     */
    private float convertFontSize(float windowsFontSize) {
        // 第一步：Windows字体大小转换为Android字体大小
        float androidFontSize = windowsFontSize / (1440f / 72f);
        
        // 第二步：如果是A5纸张，需要按比例缩放字体
        // 使用宽度比例来缩放字体，保持视觉一致性
        if (mPaperSize == PAPER_SIZE_A5) {
            float scaleRatio = A5_WIDTH / A4_WIDTH;
            androidFontSize = androidFontSize * scaleRatio;
        }
        
        return androidFontSize;
    }

    /**
     * 解析颜色
     */
    private int parseColor(String colorStr) {
        try {
            int colorInt = Integer.parseInt(colorStr);
            if (colorInt == 0) {
                return Color.BLACK;
            } else if (colorInt == 16711680) {
                return Color.RED;
            } else if (colorInt == 255) {
                return Color.BLUE;
            }
            // RGB转换
            int red = (colorInt >> 16) & 0xFF;
            int green = (colorInt >> 8) & 0xFF;
            int blue = colorInt & 0xFF;
            return Color.rgb(red, green, blue);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    /**
     * 从URL加载图片
     */
    private Bitmap loadImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            Log.e(TAG, "加载图片失败", e);
            return null;
        }
    }

    /**
     * 清除图片缓存
     */
    public void clearImageCache() {
        for (Bitmap bitmap : mImageCache.values()) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        mImageCache.clear();
    }
}
