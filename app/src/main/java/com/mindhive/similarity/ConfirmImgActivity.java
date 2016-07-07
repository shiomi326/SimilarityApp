package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.List;


/**
 * Created by shiomi on 4/05/16.
 */
public class ConfirmImgActivity extends Activity {

    private Bitmap stdBitmap;
    private Button btnConfirm;
    private MyApplication app;
//    private FeatureDetector featureDetector;
//    private DescriptorExtractor descriptorExtractor;
//    private MatOfKeyPoint src_keyPoints;
//    private Mat src_descriptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_img);

        //Screen Size Get
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        // 現在のintentを取得する
        Intent intent = getIntent();
        // intentから指定キーの文字列を取得する
        String FilePath = intent.getStringExtra("filePath");

        //Display file path
        Toast.makeText(this, "File Name:" + FilePath, Toast.LENGTH_SHORT).show();

        //Disp Bitmap
        ImageView imgView = (ImageView) findViewById(R.id.imageViewConfirm);
        Bitmap srcBitmap = BitmapUtil.createBitmap(FilePath, 960, 720); //2* size
        Bitmap resizeBitmap = BitmapUtil.resize(srcBitmap, 960, 720);
        stdBitmap = BitmapUtil.rotateBitmap(resizeBitmap);  //rotate
        imgView.setImageBitmap(stdBitmap);

        app = (MyApplication)this.getApplication();

        //Btn Click
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Intent
                app.setBitmapObj(stdBitmap);
                Intent intent = new Intent(ConfirmImgActivity.this, GetSimilarityActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });
    }


//
//    //Get Reference Image Features
//    public void detectSrcFeatures(Bitmap srcBitmap) {
//
//        // Convert Bitmap to Mat
//        Mat resizeMat = new Mat();
//        Utils.bitmapToMat(srcBitmap, resizeMat);
//        Log.i("detectSrcFeatures", "resizeMat width*height" + resizeMat.rows() + "@" + resizeMat.cols());
//
//        // Resize
//        Mat detectMat = new Mat();
//        Size sz = new Size(resizeMat.rows()/3, resizeMat.cols()/3);
//        Imgproc.resize( resizeMat, detectMat, sz); //resize
////        SaveMat(detectMat, "reference.jpg");
//
//        // Filter
//        int strength = 3;
////        Imgproc.bilateralFilter(detectMat, detectMat, strength, 0.0, 0.0);
////        Photo.fastNlMeansDenoising(detectMat,detectMat);
////        Imgproc.boxFilter(detectMat, detectMat, detectMat.depth(), new Size(strength, strength));
//
//        // Detect && Compute
//        featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);
//        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
//        src_keyPoints = new MatOfKeyPoint();
//        src_descriptor = new Mat();
//
//        featureDetector.detect(detectMat, src_keyPoints);
//        descriptorExtractor.compute(detectMat, src_keyPoints, src_descriptor);
//
//        //Draw Key Points
//
//
//        //set Num
//        List<KeyPoint> kpList = src_keyPoints.toList();
//        int src_keyNum = kpList.size();
//
//    }
//


}
