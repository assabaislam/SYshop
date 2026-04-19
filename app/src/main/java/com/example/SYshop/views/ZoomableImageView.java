package com.example.SYshop.views;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private static final float MIN_SCALE = 1f;
    private static final float MAX_SCALE = 4f;

    private final Matrix matrixValues = new Matrix();
    private final float[] matrix = new float[9];
    private final PointF lastTouch = new PointF();
    private final ScaleGestureDetector scaleDetector;

    private float currentScale = 1f;
    private boolean isDragging = false;

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        resetZoom();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        resetZoom();
    }

    @Override
    public boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            resetZoom();
        }
        return changed;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouch.set(event.getX(), event.getY());
                isDragging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && currentScale > MIN_SCALE) {
                    float dx = event.getX() - lastTouch.x;
                    float dy = event.getY() - lastTouch.y;
                    matrixValues.postTranslate(dx, dy);
                    fixTranslation();
                    setImageMatrix(matrixValues);
                    lastTouch.set(event.getX(), event.getY());
                    isDragging = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                performClick();
                break;
        }

        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void resetZoom() {
        currentScale = MIN_SCALE;
        matrixValues.reset();
        fitToCenter();
        setImageMatrix(matrixValues);
    }

    private void fitToCenter() {
        if (getDrawable() == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        float drawableWidth = getDrawable().getIntrinsicWidth();
        float drawableHeight = getDrawable().getIntrinsicHeight();
        float scale = Math.min(getWidth() / drawableWidth, getHeight() / drawableHeight);

        float dx = (getWidth() - drawableWidth * scale) / 2f;
        float dy = (getHeight() - drawableHeight * scale) / 2f;

        matrixValues.postScale(scale, scale);
        matrixValues.postTranslate(dx, dy);
    }

    private void fixTranslation() {
        if (getDrawable() == null) {
            return;
        }

        matrixValues.getValues(matrix);
        float transX = matrix[Matrix.MTRANS_X];
        float transY = matrix[Matrix.MTRANS_Y];
        float scaleX = matrix[Matrix.MSCALE_X];
        float scaleY = matrix[Matrix.MSCALE_Y];

        float imageWidth = getDrawable().getIntrinsicWidth() * scaleX;
        float imageHeight = getDrawable().getIntrinsicHeight() * scaleY;

        float minX = Math.min(0, getWidth() - imageWidth);
        float minY = Math.min(0, getHeight() - imageHeight);
        float maxX = Math.max(0, getWidth() - imageWidth) / 2f;
        float maxY = Math.max(0, getHeight() - imageHeight) / 2f;

        float clampedX = Math.min(Math.max(transX, minX), maxX);
        float clampedY = Math.min(Math.max(transY, minY), maxY);

        matrixValues.postTranslate(clampedX - transX, clampedY - transY);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float nextScale = currentScale * scaleFactor;

            if (nextScale < MIN_SCALE) {
                scaleFactor = MIN_SCALE / currentScale;
            } else if (nextScale > MAX_SCALE) {
                scaleFactor = MAX_SCALE / currentScale;
            }

            currentScale *= scaleFactor;
            matrixValues.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            fixTranslation();
            setImageMatrix(matrixValues);
            return true;
        }
    }
}
