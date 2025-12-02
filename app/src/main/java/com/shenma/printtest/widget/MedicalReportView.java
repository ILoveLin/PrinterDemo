package com.shenma.printtest.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.shenma.printtest.util.LabelBean;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 自定义医用报告View
 * 支持1-9张图片的医用报告渲染
 * 使用自定义View渲染医用报告
 * author： LoveLin
 * time：2025/12/1 
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
    
    // 用于取消过期的图片加载任务
    private volatile int mLoadVersion = 0;
    
    // 线程池，限制并发数量
    private ExecutorService mExecutor;
    private List<Future<?>> mPendingTasks = new ArrayList<>();
    
    // 图片下载进度 (0-100, -1表示失败)
    private Map<String, Integer> mImageProgress = new HashMap<>();
    
    // 水波动画偏移量
    private float mWaveOffset = 0f;
    
    // 进度状态常量
    private static final int PROGRESS_FAILED = -1;
    
    // 单个图片位置缓存（用于点击检测打开查看器）
    private Map<String, RectF> mImageRects = new HashMap<>();
    
    // 整个图片报告区域的边界（临床诊断/主诉以下，镜检所见以上）
    private RectF mImageAreaBounds = new RectF();
    
    // 用户缩放和平移相关
    private float mUserScale = 1.0f;           // 用户缩放比例
    private float mTranslateX = 0f;            // X轴平移
    private float mTranslateY = 0f;            // Y轴平移
    private static final float MIN_SCALE = 1.0f;   // 最小缩放
    private static final float MAX_SCALE = 3.0f;   // 最大缩放
    
    // 手势检测器
    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;
    private boolean mIsScaling = false;        // 是否正在缩放

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
        Log.d(TAG, "MedicalReportView init() 被调用");
        // 确保 View 可以接收触摸事件
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        
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
        
        // 初始化双指缩放手势检测器
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mIsScaling = true;
                return true;
            }
            
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                
                // 计算新的缩放比例
                float newScale = mUserScale * scaleFactor;
                newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));
                
                if (newScale != mUserScale) {
                    // 以焦点为中心进行缩放
                    float scaleChange = newScale / mUserScale;
                    mTranslateX = focusX - (focusX - mTranslateX) * scaleChange;
                    mTranslateY = focusY - (focusY - mTranslateY) * scaleChange;
                    mUserScale = newScale;
                    
                    constrainTranslation();
                    invalidate();
                }
                return true;
            }
            
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                mIsScaling = false;
            }
        });
        
        // 初始化双击和拖动手势检测器
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                // 必须返回 true，否则 GestureDetector 不会检测后续手势（包括双击）
                return true;
            }
            
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                float x = e.getX();
                float y = e.getY();
                Log.d(TAG, "onDoubleTap: x=" + x + ", y=" + y + ", bounds=" + mImageAreaBounds.toString());
                
                // 检查双击位置是否在B区域（图片区域），如果在则不响应
                if (isPointInImageArea(x, y)) {
                    Log.d(TAG, "onDoubleTap: 在B区域，不响应缩放");
                    return true; // 返回true表示已处理，但不执行缩放
                }
                
                Log.d(TAG, "onDoubleTap: 在A或C区域，执行缩放");
                // A区域或C区域：双击切换缩放
                float targetScale;
                if (mUserScale > 1.5f) {
                    // 已放大，恢复原始大小
                    targetScale = 1.0f;
                    mTranslateX = 0;
                    mTranslateY = 0;
                } else {
                    // 放大到2倍，以点击位置为中心
                    targetScale = 2.0f;
                    float focusX = e.getX();
                    float focusY = e.getY();
                    mTranslateX = focusX - (focusX - mTranslateX) * (targetScale / mUserScale);
                    mTranslateY = focusY - (focusY - mTranslateY) * (targetScale / mUserScale);
                }
                mUserScale = targetScale;
                constrainTranslation();
                invalidate();
                return true;
            }
            
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mIsScaling && mUserScale > 1.0f) {
                    // 只有放大时才允许拖动
                    mTranslateX -= distanceX;
                    mTranslateY -= distanceY;
                    constrainTranslation();
                    invalidate();
                    return true;
                }
                return false;
            }
        });
    }
    
    /**
     * 限制平移范围，防止拖出边界
     */
    private void constrainTranslation() {
        if (mUserScale <= 1.0f) {
            mTranslateX = 0;
            mTranslateY = 0;
            return;
        }
        
        float maxTranslateX = (mUserScale - 1) * mViewWidth / 2;
        float maxTranslateY = (mUserScale - 1) * mViewHeight / 2;
        
        mTranslateX = Math.max(-maxTranslateX, Math.min(maxTranslateX, mTranslateX));
        mTranslateY = Math.max(-maxTranslateY, Math.min(maxTranslateY, mTranslateY));
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
        final int currentVersion = mLoadVersion;
        
        // 初始化进度为0
        synchronized (mImageProgress) {
            mImageProgress.put(order, 0);
        }
        
        // 确保线程池已创建
        if (mExecutor == null || mExecutor.isShutdown()) {
            mExecutor = Executors.newFixedThreadPool(18); // 最多9个并发下载
        }
        
        // 提交任务到线程池
        Future<?> task = mExecutor.submit(() -> {
            try {
                // 检查是否已被取消（切换了模板）
                if (currentVersion != mLoadVersion) {
                    Log.d(TAG, "图片加载已取消(版本过期): " + order);
                    return;
                }
                
                Bitmap bitmap = loadImageFromUrlWithProgress(imageUrl, currentVersion, order);
                
                // 再次检查是否已被取消
                if (currentVersion != mLoadVersion) {
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    Log.d(TAG, "图片加载已取消(版本过期): " + order);
                    return;
                }
                
                if (bitmap != null) {
                    synchronized (mImageCache) {
                        // 回收旧图片
                        Bitmap existing = mImageCache.put(order, bitmap);
                        if (existing != null && !existing.isRecycled()) {
                            existing.recycle();
                        }
                    }
                    // 移除进度记录
                    synchronized (mImageProgress) {
                        mImageProgress.remove(order);
                    }
                    postInvalidate();
                } else {
                    // 加载失败，设置失败状态
                    if (currentVersion == mLoadVersion) {
                        synchronized (mImageProgress) {
                            mImageProgress.put(order, PROGRESS_FAILED);
                        }
                        postInvalidate();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "加载图片失败: " + imageUrl, e);
                // 异常时也设置失败状态
                if (currentVersion == mLoadVersion) {
                    synchronized (mImageProgress) {
                        mImageProgress.put(order, PROGRESS_FAILED);
                    }
                    postInvalidate();
                }
            }
        });
        
        synchronized (mPendingTasks) {
            mPendingTasks.add(task);
        }
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
        
        // 应用用户缩放和平移
        canvas.translate(mTranslateX, mTranslateY);
        canvas.scale(mUserScale, mUserScale, mViewWidth / 2f, mViewHeight / 2f);
        
        // 应用基础缩放（纸张到View的缩放）
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
            mLinePaint.setStrokeWidth(2f);
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
        
        // 重置图片区域边界
        float minLeft = Float.MAX_VALUE;
        float minTop = Float.MAX_VALUE;
        float maxRight = Float.MIN_VALUE;
        float maxBottom = Float.MIN_VALUE;
        
        for (LabelBean label : mImageAreaLabels) {
            String type = label.getType();
            
            if ("Image".equals(type)) {
                drawImage(canvas, label);
                
                // 计算整个图片区域的边界
                float left = convertX(Float.parseFloat(label.getLeft())) * mScaleRatio;
                float top = convertY(Float.parseFloat(label.getTop())) * mScaleRatio;
                float right = convertX(Float.parseFloat(label.getRight())) * mScaleRatio;
                float bottom = convertY(Float.parseFloat(label.getBottom())) * mScaleRatio;
                
                minLeft = Math.min(minLeft, left);
                minTop = Math.min(minTop, top);
                maxRight = Math.max(maxRight, right);
                maxBottom = Math.max(maxBottom, bottom);
            } else if ("ImageDesc".equals(type)) {
                drawImageDesc(canvas, label);
            } else if ("ImageSketch".equals(type)) {
                drawImageSketch(canvas, label);
            }
        }
        
        // 更新整个图片区域的边界
        // B区域横向扩展到整个View宽度，纵向保持图片区域的上下边界
        if (minLeft != Float.MAX_VALUE) {
            // 横向从0到View宽度，纵向保持图片区域边界
            mImageAreaBounds.set(0, minTop, mViewWidth, maxBottom);
            Log.d(TAG, "B区域边界更新: left=0, top=" + minTop + ", right=" + mViewWidth + ", bottom=" + maxBottom);
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
        
        // 保存图片区域位置（缩放后的实际屏幕坐标）
        RectF screenRect = new RectF(left * mScaleRatio, top * mScaleRatio, 
                                      right * mScaleRatio, bottom * mScaleRatio);
        mImageRects.put(order, screenRect);
        
        if (bitmap != null) {
            RectF destRect = new RectF(left, top, right, bottom);
            canvas.drawBitmap(bitmap, null, destRect, mImagePaint);
        } else {
            // 获取当前进度
            int progress = 0;
            synchronized (mImageProgress) {
                Integer p = mImageProgress.get(order);
                if (p != null) progress = p;
            }
            
            if (progress == PROGRESS_FAILED) {
                // 绘制加载失败占位图
                drawFailedPlaceholder(canvas, left, top, right, bottom);
            } else {
                // 绘制加载中占位图（带水波进度）
                drawWaveLoadingPlaceholder(canvas, left, top, right, bottom, progress);
            }
        }
    }

    /**
     * 绘制加载失败占位图
     */
    private void drawFailedPlaceholder(Canvas canvas, float left, float top, float right, float bottom) {
        // 绘制浅红色背景
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#FFF5F5"));
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left, top, right, bottom, bgPaint);
        
        // 绘制边框
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#FFCCCC"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);
        canvas.drawRect(left, top, right, bottom, borderPaint);
        
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;
        
        // 计算圆的半径
        float radius = Math.min(right - left, bottom - top) / 5;
        if (radius > 30) radius = 30;
        if (radius < 10) radius = 10;
        
        // 绘制红色圆形边框
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#FF6B6B"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(2f);
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
        
        // 绘制X号（表示失败）
        Paint xPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xPaint.setColor(Color.parseColor("#FF6B6B"));
        xPaint.setStrokeWidth(2f);
        xPaint.setStrokeCap(Paint.Cap.ROUND);
        float xSize = radius * 0.5f;
        canvas.drawLine(centerX - xSize, centerY - xSize, centerX + xSize, centerY + xSize, xPaint);
        canvas.drawLine(centerX + xSize, centerY - xSize, centerX - xSize, centerY + xSize, xPaint);
    }

    /**
     * 绘制带水波进度的加载占位图
     */
    private void drawWaveLoadingPlaceholder(Canvas canvas, float left, float top, float right, float bottom, int progress) {
        // 绘制浅灰色背景
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#F5F5F5"));
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(left, top, right, bottom, bgPaint);
        
        // 绘制边框
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#E0E0E0"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);
        canvas.drawRect(left, top, right, bottom, borderPaint);
        
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;
        
        // 计算圆的半径
        float radius = Math.min(right - left, bottom - top) / 5;
        if (radius > 30) radius = 30;
        if (radius < 10) radius = 10;
        
        // 绘制圆形边框
        Paint circleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleBorderPaint.setColor(Color.parseColor("#4A90D9"));
        circleBorderPaint.setStyle(Paint.Style.STROKE);
        circleBorderPaint.setStrokeWidth(2f);
        canvas.drawCircle(centerX, centerY, radius, circleBorderPaint);
        
        // 绘制水波填充
        if (progress > 0) {
            canvas.save();
            
            // 裁剪为圆形区域
            Path clipPath = new Path();
            clipPath.addCircle(centerX, centerY, radius - 1, Path.Direction.CW);
            canvas.clipPath(clipPath);
            
            // 计算水位高度（从底部上升）
            float waterHeight = (radius * 2) * progress / 100f;
            float waterTop = centerY + radius - waterHeight;
            
            // 绘制贝塞尔曲线水波
            Path wavePath = new Path();
            float waveHeight = radius / 6; // 波浪高度
            float waveLength = radius;     // 波浪长度
            
            // 起点在左边
            wavePath.moveTo(centerX - radius - waveLength, waterTop);
            
            // 绘制多个波浪
            float x = centerX - radius - waveLength + mWaveOffset;
            while (x < centerX + radius + waveLength) {
                // 贝塞尔曲线绘制波浪
                wavePath.quadTo(x + waveLength / 4, waterTop - waveHeight,
                               x + waveLength / 2, waterTop);
                wavePath.quadTo(x + waveLength * 3 / 4, waterTop + waveHeight,
                               x + waveLength, waterTop);
                x += waveLength;
            }
            
            // 封闭路径（填充到底部）
            wavePath.lineTo(centerX + radius + waveLength, centerY + radius);
            wavePath.lineTo(centerX - radius - waveLength, centerY + radius);
            wavePath.close();
            
            // 填充水波
            Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            wavePaint.setColor(Color.parseColor("#4A90D9"));
            wavePaint.setStyle(Paint.Style.FILL);
            wavePaint.setAlpha(180);
            canvas.drawPath(wavePath, wavePaint);
            
            canvas.restore();
        }
        
        // 更新波浪偏移量（动画效果）
        mWaveOffset += 1f;
        if (mWaveOffset > 60) {
            mWaveOffset = 0;
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
     * 从URL加载图片（带进度回调）
     */
    private Bitmap loadImageFromUrlWithProgress(String imageUrl, int loadVersion, String order) {
        HttpURLConnection connection = null;
        InputStream input = null;
        java.io.ByteArrayOutputStream baos = null;
        try {
            // 加载前检查是否已取消
            if (loadVersion != mLoadVersion) {
                return null;
            }
            
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();
            
            // 连接后再次检查
            if (loadVersion != mLoadVersion) {
                return null;
            }
            
            int contentLength = connection.getContentLength();
            input = connection.getInputStream();
            baos = new java.io.ByteArrayOutputStream();
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalBytesRead = 0;
            
            while ((bytesRead = input.read(buffer)) != -1) {
                // 检查是否已取消
                if (loadVersion != mLoadVersion) {
                    return null;
                }
                
                baos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // 更新进度
                if (contentLength > 0) {
                    int progress = (int) ((totalBytesRead * 100L) / contentLength);
                    synchronized (mImageProgress) {
                        mImageProgress.put(order, progress);
                    }
                    postInvalidate();
                }
            }
            
            // 设置进度为100
            synchronized (mImageProgress) {
                mImageProgress.put(order, 100);
            }
            postInvalidate();
            
            byte[] imageData = baos.toByteArray();
            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        } catch (Exception e) {
            if (loadVersion == mLoadVersion) {
                Log.e(TAG, "加载图片失败", e);
            }
            return null;
        } finally {
            try {
                if (baos != null) baos.close();
                if (input != null) input.close();
                if (connection != null) connection.disconnect();
            } catch (Exception ignored) {}
        }
    }

    // 记录按下时的坐标，用于判断是否是点击
    private float mDownX, mDownY;
    private static final int CLICK_THRESHOLD = 20; // 点击阈值（像素）
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 调试日志
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG, "onTouchEvent ACTION_DOWN: x=" + event.getX() + ", y=" + event.getY());
        }
        
        // 多指触摸时（双指缩放），立即请求父View不要拦截
        int pointerCount = event.getPointerCount();
        if (pointerCount >= 2) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        
        // 先让缩放手势检测器处理（双指缩放在任何区域都可用）
        boolean scaleHandled = mScaleGestureDetector.onTouchEvent(event);
        
        // 始终让 GestureDetector 接收事件（在 onDoubleTap 回调中判断是否执行缩放）
        mGestureDetector.onTouchEvent(event);
        
        // 如果正在缩放或已放大，请求父View不要拦截
        if (mIsScaling || mUserScale > 1.0f) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        
        // 处理单击图片查看（只在未缩放状态下）
        if (mUserScale <= 1.0f && !mIsScaling) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getX();
                    mDownY = event.getY();
                    // ACTION_DOWN 时也请求不拦截，确保后续事件能收到
                    getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                    
                case MotionEvent.ACTION_UP:
                    float x = event.getX();
                    float y = event.getY();
                    
                    // 判断是否是单击（移动距离小于阈值）
                    if (Math.abs(x - mDownX) < CLICK_THRESHOLD && Math.abs(y - mDownY) < CLICK_THRESHOLD) {
                        // 只在B区域（图片区域）内才尝试打开图片查看器
                        if (isPointInImageArea(x, y)) {
                            handleImageClick(x, y);
                        }
                    }
                    break;
                    
                case MotionEvent.ACTION_MOVE:
                    // 移动时如果是多指，继续请求不拦截
                    if (pointerCount >= 2) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
            }
        }
        
        return true; // 始终返回 true，确保能接收到完整的触摸事件序列
    }
    
    /**
     * 检查点击位置是否在整个图片报告区域内
     * （临床诊断/主诉以下，镜检所见以上的区域）
     */
    private boolean isPointInImageArea(float x, float y) {
        // 使用整个图片区域边界来判断
        boolean result = mImageAreaBounds.contains(x, y);
        Log.d(TAG, "isPointInImageArea: x=" + x + ", y=" + y + 
              ", bounds=" + mImageAreaBounds.toString() + ", result=" + result);
        return result;
    }
    
    /**
     * 检查点击位置是否在某张具体图片上
     */
    private boolean isPointOnImage(float x, float y) {
        for (RectF rect : mImageRects.values()) {
            if (rect.contains(x, y)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理图片点击
     */
    private void handleImageClick(float x, float y) {
        // 检测点击的是哪张图片
        for (Map.Entry<String, RectF> entry : mImageRects.entrySet()) {
            RectF rect = entry.getValue();
            if (rect.contains(x, y)) {
                String order = entry.getKey();
                Bitmap bitmap = mImageCache.get(order);
                if (bitmap != null && !bitmap.isRecycled()) {
                    // 显示图片查看器
                    showImageViewer(bitmap);
                    return;
                } else {
                    // 检查是否是加载失败的图片
                    Integer progress = mImageProgress.get(order);
                    if (progress != null && progress == PROGRESS_FAILED) {
                        Toast.makeText(getContext(), "图片加载失败，无法查看", Toast.LENGTH_SHORT).show();
                    } else if (progress != null && progress >= 0 && progress < 100) {
                        Toast.makeText(getContext(), "图片加载中，请稍候...", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        }
    }
    
    /**
     * 重置缩放状态
     */
    public void resetZoom() {
        mUserScale = 1.0f;
        mTranslateX = 0;
        mTranslateY = 0;
        invalidate();
    }
    
    /**
     * 显示图片查看器
     */
    private void showImageViewer(Bitmap clickedBitmap) {
        // 收集所有加载成功的图片
        ArrayList<Bitmap> loadedImages = new ArrayList<>();
        ArrayList<String> imageOrders = new ArrayList<>();
        
        // 按order排序获取所有已加载的图片
        for (Map.Entry<String, Bitmap> entry : mImageCache.entrySet()) {
            String order = entry.getKey();
            Bitmap bitmap = entry.getValue();
            if (bitmap != null && !bitmap.isRecycled() && !order.startsWith("sketch_") && !order.equals("logo")) {
                imageOrders.add(order);
            }
        }
        
        // 排序
        java.util.Collections.sort(imageOrders);
        
        // 按排序后的顺序添加图片
        int currentIndex = 0;
        for (int i = 0; i < imageOrders.size(); i++) {
            String order = imageOrders.get(i);
            Bitmap bitmap = mImageCache.get(order);
            if (bitmap != null) {
                loadedImages.add(bitmap);
                if (bitmap == clickedBitmap) {
                    currentIndex = i;
                }
            }
        }
        
        ImageViewerDialog dialog = new ImageViewerDialog(getContext(), loadedImages, currentIndex);
        dialog.show();
    }
    
    /**
     * 清除图片缓存
     */
    public void clearImageCache() {
        // 增加版本号，取消所有正在进行的加载任务
        mLoadVersion++;
        
        // 取消所有待执行的任务
        synchronized (mPendingTasks) {
            for (Future<?> task : mPendingTasks) {
                if (!task.isDone()) {
                    task.cancel(true);
                }
            }
            mPendingTasks.clear();
        }
        
        // 关闭旧线程池，立即创建新的
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }
        
        synchronized (mImageCache) {
            for (Bitmap bitmap : mImageCache.values()) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            mImageCache.clear();
        }
        
        // 清除进度记录
        synchronized (mImageProgress) {
            mImageProgress.clear();
        }
        
        // 清除图片区域位置缓存
        mImageRects.clear();
        mImageAreaBounds.setEmpty();
        
        // 重置波浪偏移
        mWaveOffset = 0;
        
        // 重置缩放状态
        resetZoom();
    }
}
