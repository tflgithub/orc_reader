package com.fodel.ocrreader.ui.camera;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Administrator on 2018/3/13.
 */

public class ScanView extends View {

    private Rect mFrame;//最佳扫描区域的Rect
    private Paint mLinePaint;//边框画笔
    private Paint mScanPaint_Gridding;//网格样式画笔
    private Paint bgPaint;//背景画笔
    private Path mBoundaryLinePath;//边框path
    private int mBoundaryColor = Color.WHITE;
    private float mBoundaryStrokeWidth = 8f;//扫描区域边线样式-线宽

    private Path mGriddingPath;//网格样式的path
    private LinearGradient mLinearGradient_Gridding;//网格画笔的shader
    private float mGriddingLineWidth = 2;//网格线的线宽，单位pix
    private int mGriddingDensity = 40;//网格样式的，网格密度，值越大越密集

    private float mCornerLineLenRatio = 0.06f;//扫描边框角线占边总长的比例
    private float mCornerLineLen = 50f;//根据比例计算的边框长度，从四角定点向临近的定点画出的长度

    private Matrix mScanMatrix;//变换矩阵，用来实现动画效果
    private ValueAnimator mValueAnimator;//值动画，用来变换矩阵操作

    private int mScanAnimatorDuration = 1800;//值动画的时长
    private int mScancolor;//扫描颜色
    private Context mContext;
    private Rect rect;
    private int viewWidth;
    private int viewHeight;
    private int rectWidth;

    public ScanView(Context context) {
        this(context, null);
    }

