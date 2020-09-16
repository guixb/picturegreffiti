package com.gxb.picturecanvas.shape;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.gxb.picturecanvas.tools.BitmapUtils;
import com.gxb.picturecanvas.tools.FileUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.LinkedList;

import androidx.annotation.Nullable;

import static android.text.InputType.TYPE_CLASS_TEXT;

public class TPictureCompilerView extends View implements CustomLayout.OnSoftInputListener {

    private final int MOVE_THESHOLD = dip2px(5);
    /**
     * 上一步stack
     */
    private LinkedList<LinkedList<TShape>> previous = new LinkedList<>();

    /**
     * 下一步stack
     */
    private LinkedList<LinkedList<TShape>> next = new LinkedList<>();

    /**
     * 图层列表
     */
    protected LinkedList<TShape> shapeLayouts = new LinkedList<>();

    /**
     * 当前使用的图形
     */
    private Constructor<? extends TShape> curMode;

    /**
     * 当前绘制图形
     */
    private TShape newShape;

    /**
     * 焦点图层
     */
    private TShape focusShape;

    /**
     * 原图
     */
    private Bitmap src;

    private TShape.TouchData moveData;

    /**
     *
     */
    private OnCanvasListener onCanvasListener;

    private String path;

    private DisplayMetrics display;

