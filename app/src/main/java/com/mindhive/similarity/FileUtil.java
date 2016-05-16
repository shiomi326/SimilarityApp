package com.mindhive.similarity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by shiomi on 5/05/16.
 */
public class FileUtil {
    public Bitmap ReadJpgFiles(String filePath ) {

        Bitmap bitmap = null;

//        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/dir/");

        File file = new File(filePath);
//            File file = new File(dir.getAbsolutePath() + "/test.jpg");
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(file.getPath());
//            ((ImageView)findViewById(R.id.imageView1)).setImageBitmap(_bm);
        } else {
            //存在しない
        }
        return bitmap;
    }


    // Array
    public ArrayList<String> searchFiles(String dir_path, String expr, boolean search_subdir)
    {
        final File dir = new File(dir_path);

        ArrayList<String> find_files = new ArrayList<String>();
        final File[] files = dir.listFiles();
        if(null != files){
            for(int i = 0; i < files.length; ++i) {
                if(!files[i].isFile()){
                    if(search_subdir){
                        ArrayList<String> sub_files = searchFiles(files[i].getPath(), expr, search_subdir);
                        find_files.addAll(sub_files);
                    }
                    continue;
                }

                final String filename = files[i].getName();
                if((null == expr) || filename.matches(expr)){
                    find_files.add(dir.getPath() + "/" + filename);
                }
            }
        }
        return find_files;
    }

}
