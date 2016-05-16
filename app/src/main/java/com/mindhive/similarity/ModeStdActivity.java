package com.mindhive.similarity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by shiomi on 4/05/16.
 */
public class ModeStdActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "ModeStdActivity";

    // Load OpenCV 3.0
    static {
        System.loadLibrary("opencv_java3");
    }

    private int mWidth, mHeight;
//    private CameraBridgeViewBase mCameraView;
    private OpenCVCameraView mCameraView;
    private Mat mOutputFrame;
//    private Button btnTakePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mode_std);


        // 画面サイズを定義ファイルから取得
//        mWidth = getResources().getDimensionPixelSize(R.dimen.view_width);
//        mHeight = getResources().getDimensionPixelSize(R.dimen.view_height);

//        mWidth = 1280;
//        mHeight = 1280;

        // カメラの初期設定
//        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView = (OpenCVCameraView) findViewById(R.id.camera_view);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mCameraView.setCvCameraViewListener(this);
//        mCamera.setMaxFrameSize(mWidth, mHeight);


        Button btnTakePicture = (Button) findViewById(R.id.takePicture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Log.i(TAG,"onTouch event");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentDateandTime = sdf.format(new Date());
                String fileName = Environment.getExternalStorageDirectory().getPath() +
                        "/sample_picture_" + currentDateandTime + ".jpg";
                mCameraView.takePicture(fileName);
                Toast.makeText(ModeStdActivity.this, fileName + " saved", Toast.LENGTH_SHORT).show();
            }
        });


    }




    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onResume(){
        super.onResume();
        //static link openCV
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        //OpenCV for Android
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this,
//                new OpenCVLoaderCallback(this, mCamera));
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onPause() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mCameraView != null)
            mCameraView.disableView();
    }


    // 以下はCvCameraViewListener2インターフェースの実装
    @Override
    public void onCameraViewStarted(int width, int height) {
        // Mat(int rows, int cols, int type)
        // rows(行): height, cols(列): width
        mOutputFrame = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mOutputFrame.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        return frame;
        // Cannyフィルタをかける
//        Imgproc.Canny(inputFrame.gray(), mOutputFrame, 80, 100);
//        // ビット反転
//        Core.bitwise_not(mOutputFrame, mOutputFrame);
//        return mOutputFrame;
    }



}
