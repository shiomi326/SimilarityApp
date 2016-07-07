package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
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
import org.opencv.photo.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private MyApplication app;

    private int detectorType = 1; //0=ORB, 1=AKAZE
    private FeatureDetector featureDetector;
    private DescriptorExtractor descriptorExtractor;
    private MatOfKeyPoint src_keyPoints;
    private MatOfKeyPoint keyPoints;
    private Mat src_descriptor;
    private Mat descriptor;
    private int src_keyNum = 0;
    private MatOfDMatch matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_get_similarity);

        // Get readImage
        app = (MyApplication)this.getApplication();
        Bitmap rdBitmap = app.getBitmapObj();
        ImageView imgView = (ImageView) findViewById(R.id.imageViewOverlay);
        imgView.setImageBitmap(rdBitmap);

        // Feature values
        src_keyPoints = new MatOfKeyPoint();
        src_descriptor = new Mat();
        keyPoints = new MatOfKeyPoint();
        descriptor = new Mat();
        if(detectorType==0) {
            featureDetector = FeatureDetector.create(FeatureDetector.ORB);
            descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        }else{
            featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);
            descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        }
        matches = new MatOfDMatch();


        // Detect Reference image features
        detectSrcFeatures(rdBitmap);

        // Init Camera
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mCameraView.setCvCameraViewListener(this);

    }


    // Read OpenCV
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

        app.clearBitmapObj();
    }

    // CvCameraViewListener2 Interface
    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        return matchFeatures(frame);
