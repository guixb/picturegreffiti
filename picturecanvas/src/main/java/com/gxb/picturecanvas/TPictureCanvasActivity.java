package com.gxb.picturecanvas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RadioGroup;

import com.gxb.picturecanvas.shape.CustomLayout;
import com.gxb.picturecanvas.shape.TArrowShape;
import com.gxb.picturecanvas.shape.TLineShape;
import com.gxb.picturecanvas.shape.TPictureCompilerView;
import com.gxb.picturecanvas.shape.TextShape;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TPictureCanvasActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, TPictureCompilerView.OnCanvasListener, CustomLayout.OnSoftInputListener{

    public static final int REQUEST_CANVAS = 0x0011;

    public static final String FLAG_PIC_PATH = "flag_pic_path";

    private TPictureCompilerView cv;

    private View vTitle;

    private View vBottom;

    private View vDel;

    private RadioGroup rgOptions;

    private AlphaAnimation animVisble;

    private AlphaAnimation animHide;

    public static void start(Activity context, String path, boolean isCover) {
        context.startActivityForResult(new Intent(context, TPictureCanvasActivity.class)
                .putExtra("image_path", path).putExtra("isCover", isCover), REQUEST_CANVAS);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_canvas);
        CustomLayout layout = findViewById(R.id.rl_custom);
        vTitle = findViewById(R.id.ll_title);
        vBottom = findViewById(R.id.rl_bottom);
        vDel = findViewById(R.id.txt_del);
        rgOptions = findViewById(R.id.rg_options);
        animVisble = new AlphaAnimation(0,1);
        animHide = new AlphaAnimation(1,0);
        cv = findViewById(R.id.cv_pic);

        layout.setOnSoftInputListener(this);
        animVisble.setDuration(500);
        animHide.setDuration(500);
        cv.setSrcImage(getIntent().getStringExtra("image_path"));
        cv.setOnCanvasListener(this);
        rgOptions.setOnCheckedChangeListener(this);
        rgOptions.check(R.id.rbtn_arrow);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.rbtn_arrow) {
            cv.setShape(TArrowShape.class);
        } else if(checkedId == R.id.rbtn_line){
            cv.setShape(TLineShape.class);
        } else if(checkedId == R.id.rbtn_text){
            cv.setShape(TextShape.class);
        }
    }

    public void onClick(View v) {
        if(v.getId() == R.id.iv_previous) {
            cv.onPrevious();
        } else if(v.getId() == R.id.iv_next) {
            cv.onNext();
        } else if(v.getId() == R.id.txt_del) {
            cv.onDelete();
        } else if(v.getId() == R.id.tv_close) {
            finish();
        } else if(v.getId() == R.id.tv_confirm){
            setResult(RESULT_OK, new Intent().putExtra(FLAG_PIC_PATH, cv.saveBitmapToPath(getIntent().getBooleanExtra("isCover", false))));
            finish();
        }
    }

    @Override
    public void onCanvas(boolean hideCtrl, boolean unfocus) {
        if(unfocus) {
            rgOptions.setVisibility(View.VISIBLE);
            vDel.setVisibility(View.GONE);
        } else {
            rgOptions.setVisibility(View.GONE);
            vDel.setVisibility(View.VISIBLE);
        }
        if(hideCtrl || cv.isEdit()) {
            if(vBottom.getVisibility() != View.GONE) {
                vTitle.startAnimation(animHide);
                vBottom.startAnimation(animHide);
                vBottom.setVisibility(View.GONE);
                vTitle.setVisibility(View.GONE);
            }
        } else {
            if(vBottom.getVisibility() != View.VISIBLE) {
                vTitle.startAnimation(animVisble);
                vBottom.startAnimation(animVisble);
                vTitle.setVisibility(View.VISIBLE);
                vBottom.setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void stepChange(boolean hasPrevious, boolean hasNext) {
        findViewById(R.id.iv_previous).setVisibility(hasPrevious? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.iv_next).setVisibility(hasNext? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onHide() {
        cv.setFocusShape(null);
    }
}
