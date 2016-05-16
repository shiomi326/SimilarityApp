package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shiomi on 5/05/16.
 */
public class GetSimilarityActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Load OpenCV 3.0
    static {
        System.loadLibrary("opencv_java3");
    }

    private int mWidth, mHeight;
    private CameraBridgeViewBase mCameraView;
    Mat mRgba;
    Bitmap bmpDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_get_similarity);

        //Get Descriptor
//        Intent intent = getIntent();
//        bmpDesc = intent.getParcelableExtra("descriptor");

        // 画面サイズを定義ファイルから取得
//        mWidth = getResources().getDimensionPixelSize(R.dimen.view_width);
//        mHeight = getResources().getDimensionPixelSize(R.dimen.view_height);
//        mWidth = 1280;
//        mHeight = 1280;

        // カメラの初期設定
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mCameraView.setCvCameraViewListener(this);
//        mCamera.setMaxFrameSize(mWidth, mHeight);

    }

    //Read OpenCV
//    private static class OpenCVLoaderCallback extends BaseLoaderCallback {
//        private final CameraBridgeViewBase mCameraView;
//        private OpenCVLoaderCallback(Context context, CameraBridgeViewBase cameraView) {
//            super(context);
//            mCameraView = cameraView;
//        }
//
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:
//                    mCameraView.enableView();
//                    break;
//                default:
//                    super.onManagerConnected(status);
//                    break;
//            }
//        }
//    }



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
        // static link OpenCV
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        // OpenCV for Android
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this,
//                new OpenCVLoaderCallback(this, mCameraView));
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mCameraView != null)
            mCameraView.disableView();
    }

    // 以下はCvCameraViewListener2インターフェースの実装
    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        return detectAkazeFeatures(frame);
//        return frame;
    }


    //Detect Features
    public Mat detectAkazeFeatures(Mat rgba) {

        Mat detectMat = new Mat();
        Imgproc.cvtColor(rgba, detectMat, Imgproc.COLOR_RGBA2RGB);

        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
//        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.detect(detectMat, keyPoints);

//        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        Mat descriptor = new Mat();
        descriptorExtractor.compute(detectMat, keyPoints, descriptor);


        if(!keyPoints.empty()){
            // Draw kewpoints
            Mat outputImage = new Mat();
            Scalar color = new Scalar(0, 0, 255); // BGR
            int flags = Features2d.DRAW_RICH_KEYPOINTS; // For each keypoint, the circle around keypoint with keypoint size and orientation will be drawn.
            Features2d.drawKeypoints(detectMat, keyPoints, detectMat, color , flags);
            Imgproc.cvtColor(detectMat, outputImage, Imgproc.COLOR_RGB2RGBA);


            ///////

            MatOfDMatch matches = new MatOfDMatch();
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//            matcher.match(descriptor, ConfirmImgActivity.srcDescriptor, matches);
            matcher.match(ConfirmImgActivity.srcDescriptor, descriptor, matches);

            Double threshold = 45.0;
            List<DMatch> matchesList = matches.toList();
            LinkedList<DMatch> listOfGoodMatches = new LinkedList<>();

            // find good features
            for (int i=0; i<matchesList.size(); i++){
                Double distance = (double) matchesList.get(i).distance;
                if(distance < threshold)   listOfGoodMatches.add(matchesList.get(i));
            }


//            double max_dist = 0;
//            double min_dist = 100;
//            for (int i = 0; i < descriptor.rows(); i++) {
//                Double distance = (double) matchesList.get(i).distance;
//                if (distance < min_dist) min_dist = distance;
//                if (distance > max_dist) max_dist = distance;
//            }
//
//            for (int i = 0; i < descriptor.rows(); i++) {
//                if (matchesList.get(i).distance < 2.4 * min_dist) {
//                    listOfGoodMatches.add(matchesList.get(i));
//                }
//            }


            int matchNum = listOfGoodMatches.size();
//            int srcNum = ConfirmImgActivity.srcDescriptor.rows();
            int srcNum = matchesList.size();

            double goodpercend = ((double)matchNum/ (double)srcNum);

            String result;
            Scalar dispColor;
            if(goodpercend > 0.15){
                result = String.format("Detected=%.3f", goodpercend);
                dispColor = new Scalar(0, 255, 0, 255);
            }else{
                result = String.format("%.3f", goodpercend);
                dispColor = new Scalar(255, 0, 0, 255);
            }
            Imgproc.putText(outputImage, result, new Point(20, 40), 4, 1, dispColor, 2);

            /*
            double percent = ((double)matchNum/ (double)srcNum);
            String result = String.format("%.2f", percent);
            NumberFormat df = NumberFormat.getPercentInstance();
            String dispResult = df.format(new BigDecimal(result));
//            Toast.makeText(this, "File Name:" + result, Toast.LENGTH_SHORT).show();
            Imgproc.putText(outputImage, dispResult, new Point(20, 40), 4, 1, new Scalar(0, 255, 0, 255), 2);
//            Core.putText(outputImage, result, new Point(32, 32), Core.FONT_HERSHEY_SIMPLEX, 0.6f, n );
            */

            return outputImage;
        }
//
        return rgba;
    }

}
