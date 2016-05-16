package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shiomi on 4/05/16.
 */
public class ConfirmImgActivity extends Activity {

    // Load OpenCV 3.0
    static {
        System.loadLibrary("opencv_java3");
    }

    private Button btnConfirm;
    private Mat orgMat;

    public static Mat srcDescriptor = null;

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
        Bitmap srcBitmap = BitmapUtil.createBitmap(FilePath, point.x, point.y);
        Bitmap stdBitmap = BitmapUtil.rotateBitmap(srcBitmap);  //rotate
        srcBitmap = null;

        imgView.setImageBitmap(stdBitmap);
        //For processing image
//        Bitmap orgBitmap = BitmapFactory.decodeFile(FilePath);
//        orgMat = new Mat(orgBitmap.getWidth(), orgBitmap.getHeight(), CvType.CV_8UC3);
//        Utils.bitmapToMat(orgBitmap, orgMat);

        //Resize
        orgMat = new Mat();
        Mat resizeMat = new Mat(stdBitmap.getWidth(), stdBitmap.getHeight(), CvType.CV_8UC3);
        Size sz = new Size(720, 960);
        Imgproc.resize( resizeMat, orgMat, sz);

//        orgMat = new Mat(stdBitmap.getWidth(), stdBitmap.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(stdBitmap, orgMat);


        //Btn Click
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Get features

                detectAkazeFeatures();

                //Intent
                Intent intent= new Intent(ConfirmImgActivity.this, GetSimilarityActivity.class);
//                intent.putExtra( "descriptor", descriptor );
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });
    }


    //Get Features
    public void detectAkazeFeatures() {

//        Mat detectMat = new Mat();

        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
//        Mat srcDescriptor = new Mat();
        srcDescriptor = new Mat();
        //detect
//        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.AKAZE);
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.detect(orgMat, keyPoints);
        //descriptor
//        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.AKAZE);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        descriptorExtractor.compute(orgMat, keyPoints, srcDescriptor);

//        if(!keyPoints.empty()){
//            // Draw kewpoints
//            Mat outputImage = new Mat();
//            Scalar color = new Scalar(0, 0, 255); // BGR
//            int flags = Features2d.DRAW_RICH_KEYPOINTS; // For each keypoint, the circle around keypoint with keypoint size and orientation will be drawn.
//            Features2d.drawKeypoints(detectMat, keyPoints, detectMat, color , flags);
//            Imgproc.cvtColor(detectMat, outputImage, Imgproc.COLOR_RGB2RGBA);
//            return outputImage;
//        }
//
        //Convert Mat2Bitmap
//        Bitmap bmpDesc = Bitmap.createBitmap(descriptor.width(), descriptor.height(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(descriptor, bmpDesc);
//        return bmpDesc;

    }




}
