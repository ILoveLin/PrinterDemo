package com.shenma.printtest.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片查看器Dialog
 * 支持双指缩放、旋转、左右切换功能
 */
public class ImageViewerDialog extends Dialog {

    private List<Bitmap> mImages;
    private int mCurrentIndex;
    private ImageTouchView mImageView;
    private float mCurrentRotation = 0f;
    private TextView mIndexText;
    private View mLeftArrow;
    private View mRightArrow;
    
    // 是否允许点击外部区域关闭，默认为true
    private boolean mDismissOnTouchOutside = true;

    public ImageViewerDialog(@NonNull Context context, List<Bitmap> images, int currentIndex) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.mImages = images != null ? images : new ArrayList<>();
        this.mCurrentIndex = Math.max(0, Math.min(currentIndex, mImages.size() - 1));
    }

    // 兼容旧的单图片构造函数
    public ImageViewerDialog(@NonNull Context context, Bitmap bitmap) {
        this(context, new ArrayList<Bitmap>() {{
            add(bitmap);
        }}, 0);
    }
    
    /**
     * 设置是否允许点击图片外部区域关闭查看器
     * @param dismissOnTouchOutside true-点击外部关闭，false-只能点击X关闭
     * @return 返回自身，支持链式调用
     */
    public ImageViewerDialog setDismissOnTouchOutside(boolean dismissOnTouchOutside) {
        this.mDismissOnTouchOutside = dismissOnTouchOutside;
        return this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 创建根布局
        FrameLayout rootLayout = new FrameLayout(getContext());
        rootLayout.setBackgroundColor(Color.parseColor("#B0333333")); // 半透明灰色蒙层
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 创建图片显示View
        mImageView = new ImageTouchView(getContext());
        if (!mImages.isEmpty()) {
            mImageView.setBitmap(mImages.get(mCurrentIndex));
        }
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.addView(mImageView, imageParams);

        // 创建左箭头
        mLeftArrow = createArrowButton(true);
        FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(120, 120);
        leftParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        leftParams.leftMargin = 20;
        rootLayout.addView(mLeftArrow, leftParams);

        // 创建右箭头
        mRightArrow = createArrowButton(false);
        FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(120, 120);
        rightParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        rightParams.rightMargin = 20;
        rootLayout.addView(mRightArrow, rightParams);

        // 创建底部工具栏
        LinearLayout bottomBar = new LinearLayout(getContext());
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER);
        bottomBar.setBackgroundColor(Color.parseColor("#80000000"));
        bottomBar.setPadding(0, 30, 0, 30);

        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomParams.gravity = Gravity.BOTTOM;

        // 图片索引文字
        mIndexText = new TextView(getContext());
        mIndexText.setTextColor(Color.WHITE);
        mIndexText.setTextSize(14);
        updateIndexText();

        // 旋转按钮
        View rotateBtn = createToolButton(ICON_ROTATE);
        rotateBtn.setOnClickListener(v -> rotate90());

        // 关闭按钮
        View closeBtn = createToolButton(ICON_CLOSE);
        closeBtn.setOnClickListener(v -> dismiss());

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.rightMargin = 60;

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(100, 100);
        btnParams.setMargins(30, 0, 30, 0);

        bottomBar.addView(mIndexText, textParams);
        bottomBar.addView(rotateBtn, btnParams);
        bottomBar.addView(closeBtn, btnParams);

        rootLayout.addView(bottomBar, bottomParams);

        updateArrowVisibility();

        setContentView(rootLayout);

        // 设置全屏
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }


    private void updateIndexText() {
        if (mImages.size() > 1) {
            mIndexText.setText((mCurrentIndex + 1) + "/" + mImages.size());
            mIndexText.setVisibility(View.VISIBLE);
        } else {
            mIndexText.setVisibility(View.GONE);
        }
    }

    private void updateArrowVisibility() {
        if (mImages.size() <= 1) {
            mLeftArrow.setVisibility(View.GONE);
            mRightArrow.setVisibility(View.GONE);
        } else {
            mLeftArrow.setVisibility(mCurrentIndex > 0 ? View.VISIBLE : View.INVISIBLE);
            mRightArrow.setVisibility(mCurrentIndex < mImages.size() - 1 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void showPrevious() {
        if (mCurrentIndex > 0) {
            mCurrentIndex--;
            mCurrentRotation = 0f;
            mImageView.setBitmap(mImages.get(mCurrentIndex));
            mImageView.setRotationAngle(0);
            updateIndexText();
            updateArrowVisibility();
        }
    }

    private void showNext() {
        if (mCurrentIndex < mImages.size() - 1) {
            mCurrentIndex++;
            mCurrentRotation = 0f;
            mImageView.setBitmap(mImages.get(mCurrentIndex));
            mImageView.setRotationAngle(0);
            updateIndexText();
            updateArrowVisibility();
        }
    }

    private void rotate90() {
        mCurrentRotation = (mCurrentRotation + 90) % 360;
        mImageView.setRotationAngle(mCurrentRotation);
    }

    /**
     * 创建左右箭头按钮
     */
    private View createArrowButton(boolean isLeft) {
        return new View(getContext()) {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private boolean isPressed = false;

            {
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(6f);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeJoin(Paint.Join.ROUND);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float w = getWidth();
                float h = getHeight();
                float cx = w / 2f;
                float cy = h / 2f;

                // 半透明背景
                Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bgPaint.setColor(isPressed ? Color.parseColor("#60FFFFFF") : Color.parseColor("#30FFFFFF"));
                bgPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, w / 2f - 4, bgPaint);

                // 箭头
                float arrowSize = w * 0.25f;
                Path arrowPath = new Path();
                if (isLeft) {
                    arrowPath.moveTo(cx + arrowSize * 0.5f, cy - arrowSize);
                    arrowPath.lineTo(cx - arrowSize * 0.5f, cy);
                    arrowPath.lineTo(cx + arrowSize * 0.5f, cy + arrowSize);
                } else {
                    arrowPath.moveTo(cx - arrowSize * 0.5f, cy - arrowSize);
                    arrowPath.lineTo(cx + arrowSize * 0.5f, cy);
                    arrowPath.lineTo(cx - arrowSize * 0.5f, cy + arrowSize);
                }
                canvas.drawPath(arrowPath, paint);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        isPressed = false;
                        invalidate();
                        if (isLeft) showPrevious();
                        else showNext();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        isPressed = false;
                        invalidate();
                        break;
                }
                return true;
            }
        };
    }


    // 图标类型
    private static final int ICON_ROTATE = 0;
    private static final int ICON_CLOSE = 1;

    /**
     * 创建底部工具按钮
     */
    private View createToolButton(int iconType) {
        return new View(getContext()) {
            private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private boolean isPressed = false;

            {
                iconPaint.setColor(Color.WHITE);
                iconPaint.setStyle(Paint.Style.STROKE);
                iconPaint.setStrokeWidth(4f);
                iconPaint.setStrokeCap(Paint.Cap.ROUND);
                iconPaint.setStrokeJoin(Paint.Join.ROUND);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float w = getWidth();
                float h = getHeight();
                float cx = w / 2f;
                float cy = h / 2f;
                float radius = Math.min(w, h) / 2f - 4;

                // 背景
                Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bgPaint.setColor(isPressed ? Color.parseColor("#50FFFFFF") : Color.parseColor("#00000000"));
                bgPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, radius, bgPaint);

                // 图标
                float iconSize = radius * 0.55f;
                if (iconType == ICON_ROTATE) {
                    drawRotateIcon(canvas, cx, cy, iconSize);
                } else {
                    drawCloseIcon(canvas, cx, cy, iconSize);
                }
            }

            private void drawRotateIcon(Canvas canvas, float cx, float cy, float size) {
                // 圆弧
                RectF arcRect = new RectF(cx - size, cy - size, cx + size, cy + size);
                canvas.drawArc(arcRect, -45, -270, false, iconPaint);

                // 箭头
                float arrowX = cx - size;
                float arrowY = cy;
                float arrowLen = size * 0.4f;
                canvas.drawLine(arrowX, arrowY, arrowX - arrowLen * 0.7f, arrowY - arrowLen * 0.5f, iconPaint);
                canvas.drawLine(arrowX, arrowY, arrowX + arrowLen * 0.3f, arrowY - arrowLen * 0.7f, iconPaint);
            }

            private void drawCloseIcon(Canvas canvas, float cx, float cy, float size) {
                iconPaint.setStrokeWidth(5f);
                float s = size * 0.7f;
                canvas.drawLine(cx - s, cy - s, cx + s, cy + s, iconPaint);
                canvas.drawLine(cx + s, cy - s, cx - s, cy + s, iconPaint);
                iconPaint.setStrokeWidth(4f);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isPressed = true;
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isPressed = false;
                        invalidate();
                        break;
                }
                return super.onTouchEvent(event);
            }

            @Override
            public void setOnClickListener(OnClickListener l) {
                super.setOnClickListener(v -> {
                    if (l != null) l.onClick(v);
                });
                setClickable(true);
            }
        };
    }


    /**
     * 支持双指缩放的图片View
     */
    private class ImageTouchView extends View {
        private Bitmap mBitmap;
        private Matrix mMatrix = new Matrix();
        private Matrix mSavedMatrix = new Matrix();
        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        private static final int NONE = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private int mMode = NONE;

        private float mOldDist = 1f;
        private float mStartX, mStartY;
        private float mMidX, mMidY;

        private static final float MIN_SCALE = 0.5f;
        private static final float MAX_SCALE = 5.0f;

        private float mRotationAngle = 0f;
        private float mInitScale = 1f;
        private boolean mInitialized = false;
        
        // 用于检测单击
        private static final int CLICK_THRESHOLD = 20;
        private long mDownTime;

        public ImageTouchView(Context context) {
            super(context);
            mPaint.setFilterBitmap(true);
        }

        public void setBitmap(Bitmap bitmap) {
            this.mBitmap = bitmap;
            mInitialized = false;
            mRotationAngle = 0f;
            if (getWidth() > 0 && getHeight() > 0) {
                resetMatrix();
                mInitialized = true;
            }
            invalidate();
        }

        public void setRotationAngle(float angle) {
            this.mRotationAngle = angle;
            resetMatrix();
            invalidate();
        }

        private void resetMatrix() {
            if (mBitmap == null || getWidth() == 0 || getHeight() == 0) return;

            mMatrix.reset();

            float bitmapWidth = mBitmap.getWidth();
            float bitmapHeight = mBitmap.getHeight();

            if (mRotationAngle == 90 || mRotationAngle == 270 ||
                    mRotationAngle == -90 || mRotationAngle == -270) {
                float temp = bitmapWidth;
                bitmapWidth = bitmapHeight;
                bitmapHeight = temp;
            }

            float scaleX = getWidth() * 0.9f / bitmapWidth;
            float scaleY = getHeight() * 0.8f / bitmapHeight;
            mInitScale = Math.min(scaleX, scaleY);

            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;

            mMatrix.postTranslate(-mBitmap.getWidth() / 2f, -mBitmap.getHeight() / 2f);
            mMatrix.postRotate(mRotationAngle);
            mMatrix.postScale(mInitScale, mInitScale);
            mMatrix.postTranslate(centerX, centerY);

            mSavedMatrix.set(mMatrix);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (!mInitialized && mBitmap != null) {
                resetMatrix();
                mInitialized = true;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mBitmap != null && !mBitmap.isRecycled()) {
                canvas.drawBitmap(mBitmap, mMatrix, mPaint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mSavedMatrix.set(mMatrix);
                    mStartX = event.getX();
                    mStartY = event.getY();
                    mDownTime = System.currentTimeMillis();
                    mMode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    mOldDist = spacing(event);
                    if (mOldDist > 10f) {
                        mSavedMatrix.set(mMatrix);
                        midPoint(event);
                        mMode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    // 检测是否是单击（没有移动且时间短）
                    if (mMode == DRAG && event.getPointerCount() == 1) {
                        float dx = Math.abs(event.getX() - mStartX);
                        float dy = Math.abs(event.getY() - mStartY);
                        long duration = System.currentTimeMillis() - mDownTime;
                        
                        if (dx < CLICK_THRESHOLD && dy < CLICK_THRESHOLD && duration < 300) {
                            // 是单击，检查是否点击在图片外部
                            if (mDismissOnTouchOutside && !isPointInsideImage(event.getX(), event.getY())) {
                                dismiss();
                                return true;
                            }
                        }
                    }
                    mMode = NONE;
                    break;
                    
                case MotionEvent.ACTION_POINTER_UP:
                    mMode = NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mMode == DRAG) {
                        mMatrix.set(mSavedMatrix);
                        float dx = event.getX() - mStartX;
                        float dy = event.getY() - mStartY;
                        mMatrix.postTranslate(dx, dy);
                    } else if (mMode == ZOOM && event.getPointerCount() >= 2) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            mMatrix.set(mSavedMatrix);
                            float scale = newDist / mOldDist;

                            float[] values = new float[9];
                            mMatrix.getValues(values);
                            float currentScale = values[Matrix.MSCALE_X];

                            float targetScale = currentScale * scale;
                            if (targetScale < MIN_SCALE * mInitScale) {
                                scale = MIN_SCALE * mInitScale / currentScale;
                            } else if (targetScale > MAX_SCALE * mInitScale) {
                                scale = MAX_SCALE * mInitScale / currentScale;
                            }

                            mMatrix.postScale(scale, scale, mMidX, mMidY);
                        }
                    }
                    break;
            }

            invalidate();
            return true;
        }
        
        /**
         * 检查点是否在图片区域内
         */
        private boolean isPointInsideImage(float x, float y) {
            if (mBitmap == null) return false;
            
            // 获取图片在屏幕上的实际边界
            float[] pts = new float[]{
                    0, 0,
                    mBitmap.getWidth(), 0,
                    mBitmap.getWidth(), mBitmap.getHeight(),
                    0, mBitmap.getHeight()
            };
            mMatrix.mapPoints(pts);
            
            // 计算图片边界矩形
            float minX = Math.min(Math.min(pts[0], pts[2]), Math.min(pts[4], pts[6]));
            float maxX = Math.max(Math.max(pts[0], pts[2]), Math.max(pts[4], pts[6]));
            float minY = Math.min(Math.min(pts[1], pts[3]), Math.min(pts[5], pts[7]));
            float maxY = Math.max(Math.max(pts[1], pts[3]), Math.max(pts[5], pts[7]));
            
            return x >= minX && x <= maxX && y >= minY && y <= maxY;
        }

        private float spacing(MotionEvent event) {
            if (event.getPointerCount() < 2) return 0;
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        }

        private void midPoint(MotionEvent event) {
            if (event.getPointerCount() < 2) return;
            mMidX = (event.getX(0) + event.getX(1)) / 2;
            mMidY = (event.getY(0) + event.getY(1)) / 2;
        }
    }
}
