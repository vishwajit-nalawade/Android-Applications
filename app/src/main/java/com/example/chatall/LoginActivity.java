package com.example.chatall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    //private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private Button LoginButton , PhoneLoginButton;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccountLink , ForgotPasswordLink;
    private ProgressDialog LoadingBar;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AllowUserToLogin();
            }
        });

        PhoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);

            }
        });

        ForgotPasswordLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final EditText resetMail = new EditText(v.getContext());
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this , R.style.AlertDialog);
                builder.setTitle("Enter your Registered Email ID");
                resetMail.setHint("e.g. : user@example.com");
                builder.setView(resetMail);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(LoginActivity.this, "Reset Link sent Successfully, Check EMail", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(LoginActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        dialogInterface.cancel();
                    }
                });
                builder.show();



            }
        });
    }


    private void AllowUserToLogin()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please enter your Email",Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please enter your Password",Toast.LENGTH_SHORT).show();
        }
        else
        {
            LoadingBar.setTitle("Signing In");
            LoadingBar.setMessage("Please Wait");
            LoadingBar.setCanceledOnTouchOutside(true);
            LoadingBar.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken  = FirebaseInstanceId.getInstance().getToken();
                                UsersRef.child(currentUserID).child("device_token").setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    SendUserToMainActivity();
                                                    Toast.makeText(LoginActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                                    LoadingBar.dismiss();

                                                }

                                            }
                                        });


                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error :" + message,Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }
                        }
                    });
        }
    }


    private void InitializeFields()
    {
        LoginButton = findViewById(R.id.login_button);
        PhoneLoginButton = findViewById(R.id.phone_login_button);
        UserEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);
        NeedNewAccountLink = findViewById(R.id.need_new_account_link);
        ForgotPasswordLink = findViewById(R.id.forget_password_link);
        LoadingBar = new ProgressDialog(this);

    }





   /* @Override
    protected void onStart()
    {
        super.onStart();
        if(currentUser != null)
        {
            SendUserToMainActivity();
        }

    } */

    private void SendUserToMainActivity()
    {
        Intent mainintent = new Intent(LoginActivity.this,MainActivity.class);
        mainintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainintent);
        finish();
    }

    private void SendUserToRegisterActivity()
    {
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
}



