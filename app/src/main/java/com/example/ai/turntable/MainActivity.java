package com.example.ai.turntable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {


    private LuckyPan mLuckyPan;
    private ImageView mStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLuckyPan = findViewById(R.id.id_luckyPan);
        mStartBtn = findViewById(R.id.id_start_btn);

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             if (!mLuckyPan.isStart()){
                mLuckyPan.luckyStart();
                mStartBtn.setImageResource(R.drawable.btn_stop);
             }else {
                 if (!mLuckyPan.isShouldEnd()){
                     mLuckyPan.luckyEnd();
                     mStartBtn.setImageResource(R.drawable.btn_start);
                 }
             }

            }
        });

    }

}
