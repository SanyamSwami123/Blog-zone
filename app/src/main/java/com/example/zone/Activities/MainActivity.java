package com.example.zone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zone.PhotoActivity.PostListActivity;
import com.example.zone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mUser;
    private Button loginButton;
    private Button createActButton;
    private EditText emailField;
    private EditText passwordField;


    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        loginButton = (Button) findViewById(R.id.loginbuttonEt);
        createActButton = (Button) findViewById(R.id.loginCreateAccount);
        emailField = (EditText) findViewById(R.id.loginEmaiEt);
        passwordField = (EditText) findViewById(R.id.loginPasswordEt);

        createActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateAccountActivity.class));
            }
        });


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mUser = firebaseAuth.getCurrentUser();

                if (mUser != null) {
                    Toast.makeText(MainActivity.this, "Signed In", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, PostListActivity.class));
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "Not Signed In", Toast.LENGTH_LONG).show();
                }




            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(emailField.getText().toString())
                        && !TextUtils.isEmpty(passwordField.getText().toString())) {

                    String email = emailField.getText().toString();
                    String pwd = passwordField.getText().toString();

                    login(email, pwd);
                }else {

                }
            }
        });



    }

    private void login(String email, String pwd) {

        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Signed in", Toast.LENGTH_LONG)
                                    .show();

                            startActivity(new Intent(MainActivity.this, PostListActivity.class));
                            finish();
                        }else {
                            Toast.makeText(MainActivity.this, "Account Not Created Yet!!!Create One", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }



    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
