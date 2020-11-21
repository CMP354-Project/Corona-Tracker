package com.example.mycoronatracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity implements OnClickListener {

    private FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerButton:
                if (!validateRegisterDetails()) {
                    break;
                }

                String email = registerEmailET.getText().toString();
                String password = registerPassET.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("SUCCESS", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("FAIL", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }

                            private void updateUI(FirebaseUser user) {
                                if (user != null) {
                                    String userEmail = user.getEmail().toString();
                                    Intent homeScreen = new Intent(RegisterActivity.this, MainActivity.class);
                                    homeScreen.putExtra("email", userEmail);
                                    startActivity(homeScreen);
                                }
                            }

                        });
                break;
        }
    }

    private boolean validateRegisterDetails() {
        String error = "";
        if (registerEmailET == null || registerPassET == null || registerConfirmPassET == null) {
            error = "Please fill all fields.";
            registerEmailTV.setText(error);
            registerPassTV.setText(error);
            registerConfirmPassTV.setText(error);
            return false;
        }
        else if (!isValidEmail(registerEmailET.getText().toString())) {
            error = "Please enter a valid email.";
            registerEmailTV.setText(error);
            return false;
        }
        // TODO: Implement preexisting email checking
        else if (registerPassET.getText().toString().length() < 8) {
            error = "The password must be longer than 8 characters.";
            registerPassTV.setText(error);
            return false;
        }
        else if (!registerPassET.getText().toString().equals(registerConfirmPassET.getText().toString())) {
            error = "Both passwords must match.";
            registerPassTV.setText(error);
            registerConfirmPassTV.setText(error);
            return false;
        }
        return true;
    }

    public static boolean isValidEmail(CharSequence target) {
        return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}