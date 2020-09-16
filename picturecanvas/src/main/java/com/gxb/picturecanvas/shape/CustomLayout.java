package com.gxb.picturecanvas.shape;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CustomLayout extends RelativeLayout {

    int hisB;

    private OnSoftInputListener listener;

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnSoftInputListener(OnSoftInputListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed && hisB < b && listener != null) {
            listener.onHide();
        }
        hisB = b;
    }

    public interface OnSoftInputListener {
        void onHide();
    }
}
