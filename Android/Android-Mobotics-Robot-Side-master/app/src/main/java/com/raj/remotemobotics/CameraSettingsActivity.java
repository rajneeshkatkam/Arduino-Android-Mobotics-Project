package com.raj.remotemobotics;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class CameraSettingsActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private String TAG="ErrMobotics";
    private CameraBridgeViewBase mainCam;
    ImageView camImageView;
    Bitmap bitmap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera_settings);
        camImageView= (ImageView) findViewById(R.id.camImageView);
        mainCam = (CameraBridgeViewBase) findViewById(R.id.mainCam);

        mainCam.setVisibility(SurfaceView.VISIBLE);
        mainCam.setCvCameraViewListener(this);


    }



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    mainCam.enableView();


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }




    @Override
    public void onPause()
    {
        super.onPause();
        if (mainCam != null)
            mainCam.disableView();

    }

    public void onDestroy() {
        super.onDestroy();
        if (mainCam != null)
            mainCam.disableView();

        finish();

    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat m=inputFrame.rgba();
        Mat gray=inputFrame.gray();
        MatToBitmap(gray);

        return m;

    }


    void MatToBitmap(Mat m)
    {

        bitmap = Bitmap.createBitmap(mainCam.getWidth()/4,mainCam.getHeight()/4, Bitmap.Config.ARGB_8888);
        try {
            bitmap = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(m, bitmap);
            camImageView.setImageBitmap(bitmap);
            camImageView.invalidate();

        }catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
