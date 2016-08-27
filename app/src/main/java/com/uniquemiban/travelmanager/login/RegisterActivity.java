package com.uniquemiban.travelmanager.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.R;
import com.uniquemiban.travelmanager.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 101;

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordRepeatEditText;

    private Button mChooseImage;
    private ImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNameEditText = (EditText)findViewById(R.id.edit_text_name_register);
        mEmailEditText = (EditText)findViewById(R.id.edit_text_email_register);
        mPasswordEditText = (EditText)findViewById(R.id.edit_text_password_register);
        mPasswordRepeatEditText = (EditText)findViewById(R.id.edit_text_password_repeat_register);

        mChooseImage = (Button)findViewById(R.id.button_choose_image);
        mProfileImage = (ImageView)findViewById(R.id.image_view_photo);

        mChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        String emailLogin = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        if(!TextUtils.isEmpty(emailLogin))
            mEmailEditText.setText(emailLogin);

        String passwordLogin = getIntent().getStringExtra(LoginActivity.EXTRA_PASSWORD);
        if(!TextUtils.isEmpty(passwordLogin))
            mPasswordEditText.setText(passwordLogin);

        final ProgressDialog progressDialog = new ProgressDialog(this, DialogFragment.STYLE_NO_FRAME);
        findViewById(R.id.button_create_account_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                if(!isValidName(mNameEditText.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Incorrect Name", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!isValidEmail(mEmailEditText.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Incorrect Email", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!isValidPassword(mPasswordEditText.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Incorrect Password", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!mPasswordEditText.getText().toString().equals(mPasswordRepeatEditText.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Repeated Password != Password", Toast.LENGTH_LONG).show();
                    return;
                }

                final FirebaseAuth auth = FirebaseAuth.getInstance();

                final StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl("gs://travelmanager-701b9.appspot.com");

                progressDialog.show();

                auth.createUserWithEmailAndPassword(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> pTask) {
                                if(pTask.isSuccessful()){
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(pTask.getResult().getUser().getUid())
                                                                                    .child("Name").setValue(mNameEditText.getText().toString());

                                    StorageReference storageRef = storage.child("user/" + auth.getCurrentUser().getUid() + ".jpg");


                                    mProfileImage.setDrawingCacheEnabled(true);
                                    mProfileImage.buildDrawingCache();
                                    Bitmap bitmap = mProfileImage.getDrawingCache();

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                                    if(bitmap != null){
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] data = baos.toByteArray();

                                        UploadTask uploadTask = storageRef.putBytes(data);

                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception pE) {
                                                Toast.makeText(RegisterActivity.this, "Error uploading image", Toast.LENGTH_LONG).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot pTaskSnapshot) {
                                                try {
                                                    String downloadUrl = pTaskSnapshot.getDownloadUrl().toString();
                                                    FirebaseDatabase.getInstance().getReference().child("Users")
                                                            .child(auth.getCurrentUser().getUid()).child("PhotoUrl").setValue(downloadUrl);
                                                    getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE).edit().putString(LoginActivity.SHARED_PHOTO_URL, downloadUrl).commit();
                                                }catch (Exception e){}
                                            }
                                        });
                                    }

                                    getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE).edit().putString(LoginActivity.SHARED_NAME, mNameEditText.getText().toString()).commit();

                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Account Was Created!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(RegisterActivity.this, NavigationDrawerActivity.class));
                                    finish();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, pTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private boolean isValidName(String pName) {
        if(TextUtils.isEmpty(pName))
            return false;
        return true;
    }

    private boolean isValidEmail(String pEmail) {
        if(TextUtils.isEmpty(pEmail))
            return false;
        return true;
    }

    private boolean isValidPassword(String pPassword) {
        if(TextUtils.isEmpty(pPassword))
            return false;
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Picasso.with(this)
                        .load(uri)
                        .resize(150, 150)
                        .centerCrop()
                        .into(mProfileImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
