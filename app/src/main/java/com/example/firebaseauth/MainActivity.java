package com.example.firebaseauth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private static final int GOOGLE_SIGN_IN_REQUEST = 112;
    FirebaseAuth auth;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        InitializeGoogleLogin();
        InitializeFacebook();
        InitializeOTPLogin();
    }

    private void InitializeFacebook() {
        LoginButton fb_login = findViewById(R.id.fb_login);
        callbackManager = CallbackManager.Factory.create();

        fb_login.setPermissions("email","public_profile");
        fb_login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Facebook", "On Success");
                handleFacebookLogin(loginResult);
            }

            @Override
            public void onCancel() {
                Log.d("Facebook", "On Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Facebook", "On Error");
            }
        });
    }

    private void handleFacebookLogin (LoginResult loginResult){
        AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());

        auth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            SendUserData(user);
                            Log.d("Login","Success");
                        }
                        else {
                            Log.d("Login","Error");
                        }
                    }
                });
    }

    private void InitializeGoogleLogin() {
        Button google_login = findViewById(R.id.google_login);

        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoGoogleLogin();
            }
        });
    }

    private void InitializeOTPLogin(){
        Button sendOtp = findViewById(R.id.send_otp);
        Button verifyOtp = findViewById(R.id.verify_otp);
        EditText phoneInput = findViewById(R.id.phone_input);
        EditText otpInput = findViewById(R.id.otp_input);
    }
    private void DoGoogleLogin() {

        //creating google sigining option object
        GoogleSignInOptions goption = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestIdToken("313944750353-jbjk9ibdujijfqukpdo2e38pig1ur030.apps.googleusercontent.com")
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        //create a google client object
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this,goption);

        //launchig google dialogue intent
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent,GOOGLE_SIGN_IN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check result from google
        if(requestCode==GOOGLE_SIGN_IN_REQUEST)
        {
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                processFirebaseLoginStep(account.getIdToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }
    private void  processFirebaseLoginStep(String token)
    {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(token,null);
        auth.signInWithCredential(authCredential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseUser user = auth.getCurrentUser();
                            SendUserData(user);
                        }
                    }
                });
    }
    private void SendUserData(FirebaseUser user)
    {
        Log.d("Login Sucess","Success");
        Log.d("User",user.getUid());
    }
}