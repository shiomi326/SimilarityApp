package com.mindhive.similarity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//public class MainActivity extends AppCompatActivity {
public class MainActivity extends Activity {

    private Button btnModeSim;
    private Button btnModeStd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnModeSim = (Button) findViewById(R.id.btnModeSim);
        btnModeSim.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//                Intent intent= new Intent(MainActivity.this, ModeSimActivity.class);
                Intent intent= new Intent(MainActivity.this, ReferImgActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });

        btnModeStd = (Button) findViewById(R.id.btnModeStd);
        btnModeStd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // TODO Auto-generated method stub
                Intent intent= new Intent(MainActivity.this, ModeStdActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });



//        btnModeSim.setOnClickListener(this);
    }

//    @Override
//    public void onClick(View v) {
//        showMessage();
//    }


}

