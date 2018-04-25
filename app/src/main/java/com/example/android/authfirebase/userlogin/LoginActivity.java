package com.example.android.authfirebase.userlogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.authfirebase.ProfileActivity;
import com.example.android.authfirebase.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private TextView textView;
    private FirebaseAuth.AuthStateListener firebase_auth_listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        textView = findViewById(R.id.verifyEmail);

        firebase_auth_listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent=new Intent(getApplicationContext(),ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this, "welcome user", Toast.LENGTH_LONG).show();
                }
            }
        };

    }


    public void goto_MainActivity(View view) {

        Intent intent=new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void login_user(View view) {

        String user_email = username.getText().toString();
        String user_password = password.getText().toString();


        progressDialog.setMessage("Login..Please Wait!!");

        if (TextUtils.isEmpty(user_email) || TextUtils.isEmpty(user_password)) {
            Toast.makeText(this, "Fill the credential", Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            Toast.makeText(LoginActivity.this, "Successful Sign In",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                            startActivity(intent);
                            finish();
                            // Sign in success, update UI with the signed-in user's information

                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed: Click Sign Up",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebase_auth_listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebase_auth_listener != null)
            firebaseAuth.removeAuthStateListener(firebase_auth_listener);
    }

    public void goto_verifyEmail(View view) {
        Intent intent = new Intent(this, ResendVerificationEmail.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
