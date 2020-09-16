package com.gxb.picturecanvas.shape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * 图像抽象
 */
public abstract class TShape {

    protected final int DEFAULT_EMPTY = -1;

    /**
     * 图片编辑
     */
    protected TPictureCompilerView view;

    protected final int TOUCH_SIZE = TPictureCompilerView.dip2px(8);

    // 句饼坐标
    protected PointF[] handlePoints;

    // 焦点蓝线
    protected Paint focusPaint;

    // 拖拽句饼
    protected Paint handlePaint;

    // 主画笔
    protected Paint mPaint;

    // 当前拖动的角-点
    protected PointF activePOI;

    // 是否快速的双击
    private long lastClickTime;

    public TShape(TPictureCompilerView view) {
        this.view = view;
        focusPaint = new Paint();
        handlePaint = new Paint();

        focusPaint.setColor(Color.rgb(105, 215, 255));
        focusPaint.setStyle(Paint.Style.STROKE);
        focusPaint.setStrokeWidth(5);
        focusPaint.setAntiAlias(true);

        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setAntiAlias(true);

    }

    /**
     * 无焦点移动
     * @param moveData
     * @param action
     */
    public void onTouchUnfocus(TouchData moveData, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                view.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                addPoint(moveData.hisX, moveData.hisY);
                view.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(hasCanvas()) {
                    view.addShape(this);
                    requstFocus();
                    view.invalidate();
                }
                break;
        }
    }

    /**
     * 焦点移动
     * @param moveData
     * @param action
     */
    public void onTouchFocus(TouchData moveData, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.view.invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                // 防止抖动
                if(activePOI != null) {
                    zoom(moveData.moveX, moveData.moveY);
                } else {
                    move(moveData.moveX, moveData.moveY);
                }
                this.view.invalidate();
                break;
            case MotionEvent.ACTION_UP:
                activePOI = null;
                touchUp(moveData);
                this.view.invalidate();
                break;
        }
    }

    public boolean innerHandle(float x, float y) {
        float radisSize = TOUCH_SIZE * 1.5f;
        for(PointF item : handlePoints) {
            if(new RectF(item.x - radisSize, item.y - radisSize, item.x + radisSize, item.y + radisSize).contains(x, y)) {
                activePOI = item;
                return true;
            }
        }
        return false;
    }

    protected boolean isDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 300) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    protected void copy(TShape shape) {
        if(handlePoints != null) {
            shape.handlePoints = new PointF[handlePoints.length];
            for(int i = 0;i < handlePoints.length;i++) {
                shape.handlePoints[i] = new PointF(handlePoints[i].x, handlePoints[i].y);
            }
        }
        if(focusPaint != null) {
            shape.focusPaint = new Paint(focusPaint);
        }
        if(handlePaint != null) {
            shape.handlePaint = new Paint(handlePaint);
        }
        if(mPaint != null) {
            shape.mPaint = new Paint(mPaint);
        }
        if(activePOI != null) {
            shape.activePOI = new PointF(activePOI.x, activePOI.y);
        }

    }

    public void clearFocus(){}

    public void requstFocus(){}

    protected void touchUp(TouchData moveData) {if(moveData.isMove) view.addPrevious();}

    public abstract TShape clone();

    public abstract void addPoint(float x, float y);

    public abstract boolean hasCanvas();

    public abstract boolean innerView(float x, float y);

    protected abstract void onDraw(Canvas canvas);

    protected abstract void onDrawFocus(Canvas canvas);

    protected abstract void move(float dx, float dy);

    protected abstract void zoom(float dx, float dy);

    public static class TouchData {
        private long createTime;

        boolean isMove;

        float hisX;
        float hisY;
        float startX;
        float startY;

        float moveX;
        float moveY;

        public TouchData(float x, float y) {
            createTime = System.currentTimeMillis();
            this.isMove = false;
            this.startX = x;
            this.startY = y;
            setHis(x, y);
        }

        public void setStart(float x, float y) {
            this.startX = x;
            this.startY = y;
            setHis(x, y);
        }

        public void setHis(float x, float y) {
            this.hisX = x;
            this.hisY = y;
        }

        public boolean move(float x, float y, float threshold) {
            this.moveX = x - hisX;
            this.moveY = y - hisY;
            hisX = x;
            hisY = y;
            if(Math.abs(x - startX) > threshold || Math.abs(y - startY) > threshold) this.isMove = true;
            return isMove;
        }

    }
}
