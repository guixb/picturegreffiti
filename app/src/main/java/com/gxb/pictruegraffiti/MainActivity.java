package com.gxb.pictruegraffiti;

import android.os.Bundle;

import com.gxb.picturecanvas.TPictureCanvasActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TPictureCanvasActivity.start(this, null, false);
    }
}
