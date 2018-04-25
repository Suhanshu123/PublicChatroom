package com.example.android.authfirebase.userlogin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.authfirebase.ProfileActivity;
import com.example.android.authfirebase.R;
import com.example.android.authfirebase.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private Button button;
    private EditText email;
    private EditText password;
    private TextView textView;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebase_auth_listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn_register);
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.input_password);
        textView = findViewById(R.id.hello);

        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();

    }


    public void register_user(View view) {
        final String user_email = email.getText().toString();
        String user_password = password.getText().toString();

        if (TextUtils.isEmpty(user_email)) {
            Toast.makeText(this, "Fill the Email", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(user_password)) {
            Toast.makeText(this, "Fill the Password", Toast.LENGTH_LONG).show();
        }

        progressDialog.setMessage("Registering..Please Wait!!");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            User user = new User();

//                          user.setName(user_email.substring(0, user_email.indexOf("@")));
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName("Suhanshu Anushka Team's")
                                    .build();
                            firebaseAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Successful Update", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            user.setName(firebaseAuth.getCurrentUser().getDisplayName()+"Suhanshu");
                            user.setPhone("23");
                            user.setProfile_image("");
                            user.setUser_id(firebaseAuth.getUid());

                            FirebaseDatabase.getInstance().getReference()
                                    .child("user")
                                    .child(firebaseAuth.getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Values are added to firebase successfully", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Not  insertion of values", Toast.LENGTH_LONG).show();

                                }
                            });

                            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                            Toast.makeText(MainActivity.this, "Registration Successful.", Toast.LENGTH_LONG).show();


                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed: Maybe you are logged in",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }

    public void goto_loginActivity(View view) {
        Intent intent=new Intent(this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


}