    // This constructor is used when the class is built from an XML resource.
    public ScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }

    private void init() {
        // Initialize these once for performance rather than calling them every time in onDraw().
        mScanPaint_Gridding = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanPaint_Gridding.setStyle(Paint.Style.STROKE);
        mScanPaint_Gridding.setStrokeWidth(mGriddingLineWidth);

        mScancolor = Color.WHITE;

        //扫描区域的四角线框的样式
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mBoundaryColor);
        mLinePaint.setStrokeWidth(mBoundaryStrokeWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);

        //变换矩阵，用来处理扫描的上下扫描效果
        mScanMatrix = new Matrix();
        mScanMatrix.setTranslate(0, 30);

        //背景画笔初始化
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStrokeWidth(3);
        bgPaint.setTextSize(35);

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        viewWidth = outMetrics.widthPixels;
        viewHeight = outMetrics.heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int measuredHeight = this.getMeasuredHeight();
        int measuredWidth = this.getMeasuredWidth();
        int rectHeight = 7 * measuredHeight / 8;
        int rectWidth = 7 * measuredWidth / 8;
        int rectLen = rectHeight > rectWidth ? rectWidth : rectHeight;
        int rectLeft = (measuredWidth - rectLen) / 2;
        int rectTop = (measuredHeight - rectLen) / 2;
        int rectBottom = rectTop + rectLen - dp2px(mContext, 100);
        mFrame = new Rect(rectLeft, rectTop, rectLeft + rectLen, rectBottom);
        initBoundaryAndAnimator();
    }

    private int dp2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    private void initBoundaryAndAnimator() {
        if (mBoundaryLinePath == null) {
            mCornerLineLen = mFrame.width() * mCornerLineLenRatio;
            mBoundaryLinePath = new Path();
            mBoundaryLinePath.moveTo(mFrame.left, mFrame.top + mCornerLineLen);
            mBoundaryLinePath.lineTo(mFrame.left, mFrame.top);
            mBoundaryLinePath.lineTo(mFrame.left + mCornerLineLen, mFrame.top);

            mBoundaryLinePath.moveTo(mFrame.right - mCornerLineLen, mFrame.top);
            mBoundaryLinePath.lineTo(mFrame.right, mFrame.top);
            mBoundaryLinePath.lineTo(mFrame.right, mFrame.top + mCornerLineLen);

            mBoundaryLinePath.moveTo(mFrame.right, mFrame.bottom - mCornerLineLen);
            mBoundaryLinePath.lineTo(mFrame.right, mFrame.bottom);
            mBoundaryLinePath.lineTo(mFrame.right - mCornerLineLen, mFrame.bottom);

            mBoundaryLinePath.moveTo(mFrame.left + mCornerLineLen, mFrame.bottom);
            mBoundaryLinePath.lineTo(mFrame.left, mFrame.bottom);
            mBoundaryLinePath.lineTo(mFrame.left, mFrame.bottom - mCornerLineLen);
        }

        if (mValueAnimator == null) {
            initScanValueAnim(mFrame.height());
        }
    }

    public void initScanValueAnim(int height) {
        mValueAnimator = new ValueAnimator();
        mValueAnimator.setDuration(mScanAnimatorDuration);
        mValueAnimator.setFloatValues(-height, 0);
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        mValueAnimator.setInterpolator(new DecelerateInterpolator());
        mValueAnimator.setRepeatCount(Integer.MAX_VALUE);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mScanMatrix != null && mLinearGradient_Gridding != null/* && mScanPaint != null*/) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    mScanMatrix.setTranslate(0, animatedValue);
                    mLinearGradient_Gridding.setLocalMatrix(mScanMatrix);
                    invalidate();
                }
            }
        });
        start();
    }


    public void start() {
        if (mValueAnimator != null) {
            mValueAnimator.start();
        }
    }

    public void stop() {
        if (mValueAnimator!=null && mValueAnimator.isStarted()) {
            mValueAnimator.cancel();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mFrame == null || mBoundaryLinePath == null) {
            return;
        }
        canvas.drawPath(mBoundaryLinePath, mLinePaint);
        initGriddingPathAndStyle();
        canvas.drawPath(mGriddingPath, mScanPaint_Gridding);
        rectWidth = mFrame.width();
        //画蒙层
        bgPaint.setColor(0xa0000000);
        //下
        rect = new Rect(0, mFrame.bottom, viewWidth, viewHeight);
        canvas.drawRect(rect, bgPaint);
        //上
        rect = new Rect(0, 0, viewWidth, mFrame.top);
        canvas.drawRect(rect, bgPaint);
        //左
        rect = new Rect(0, mFrame.top, (viewWidth - rectWidth) / 2, mFrame.bottom);
        canvas.drawRect(rect, bgPaint);
        //右
        rect = new Rect(viewWidth - (viewWidth - rectWidth) / 2, mFrame.top, viewWidth, mFrame.bottom);
        canvas.drawRect(rect, bgPaint);
    }

    private void initGriddingPathAndStyle() {
        if (mGriddingPath == null) {
            mGriddingPath = new Path();
            float wUnit = mFrame.width() / (mGriddingDensity + 0f);
            float hUnit = mFrame.height() / (mGriddingDensity + 0f);
            for (int i = 0; i <= mGriddingDensity; i++) {
                mGriddingPath.moveTo(mFrame.left + i * wUnit, mFrame.top);
                mGriddingPath.lineTo(mFrame.left + i * wUnit, mFrame.bottom);
            }
            for (int i = 0; i <= mGriddingDensity; i++) {
                mGriddingPath.moveTo(mFrame.left, mFrame.top + i * hUnit);
                mGriddingPath.lineTo(mFrame.right, mFrame.top + i * hUnit);
            }
        }
        if (mLinearGradient_Gridding == null) {
            mLinearGradient_Gridding = new LinearGradient(0, mFrame.top, 0, mFrame.bottom + 0.01f * mFrame.height(), new int[]{Color.TRANSPARENT, Color.TRANSPARENT, mScancolor, Color.TRANSPARENT}, new float[]{0, 0.5f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);
            mLinearGradient_Gridding.setLocalMatrix(mScanMatrix);
            mScanPaint_Gridding.setShader(mLinearGradient_Gridding);
        }
    }
}
