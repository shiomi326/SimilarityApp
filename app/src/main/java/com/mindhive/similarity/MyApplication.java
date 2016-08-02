package com.mindhive.similarity;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * Created by shiom on 17/05/2016.
 */
public class MyApplication extends Application {
    private final String TAG = "APPLICATION";
    private Bitmap objBitmap;
    private Mat objMat;
    private MatOfKeyPoint objMatOfKeyPoint;

    @Override
    public void onCreate() {
        //Application作成時
        Log.v(TAG,"--- onCreate() in ---");
    }

    @Override
    public void onTerminate() {
        //Application終了時
        Log.v(TAG,"--- onTerminate() in ---");
    }

    //For Bitmap
    public void setBitmapObj(Bitmap bmp){
        objBitmap = bmp;
    }
    public Bitmap getBitmapObj(){
        return objBitmap;
    }
    public void clearBitmapObj(){
        objBitmap = null;
    }

    //For Mat (OpenCV)
    public void setMatObj(Mat mat){objMat = mat;}
    public Mat getMatObj(){return objMat;}
    public void clearMatObj(){objMat = null;}

    //For Mat of Keypoint (OpenCV)
    public void setMatOfKeyPointObj(MatOfKeyPoint matOfKeyPoint){objMatOfKeyPoint = matOfKeyPoint;}
    public MatOfKeyPoint getMatOfKeyPointObj(){return objMatOfKeyPoint;}
    public void clearMatOfKeyPointObj(){objMatOfKeyPoint = null;}

}