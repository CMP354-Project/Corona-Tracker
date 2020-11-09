package com.example.mycoronatracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity implements OnClickListener {

    private EditText registerEmailET;
    private EditText registerPassET;
    private EditText registerConfirmPassET;
    private TextView registerEmailTV;
    private TextView registerPassTV;
    private TextView registerConfirmPassTV;

    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerEmailET = (EditText) findViewById(R.id.registerEmailET);
        registerPassET = (EditText) findViewById(R.id.registerPassET);
        registerConfirmPassET = (EditText) findViewById(R.id.confirmPassEditText);
        registerEmailTV = (TextView) findViewById(R.id.registerEmailErrorTV);
        registerPassTV = (TextView) findViewById(R.id.registerPassErrorTV);
        registerConfirmPassTV = (TextView) findViewById(R.id.confirmPassErrorTV);

        registerButton = (Button) findViewById(R.id.registerButton);

        registerButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerButton:
                validateRegisterDetails();
                Intent loginActivity = new Intent(this, LoginActivity.class);
                startActivity(loginActivity);
                break;
        }
    }

    private void validateRegisterDetails() {
    }
}