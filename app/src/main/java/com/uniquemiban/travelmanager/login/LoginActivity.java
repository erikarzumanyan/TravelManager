package com.uniquemiban.travelmanager.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uniquemiban.travelmanager.utils.Constants;
import com.uniquemiban.travelmanager.start.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.R;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PASSWORD = "password";

    public static final String SHARED_SKIP = "shared_skip";

    public static final String SHARED_NAME = "shared_name";
    public static final String SHARED_PHOTO_URL = "shared_photo_url";

    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEditText = (EditText)findViewById(R.id.edit_text_email_login);
        mPasswordEditText = (EditText)findViewById(R.id.edit_text_password_login);

        findViewById(R.id.button_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> pTask) {
                                if(pTask.isSuccessful()){
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.FIREBASE_USERS).child(user.getUid()).child("Name");

                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot pDataSnapshot) {
                                            getSharedPreferences(Constants.FIREBASE_USERS, MODE_PRIVATE).edit().putString(SHARED_NAME, pDataSnapshot.getValue(String.class)).commit();
                                            ref.removeEventListener(this);
                                            startActivity(new Intent(LoginActivity.this, NavigationDrawerActivity.class));
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError pDatabaseError) {
                                            startActivity(new Intent(LoginActivity.this, NavigationDrawerActivity.class));
                                            finish();
                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this, "Try Again", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        findViewById(R.id.button_create_account_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra(EXTRA_EMAIL, mEmailEditText.getText().toString());
                intent.putExtra(EXTRA_PASSWORD, mPasswordEditText.getText().toString());
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.text_view_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).edit().putBoolean(SHARED_SKIP, true).commit();
                startActivity(new Intent(LoginActivity.this, NavigationDrawerActivity.class));
                finish();
            }
        });
    }

}
