package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
//import android.graphics.Point;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * Created by shiomi on 4/05/16.
 */
public class ConfirmImgActivity extends Activity implements View.OnTouchListener {

    private Bitmap stdBitmap;
    private int offsetY;
    private ImageView imgView;
    private Button btnConfirm;
    private MyApplication app;
    private FeatureDetector featureDetector;
    private DescriptorExtractor descriptorExtractor;
    private MatOfKeyPoint src_keyPoints;
    private Mat src_descriptor;
    private MatOfKeyPoint selected_keyPoints;
    private Mat selected_descriptor;
    private Point tPoint;
    static int mScale = 6;
    private LinkedList<Integer> listOfKeyNo;


    // Load OpenCV 3.0
    static {
        System.loadLibrary("opencv_java3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_img);

        //Screen Size Get
        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point point = new android.graphics.Point();
        display.getSize(point);

        //New
        tPoint = new Point();
        listOfKeyNo = new LinkedList<>();

        // 現在のintentを取得する
        Intent intent = getIntent();
        // intentから指定キーの文字列を取得する
        String FilePath = intent.getStringExtra("filePath");

        //Display file path
        Toast.makeText(this, "File Name:" + FilePath, Toast.LENGTH_SHORT).show();

        //Disp Bitmap
        imgView = (ImageView) findViewById(R.id.imageViewConfirm);
        Bitmap srcBitmap = BitmapUtil.createBitmap(FilePath, 960, 720); //2* size
        Bitmap resizeBitmap = BitmapUtil.resize(srcBitmap, 960, 720);
        stdBitmap = BitmapUtil.rotateBitmap(resizeBitmap);  //rotate
        Bitmap drawBit = detectSrcFeatures(stdBitmap);
        imgView.setImageBitmap(drawBit);
        imgView.setOnTouchListener(this);

        app = (MyApplication)this.getApplication();

        //Btn Click
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent
                setSelectedFeatures();
                app.setMatObj(selected_descriptor);
                app.setMatOfKeyPointObj(selected_keyPoints);
                app.setBitmapObj(stdBitmap);
                Intent intent = new Intent(ConfirmImgActivity.this, GetSimilarityActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        int statusBarHeight = getStatusBarHeight(this);
        int[] location = new int[2];
        imgView.getLocationOnScreen(location);
        offsetY = statusBarHeight + location[1];
        Log.i("offsetY", "offsetY="+offsetY);
    }

    public static int getStatusBarHeight(Activity activity){
        final Rect rect = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tPoint.x = event.getX();
            tPoint.y = event.getY() - offsetY;
            Log.d("TouchEvent", "X:" + event.getX() + ",Y:" + event.getY());

            if(tPoint.y > 0 && tPoint.y < 960){
                chooseKeyPoints((int)tPoint.x, (int)tPoint.y);
                Log.i("TouchEvent", "Touched in ImageView!!");
            }

        }
     return true;
    }

    public void chooseKeyPoints(int x, int y) {

        List<KeyPoint> keyPointsList = src_keyPoints.toList();
        int keyNum = keyPointsList.size();
        double minDis = Double.MAX_VALUE;
        int index = 0;
        for(int i=0; i<keyNum; i++){
            KeyPoint trainKey = keyPointsList.get(i);
            int kX = (int)(trainKey.pt.x*mScale);
            int kY = (int)(trainKey.pt.y*mScale);
            double dis = culcDistance(x, y , kX, kY);
            if(dis < minDis){
                minDis = dis;
                index = i;
            }
        }
        Log.i("TouchEvent", "minDis == "+ minDis);

        // range
        if(minDis < 30 ){
            int checkNum = listOfKeyNo.indexOf(index);
            if(checkNum == -1){
                listOfKeyNo.add(index);
            }else{
                listOfKeyNo.remove(checkNum);
            }

            Mat drawMat = new Mat();
            Utils.bitmapToMat(stdBitmap, drawMat);
            Scalar blue = new Scalar(0, 80, 255, 255);
            Scalar yellow = new Scalar(255, 255, 0, 255);

            for(int i=0; i<keyNum; i++) {
                KeyPoint trainKey = keyPointsList.get(i);
                Imgproc.circle(drawMat, new Point((int) trainKey.pt.x * mScale, (int) trainKey.pt.y * mScale), (int) (trainKey.size * mScale), blue);
            }

            for(int i=0; i<listOfKeyNo.size(); i++){
                int num = listOfKeyNo.get(i);
                KeyPoint trainKey = keyPointsList.get(num);
                Imgproc.circle(drawMat, new Point((int) trainKey.pt.x * mScale, (int) trainKey.pt.y * mScale), (int) (trainKey.size * mScale), yellow);
            }
            Bitmap bmp = Bitmap.createBitmap(drawMat.cols(), drawMat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(drawMat, bmp);
            imgView.setImageBitmap(bmp);
        }

    }

    private double culcDistance(int x1,int y1,int x2,int y2) {
        double x = x1 - x2;
        double y = y1 - y2;
        return Math.sqrt( (x * x + y * y));
    }

    // Get selected keypoint and descripter
    private void setSelectedFeatures(){
        List<KeyPoint> keyPointsList = src_keyPoints.toList();
        List<KeyPoint> selectedkeyPointsList = new LinkedList<>();

        // New
        selected_keyPoints = new MatOfKeyPoint();
        selected_descriptor = new Mat();

        for(int i=0; i<listOfKeyNo.size(); i++){
            int num = listOfKeyNo.get(i);
            KeyPoint Keypoint = keyPointsList.get(num);
            selectedkeyPointsList.add(Keypoint);
//            Log.i("Mat", "col+row" + src_descriptor.cols()+ "*" +src_descriptor.rows());
            selected_descriptor.push_back(src_descriptor.row(num));
        }
        selected_keyPoints.fromList(selectedkeyPointsList);
    }


    //Get Reference Image Features
    public Bitmap detectSrcFeatures(Bitmap srcBitmap) {

        // Convert Bitmap to Mat
        Mat drawMat = new Mat();
        Utils.bitmapToMat(srcBitmap, drawMat);
//        Imgproc.cvtColor(drawMat, drawMat, Imgproc.COLOR_BGR2BGRA );
        Log.i("detectSrcFeatures", "resizeMat width*height" + drawMat.rows() + "@" + drawMat.cols());

        // Resize
        Mat detectMat = new Mat();
        Size sz = new Size(drawMat.width()/mScale, drawMat.height()/mScale);
        Imgproc.resize( drawMat, detectMat, sz); //resize
//        SaveMat(detectMat, "reference.jpg");

        // Filter
        int strength = 3;
//        Imgproc.bilateralFilter(detectMat, detectMat, strength, 0.0, 0.0);
//        Photo.fastNlMeansDenoising(detectMat,detectMat);
//        Imgproc.boxFilter(detectMat, detectMat, detectMat.depth(), new Size(strength, strength));

        // Detect && Compute
        featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        src_keyPoints = new MatOfKeyPoint();
        src_descriptor = new Mat();

        featureDetector.detect(detectMat, src_keyPoints);
        descriptorExtractor.compute(detectMat, src_keyPoints, src_descriptor);
//        featureDetector.detect(drawMat, src_keyPoints);
//        descriptorExtractor.compute(drawMat, src_keyPoints, src_descriptor);
////        Features2d.drawKeypoints(drawMat, src_keyPoints, drawMat, new Scalar(2,254,255,255), Features2d.DRAW_RICH_KEYPOINTS);
//        //Draw Key Points
//        List<KeyPoint> keyPointsList = src_keyPoints.toList();
//        int keyNum = keyPointsList.size();
//        for(int i=0; i<keyNum; i++){
//                KeyPoint trainKey = keyPointsList.get(i);
//                Imgproc.circle(drawMat, new org.opencv.core.Point( (int)trainKey.pt.x,(int)trainKey.pt.y), (int)(trainKey.size), new Scalar(0,0,255,255));
//        }
//
        //Draw Key Points
        List<KeyPoint> keyPointsList = src_keyPoints.toList();
        int keyNum = keyPointsList.size();
        for(int i=0; i<keyNum; i++){
            KeyPoint trainKey = keyPointsList.get(i);
            Imgproc.circle(drawMat, new Point( (int)trainKey.pt.x*mScale,(int)trainKey.pt.y*mScale), (int)(trainKey.size*mScale), new Scalar(0,0,255,255));
        }

        // Bitmap
        Log.i("detectSrcFeatures", "before draw Mat");
        Bitmap bmp = Bitmap.createBitmap(drawMat.cols(), drawMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(drawMat, bmp);

        return bmp;
    }


}