//        return frame;
    }


    //Get Reference Image Features
    public void detectSrcFeatures(Bitmap srcBitmap) {

        // Convert Bitmap to Mat
//        Mat resizeMat = new Mat(srcBitmap.getWidth(), srcBitmap.getHeight(), CvType.CV_8UC3);
        Mat resizeMat = new Mat();
        Utils.bitmapToMat(srcBitmap, resizeMat);
//        Imgproc.cvtColor(resizeMat, resizeMat, Imgproc.COLOR_RGB2GRAY);
        Log.i("detectSrcFeatures", "resizeMat width*height" + resizeMat.rows() + "@" + resizeMat.cols());

        // Resize
        Mat detectMat = new Mat();
        Size sz = new Size(resizeMat.rows()/3, resizeMat.cols()/3);
        Imgproc.resize( resizeMat, detectMat, sz); //resize
//        SaveMat(detectMat, "reference.jpg");

        // Filter
        int strength = 3;
//        Imgproc.bilateralFilter(detectMat, detectMat, strength, 0.0, 0.0);
//        Photo.fastNlMeansDenoising(detectMat,detectMat);
//        Imgproc.boxFilter(detectMat, detectMat, detectMat.depth(), new Size(strength, strength));

        // Detect && Compute
        featureDetector.detect(detectMat, src_keyPoints);
        descriptorExtractor.compute(detectMat, src_keyPoints, src_descriptor);

        //set Num
        List<KeyPoint> kpList = src_keyPoints.toList();
        src_keyNum = kpList.size();

    }


    //Detect Features
    public Mat matchFeatures(Mat rgba) {

        // Resize
        Mat resizeMat = new Mat();
        Imgproc.cvtColor(rgba, resizeMat, Imgproc.COLOR_RGBA2RGB);
//        Log.i("matchFeatures", "resizeMat width*height" + resizeMat.rows() + "@" + resizeMat.cols());
//        Core.flip(resizeMat.t(), resizeMat, 0); //rotate 90
//        Core.flip(resizeMat.t(), resizeMat, 1); //rotate 270
//        Imgproc.cvtColor(rgba, resizeMat, Imgproc.COLOR_RGBA2GRAY);

        Mat detectMat = new Mat();
        Size sz = new Size(resizeMat.rows()/3, resizeMat.cols()/3);
//        Log.i("matchFeatures", "detectMat width*height" + sz.width +"@"+ sz.height);
        Imgproc.resize( resizeMat, detectMat, sz); //resize

//        SaveMat(detectMat, "detect.jpg");

        // Filter
        int strength = 3;
//        Imgproc.bilateralFilter(detectMat, detectMat, strength, 0.0, 0.0);
//        Photo.fastNlMeansDenoising(detectMat,detectMat);
//        Imgproc.boxFilter(detectMat, detectMat, detectMat.depth(), new Size(strength, strength));

        // Detect & Compute
        featureDetector.detect(detectMat, keyPoints);
        descriptorExtractor.compute(detectMat, keyPoints, descriptor);

        if( !src_keyPoints.empty() && !keyPoints.empty()){


            //Find Match Points and Calc match rates
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
            matcher.match(src_descriptor, descriptor, matches);

            // Find min distance
//            double minDistance = 1000;
            List<DMatch> matchesList = matches.toList();
//            for (int i = 0; i < matchesList.size(); i++) {
//                double dist = matchesList.get(i).distance;
//                if (dist < minDistance) {
//                    minDistance = dist;
//                }
//            }


            double threshold = 60.0;
//            double threshold = minDistance*3.0;
//            List<DMatch> matchesList = matches.toList();
            LinkedList<DMatch> listOfGoodMatches = new LinkedList<>();

            // find good features
            for (int i=0; i<matchesList.size(); i++){
                if(matchesList.get(i).distance < threshold){
                    listOfGoodMatches.add(matchesList.get(i));
                }
            }


            //Find good match key points
            List<KeyPoint> src_keyPointsList = src_keyPoints.toList();
            List<KeyPoint> keyPointsList = keyPoints.toList();
            LinkedList<KeyPoint> listOfGoodKeyPoints = new LinkedList<>();

            //draw detect Match
//            for (int i=0; i<keyPointsList.size(); i++){
//                //draw
//                KeyPoint trainKey = keyPointsList.get(i);
//                Imgproc.circle(rgba, new Point((int)trainKey.pt.x*3,(int)trainKey.pt.y*3), (int)(trainKey.size*3), new Scalar(0,0,255,255));
//            }

            int matchNum = listOfGoodMatches.size();
            int queryIdx, trainIdx;
            double range = detectMat.height()/8.0; //30
            for (int i=0; i<matchNum; i++){
                queryIdx = listOfGoodMatches.get(i).queryIdx;
                trainIdx = listOfGoodMatches.get(i).trainIdx;

                //find same position
                KeyPoint queryKey = src_keyPointsList.get(queryIdx);
                KeyPoint trainKey = keyPointsList.get(trainIdx);

                // Check Position
                if(queryKey.pt.x+range > trainKey.pt.x && queryKey.pt.x-range < trainKey.pt.x
                        && queryKey.pt.y+range > trainKey.pt.y && queryKey.pt.y-range < trainKey.pt.y){
                    listOfGoodKeyPoints.add(keyPointsList.get(trainIdx));
                    //draw
//                    Imgproc.circle(rgba, new Point((int)trainKey.pt.x*3,(int)trainKey.pt.y*3), (int)(trainKey.size*3), new Scalar(0,255,0,255));
//                    Imgproc.circle(rgba, new Point((int)trainKey.pt.y*3,(int)trainKey.pt.x*3), (int)(trainKey.size*3), new Scalar(0,255,0,255));
                }

//                listOfGoodKeyPoints.add(keyPointsList.get(trainIdx));
            }
//            Imgproc.circle(rgba, new Point(100,200), 20, new Scalar(0,255,0,255));
//            MatOfKeyPoint goodKp = new MatOfKeyPoint();
//            goodKp.fromList(listOfGoodKeyPoints);


            //Calculate match rate
            int fmatchNum = listOfGoodKeyPoints.size();
            double match_rate = ((double)fmatchNum/ (double)src_keyNum);


            //Threshold
            String result;
            Scalar dispColor;
            if(match_rate > 0.20){
                result = String.format("Detected=%.3f", match_rate);
                dispColor = new Scalar(0, 255, 0, 255);
            }else{
                result = String.format("%.3f", match_rate);
                dispColor = new Scalar(255, 0, 0, 255);
            }

            //Draw Result
//            int flags = Features2d.DRAW_RICH_KEYPOINTS; // For each keypoint, the circle around keypoint with keypoint size and orientation will be drawn.
//            Features2d.drawKeypoints(detectMat, goodKp, rgba, dispColor , flags);
            Imgproc.putText(rgba, result, new Point(20, 45), 3, 2, dispColor, 2);

        }
//
        return rgba;
    }

    private void SaveMat(Mat saveMat, String fileName){
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(saveMat.cols(), saveMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(saveMat, bmp);
        } catch (CvException e) {
//            Log.d(TAG, e.getMessage());
        }

        saveMat.release();

        FileOutputStream out = null;

        File sd = new File(Environment.getExternalStorageDirectory() + "/");
        boolean success = true;
        if (!sd.exists()) {
            success = sd.mkdir();
        }
        if (success) {
            File dest = new File(sd, fileName);

            try {
                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.JPEG, 80, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored

            } catch (Exception e) {
                e.printStackTrace();
//                Log.d(TAG, e.getMessage());
            } finally {
                try {
                    if (out != null) {
                        out.close();
//                        Log.d(TAG, "OK!!");
                    }
                } catch (IOException e) {
//                    Log.d(TAG, e.getMessage() + "Error");
                    e.printStackTrace();
                }
            }
        }
    }
}
