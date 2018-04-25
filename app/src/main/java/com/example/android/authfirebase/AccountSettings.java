package com.example.android.authfirebase;

import android.Manifest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.authfirebase.DialogsClass.CreateDialog;
import com.example.android.authfirebase.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AccountSettings extends AppCompatActivity implements CreateDialog.OnPhotoReceivedListener {

    private static final int REQUEST_CODE = 1234;
    ImageView imageView;
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private double progress;
    private Button button;
    private byte[] mBytes;
    private ProgressDialog dialog;

    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        imageView = findViewById(R.id.image_selector_view);
        button = findViewById(R.id.btton_save);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Photo....");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStoragePermissions) {
                    CreateDialog dialog = new CreateDialog();
                    dialog.show(getSupportFragmentManager(), "My dialog box");
                } else {
                    verifyStoragePermissions();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedImageUri != null) {
                    uploadNewPhoto(mSelectedImageUri);
                } else if (mSelectedImageBitmap != null) {
                    uploadNewPhoto(mSelectedImageBitmap);
                }
            }
        });

        Query query1 = FirebaseDatabase.getInstance().getReference().child("users")
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //this loop will return a single result
                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){


                    User user = singleSnapshot.getValue(User.class);

                    if(user.getProfile_image()!=null) {

                        Glide.with(getApplicationContext())
                                .load(user.getProfile_image())
                                .into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        String email=FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
        String domain=email.substring(0,email.indexOf('@'));


        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("name")
                .setValue(domain);

    }

    private void executeUploadTask() {
        dialog.show();

        //specify where the photo will be stored

        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images/users" + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "/profile_image"); //just replace the old image with the new one

        if (mBytes.length / MB < MB_THRESHHOLD) {

            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setContentLanguage("en") //see nodes below
                    /*
                    Make sure to use proper language code ("English" will cause a crash)
                    I actually submitted this as a bug to the Firebase github page so it might be
                    fixed by the time you watch this video. You can check it out at https://github.com/firebase/quickstart-unity/issues/116
                     */
                    .setCustomMetadata("Suhanshu Patel special meta data", "My first Project")
                    .setCustomMetadata("location", "India")
                    .build();
            //if the image size is valid then we can submit to database
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes, metadata);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    dialog.dismiss();
                    //Now insert the download url into the firebase database
                    Uri firebaseURL = taskSnapshot.getDownloadUrl();
                    Toast.makeText(AccountSettings.this, "Upload Success", Toast.LENGTH_SHORT).show();
                    Log.d("Account", "onSuccess: firebase download url : " + firebaseURL.toString());
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("profile_image")
                            .setValue(firebaseURL.toString());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(AccountSettings.this, "could not upload photo", Toast.LENGTH_SHORT).show();


                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Toast.makeText(getApplicationContext(), progress + "% Uploaded", Toast.LENGTH_SHORT).show();
                }
            })
            ;
        } else {
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadNewPhoto(Uri mSelectedImageUri) {
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(mSelectedImageUri);
    }


    private void uploadNewPhoto(Bitmap bitmap) {
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);

    }

    public void open_chat_activity(View view) {
        Intent intent=new Intent(this,ChatActivity.class);
        startActivity(intent);
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bm) {
            if (bm != null) {
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(AccountSettings.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params) {
            Log.d("Account", "doInBackground: started.");

            if (mBitmap == null) {

                try {

//                    converting uri to bitmap

                    mBitmap = MediaStore.Images.Media.getBitmap(AccountSettings.this.getContentResolver(), params[0]);

                    Log.d("Account", "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount() / MB + " MB");
                } catch (IOException e) {
                    Log.e("Account", "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++) {
                if (i == 10) {
                    Toast.makeText(AccountSettings.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap, 100 / i);
                Log.d("AccountSettings.class", "doInBackground: megabytes: (" + (11 - i) + "0%) " + bytes.length / MB + " MB");
                if (bytes.length / MB < MB_THRESHHOLD) {
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mBytes = bytes;
            //execute the upload
            executeUploadTask();
        }
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }




// <-------------------------------------       Yesterday Code's     ------------------------------------------------------------>


    public void verifyStoragePermissions() {

        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    AccountSettings.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void getImagePath(Uri imagePath) {
        if (!imagePath.toString().equals("")) {
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;

            Toast.makeText(this, "Image is selected" + imagePath, Toast.LENGTH_SHORT).show();

            Glide.with(getApplicationContext())
                    .load(imagePath)
                    .into(imageView);


        }

    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Toast.makeText(this, "Image is clicked", Toast.LENGTH_SHORT).show();
            imageView.setImageBitmap(bitmap);
        }
    }
}
