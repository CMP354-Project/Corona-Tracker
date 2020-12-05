package com.example.mycoronatracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity implements OnClickListener {

    private List<HashMap<String, Object>> tempLocation = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText registerNameET;
    private EditText registerEmailET;
    private EditText registerPassET;
    private EditText registerConfirmPassET;
    private TextView registerNameTV;
    private TextView registerEmailTV;
    private TextView registerPassTV;
    private TextView registerConfirmPassTV;

    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerNameET = (EditText) findViewById(R.id.registerNameET);
        registerEmailET = (EditText) findViewById(R.id.registerEmailET);
        registerPassET = (EditText) findViewById(R.id.registerPassET);
        registerConfirmPassET = (EditText) findViewById(R.id.confirmPassEditText);
        registerNameTV = (TextView) findViewById(R.id.registerNameErrorTV);
        registerEmailTV = (TextView) findViewById(R.id.registerEmailErrorTV);
        registerPassTV = (TextView) findViewById(R.id.registerPassErrorTV);
        registerConfirmPassTV = (TextView) findViewById(R.id.confirmPassErrorTV);

        registerButton = (Button) findViewById(R.id.registerButton);

        registerButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerButton: // Registering user
                if (!validateRegisterDetails()) {
                    break;
                }
                clearErrors();
                registerButton.setVisibility(View.GONE);
                String name = registerNameET.getText().toString();
                String email = registerEmailET.getText().toString();
                String password = registerPassET.getText().toString();
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyy H:mm", Locale.getDefault());
                String formattedDate = df.format(c);
                HashMap<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("user", email);
                user.put("location", tempLocation); // add temp location for later updates
                user.put("date", formattedDate); // add date of registration
                user.put("infected", false);
                user.put("notify", false);
                try {
                    db.collection("users")
                            .document(email)
                            .set(user)
                            .addOnSuccessListener(new OnSuccessListener() {
                                @Override
                                public void onSuccess(Object o) {
                                    Log.d("SUCCESS", "Document added");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("FAILURE", "Error adding document", e);
                                    registerButton.setVisibility(View.VISIBLE);
                                }
                            });
                    authenticate(email, password);
                } catch (Exception e) {
                    Log.d("EXCEPTION", e.getMessage());
                }
                break;
        }
    }

    private void clearErrors() { // clears all errors
        registerNameTV.setText("");
        registerEmailTV.setText("");
        registerPassTV.setText("");
        registerConfirmPassTV.setText("");
    }

    private void authenticate(String email, String password) {
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
                            registerButton.setVisibility(View.VISIBLE);
                        }
                    }

                    private void updateUI(FirebaseUser user) {
                        if (user != null) {
                            Intent homeScreen = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(homeScreen); // starts home activity
                        }
                    }
                });
    }

    private boolean validateRegisterDetails() { // function to validate registration fields
        clearErrors();
        String error = "";
        if (registerEmailET == null || registerPassET == null || registerConfirmPassET == null || registerNameET == null) {
            error = "Please fill all fields.";
            registerNameTV.setText(error);
            registerEmailTV.setText(error);
            registerPassTV.setText(error);
            registerConfirmPassTV.setText(error);
            return false;
        } else if (!isValidEmail(registerEmailET.getText().toString())) {
            error = "Please enter a valid email.";
            registerEmailTV.setText(error);
            return false;
        }
        // TODO: Implement preexisting email checking
        else if (registerPassET.getText().toString().length() < 8) {
            error = "The password must be longer than 8 characters.";
            registerPassTV.setText(error);
            return false;
        } else if (!registerPassET.getText().toString().equals(registerConfirmPassET.getText().toString())) {
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