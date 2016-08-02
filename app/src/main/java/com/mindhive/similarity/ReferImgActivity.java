package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by shiomi on 4/05/16.
 */
public class ReferImgActivity extends Activity {
    private static final String TAG = "ReferImgActivity";

    private List<String> imgList = new ArrayList<String>();
    private List<String> imgPath = new ArrayList<String>();
    private ListView lv;
    private File[] files;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_img);

        String sdPath = Environment.getExternalStorageDirectory().getPath();
        files = new File(sdPath).listFiles();
        if(files != null){
            for(int i = 0; i < files.length; i++){
                if(files[i].isFile() && files[i].getName().endsWith(".jpg")){
                    imgList.add(files[i].getName());    //File Name
                    imgPath.add(files[i].getPath());    //File Path
                }
            }

            //Sort
            Collections.sort(imgList);
            Collections.sort(imgPath);
            Collections.reverse(imgList);
            Collections.reverse(imgPath);

            lv = (ListView) findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, imgList);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String item = imgPath.get(position);
                    // Intent Activity
                    Intent intent= new Intent(ReferImgActivity.this, ConfirmImgActivity.class);
                    intent.putExtra( "filePath", item );
                    intent.setAction(Intent.ACTION_VIEW);
                    startActivity(intent);

                }
            });
        }
    }
    public void showItem(String str){
        Toast.makeText(this, "File Name:" + str, Toast.LENGTH_SHORT).show();
    }
}
