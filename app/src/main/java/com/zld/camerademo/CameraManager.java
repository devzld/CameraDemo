package com.zld.camerademo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by lingdong on 2019/3/22.
 */
public class CameraManager implements SurfaceHolder.Callback {

    private Activity mActivity;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private int mCameraId = 0;
    private boolean safeToTakePicture = false;

    public void init(Activity activity, SurfaceView surfaceView) {
        this.mActivity = activity;
        this.mSurfaceView = surfaceView;
    }


    public void onResume() {
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
        }
    }

    public void onPause() {
        releaseCamera();
    }

    public void safeTakePicture() {
        if (safeToTakePicture) {
            takePicture();
            safeToTakePicture = false;
        }
    }

    private void takePicture() {
        //参数说明
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {
                camera.startPreview();
                //将data 转换为位图 或者你也可以直接保存为文件使用 FileOutputStream
                //这里我相信大部分都有其他用处把 比如加个水印 后续再讲解
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, bitmap);
                //这里打印宽高 就能看到 CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), 200);
                // 这设置的最小宽度影响返回图片的大小 所以这里一般这是1000左右把我觉得
                Log.d("bitmapWidth==", bitmap.getWidth() + "");
                Log.d("bitmapHeight==", bitmap.getHeight() + "");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileOutputStream outputStream = new FileOutputStream(FileUtil.getFile());
                            outputStream.write(data, 0, data.length);
                            outputStream.flush();
                            outputStream.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                safeToTakePicture = true;
            }
        });
    }


    /**
     * 获取Camera实例
     *
     * @return
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
            setupCamera(camera);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            //这里要设置相机的一些参数，下面会详细说下
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CameraUtil.getInstance().setCameraDisplayOrientation(mActivity, mCameraId, camera);
            camera.setDisplayOrientation(90);
            camera.startPreview();
            safeToTakePicture = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // Autofocus mode is supported 自动对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        //这里第三个参数为最小尺寸 getPropPreviewSize方法会对从最小尺寸开始升序排列 取出所有支持尺寸的最小尺寸
        Camera.Size previewSize = CameraUtil.getInstance().getPropPreviewSize(parameters.getSupportedPreviewSizes(), 1000);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), 1000);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);

        camera.setParameters(parameters);

        Log.d("previewSize.width===", previewSize.width + "");
        Log.d("previewSize.height===", previewSize.height + "");

        /**
         * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
         * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
         * previewSize.width才是surfaceView的高度
         * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
         */
//        SurfaceView surfaceView = findViewById(R.id.surface_view);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, screenWidth * previewSize.width / previewSize.height);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1, 1);
        //这里当然可以设置拍照位置 比如居中 我这里就置顶了
        //params.gravity = Gravity.CENTER;
        mSurfaceView.setLayoutParams(params);
        mSurfaceView.getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //在surface创建的时候开启相机预览
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //在相机改变的时候调用此方法， 此时应该先停止预览， 然后重新启动
        mCamera.stopPreview();
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //在destroy的时候释放相机资源
        releaseCamera();
    }
}
