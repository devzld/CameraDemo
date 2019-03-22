package com.zld.camerademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageView iv;
    private SurfaceView surfaceView;
    private CameraManager mCameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.iv);
        surfaceView = findViewById(R.id.surface_view);
        mCameraManager = new CameraManager();
        mCameraManager.init(this,surfaceView);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraManager.safeTakePicture();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraManager.onPause();
    }
}
