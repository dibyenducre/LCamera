package com.app.lcamera;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.app.apputils.PreviewSurfaceView;
import com.app.apputils.ImageDecodeTask;
import com.app.apputils.SaveImageTask;
import com.app.apputils.Constant;

import static com.app.apputils.Constant.FLAG_SAVE_IMAGE;
import static com.app.apputils.Constant.IMAGE_SAVE_FAILURE_MESSAGE;
import static com.app.apputils.Constant.IMAGE_SAVE_SUCCESS_MESSAGE;

public class CameraActivity extends AppCompatActivity {

    private android.hardware.Camera camera;
    private PreviewSurfaceView previewSurfaceView;
    private FrameLayout previewFrame;
    private Button captureButton;
    private Button switchCameraButton;

    public CameraActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewFrame = (FrameLayout)findViewById(R.id.camera_preview);
        captureButton = (Button)findViewById(R.id.button_capture);
        switchCameraButton = (Button)findViewById(R.id.button_switch);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });

        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constant.BACK_CAMERA_IN_USE) {
                    showFrontCamera();
                } else {
                    showBackCamera();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        showBackCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removePreview();
        releaseCamera();
    }

    private void showBackCamera() {
        releaseCamera();
        camera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (camera != null) {
            if (previewSurfaceView != null) {
                removePreview();
            }
            Constant.BACK_CAMERA_IN_USE = true;
            attachCameraToPreview();
        }
    }

    private void showFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();

        if (numberOfCameras > 1) {
            releaseCamera();
            camera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
            Constant.BACK_CAMERA_IN_USE = false;
            removePreview();
            attachCameraToPreview();
        } else {
            Toast.makeText(CameraActivity.this, "Front camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void attachCameraToPreview() {
        previewSurfaceView = new PreviewSurfaceView(this, camera);
        previewFrame.addView(previewSurfaceView);
    }

    private void removePreview() {
        previewFrame.removeView(previewSurfaceView);
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    public Camera getCameraInstance(int cameraId){
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            captureButton.setEnabled(false);

            camera.stopPreview();

            //decodes and returns the bitmap if this flag is set in constants.
            if(Constant.FLAG_DECODE_BITMAP){
                new ImageDecodeTask(CameraActivity.this, data, previewFrame.getHeight(), previewSurfaceView.getHeight()).execute();
            }else{
                //the method decodeBitmapComplete will not be called if the task is not started. So enable the button
                captureButton.setEnabled(true);
            }

            camera.startPreview();
        }
    };

    public void decodeBitmapComplete(Bitmap bitmap){
        captureButton.setEnabled(true);
        //The decoded bitmap is passed as a parameter. Use this for all further operations.
        if(bitmap != null){

            //Save the image to disk if this flag is set
            if(FLAG_SAVE_IMAGE){
                new SaveImageTask(this).execute(bitmap);
            }
        }
        //set the bitmap to null if it is no longer needed
        bitmap = null;

    }

    public void fileSaveComplete(Constant.FileSaveStatus fileSaveStatus){
        showToast(fileSaveStatus == Constant.FileSaveStatus.SUCCESS ? IMAGE_SAVE_SUCCESS_MESSAGE : IMAGE_SAVE_FAILURE_MESSAGE);
    }

    private void showToast(String message){
        Toast.makeText(CameraActivity.this, message, Toast.LENGTH_SHORT).show();
    }

}
