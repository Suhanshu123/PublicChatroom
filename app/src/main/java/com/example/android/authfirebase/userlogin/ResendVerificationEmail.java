package com.example.android.authfirebase.userlogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.authfirebase.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ResendVerificationEmail extends AppCompatActivity {

    private FirebaseAuth firebaseAuthor;
    private EditText email;
    private EditText password;
    private ProgressDialog progressDialog;
    private FirebaseAuth.AuthStateListener firebaseauth_listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resend_verification_email);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        firebaseAuthor = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);


        firebaseauth_listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuthor.getCurrentUser() != null) {
                    firebaseAuthor.getCurrentUser().sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        firebaseAuthor.signOut();
                                        finish();
                                        Toast.makeText(ResendVerificationEmail.this,
                                                " Email Verify link sent", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ResendVerificationEmail.this, LoginActivity.class));
                                    } else {
                                        firebaseAuthor.signOut();
                                        Toast.makeText(ResendVerificationEmail.this,
                                                " Failed..Try Again!!", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                }
            }
        };
    }

    public void sent_Email(View view) {
        String user_email = email.getText().toString();
        String user_password = password.getText().toString();
        if (TextUtils.isEmpty(user_email) && TextUtils.isEmpty(user_password)) {
            return;
        } else {
            progressDialog.setMessage("ResendingMessage...Please Wait!!");
            progressDialog.show();
            firebaseAuthor.signInWithEmailAndPassword(user_email, user_password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Wow!! you're a valid user", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ResendVerificationEmail.this,
                                        " something is wrong with your email and password", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseauth_listener != null)
            firebaseAuthor.removeAuthStateListener(firebaseauth_listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuthor.addAuthStateListener(firebaseauth_listener);
    }
}
