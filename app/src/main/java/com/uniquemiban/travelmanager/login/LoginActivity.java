package com.uniquemiban.travelmanager.login;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.uniquemiban.travelmanager.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.R;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PASSWORD = "password";

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
                                    startActivity(new Intent(LoginActivity.this, NavigationDrawerActivity.class));
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
            }
        });

        findViewById(R.id.text_view_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View pView) {
                startActivity(new Intent(LoginActivity.this, NavigationDrawerActivity.class));
            }
        });
    }

}
