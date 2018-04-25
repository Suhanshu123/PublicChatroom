package com.example.android.authfirebase.userlogin;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.authfirebase.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ProviderQueryResult;

import org.w3c.dom.Text;

public class ResetEmail extends AppCompatActivity {
    private EditText new_email, new_password;
    private FirebaseAuth firebaseAuth;
    private String user_email;
    private String user_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_email);
        new_email = findViewById(R.id.email_edit_text);
        new_password = findViewById(R.id.password_edit_text);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null)
            finish();
    }

    public void Reset_Email(View view) {

        user_email = new_email.getText().toString();
        user_password = new_password.getText().toString();

        if (!TextUtils.isEmpty(user_email) && !TextUtils.isEmpty(user_password)) {

            if (!user_email.equals(firebaseAuth.getCurrentUser().getEmail())) {
                updateEmail();

            } else Toast.makeText(this, "Same email", Toast.LENGTH_LONG).show();

        } else Toast.makeText(this, "fill the credential", Toast.LENGTH_LONG).show();

    }

    private void updateEmail() {
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Trying to update the email..Please Wait!!");
        progressDialog.show();

        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseAuth.getCurrentUser().getEmail(), user_password);
        firebaseAuth.getCurrentUser().reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {

                    firebaseAuth.fetchProvidersForEmail(user_email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().getProviders().size() == 1) {
                                    Toast.makeText(getApplicationContext(), "Email is already in used ", Toast.LENGTH_LONG).show();
                                } else {
                                    firebaseAuth.getCurrentUser().updateEmail(user_email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "updated email", Toast.LENGTH_LONG).show();
                                            } else
                                                Toast.makeText(getApplicationContext(), "Couldn't update the email ", Toast.LENGTH_LONG).show();

                                        }
                                    });


                                }
                            } else
                                Toast.makeText(getApplicationContext(), "Failed in fetching providers", Toast.LENGTH_LONG).show();

                        }

                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Credential are incorrect", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}
