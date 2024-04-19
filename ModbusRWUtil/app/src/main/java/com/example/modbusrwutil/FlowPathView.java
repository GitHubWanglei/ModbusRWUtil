package com.example.modbusrwutil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FlowPathView extends View {

    private Path path; // 动画路径
    private float[] intervals = new float[]{10, 10}; // 虚线中虚实比例
    private float cornerRadius = 30.f; // 路径拐角半径
    private Long speed = 1L; // 动画速度

    private int pathColor = Color.YELLOW; // 虚线颜色
    private int pathWidth = 8; // 虚线宽度

    private int pathBackgroundColor = Color.GRAY; // 背景颜色
    private int pathBackgroundWidth = 14; // 背景宽度

    private Long phase = 0L;
    private boolean showAnimation = false;
    private Paint paint;
    private Paint backgroundPaint;

    public FlowPathView(Context context) {
        this(context, null);
    }

    public FlowPathView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowPathView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    private void initData() {
        setBackgroundColor(Color.BLACK);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(pathColor);
        paint.setStrokeWidth(pathWidth);
        paint.setStyle(Paint.Style.STROKE);

        path = new Path();

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(pathBackgroundColor);
        backgroundPaint.setStrokeWidth(pathBackgroundWidth);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setPathEffect(new CornerPathEffect(cornerRadius));
    }

    public void startFlowAnimation() {
        showAnimation = true;
        invalidate();
    }

    public void stopFlowAnimation() {
        showAnimation = false;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (path == null) {
            return;
        }

        CornerPathEffect cornerPathEffect = new CornerPathEffect(cornerRadius);
        DashPathEffect dashPathEffect = new DashPathEffect(intervals, phase);
        ComposePathEffect composePathEffect = new ComposePathEffect(dashPathEffect, cornerPathEffect);
        paint.setPathEffect(composePathEffect);

        canvas.drawPath(path, backgroundPaint);
        canvas.drawPath(path, paint);

        if (showAnimation) {
            phase += speed;
            if (phase >= (Long.MAX_VALUE - 10) || phase <= (Long.MIN_VALUE + 10)) { // 防止越界
                phase = 0L;
            }
            invalidate();
        }
    }

    public void setPath(@NonNull Path path) {
        this.path = path;
    }

    public void setIntervals(float[] intervals) {
        this.intervals = intervals;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        backgroundPaint.setPathEffect(new CornerPathEffect(cornerRadius));
    }

    public void setSpeed(Long speed) {
        this.speed = speed;
    }

    public void setPathColor(int pathColor) {
        this.pathColor = pathColor;
        paint.setColor(pathColor);
    }

    public void setPathWidth(int pathWidth) {
        this.pathWidth = pathWidth;
        paint.setStrokeWidth(pathWidth);
    }

    public void setPathBackgroundColor(int pathBackgroundColor) {
        this.pathBackgroundColor = pathBackgroundColor;
        backgroundPaint.setColor(pathBackgroundColor);
    }

    public void setPathBackgroundWidth(int pathBackgroundWidth) {
        this.pathBackgroundWidth = pathBackgroundWidth;
        backgroundPaint.setStrokeWidth(pathBackgroundWidth);
    }
}
