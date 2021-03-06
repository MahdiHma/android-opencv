package com.fooock.ticket.opencv;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 *
 */
public class MainActivity extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2, View.OnLongClickListener,
        RealTimeCamera.PictureResult {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int CAMERA_REQUEST_CODE = 981;

    private static final String[] CAMERA_PERMISSION = {Manifest.permission.CAMERA};

    private RealTimeCamera mRealTimeCameraView;
    private CheckPermission mCheckPermission;

    private final CheckVersion mCheckVersion = new CheckVersion();
    private final RealTimeProcessor mRealTimeProcessor = new RealTimeProcessor();

    /**
     * OpenCV camera loader callback
     */
    private final LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case SUCCESS:
                    if (mCheckVersion.isGreaterThan(Build.VERSION_CODES.LOLLIPOP)) {
                        // check if the device has camera permissions
                        checkCameraPermission();
                    } else {
                        mRealTimeCameraView.enableView();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void checkCameraPermission() {
        if (!mCheckPermission.isEnabled(Manifest.permission.CAMERA)) {
            requestPermissions(CAMERA_PERMISSION, CAMERA_REQUEST_CODE);
        } else {
            // its safe to enable camera
            mRealTimeCameraView.enableView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCheckPermission = new CheckPermission(this);

        mRealTimeCameraView = (RealTimeCamera) findViewById(R.id.open_cv_camera);
        mRealTimeCameraView.setCvCameraViewListener(this);
        mRealTimeCameraView.setPictureResult(this);
        mRealTimeCameraView.setOnLongClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            if (OpenCVLoader.initDebug()) {
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRealTimeCameraView != null) {
            mRealTimeCameraView.disableView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != CAMERA_REQUEST_CODE) {
            return;
        }
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.CAMERA)
                    && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                mRealTimeCameraView.enableView();
                break;
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "Camera started (w=" + width + ", h=" + height + ")");
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "Camera stopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final Mat rgba = inputFrame.rgba();
        mRealTimeProcessor.process(rgba);
        return rgba;
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "Take a photo!");
        mRealTimeCameraView.takePhoto();
        return true;
    }

    @Override
    public void onPictureTaken(byte[] picture) {
        Log.d(TAG, "Picture taken!");
        // Send the image to be precessed
        final Intent imageBytes = new Intent(this, ProcessedImageActivity.class);
        imageBytes.putExtra("image", picture);
        // Image size data
        final Camera.Size size = mRealTimeCameraView.size();
        imageBytes.putExtra("width", size.width);
        imageBytes.putExtra("height", size.height);
        // Send to precess
        startActivity(imageBytes);
    }
}