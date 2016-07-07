package com.mindhive.similarity;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by shiom on 17/05/2016.
 */
public class MyApplication extends Application {
    private final String TAG = "APPLICATION";
    private Bitmap objBitmap;
//    private Mat objMat;

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
//    public void setMatObj(Bitmap bmp){
//        objMat = bmp;
//    }
//    public Bitmap getMatObj(){
//        return objMat;
//    }
//    public void clearMatObj(){
//        objMat = null;
//    }

}