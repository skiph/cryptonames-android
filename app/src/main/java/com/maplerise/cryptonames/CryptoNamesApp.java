package com.maplerise.cryptonames;

import android.app.Application;
import android.util.Log;

import com.maplerise.cryptonames.api.ApiClient;
import com.maplerise.cryptonames.auth.Attestor;

public class CryptoNamesApp extends Application {
    private static final String TAG = CryptoNamesApp.class.getSimpleName();

    private Attestor attestor;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "starting CryptoName application");

        // create attestor for this app context

        attestor = new Attestor(this);

        // associate with the API client

        ApiClient.setAttestor(attestor);
    }

    public Attestor getAttestor(){
        return attestor;
    }
}

// end of file
