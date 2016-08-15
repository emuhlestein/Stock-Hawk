package com.intelliviz.stockhawk.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by edm on 7/21/2016.
 */
public class StockGraphView extends View {
    private final int paintColor = Color.BLACK;
    private Paint drawPaint;

    public StockGraphView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int viewWidth = this.getMeasuredWidth();
        int viewHeight = this.getMeasuredHeight();
        canvas.drawLine(0, 0, viewWidth, viewHeight, drawPaint);
    }

    // Setup paint with color and stroke styles
    private void setupPaint() {
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }
}
