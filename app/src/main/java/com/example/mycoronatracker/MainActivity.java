package com.example.mycoronatracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnClickListener {


    private EditText emailET;
    private EditText passET;
    private Button loginBTN;
    private Button registerBTN;
    private TextView emailError;
    private TextView passError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailET = (EditText) findViewById(R.id.loginEmailET);
        passET = (EditText) findViewById(R.id.loginPassET);
        loginBTN = (Button) findViewById(R.id.startLoginBTN);
        registerBTN = (Button) findViewById(R.id.startRegisterBTN);
        emailError = (TextView) findViewById(R.id.loginEmailErrorTV);
        passError = (TextView) findViewById(R.id.loginPassErrorTV);

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