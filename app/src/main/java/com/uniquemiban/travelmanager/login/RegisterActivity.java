package com.uniquemiban.travelmanager.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.uniquemiban.travelmanager.NavigationDrawerActivity;
import com.uniquemiban.travelmanager.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordRepeatEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mNameEditText = (EditText)findViewById(R.id.edit_text_name_register);
        mEmailEditText = (EditText)findViewById(R.id.edit_text_email_register);
        mPasswordEditText = (EditText)findViewById(R.id.edit_text_password_register);
        mPasswordRepeatEditText = (EditText)findViewById(R.id.edit_text_password_repeat_register);

        String emailLogin = getIntent().getStringExtra(LoginActivity.EXTRA_EMAIL);
        if(!TextUtils.isEmpty(emailLogin))
            mEmailEditText.setText(emailLogin);

        String passwordLogin = getIntent().getStringExtra(LoginActivity.EXTRA_PASSWORD);
        if(!TextUtils.isEmpty(passwordLogin))
            mPasswordEditText.setText(passwordLogin);

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
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.createUserWithEmailAndPassword(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> pTask) {
                                if(pTask.isSuccessful()){
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(pTask.getResult().getUser().getUid())
                                                                                    .child("Name").setValue(mNameEditText.getText().toString());
                                    Toast.makeText(RegisterActivity.this, "Account Was Created!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(RegisterActivity.this, NavigationDrawerActivity.class));
                                } else {
                                    Toast.makeText(RegisterActivity.this, pTask.getException().toString(), Toast.LENGTH_LONG).show();
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
}
