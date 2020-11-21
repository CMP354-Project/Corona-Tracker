package com.example.mycoronatracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements OnClickListener {

    public static FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();
    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
    }

//    private void validateLoginDetails() {
//        String error = "";
//        if (registerEmailET == null || registerPassET == null || registerConfirmPassET == null) {
//            error = "Please fill all fields.";
//            registerEmailTV.setText(error);
//            registerPassTV.setText(error);
//            registerConfirmPassTV.setText(error);
//            return false;
//        }
//        else if (!isValidEmail(registerEmailET.getText().toString())) {
//            error = "Please enter a valid email.";
//            registerEmailTV.setText(error);
//            return false;
//        }
//        // TODO: Implement preexisting email checking
//        else if (registerPassET.getText().toString().length() < 8) {
//            error = "The password must be longer than 8 characters.";
//            registerPassTV.setText(error);
//            return false;
//        }
//        else if (!registerPassET.getText().toString().equals(registerConfirmPassET.getText().toString())) {
//            error = "Both passwords must match.";
//            registerPassTV.setText(error);
//            registerConfirmPassTV.setText(error);
//            return false;
//        }
//        return true;
//
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:

                String email = loginEmailET.getText().toString();
                String password = loginPassET.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("SUCCESS", "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("FAIL", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                    // ...
                                }
                                // ...
                            }
                        });
                break;
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String email = loginEmailET.getText().toString();
            Intent homeActivity = new Intent(LoginActivity.this, HomeActivity.class);
            homeActivity.putExtra("email", email);
            startActivity(homeActivity);
        }
    }
}