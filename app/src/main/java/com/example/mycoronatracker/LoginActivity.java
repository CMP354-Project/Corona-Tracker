package com.example.mycoronatracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private EditText loginEmailET;
    private EditText loginPassET;
    private TextView loginEmailTV;
    private TextView loginPassTV;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmailET = (EditText) findViewById(R.id.loginEmailET);
        loginPassET = (EditText) findViewById(R.id.loginPassET);
        loginEmailTV = (TextView) findViewById(R.id.loginEmailErrorTV);
        loginPassTV = (TextView) findViewById(R.id.loginPassErrorTV);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(this);
    }



    private void validateLoginDetails() {


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                validateLoginDetails();
                Intent homeActivity = new Intent(this, HomeActivity.class);
                startActivity(homeActivity);
                break;
        }
    }
}