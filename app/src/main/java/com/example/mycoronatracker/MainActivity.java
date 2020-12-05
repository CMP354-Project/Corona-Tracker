package com.example.mycoronatracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Button loginBTN;
    private Button registerBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginBTN = (Button) findViewById(R.id.startLoginBTN);
        registerBTN = (Button) findViewById(R.id.startRegisterBTN);

        loginBTN.setOnClickListener(this);
        registerBTN.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startLoginBTN:
                Intent loginActivity = new Intent(this, LoginActivity.class);
                startActivity(loginActivity);
                break;
            case R.id.startRegisterBTN:
                Intent registerActivity = new Intent(this, RegisterActivity.class);
                startActivity(registerActivity);
                break;
        }
    }
}