package com.raj.mobotics;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

public class CameraSettingsActivity extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String  TAG                         =  "MOBO::ACTIVITY";

    public static int error=0;

    private static final int     CAMERA_REQUEST              =   1888;
    private static final int     MY_CAMERA_PERMISSION_CODE   =   100;

    public static final int      VIEW_MODE_RGBA              =   0;
    public static final int      VIEW_MODE_ACCUMULATE        =   1;
    public static final int      VIEW_MODE_THRESH            =   2;

    private MenuItem             mItemPreviewRGBA;
    private MenuItem             mItemPreviewHist;
    private CameraBridgeViewBase mOpenCvCameraView;
    //private SensorManager        mSensorManager;
    private Size                 mSize0;

    private int                  mHistSizeNum;
    private float                mBuff[];
    private Mat                  mIntermediateMat;
    private Mat                  mMat0;
    private MatOfInt             mChannels[];
    private MatOfInt             mHistSize;
    private MatOfFloat           mRanges;
    private Scalar               mColorsRGB[];
    private Scalar               mColorsHue[];
    private Scalar               mWhilte;
    private Point                mP1;
    private Point                mP2;
    private Point                mP3;
    private Point                mP4;
    private Point                mTopCenter;
    private Point                mBotCenter;
    private Point                mJuncL;
    private Point                mJuncC;
    private Point                mJuncR;

    public  static int           viewMode                    =   VIEW_MODE_RGBA;
    public  static int           fwhiteIndexTop;
    public  static int           fWhiteIndexBot;
    public  static int           fBlackIndexTop;
    public  static int           fBlackIndexBot;
    public  static int           fWhiteIndexDet;
    public  static int           fBlackIndexDet;

    //---------------------------------ACTIVITY CALLBACKS---------------------------------//

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera_settings);
        //mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOpenCvCameraView = findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
        else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        //mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        //mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME, SensorManager.SENSOR_DELAY_UI);


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        //mSensorManager.unregisterListener(this);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //-----------------------------------------------------------------------------------//

    //---------------------------------SENSOR CALLBACKS---------------------------------//

    //---------------------------------OCV CAMERA CALLBACKS---------------------------------//

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onCameraViewStarted(int width, int height) {
        mColorsRGB       = new Scalar[] {new Scalar(200,0,0,255),new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
        mP1              = new Point();
        mP2              = new Point();
        mP3              = new Point();
        mP4              = new Point();
        mTopCenter       = new Point();
        mBotCenter       = new Point();
        mJuncL           = new Point();
        mJuncC           = new Point();
        mJuncR           = new Point();
    }

    public void onCameraViewStopped() {
        if (mIntermediateMat != null)
            mIntermediateMat.release();
        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat     rgba        = inputFrame.rgba();
        Mat     gray        = new Mat();
        Size    sizeRgba    = rgba.size();
        Mat     rgbaInnerWindow;
        int     rows        = (int) sizeRgba.height;
        int     cols        = (int) sizeRgba.width;
        int     left        = cols / 8;
        int     jDetector   = rows / 10;
        int     top         = rows / 3;
        int     bottom      = rows / 2;



        switch (CameraSettingsActivity.viewMode) {

            case CameraSettingsActivity.VIEW_MODE_RGBA:
                break;

            case CameraSettingsActivity.VIEW_MODE_ACCUMULATE:
                Imgproc.cvtColor(rgba,rgba,Imgproc.COLOR_BGR2GRAY);
                break;

            case CameraSettingsActivity.VIEW_MODE_THRESH:
                Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_BGR2GRAY);
                Imgproc.GaussianBlur(gray,gray,new Size(5,5),5);
                Imgproc.threshold(gray,gray, 0,255,Imgproc.THRESH_OTSU);  ////Try changing values
                double upper[];
                double lower[];
                boolean topLeftFlag = true;
                boolean topRightFlag = false;
                boolean botLeftFlag = true;
                boolean botRightFlag = false;
                boolean junctionDetectTopLeftFlag= true;
                boolean junctionDetectTopRightFlag= false;

                for (int j=0; j<cols; j++) {
                    if (gray.get(top,j)[0] == 255 && topLeftFlag){
                            fwhiteIndexTop = j;
                            topLeftFlag = false;
                            topRightFlag=true;
                    }

                    if(gray.get(top,j)[0]==0 && topRightFlag){
                        fBlackIndexTop = j;
                        topRightFlag=false;
                    }

                    if (gray.get(bottom,j)[0]==255 && botLeftFlag){

                            fWhiteIndexBot = j;
                            botLeftFlag=false;
                            botRightFlag=true;
                    }

                    if(gray.get(bottom,j)[0]==0 && botRightFlag){
                        fBlackIndexBot = j;
                        botRightFlag=false;
                    }

                    if (gray.get(jDetector,j)[0]==255 && junctionDetectTopLeftFlag){

                            fWhiteIndexDet= j;
                            junctionDetectTopLeftFlag=false;
                            junctionDetectTopRightFlag=true;
                    }

                    if(gray.get(jDetector,j)[0]==0 && junctionDetectTopRightFlag){
                        fBlackIndexDet = j;
                        junctionDetectTopRightFlag=false;

                    }


                    if(!topLeftFlag && !topRightFlag && !botLeftFlag && !botRightFlag && !junctionDetectTopLeftFlag && !junctionDetectTopRightFlag)
                        break;

                }


                mP1.x = fwhiteIndexTop; mP2.x = fWhiteIndexBot; mP3.x = fBlackIndexTop; mP4.x = fBlackIndexBot;
                mP1.y = top; mP2.y = bottom; mP3.y = top; mP4.y = bottom;

                mTopCenter.x = (int)((mP1.x + mP3.x)/2); mTopCenter.y = top;
                mBotCenter.x = (int)((mP2.x + mP4.x)/2); mBotCenter.y = bottom;

                mJuncL.x = fWhiteIndexDet; mJuncL.y = jDetector;
                mJuncR.x = fBlackIndexDet; mJuncR.y = jDetector;
                mJuncC.x = (int)((mJuncL.x + mJuncR.x)/2); mJuncC.y = jDetector;



                error = ((cols/2)-(int)(((mTopCenter.x+mBotCenter.x)/2))) +(int)(mTopCenter.x-mBotCenter.x) ;




                if(((mJuncR.x - mJuncL.x)>(1.5*(mP3.x -  mP1.x)))&& mJuncC.x>mTopCenter.x){
                    Imgproc.putText(rgba,"R-junc",mJuncC,3,3,mColorsRGB[0]);
                }
                else if(((mJuncR.x - mJuncL.x)>(1.5*(mP3.x -  mP1.x)))&& mJuncC.x<mTopCenter.x){
                    Imgproc.putText(rgba,"L-junc",mJuncC,3,3,mColorsRGB[0]);
                }

                Imgproc.putText(rgba,String.valueOf(error),new Point(top,left),3,1,mColorsRGB[0]);
                Imgproc.circle(rgba,mTopCenter,5,mColorsRGB[0],5);
                Imgproc.circle(rgba,mBotCenter,5,mColorsRGB[0],5);
                Imgproc.circle(rgba,mJuncL,5,mColorsRGB[1],5);
                Imgproc.circle(rgba,mJuncR,5,mColorsRGB[1],5);
                Imgproc.circle(rgba,mP1,5,mColorsRGB[2],5);
                Imgproc.circle(rgba,mP2,5,mColorsRGB[2],5);
                Imgproc.circle(rgba,mP3,5,mColorsRGB[2],5);
                Imgproc.circle(rgba,mP4,5,mColorsRGB[2],5);
                //Imgproc.line(rgba,mP1,mP3,mColorsRGB[2],5);
                //Imgproc.line(rgba,mP2,mP4,mColorsRGB[2],5);

                break;

        }

        return rgba;


    }

    //--------------------------------------------------------------------------------------//



    //---------------------------------MENU CALLBACKS---------------------------------//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mode, menu);
        //mItemPreviewRGBA  = menu.add("Preview RGBA");
        //mItemPreviewHist  = menu.add("Histograms");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getItemId() == R.id.norm)
            viewMode = VIEW_MODE_RGBA;
        else if (item.getItemId()==R.id.thresh)
            viewMode = VIEW_MODE_THRESH;
        else if(item.getItemId()==R.id.accum)
            viewMode = VIEW_MODE_ACCUMULATE;
        return true;
    }

    //--------------------------------------------------------------------------------//


}
