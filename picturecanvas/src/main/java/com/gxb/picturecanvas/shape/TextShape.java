package com.gxb.picturecanvas.shape;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;

/**
 * 文本图像
 */
public class TextShape extends TShape {

    private int minWidth = TPictureCompilerView.dip2px(100);

    private int textSize = TPictureCompilerView.dip2px(20);

    private RectF rect;

    // 焦点背景
    protected Paint editPaint;

    protected Paint secPaint;

    // 是否编辑
    private boolean isEdit = true;

    private StringBuilder text;

    // 上次编辑的内容
    private String hisText;

    private final List<String> lines = new ArrayList<>();

    public TextShape(TPictureCompilerView view, PointF pointF) {
        super(view);
        text = new StringBuilder();
        mPaint = new Paint();
        editPaint = new Paint();
        secPaint = new Paint();
        rect = pointF == null?new RectF(DEFAULT_EMPTY, DEFAULT_EMPTY, DEFAULT_EMPTY, DEFAULT_EMPTY) : new RectF(pointF.x, pointF.y, pointF.x + minWidth, DEFAULT_EMPTY);

        handlePoints = new PointF[]{new PointF()};
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(textSize);
        mPaint.setAntiAlias(true);
        secPaint.setColor(Color.WHITE);
        secPaint.setTextSize(textSize);
        secPaint.setAntiAlias(true);
        secPaint.setStrokeWidth(10);
        secPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        editPaint.setColor(Color.argb(44, 0, 0, 0));
        editPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        lines.clear();
        final String sb = text.toString();
        int start = 0;
        do {
            final int cur = sb.indexOf("\n", start) + 1;
            final String tmp = sb.substring(start, cur > 0?cur : sb.length());
            int index = 0;
            do {
                final int size = mPaint.breakText(tmp, index, tmp.length(),true, rect.width(), null);
                lines.add(tmp.substring(index, index + size));
                index += size;
            } while(index < tmp.length());
            start = cur;
        } while(start > 0);

        rect.bottom = rect.top + (lines.size() + 1f) * textSize;
        handlePoints[0].set(rect.right, rect.bottom);

        if(isEdit) canvas.drawRect(rect, editPaint);
        for(int i = 0;i < lines.size(); i++) {
            if(!isEdit) canvas.drawText(lines.get(i), rect.left, rect.top + textSize * (i + 1.3f), secPaint);
            canvas.drawText(lines.get(i), rect.left, rect.top + textSize * (i + 1.3f), mPaint);
        }
    }

    @Override
    public void addPoint(float x, float y) {
//        rect.top = x;
//        rect.left = y;
    }

    @Override
    public boolean hasCanvas() {
        return true;
    }

    @Override
    protected void onDrawFocus(Canvas canvas) {
        canvas.drawRect(rect, focusPaint);
        if(!isEdit) {
            for(PointF item : handlePoints) {
                handlePaint.setShader(new RadialGradient(item.x, item.y, TOUCH_SIZE,
                        new int[]{Color.rgb(135, 215, 255), Color.WHITE}, new float[]{0.8f, 0.2f}, Shader.TileMode.MIRROR));
                canvas.drawCircle(item.x, item.y, TOUCH_SIZE, handlePaint);
            }
        }

    }

    @Override
    protected void move(float dx, float dy) {
        if(isEdit) {
           // 编辑时候光标
        } else {
            rect.offset(dx, dy);
        }
    }

    @Override
    protected void zoom(float dx, float dy) {
        if(isEdit) {
            // 编辑时候光标

        } else {
            rect.right = Math.max(rect.right + dx, rect.left + TPictureCompilerView.dip2px(30));
//            rect.bottom = Math.max(rect.bottom + dy, rect.top + minHeight);
        }
    }

    @Override
    public boolean innerView(float x, float y) {
        return rect.contains(x, y);
    }

    @Override
    public void touchUp(TouchData data) {
        if(!data.isMove && isDoubleClick()) {
            if(!isTrimEmpty(text.toString())) view.addPrevious();
            isEdit = true;
            hisText = text.toString().trim();
            toggleSoftInput(true);
            view.invalidate();
        }
        if(isEdit) {
            // 如果是编辑状态
        }

    }

    @Override
    public void clearFocus() {
        // 清除焦点时内容修改则保留撤销
        if(text.toString().equals(hisText)) view.removePrevious();
        isEdit = false;
        toggleSoftInput(false);
        if(isTrimEmpty(text.toString()))view.shapeLayouts.remove(this);
    }

    @Override
    public void requstFocus() {
        view.setFocusShape(this);
        if(isEdit) {
            toggleSoftInput(true);
        }
    }

    public void inputText(String txt) {
        if(!TextUtils.isEmpty(txt)) {
            text.append(txt);
        } else if(text.length() > 0) {
            text.deleteCharAt(text.length() - 1);
        }
        view.invalidate();
    }

    private void toggleSoftInput(boolean show) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if(show) {
            view.requestFocus();
            imm.showSoftInput(view, SOFT_INPUT_ADJUST_RESIZE);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    private boolean isTrimEmpty(final String s) {
        return (s == null || s.trim().length() == 0);
    }

    @Override
    public TextShape clone() {
        TextShape obj = new TextShape(view, new PointF());
        copy(obj);
        obj.isEdit = false;
        obj.rect = new RectF(rect);
        obj.textSize = textSize;
        obj.text = new StringBuilder(text);
        return obj;
    }
}
