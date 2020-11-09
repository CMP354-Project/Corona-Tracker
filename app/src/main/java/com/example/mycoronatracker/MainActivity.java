package com.example.mycoronatracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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

        emailET = (EditText) findViewById(R.id.emailEditText);
        passET = (EditText) findViewById(R.id.passEditText);
        loginBTN = (Button) findViewById(R.id.loginButton);
        registerBTN = (Button) findViewById(R.id.registerButton);
        emailError = (TextView) findViewById(R.id.emailErrorTV);
        passError = (TextView) findViewById(R.id.passErrorTV);

        loginBTN.setOnClickListener(this);
        registerBTN.setOnClickListener(this);

        emailET.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    if (!v.getText().toString().contains("@")) {

                    }
                }
                return false;
            }
        });
        passError.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    if (!v.getText().toString().contains("@")) {

                    }
                }
                return false;
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    private void validateLoginDetails(TextView v) {


    }
}