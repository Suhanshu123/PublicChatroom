package com.example.android.authfirebase;

import android.content.Intent;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.authfirebase.userlogin.LoginActivity;
import com.example.android.authfirebase.userlogin.ResetEmail;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView textView,user_name,phone_number,uid,profile_link;
    private FirebaseAuth.AuthStateListener firebase_auth_listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        textView=findViewById(R.id.email_of_user);
        user_name=findViewById(R.id.name_of_user);
        phone_number=findViewById(R.id.phone_of_user);
        uid=findViewById(R.id.uid_of_user);

        firebaseAuth=FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser()!=null) {
            String userEmailid = firebaseAuth.getCurrentUser().getEmail();
            textView.setText(userEmailid);
            setUserDetail();
        }

        firebase_auth_listener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    finish();
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));

                }
                else Toast.makeText(getApplicationContext(),"Hello "+firebaseAuth.getCurrentUser().getEmail(),Toast.LENGTH_LONG).show();
            }
        };



    }

    public void sign_out(View view) {
        firebaseAuth.signOut();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebase_auth_listener!=null)
            firebaseAuth.removeAuthStateListener(firebase_auth_listener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebase_auth_listener);
    }

    public void getUserDetail(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        String name=user.getDisplayName();
        String id=user.getUid();
        String phone_number=user.getPhoneNumber();

        user_name.setText(name);
        this.phone_number.setText(phone_number);
        uid.setText(id);
    }

    public void setUserDetail(){
        if(firebaseAuth.getCurrentUser()!=null){
            UserProfileChangeRequest profileChangeRequest=new UserProfileChangeRequest.Builder()
                    .setDisplayName("Suhanshu Anushka Team's")
                    .build();
            firebaseAuth.getCurrentUser().updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Successful Update",Toast.LENGTH_LONG).show();
                        getUserDetail();
                    }
                }
            });
        }
    }

    public void reset_password(View view) {
        firebaseAuth.sendPasswordResetEmail(firebaseAuth.getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(),"Password ResentLink Sent",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void reset_email(View view) {
        startActivity(new Intent(this,ResetEmail.class));
    }

    public void account_settings(View view) {
        Intent intent=new Intent(this,AccountSettings.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
}
