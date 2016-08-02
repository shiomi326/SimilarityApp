package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shiomi on 5/05/16.
 */
public class GetSimilarityActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

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
    private MatOfDMatch matches;
    private int matchMethod = 0;
    private Point src_center;
    private int src_radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_get_similarity);

        // Get readImage
        app = (MyApplication)this.getApplication();
        src_keyPoints = app.getMatOfKeyPointObj();
        src_descriptor = app.getMatObj();
        Bitmap rdBitmap = app.getBitmapObj();
        ImageView imgView = (ImageView) findViewById(R.id.imageViewOverlay);

        Bitmap transBitmap = GetTranceBitmap2(rdBitmap, src_keyPoints);
        imgView.setImageBitmap(transBitmap);
//        imgView.setImageBitmap(rdBitmap);
        imgView.setOnTouchListener(this);

        // Feature values
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


        // Init Camera
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mCameraView.setCvCameraViewListener(this);

    }

    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            matchMethod++;
            if(matchMethod>5) matchMethod = 0;
            int threhold = 4+(matchMethod+1)*4;
            Toast.makeText(this, "Threshold="+ threhold, Toast.LENGTH_SHORT).show();
        }
        return true;
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

        //Clear objects
        app.clearBitmapObj();
        app.clearMatObj();
        app.clearMatOfKeyPointObj();
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

    private Bitmap GetTranceBitmap2(Bitmap srcBitmap, MatOfKeyPoint srckeyPoints) {
        List<KeyPoint> srckeyPointsList = srckeyPoints.toList();
        int KeyNum = srckeyPointsList.size();
        if(KeyNum < 1){
            return srcBitmap;
        }

        // Circle
        Point[] srcPoints = new Point[KeyNum];
        for (int i=0; i < KeyNum; i++) {
            srcPoints[i] = new Point(srckeyPointsList.get(i).pt.x * ConfirmImgActivity.mScale,
                                    srckeyPointsList.get(i).pt.y * ConfirmImgActivity.mScale);
        }
        MatOfPoint2f MatofPoint2f = new MatOfPoint2f(srcPoints);
        src_center = new Point();
        float[] radiusArray = new float[1];
        Imgproc.minEnclosingCircle(MatofPoint2f, src_center, radiusArray);
        src_radius = (int)(radiusArray[0]*1.4);

        // Create Bitmap
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        int[] pixels = new int[width * height];

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);


        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++) {
                if(!checkInside(x,y, (int)src_center.x, (int)src_center.y, src_radius)) {
                    pixels[x + y * width] = 0;
                }
            }
        }
        bitmap.eraseColor(Color.argb(0, 0, 0, 0));
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }

    private Boolean checkInside( int x, int y, int cx, int cy, int r)
    {
        return (cx - x) * (cx - x) + (cy - y) * (cy - y) < r*r;
    }

    private Bitmap GetTranceBitmap(Bitmap srcBitmap, MatOfKeyPoint srckeyPoints){
        List<KeyPoint> srckeyPointsList = srckeyPoints.toList();

        //get rect from query list
        int eX = 0, sX = 1000, eY = 0, sY = 1000;
        for(int i=0; i<srckeyPointsList.size(); i++){
            if(eX < srckeyPointsList.get(i).pt.x) eX = (int)srckeyPointsList.get(i).pt.x;
            if(sX > srckeyPointsList.get(i).pt.x) sX = (int)srckeyPointsList.get(i).pt.x;
            if(eY < srckeyPointsList.get(i).pt.y) eY = (int)srckeyPointsList.get(i).pt.y;
            if(sY > srckeyPointsList.get(i).pt.y) sY = (int)srckeyPointsList.get(i).pt.y;
        }

        sX = sX* ConfirmImgActivity.mScale;
        eX = eX* ConfirmImgActivity.mScale;
        sY = sY* ConfirmImgActivity.mScale;
        eY = eY* ConfirmImgActivity.mScale;
        Bitmap newBitmap = TranceBitmap(srcBitmap, sX, sY, eX, eY);
        return newBitmap;
    }

    public static Bitmap TranceBitmap(Bitmap srcBitmap, int sx, int sy, int ex, int ey) {
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        int[] pixels = new int[width * height];

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        //check
        sx += -50;
        sy += -50;
        ex += 50;
        ey += 50;

        if(sx<0)        sx = 0;
        if(sy<0)        sy = 0;
        if(ex>width)    sx = width;
        if(ey>height)   sx = height;

        //1.
        for(int y=0; y<sy; y++){
            for(int x=0; x<width; x++) {
                pixels[x + y * width] = 0;
            }
        }
        //2.
        for(int y=sy; y<ey; y++){
            for(int x=0; x<sx; x++) {
                pixels[x + y * width] = 0;
            }
        }
        //3.
        for(int y=sy; y<ey; y++){
            for(int x=ex; x<width; x++) {
                pixels[x + y * width] = 0;
            }
        }
        //4.
        for(int y=ey; y<height; y++){
            for(int x=0; x<width; x++) {
                pixels[x + y * width] = 0;
            }
        }

        bitmap.eraseColor(Color.argb(0, 0, 0, 0));
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;
    }


    // Match Features
    public Mat matchFeatures(Mat rgba) {

        // Resize
        Mat resizeMat = new Mat();
        Imgproc.cvtColor(rgba, resizeMat, Imgproc.COLOR_RGBA2RGB);
//        Log.i("matchFeatures", "resizeMat width*height" + resizeMat.rows() + "@" + resizeMat.cols());
//        Core.flip(resizeMat.t(), resizeMat, 0); //rotate 90
//        Core.flip(resizeMat.t(), resizeMat, 1); //rotate 270
//        Imgproc.cvtColor(rgba, resizeMat, Imgproc.COLOR_RGBA2GRAY);

        Mat detectMat = new Mat();
        Size sz = new Size(resizeMat.width()/ConfirmImgActivity.mScale, resizeMat.height()/ConfirmImgActivity.mScale);
        Imgproc.resize( resizeMat, detectMat, sz); //resize

//        SaveMat(detectMat, "detect.jpg");

        // Filter
//        int strength = 3;
//        Imgproc.bilateralFilter(detectMat, detectMat, strength, 0.0, 0.0);
//        Photo.fastNlMeansDenoising(detectMat,detectMat);
//        Imgproc.boxFilter(detectMat, detectMat, detectMat.depth(), new Size(strength, strength));

        // Detect & Compute
        featureDetector.detect(detectMat, keyPoints);
        descriptorExtractor.compute(detectMat, keyPoints, descriptor);


        if( !src_keyPoints.empty() && !keyPoints.empty()){

            //draw detect Match
//            for (int i=0; i<keyPointsList.size(); i++){
//                //draw
//                KeyPoint trainKey = keyPointsList.get(i);
//                Imgproc.circle(rgba, new Point((int)trainKey.pt.x*ConfirmImgActivity.mScale,(int)trainKey.pt.y*ConfirmImgActivity.mScale)
//                        , (int)(trainKey.size*ConfirmImgActivity.mScale), new Scalar(255,255,0,255));
//            }

            //Find Match Points and Calc match rates
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
            matcher.match(src_descriptor, descriptor, matches);

            // Find min distance
            double minDistance = 1000;
            List<DMatch> matchesList = matches.toList();
            int queryIdx=0, trainIdx=0;
            for (int i = 0; i < matchesList.size(); i++) {
                double dist = matchesList.get(i).distance;
                if (dist < minDistance) {
                    minDistance = dist;
                    queryIdx = matchesList.get(i).queryIdx;
                    trainIdx = matchesList.get(i).trainIdx;
                }
            }

            List<KeyPoint> src_keyPointsList = src_keyPoints.toList();
            List<KeyPoint> keyPointsList = keyPoints.toList();
            Point minDisSrcPoint = src_keyPointsList.get(queryIdx).pt;
            Point minDisPoint = keyPointsList.get(trainIdx).pt;


            double threshold = 60;
//            double threshold = minDistance*3.0;
            LinkedList<DMatch> listOfGoodMatches = new LinkedList<>();
            // find good features
            for (int i=0; i<matchesList.size(); i++){
                if(matchesList.get(i).distance < threshold){
                    listOfGoodMatches.add(matchesList.get(i));
                }
            }

            //Find good match key points
            int matchNum = listOfGoodMatches.size();

            // this values are for findHomography function
            LinkedList<Point> queryList = new LinkedList<>();
            LinkedList<Point> trainList = new LinkedList<>();

            for (int i=0; i<matchNum; i++){
                queryIdx = listOfGoodMatches.get(i).queryIdx;
                trainIdx = listOfGoodMatches.get(i).trainIdx;

                //find match keypoints
                Point queryPos = src_keyPointsList.get(queryIdx).pt;
                Point trainPos = keyPointsList.get(trainIdx).pt;

                // Check Position
                double disThreshold = 4+(matchMethod+1)*4;
                if(checkPointsRelation(minDisSrcPoint, minDisPoint, queryPos, trainPos, disThreshold)){
                    Point checkPoint = new Point(trainPos.x * ConfirmImgActivity.mScale, trainPos.y * ConfirmImgActivity.mScale);
                    // train position has to be inside reference circle
                    if(checkInside((int)checkPoint.x, (int)checkPoint.y, (int)src_center.x, (int)src_center.y, src_radius)) {
                        queryList.addLast(queryPos);
                        trainList.addLast(trainPos);
                        //draw
                        float size = keyPointsList.get(trainIdx).size;
                        Imgproc.circle(rgba, new Point((int)trainPos.x*ConfirmImgActivity.mScale,(int)trainPos.y*ConfirmImgActivity.mScale),
                                (int)(size*ConfirmImgActivity.mScale), new Scalar(255,255,0,255));
                    }
                }
            }
            int goodMatchNum = trainList.size();


            //Find a center of macth points
            boolean correctCenter = false;
            if(goodMatchNum > 2) {
                Point[] srcPoints = new Point[goodMatchNum];
                for (int i = 0; i < goodMatchNum; i++) {
                    srcPoints[i] = new Point(trainList.get(i).x * ConfirmImgActivity.mScale,
                            trainList.get(i).y * ConfirmImgActivity.mScale);
                }

                MatOfPoint2f MatofPoint2f = new MatOfPoint2f(srcPoints);
                Point center = new Point();
                float[] radiusArray = new float[1];
                Imgproc.minEnclosingCircle(MatofPoint2f, center, radiusArray);

                double centerDiff = calcDistance(src_center.x, src_center.y, center.x, center.y);
                double range = (detectMat.height()*ConfirmImgActivity.mScale) / 8.0; //30
                if (centerDiff < range) {
                    correctCenter = true;
                }
            }


            int match_method = 0;

            if(match_method == 0) {
                //Calculate match rate
                double match_rate = ((double) goodMatchNum / (double) src_keyPointsList.size());

                //Threshold
                String result;
                Scalar dispColor;
                if (match_rate >= 0.50 && correctCenter) {
                    result = String.format("Detected=%.3f", match_rate);
                    dispColor = new Scalar(0, 255, 0, 255);
                } else {
                    result = String.format("%.3f", match_rate);
                    dispColor = new Scalar(255, 0, 0, 255);
                }

                //Draw Result
//            Features2d.drawKeypoints(detectMat, goodKp, rgba, dispColor , Features2d.DRAW_RICH_KEYPOINTS);
                Imgproc.putText(rgba, result, new Point(20, 45), 3, 2, dispColor, 2);
            }else{
                // ===================================================================
                // Detect Object outline with findHomography (RANSAC)
                // ===================================================================

                if (goodMatchNum >= 8) {

                    //get rect from query list
                    double maxX = 0, minX = 1000, maxY = 0, minY = 1000;
                    for(int i=0; i<queryList.size(); i++){
                        if(maxX < queryList.get(i).x) maxX = queryList.get(i).x;
                        if(minX > queryList.get(i).x) minX = queryList.get(i).x;
                        if(maxY < queryList.get(i).y) maxY = queryList.get(i).y;
                        if(minY > queryList.get(i).y) minY = queryList.get(i).y;
                    }


                    MatOfPoint2f obj = new MatOfPoint2f();
                    obj.fromList(queryList);

                    MatOfPoint2f scene = new MatOfPoint2f();
                    scene.fromList(trainList);

                    Mat H = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 5);

                    //release
                    obj.release();
                    scene.release();


                    boolean result = true;

                    if(H.height() >= 3 && H.width() >= 2) {

                        double det = H.get(0, 0)[0] * H.get(1, 1)[0] - H.get(1, 0)[0] * H.get(0, 1)[0];
                        if (det < 0) {
                            result = false;
                        }
                        double N1 = Math.sqrt(H.get(0, 0)[0] * H.get(0, 0)[0] + H.get(1, 0)[0] * H.get(1, 0)[0]);
                        if (N1 > 4 || N1 < 0.1) {
                            result = false;
                        }

                        double N2 = Math.sqrt(H.get(0, 1)[0] * H.get(0, 1)[0] + H.get(1, 1)[0] * H.get(1, 1)[0]);
                        if (N2 > 4 || N2 < 0.1) {
                            result = false;
                        }

                        double N3 = Math.sqrt(H.get(2, 0)[0] * H.get(2, 0)[0] + H.get(2, 1)[0] * H.get(2, 1)[0]);
                        if (N3 > 0.002) {
                            result = false;
                        }
                        Log.i("####### DEBUG #######", det + " " + N1 + " " + N2 + " " + N3);
                    }else{
                        result = false;
                    }


                    if (result) {
                        Log.i("#### DETECTION ####", "Detected stuff");

                        Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
                        Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

//                        double data0[] = {0, 0};
//                        double data1[] = {detectMat.cols(), 0};
//                        double data2[] = {detectMat.cols(), detectMat.rows()};
//                        double data3[] = {0, detectMat.rows()};
                        double data0[] = {minX, minY};
                        double data1[] = {maxX, minY};
                        double data2[] = {maxX, maxY};
                        double data3[] = {minX, maxY};
                        obj_corners.put(0, 0, data0);
                        obj_corners.put(1, 0, data1);
                        obj_corners.put(2, 0, data2);
                        obj_corners.put(3, 0, data3);

                        Core.perspectiveTransform(obj_corners, scene_corners, H);

                        Point ps0 = new Point(scene_corners.get(0, 0));
                        Point ps1 = new Point(scene_corners.get(1, 0));
                        Point ps2 = new Point(scene_corners.get(2, 0));
                        Point ps3 = new Point(scene_corners.get(3, 0));

                        // Draw Rect
                        int scale = ConfirmImgActivity.mScale;
                        Imgproc.line(rgba, new Point(ps0.x*scale, ps0.y*scale), new Point(ps1.x*scale, ps1.y*scale), new Scalar(0, 255, 0, 255), 4);
                        Imgproc.line(rgba, new Point(ps1.x*scale, ps1.y*scale), new Point(ps2.x*scale, ps2.y*scale), new Scalar(0, 255, 0, 255), 4);
                        Imgproc.line(rgba, new Point(ps2.x*scale, ps2.y*scale), new Point(ps3.x*scale, ps3.y*scale), new Scalar(0, 255, 0, 255), 4);
                        Imgproc.line(rgba, new Point(ps3.x*scale, ps3.y*scale), new Point(ps0.x*scale, ps0.y*scale), new Scalar(0, 255, 0, 255), 4);

                        //release
                        obj_corners.release();
                        scene_corners.release();
                    }
                    H.release();
                }
            }

        }

        resizeMat.release();
        detectMat.release();

        return rgba;
    }

    private boolean checkPointsRelation(Point stdQueryPos, Point stdTrainPos, Point queryPos, Point trainPos, double threshold){

        double queryDis = calcDistance(stdQueryPos.x, stdQueryPos.y, queryPos.x, queryPos.y);
        double trainDis = calcDistance(stdTrainPos.x, stdTrainPos.y, trainPos.x, trainPos.y);

        if(Math.abs(queryDis-trainDis) < threshold){
            return true;
        }
        return false;
    }
    private double calcDistance(double x1,double y1,double x2,double y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        return Math.sqrt( (x * x + y * y));
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
