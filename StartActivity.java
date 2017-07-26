package com.example.android.wechat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mRegBttn;
    private Button mLogBttn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBttn=(Button)findViewById(R.id.start_reg_bttn);
        mLogBttn=(Button)findViewById(R.id.start_login_bttn);

        mRegBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(regIntent);
                //finish();
            }
        });

        mLogBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logIntent=new Intent(StartActivity.this,LoginActivity.class);
                startActivity(logIntent);
            }
        });

    }
}
