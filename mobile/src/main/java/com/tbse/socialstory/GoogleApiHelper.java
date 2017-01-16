package com.tbse.socialstory;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by alexweav on 11/2/16.
 */

public class GoogleApiHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    OnGoogleApiConnectedListener googleAPIListener;

    public interface OnGoogleApiConnectedListener{
        void onGoogleApiConnected(GoogleApiClient googleApiClient);
    }

    public GoogleApiHelper(GoogleSignInOptions options, Context context,
                           OnGoogleApiConnectedListener googleAPIListener) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();
        this.googleAPIListener = googleAPIListener;
        connect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return this.mGoogleApiClient;
    }

    public void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        if (mGoogleApiClient != null && isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isConnected() {
        if (mGoogleApiClient != null) {
            return mGoogleApiClient.isConnected();
        } else {
            return false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(MainActivity.TAG, "Google API onConnected, isConnected? "
                + mGoogleApiClient.isConnected());
        googleAPIListener.onGoogleApiConnected(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(MainActivity.TAG, "Google API onConnectionFailed");
    }
}
