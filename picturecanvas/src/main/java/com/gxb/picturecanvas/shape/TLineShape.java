package com.gxb.picturecanvas.shape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;

/**
 * 线条图像
 */
public class TLineShape extends TShape {

    private Path path;

    private Path touchPath;

    private PointF hisP;

    public TLineShape(TPictureCompilerView view, PointF start) {
        super(view);
        mPaint = new Paint();
        path = new Path();
        touchPath = new Path();
        handlePoints = new PointF[]{new PointF()};
        if(start == null) {
            hisP = new PointF(DEFAULT_EMPTY, DEFAULT_EMPTY);
        } else {
            hisP = new PointF(start.x, start.y);
            path.moveTo(start.x, start.y);
            path.lineTo(start.x, start.y);
            touchPath.moveTo(start.x, start.y);
            touchPath.lineTo(start.x, start.y);
        }
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(30);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

    }

    @Override
    public void addPoint(float x, float y) {
        touchPath.addCircle(x, y, 30, Path.Direction.CCW);
        path.quadTo(hisP.x, hisP.y, x, y);
        handlePoints[0].set(Math.max(handlePoints[0].x, x + TOUCH_SIZE * 1.5f), Math.max(handlePoints[0].y, y + TOUCH_SIZE * 1.5f));
        hisP.set(x, y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, mPaint);
    }

    @Override
    protected void onDrawFocus(Canvas canvas) {
        canvas.drawPath(path, focusPaint);
        for(PointF item : handlePoints) {
            handlePaint.setShader(new RadialGradient(item.x, item.y, TOUCH_SIZE,
                    new int[]{Color.rgb(135, 215, 255), Color.WHITE}, new float[]{0.8f, 0.2f}, Shader.TileMode.MIRROR));
            canvas.drawCircle(item.x, item.y, TOUCH_SIZE, handlePaint);
        }
    }

    @Override
    public boolean hasCanvas() {
        return hisP.x > 0 && hisP.y > 0;
    }

    @Override
    protected void move(float dx, float dy) {
        path.offset(dx, dy);
        touchPath.offset(dx, dy);
        handlePoints[0].offset(dx, dy);
    }

    @Override
    protected void zoom(float dx, float dy) {

    }

    @Override
    public boolean innerView(float x, float y) {
        RectF bounds = new RectF();
        Region region = new Region();
        touchPath.computeBounds(bounds, true);
        region.setPath(touchPath, new Region((int)bounds.left, (int)bounds.top,(int)bounds.right, (int)bounds.bottom));
        if(region.contains((int)x, (int)y)) return true;
        return false;
    }

    @Override
    public TLineShape clone() {
        TLineShape obj = new TLineShape(view, hisP);
        copy(obj);
        obj.path = new Path(path);
        return obj;
    }
}
