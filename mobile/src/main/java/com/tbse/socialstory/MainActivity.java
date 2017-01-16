package com.tbse.socialstory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.tbse.socialstory.dummy.DummyContent;
import com.tbse.socialstory.fragments.LoginFragment;
import com.tbse.socialstory.fragments.StoryFragment;

import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity
        implements StoryFragment.OnListFragmentInteractionListener,
        LoginFragment.OnFragmentInteractionListener,
        GoogleApiHelper.OnGoogleApiConnectedListener {


    public static final String TAG = "ss";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Fragment loginFragment = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            @DebugLog
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    navigateToStoryFragment();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    firebaseAuth.signOut();
                    navigateToLoginFragment();
                }
                // ...
            }
        };

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Share stories with friends or the public! Coming Soon...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        navigateToLoginFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_logout) {
            Log.d("ss", "Firebase signOut");
            FirebaseAuth.getInstance().signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @DebugLog
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == LoginFragment.RC_SIGN_IN) {
            final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    @DebugLog
    public void onGoogleApiConnected(GoogleApiClient googleApiClient) {
        if (googleApiClient.isConnected()) {
            navigateToStoryFragment();
        } else {
            googleApiClient.disconnect();
            navigateToLoginFragment();
        }
    }

    @DebugLog
    private void handleSignInResult(final GoogleSignInResult result) {
        if (result.isSuccess()) {
            Log.d(TAG, " - is success");
            // Google Sign In was successful, authenticate with Firebase
            final GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
            navigateToStoryFragment();
        } else {
            Log.d(TAG, " - is failure");
            Log.d(TAG, "msg: " + result.getStatus());
            // Google Sign In failed, update UI appropriately
            // ...
        }
    }

    @DebugLog
    private void navigateToLoginFragment() {
        if (loginFragment == null) {
            loginFragment = LoginFragment.newInstance("1", "2");

            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack("LoginFragment")
                    .add(R.id.main_frame_layout, loginFragment, "LoginFragment")
                    .commit();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @DebugLog
    private void navigateToStoryFragment() {
        Fragment storyFragment = StoryFragment.newInstance(1);

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("StoryFragment")
                .replace(R.id.main_frame_layout, storyFragment, "StoryFragment")
                .commit();
    }

    @DebugLog
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    @DebugLog
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
