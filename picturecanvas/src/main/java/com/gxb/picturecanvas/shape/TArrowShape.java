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
 * 方向图像
 */
public class TArrowShape extends TShape {

    /**
     * 路径path
     */
    private Path path;

    /**
     * 点击区域path
     */
    private Path touchPath;

    public TArrowShape(TPictureCompilerView view, PointF pointF) {
        super(view);
        path = new Path();
        touchPath = new Path();
        handlePoints = new PointF[]{new PointF(DEFAULT_EMPTY, DEFAULT_EMPTY), (pointF == null?new PointF(DEFAULT_EMPTY, DEFAULT_EMPTY) : pointF)};
        mPaint = new Paint();

        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    protected void onDraw(Canvas canvas) {
        final double centerAngle = computeAngle(handlePoints[0], handlePoints[1]);
        final PointF arrowLeftOut = computePoint(handlePoints[0], centerAngle - 35, TPictureCompilerView.dip2px(28));
        final PointF arrowRightOut = computePoint(handlePoints[0], centerAngle + 35, TPictureCompilerView.dip2px(28));

        final PointF arrowLeftInner = computePoint(handlePoints[0], centerAngle - 20, TPictureCompilerView.dip2px(18));
        final PointF arrowRightInner = computePoint(handlePoints[0], centerAngle + 20, TPictureCompilerView.dip2px(18));

        final PointF endLeftOut = computePoint(handlePoints[1], centerAngle - 90, TPictureCompilerView.dip2px(1));
        final PointF endRightOut = computePoint(handlePoints[1], centerAngle + 90, TPictureCompilerView.dip2px(1));
        path.reset();
        touchPath.reset();
        touchPath.moveTo(handlePoints[0].x, handlePoints[0].y);
        touchPath.lineTo(handlePoints[0].x, handlePoints[0].y);
        touchPath.lineTo(arrowLeftOut.x, arrowLeftOut.y);
        touchPath.lineTo(endLeftOut.x, endLeftOut.y);
        touchPath.lineTo(endRightOut.x, endRightOut.y);
        touchPath.lineTo(arrowRightOut.x, arrowRightOut.y);
        touchPath.close();
        path.moveTo(handlePoints[0].x, handlePoints[0].y);
        path.lineTo(handlePoints[0].x, handlePoints[0].y);
        path.lineTo(arrowLeftOut.x, arrowLeftOut.y);
        path.lineTo(arrowLeftInner.x, arrowLeftInner.y);
        path.lineTo(endLeftOut.x, endLeftOut.y);
        path.lineTo(endRightOut.x, endRightOut.y);
        path.lineTo(arrowRightInner.x, arrowRightInner.y);
        path.lineTo(arrowRightOut.x, arrowRightOut.y);
        path.close();
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
    protected void move(float dx, float dy) {
        handlePoints[0].offset(dx, dy);
        handlePoints[1].offset(dx, dy);
    }

    @Override
    protected void zoom(float dx, float dy) {
        activePOI.offset(dx, dy);
    }

    @Override
    public void addPoint(float x, float y) {
        handlePoints[0].set(x, y);
    }

    @Override
    public boolean hasCanvas() {
        if(handlePoints[0].x <= DEFAULT_EMPTY || handlePoints[0].y <= DEFAULT_EMPTY ||
                handlePoints[1].x <= DEFAULT_EMPTY || handlePoints[1].y <= DEFAULT_EMPTY) {
            return false;
        }
        float dx = Math.abs(handlePoints[0].x - handlePoints[1].x);
        float dy = Math.abs(handlePoints[0].y - handlePoints[1].y);
        return Math.sqrt(dx * dx + dy * dy) > TPictureCompilerView.dip2px(20);
    }

    @Override
    public boolean innerView(float x, float y) {
        RectF bounds = new RectF();
        Region region = new Region();
        touchPath.computeBounds(bounds, true);
        region.setPath(touchPath, new Region((int)bounds.left, (int)bounds.top,(int)bounds.right, (int)bounds.bottom));
        return region.contains((int)x, (int)y);
    }

    private double computeAngle(PointF startPoint, PointF endPoint) {
        double x = Math.abs(startPoint.x - endPoint.x);
        double y = Math.abs(startPoint.y - endPoint.y);
        double z = Math.sqrt(x * x + y * y);
        double angle = Math.asin(y / z) / Math.PI * 180;
        if(startPoint.x < endPoint.x && startPoint.y > endPoint.y) {
            angle = 180 - angle;
        } else if(startPoint.x > endPoint.x && startPoint.y < endPoint.y) {
            angle = 360 - angle;
        } else if(startPoint.x < endPoint.x && startPoint.y < endPoint.y) {
            angle += 180;
        }
        return 180 + angle;
    }

    private PointF computePoint(PointF point, double angle, double line) {
        return new PointF((float)(point.x + line * Math.cos(Math.toRadians(angle))), (float)(point.y + line * Math.sin(Math.toRadians(angle))));
    }

    @Override
    public TArrowShape clone() {
        TArrowShape obj = new TArrowShape(view, null);
        copy(obj);
        obj.path = new Path(path);
        obj.touchPath = new Path(touchPath);
        return obj;
    }
}
