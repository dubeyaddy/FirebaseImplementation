package com.practiceapp.firebasesampleapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG= "GoogleActivity";
    private static final int RC_SIGN_IN=9001;

    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.signOutButton).setOnClickListener(this);
        findViewById(R.id.disconnectButton).setOnClickListener(this);

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);

        SignInButton signInButton= findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        mAuth=FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser= mAuth.getCurrentUser();

        if (currentUser!=null){
            Log.d(TAG,"Currently Signed in:"+currentUser.getEmail());
            Toast.makeText(MainActivity.this,"Currently Looged in: "+currentUser.getEmail(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount>task=GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account=task.getResult(ApiException.class);
                Toast.makeText(this,"Google Sign In Succeeded",Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e){
                Log.w(TAG,"Google Sign In failed",e);
                Toast.makeText(this,"Google Sign In failed",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG,"firebaseAuthWithGoogle:"+acct.getId());

        AuthCredential credential= GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user= mAuth.getCurrentUser();
                    Log.d(TAG,"SignInSuccess User:"+user.getEmail());
                    Toast.makeText(MainActivity.this,"Firebase Authentication Succeded",Toast.LENGTH_SHORT).show();
                }else {
                    Log.w(TAG,"Sign In Failed",task.getException());
                    Toast.makeText(MainActivity.this,"Sign in failed",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public void signInToGoogle(){
        Intent signIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent,RC_SIGN_IN);
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"Signed out",Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void revokeAccess(){
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.w(TAG,"Revoked Access");
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int i=v.getId();
        if (i==R.id.sign_in_button){
            signInToGoogle();
        }else if (i== R.id.signOutButton){
            signOut();
        }else if (i== R.id.disconnectButton){
            revokeAccess();
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}