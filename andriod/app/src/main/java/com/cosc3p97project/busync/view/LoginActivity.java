package com.cosc3p97project.busync.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cosc3p97project.busync.MainActivity;
import com.cosc3p97project.busync.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button LoginButton, PhoneLoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgetPasswordLink;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AllowUserToLogin();

            }
        });
    }

    // Add the onStart method to check if the user is already logged in.
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            SendUserToMainActivity();
        }
    }

    // Add the AllowUserToLogin method to authenticate the user with Firebase.
    private void AllowUserToLogin() {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please enter email",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
        }
        else
        {

            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    String deviceToken = task.getResult();
                                    UsersRef.child(currentUserId).child("device_Token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                SendUserToMainActivity();
                                                Toast.makeText(LoginActivity.this, "Logged in Successful...", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    String message = task.getException().toString();
                                    Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        String message = task.getException().toString();
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                        } else if (task.getException() instanceof FirebaseNetworkException) {
                            Toast.makeText(LoginActivity.this, "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    // Add the InitializeFields method to set up the layout elements.
    private void InitializeFields() {
        LoginButton = (Button) findViewById(R.id.login_button);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        NeedNewAccountLink = (TextView) findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
    }

    // Add the SendUserToMainActivity method to redirect the user to the main activity.
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
       mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    // Add the SendUserToRegisterActivity method to redirect the user to the register activity.
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}