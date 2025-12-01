package com.shenma.printtest.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片查看器Dialog
 * 支持双指缩放、旋转、左右滑动切换功能
 */
public class ImageViewerDialog extends Dialog {

    private List<Bitmap> mImages;
    private int mCurrentIndex;
    private ImageTouchView mImageView;
    private float mCurrentRotation = 0f;
    private View mLeftArrow;
    private View mRightArrow;
    private IndicatorView mIndicatorView;

    // 是否允许点击外部区域关闭，默认为true
    private boolean mDismissOnTouchOutside = true;

    public ImageViewerDialog(@NonNull Context context, List<Bitmap> images, int currentIndex) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.mImages = images != null ? images : new ArrayList<>();
        this.mCurrentIndex = Math.max(0, Math.min(currentIndex, mImages.size() - 1));
    }

    public ImageViewerDialog(@NonNull Context context, Bitmap bitmap) {
        this(context, new ArrayList<Bitmap>() {{
            add(bitmap);
        }}, 0);
    }

    public ImageViewerDialog setDismissOnTouchOutside(boolean dismissOnTouchOutside) {
        this.mDismissOnTouchOutside = dismissOnTouchOutside;
        return this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout rootLayout = new FrameLayout(getContext());
        rootLayout.setBackgroundColor(Color.parseColor("#B0333333"));
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // 图片显示View
        mImageView = new ImageTouchView(getContext());
        if (!mImages.isEmpty()) {
            mImageView.setBitmap(mImages.get(mCurrentIndex));
        }
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        imageParams.bottomMargin = 160;
        rootLayout.addView(mImageView, imageParams);

        // 左箭头
        mLeftArrow = createArrowButton(true);
        FrameLayout.LayoutParams leftParams = new FrameLayout.LayoutParams(120, 120);
        leftParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        leftParams.leftMargin = 20;
        rootLayout.addView(mLeftArrow, leftParams);

        // 右箭头
        mRightArrow = createArrowButton(false);
        FrameLayout.LayoutParams rightParams = new FrameLayout.LayoutParams(120, 120);
        rightParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        rightParams.rightMargin = 20;
        rootLayout.addView(mRightArrow, rightParams);

        // 底部容器
        LinearLayout bottomContainer = new LinearLayout(getContext());
        bottomContainer.setOrientation(LinearLayout.VERTICAL);
        bottomContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        bottomContainer.setBackgroundColor(Color.parseColor("#80000000"));
        bottomContainer.setPadding(0, 20, 0, 30);

        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomParams.gravity = Gravity.BOTTOM;

        // 指示器
        mIndicatorView = new IndicatorView(getContext());
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 40);
        indicatorParams.bottomMargin = 15;
        bottomContainer.addView(mIndicatorView, indicatorParams);

        // 工具栏
        LinearLayout toolBar = new LinearLayout(getContext());
        toolBar.setOrientation(LinearLayout.HORIZONTAL);
        toolBar.setGravity(Gravity.CENTER);

        View rotateBtn = createToolButton(ICON_ROTATE);
        rotateBtn.setOnClickListener(v -> rotate90());

        View closeBtn = createToolButton(ICON_CLOSE);
        closeBtn.setOnClickListener(v -> dismiss());

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(90, 90);
        btnParams.setMargins(40, 0, 40, 0);

        toolBar.addView(rotateBtn, btnParams);
        toolBar.addView(closeBtn, btnParams);
        bottomContainer.addView(toolBar);

        rootLayout.addView(bottomContainer, bottomParams);

        updateArrowVisibility();
        updateIndicator();

        setContentView(rootLayout);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    private void updateIndicator() {
        if (mIndicatorView != null) {
            mIndicatorView.setCount(mImages.size());
            mIndicatorView.setCurrentIndex(mCurrentIndex);
        }
    }

    private void updateArrowVisibility() {
        if (mImages.size() <= 1) {
            mLeftArrow.setVisibility(View.GONE);
            mRightArrow.setVisibility(View.GONE);
        } else {
            mLeftArrow.setVisibility(View.VISIBLE);
            mRightArrow.setVisibility(View.VISIBLE);
        }
    }

    private void showPrevious() {
        if (mImages.size() <= 1) return;
        mImageView.animateToImage(true);
    }

    private void showNext() {
        if (mImages.size() <= 1) return;
        mImageView.animateToImage(false);
    }

    private void rotate90() {
        mCurrentRotation = (mCurrentRotation + 90) % 360;
        mImageView.setRotationAngle(mCurrentRotation);
    }


    /**
     * 指示器View - 小点点
     */
    private class IndicatorView extends View {
        private int mCount = 0;
        private int mCurrentIndex = 0;
        private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int mDotRadius = 10;
        private final int mDotSpacing = 24;

        public IndicatorView(Context context) {
            super(context);
        }

        public void setCount(int count) {
            this.mCount = count;
            requestLayout();
            invalidate();
        }

        public void setCurrentIndex(int index) {
            this.mCurrentIndex = index;
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = mCount * (mDotRadius * 2 + mDotSpacing) - mDotSpacing + 40;
            int height = mDotRadius * 2 + 20;
            setMeasuredDimension(Math.max(width, 0), height);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mCount <= 1) return;

            float totalWidth = mCount * (mDotRadius * 2 + mDotSpacing) - mDotSpacing;
            float startX = (getWidth() - totalWidth) / 2f + mDotRadius;
            float cy = getHeight() / 2f;

            for (int i = 0; i < mCount; i++) {
                float cx = startX + i * (mDotRadius * 2 + mDotSpacing);
                if (i == mCurrentIndex) {
                    mPaint.setColor(Color.WHITE);
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(cx, cy, mDotRadius, mPaint);
                } else {
                    mPaint.setColor(Color.parseColor("#80FFFFFF"));
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(cx, cy, mDotRadius - 2, mPaint);
                }
            }
        }
    }

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

                Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bgPaint.setColor(isPressed ? Color.parseColor("#60FFFFFF") : Color.parseColor("#30FFFFFF"));
                bgPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, w / 2f - 4, bgPaint);

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

    private static final int ICON_ROTATE = 0;
    private static final int ICON_CLOSE = 1;

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

                Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bgPaint.setColor(isPressed ? Color.parseColor("#50FFFFFF") : Color.parseColor("#00000000"));
                bgPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, radius, bgPaint);

                float iconSize = radius * 0.55f;
                if (iconType == ICON_ROTATE) {
                    RectF arcRect = new RectF(cx - iconSize, cy - iconSize, cx + iconSize, cy + iconSize);
                    canvas.drawArc(arcRect, -45, -270, false, iconPaint);
                    float arrowX = cx - iconSize;
                    float arrowY = cy;
                    float arrowLen = iconSize * 0.4f;
                    canvas.drawLine(arrowX, arrowY, arrowX - arrowLen * 0.7f, arrowY - arrowLen * 0.5f, iconPaint);
                    canvas.drawLine(arrowX, arrowY, arrowX + arrowLen * 0.3f, arrowY - arrowLen * 0.7f, iconPaint);
                } else {
                    iconPaint.setStrokeWidth(5f);
                    float s = iconSize * 0.7f;
                    canvas.drawLine(cx - s, cy - s, cx + s, cy + s, iconPaint);
                    canvas.drawLine(cx + s, cy - s, cx - s, cy + s, iconPaint);
                    iconPaint.setStrokeWidth(4f);
                }
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
     * 支持双指缩放和滑动切换的图片View
     */
    private class ImageTouchView extends View {
        private Bitmap mBitmap;
        private Bitmap mNextBitmap; // 下一张图片（用于动画）
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
        private static final float DOUBLE_TAP_SCALE = 2.5f;
        private static final int SWIPE_THRESHOLD = 100;

        private float mRotationAngle = 0f;
        private float mInitScale = 1f;
        private boolean mInitialized = false;
        private boolean mIsZoomedIn = false;
        
        // 滑动动画相关
        private float mSlideOffset = 0f; // 当前滑动偏移
        private float mNextSlideOffset = 0f; // 下一张图片的偏移
        private boolean mIsAnimating = false;
        private int mSlideDirection = 0; // -1左滑, 1右滑

        private static final int CLICK_THRESHOLD = 20;
        private static final long DOUBLE_TAP_TIMEOUT = 300;
        private long mDownTime;
        private long mLastClickTime = 0;
        private float mLastClickX, mLastClickY;

        public ImageTouchView(Context context) {
            super(context);
            mPaint.setFilterBitmap(true);
        }

        public void setBitmap(Bitmap bitmap) {
            this.mBitmap = bitmap;
            mInitialized = false;
            mRotationAngle = 0f;
            mIsZoomedIn = false;
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
            float scaleY = getHeight() * 0.85f / bitmapHeight;
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
            
            // 绘制当前图片（带偏移）
            if (mBitmap != null && !mBitmap.isRecycled()) {
                canvas.save();
                canvas.translate(mSlideOffset, 0);
                canvas.drawBitmap(mBitmap, mMatrix, mPaint);
                canvas.restore();
            }
            
            // 绘制下一张图片（动画时）
            if (mIsAnimating && mNextBitmap != null && !mNextBitmap.isRecycled()) {
                canvas.save();
                canvas.translate(mNextSlideOffset, 0);
                // 计算下一张图片的居中矩阵
                Matrix nextMatrix = new Matrix();
                float bw = mNextBitmap.getWidth();
                float bh = mNextBitmap.getHeight();
                float scale = Math.min(getWidth() * 0.9f / bw, getHeight() * 0.85f / bh);
                nextMatrix.postTranslate(-bw / 2f, -bh / 2f);
                nextMatrix.postScale(scale, scale);
                nextMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
                canvas.drawBitmap(mNextBitmap, nextMatrix, mPaint);
                canvas.restore();
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
                    if (mIsAnimating) {
                        mMode = NONE;
                        return true;
                    }
                    
                    if (mMode == DRAG && event.getPointerCount() == 1) {
                        float dx = event.getX() - mStartX;
                        float dy = event.getY() - mStartY;
                        float absDx = Math.abs(dx);
                        float absDy = Math.abs(dy);
                        long duration = System.currentTimeMillis() - mDownTime;

                        // 检测水平滑动切换图片（未放大状态下）
                        if (!mIsZoomedIn && absDx > SWIPE_THRESHOLD && absDx > absDy * 2) {
                            if (dx > 0) {
                                animateToImage(true); // 上一张
                            } else {
                                animateToImage(false); // 下一张
                            }
                            mMode = NONE;
                            return true;
                        }

                        // 检测点击
                        if (absDx < CLICK_THRESHOLD && absDy < CLICK_THRESHOLD && duration < 300) {
                            float clickX = event.getX();
                            float clickY = event.getY();
                            long now = System.currentTimeMillis();

                            // 双击检测
                            if (now - mLastClickTime < DOUBLE_TAP_TIMEOUT
                                    && Math.abs(clickX - mLastClickX) < CLICK_THRESHOLD * 2
                                    && Math.abs(clickY - mLastClickY) < CLICK_THRESHOLD * 2) {
                                handleDoubleTap(clickX, clickY);
                                mLastClickTime = 0;
                                return true;
                            }

                            mLastClickTime = now;
                            mLastClickX = clickX;
                            mLastClickY = clickY;

                            postDelayed(() -> {
                                if (System.currentTimeMillis() - mLastClickTime >= DOUBLE_TAP_TIMEOUT) {
                                    if (mDismissOnTouchOutside && !isPointInsideImage(mLastClickX, mLastClickY)) {
                                        dismiss();
                                    }
                                }
                            }, DOUBLE_TAP_TIMEOUT);
                        }
                    }
                    mMode = NONE;
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mMode = NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mMode == DRAG) {
                        float dx = event.getX() - mStartX;
                        float dy = event.getY() - mStartY;
                        
                        if (mIsZoomedIn) {
                            // 放大状态：允许自由拖动
                            mMatrix.set(mSavedMatrix);
                            mMatrix.postTranslate(dx, dy);
                        } else {
                            // 未放大状态：只允许水平滑动（用于切换图片），不移动图片本身
                            // 图片保持原位，滑动效果在 ACTION_UP 时通过动画实现
                        }
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
                            
                            // 更新放大状态
                            mMatrix.getValues(values);
                            mIsZoomedIn = values[Matrix.MSCALE_X] > mInitScale * 1.1f;
                        }
                    }
                    break;
            }

            invalidate();
            return true;
        }

        /**
         * 带动画切换到上一张或下一张图片
         */
        private void animateToImage(boolean toPrevious) {
            if (mImages.size() <= 1 || mIsAnimating) return;
            
            mIsAnimating = true;
            int nextIndex;
            if (toPrevious) {
                nextIndex = mCurrentIndex - 1;
                if (nextIndex < 0) nextIndex = mImages.size() - 1;
                mSlideDirection = 1; // 从左边进入
            } else {
                nextIndex = mCurrentIndex + 1;
                if (nextIndex >= mImages.size()) nextIndex = 0;
                mSlideDirection = -1; // 从右边进入
            }
            
            mNextBitmap = mImages.get(nextIndex);
            final int targetIndex = nextIndex;
            final float screenWidth = getWidth();
            
            // 设置初始位置
            mSlideOffset = 0;
            mNextSlideOffset = -mSlideDirection * screenWidth;
            
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(250);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                float progress = (float) animation.getAnimatedValue();
                mSlideOffset = mSlideDirection * screenWidth * progress;
                mNextSlideOffset = -mSlideDirection * screenWidth * (1 - progress);
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentIndex = targetIndex;
                    mBitmap = mNextBitmap;
                    mNextBitmap = null;
                    mSlideOffset = 0;
                    mNextSlideOffset = 0;
                    mIsAnimating = false;
                    mCurrentRotation = 0f;
                    mRotationAngle = 0f;
                    resetMatrix();
                    updateIndicator();
                    invalidate();
                }
            });
            animator.start();
        }
        
        private void handleDoubleTap(float x, float y) {
            if (mBitmap == null) return;

            float[] values = new float[9];
            mMatrix.getValues(values);
            float currentScale = values[Matrix.MSCALE_X];

            if (mIsZoomedIn) {
                resetMatrix();
                mIsZoomedIn = false;
            } else {
                float targetScale = mInitScale * DOUBLE_TAP_SCALE;
                float scaleFactor = targetScale / currentScale;
                mMatrix.postScale(scaleFactor, scaleFactor, x, y);
                mIsZoomedIn = true;
            }

            mSavedMatrix.set(mMatrix);
            invalidate();
        }

        private boolean isPointInsideImage(float x, float y) {
            if (mBitmap == null) return false;

            float[] pts = new float[]{
                    0, 0,
                    mBitmap.getWidth(), 0,
                    mBitmap.getWidth(), mBitmap.getHeight(),
                    0, mBitmap.getHeight()
            };
            mMatrix.mapPoints(pts);

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