    public TPictureCompilerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(display);
        // 获取焦点、关联键盘输入
        setFocusable(true);
        setFocusableInTouchMode(true);
        setShape(TArrowShape.class);
    }

    public void setOnCanvasListener(OnCanvasListener onCanvasListener) {
        this.onCanvasListener = onCanvasListener;
    }

    public void setShape(Class<? extends TShape> cls) {
        if(cls != null) {
            try {
                this.curMode = cls.getConstructor(TPictureCompilerView.class, PointF.class);
            } catch (Exception ignored){
            }

        }
    }

    public void setSrcImage(String path) {
        this.path = path;
        if(src != null && !src.isRecycled()) {
            src.recycle();
        }
        src = BitmapFactory.decodeFile(path);
        invalidate();
    }

    /**
     * 定位选中的shape
     * @param x
     * @param y
     * @return
     */
    private TShape positionFocusShape(float x, float y) {
        if(focusShape != null && focusShape.innerHandle(x, y)) {
            return focusShape;
        }
        for (int i = shapeLayouts.size() - 1;i >= 0;i--) {
            if(shapeLayouts.get(i).innerView(x, y)) {
                return shapeLayouts.get(i);
            }
        }
        return null;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = TYPE_CLASS_TEXT;
        return new BaseInputConnection(this, false) {
            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if(event.getAction()== KeyEvent.ACTION_UP && focusShape instanceof TextShape) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DEL:
                            ((TextShape)focusShape).inputText(null);
                            break;
                        case KeyEvent.KEYCODE_ENTER:
                            ((TextShape)focusShape).inputText("\n");
                            break;
                        case KeyEvent.KEYCODE_SPACE:
                            ((TextShape)focusShape).inputText(" ");
                            break;
                    }
                }
                return super.sendKeyEvent(event);
            }

            // 输入法提交了一个 text
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                if(focusShape instanceof TextShape) ((TextShape)focusShape).inputText(text.toString());
                return true;
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(setFocusShape(positionFocusShape(event.getX(), event.getY()))) {
                    invalidate();
                    return false;
                }
                moveData = new TShape.TouchData(event.getX(), event.getY());
                if(focusShape == null) try { newShape = curMode.newInstance(this, new PointF(event.getX(), event.getY())); } catch (Exception ignored) { }
                // 选中shape则不隐藏
                onCanvasListener.onCanvas(focusShape == null, focusShape == null);
                break;
            case MotionEvent.ACTION_MOVE:
                if(!moveData.move(event.getX(), event.getY(), MOVE_THESHOLD)) return true;
                onCanvasListener.onCanvas(true, focusShape == null);
                break;
            case MotionEvent.ACTION_UP:
                onCanvasListener.onCanvas(false, focusShape == null);
                break;
        }
        if(focusShape != null) focusShape.onTouchFocus(moveData, event.getAction());
        else if(newShape != null) newShape.onTouchUnfocus(moveData, event.getAction());
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        if(src != null) {
            int dwidth = src.getWidth();
            int dheight = src.getHeight();
            float scale = (dwidth <= display.widthPixels && dheight <= display.heightPixels)?
                    1.0f : Math.min((float) display.widthPixels / (float) dwidth, (float) display.heightPixels / (float) dheight);

            int dx = Math.round((display.widthPixels - dwidth * scale) * 0.5f);
            int dy = Math.round((display.heightPixels - dheight * scale) * 0.5f);
            canvas.drawBitmap(src, new Rect(0, 0, dwidth, dheight), new RectF(dx, dy, dx + dwidth * scale, dy + dheight * scale), null);
        }
        for(TShape item : shapeLayouts) {
            if(item != focusShape) item.onDraw(canvas);
        }
        // 绘制焦点图层
        if(focusShape != null) {
            focusShape.onDraw(canvas);
            focusShape.onDrawFocus(canvas);
        // 绘制新增图层
        } else if(newShape != null && newShape.hasCanvas()) {
            newShape.onDraw(canvas);
        }

    }

    /**
     * 设置焦点Shape，如果该shape不存在队列中则添加
     * @return 是否丢失焦点
     */
    protected boolean setFocusShape(TShape shape) {
        if(focusShape != shape) {
            if(focusShape != null) focusShape.clearFocus();
            focusShape = shape;
            if(onCanvasListener != null) {
                onCanvasListener.stepChange(!previous.isEmpty(), !next.isEmpty());
                onCanvasListener.onCanvas(focusShape != null, focusShape == null);
            }
            return focusShape == null;
        }
        return false;
    }

    protected void addShape(TShape shape) {
        addPrevious();
        this.newShape = null;
        this.shapeLayouts.add(shape);
    }

    public void addPrevious() {
        LinkedList<TShape> tmp = new LinkedList<>();
        for(TShape item : shapeLayouts) {
            tmp.add(item.clone());
        }
        previous.add(tmp);
        next.clear();
        if(onCanvasListener != null) onCanvasListener.stepChange(!previous.isEmpty(), false);
    }

    public void removePrevious() {
        previous.pollLast();
        if(onCanvasListener != null) onCanvasListener.stepChange(!previous.isEmpty(), !next.isEmpty());
    }

    /**
     * 上一步
     */
    public void onPrevious() {
        if(!previous.isEmpty()) {
            setFocusShape(null);
            LinkedList<TShape> tmp = previous.pollLast();
            next.add(shapeLayouts);
            shapeLayouts = tmp;
            if(onCanvasListener != null) onCanvasListener.stepChange(!previous.isEmpty(), !next.isEmpty());
            invalidate();
        }
    }

    // 下一步
    public void onNext() {
        if(!next.isEmpty()) {
            setFocusShape(null);
            LinkedList<TShape> tmp = next.pollLast();
            previous.add(shapeLayouts);
            shapeLayouts = tmp;
            if(onCanvasListener != null) onCanvasListener.stepChange(!previous.isEmpty(), !next.isEmpty());
            invalidate();
        }
    }

    public void onDelete() {
        if(focusShape != null) {
            addPrevious();
            shapeLayouts.remove(focusShape);
            setFocusShape(null);
            invalidate();
        }
    }

    public String saveBitmapToPath(boolean isCover) {

        Bitmap img = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(img);
        draw(canvas);
        File file;
        if(isCover && path != null) {
            file = new File(path);
        } else {
            file = new File(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?
                    getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) : getContext().getCacheDir(),
                    String.format("IMG_CROP_%s.jpg", System.currentTimeMillis()));
        }

        FileUtils.createOrExistsFile(file);
        BitmapUtils.saveBitmapFile(img, file);
        return file.getPath();
    }

    public static int dip2px(int dip) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int)(dip * scale + 0.5f);
    }

    @Override
    public void onHide() {
        setFocusShape(null);
    }

    public interface OnCanvasListener {
        void onCanvas(boolean hasCanvas, boolean hasFocusShape);

        void stepChange(boolean hasPrevious, boolean hasNext);
    }
}
