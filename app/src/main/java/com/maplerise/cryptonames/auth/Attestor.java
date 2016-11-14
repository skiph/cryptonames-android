package com.maplerise.cryptonames.auth;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Base64;
import android.util.Log;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import com.criticalblue.attestationlibrary.ApproovAttestation;
import com.criticalblue.attestationlibrary.android.AndroidPlatformSpecifics;

public class Attestor {
    private static final String TAG = Attestor.class.getSimpleName();

    public static final int INIT = -1;
    public static final int OFF = 0;
    public static final int INVALID = 1;
    public static final int VALID = 2;
    public static final int EXPIRED = 3;
    public static final int ON = 4;

    private int mode;
    private String token;
    private String key, alt_key;
    private ApproovAttestation attestation;
    private AndroidPlatformSpecifics platformSpecifics;
    private TokenReceiver tokenReceiver;

    public Attestor(Context context) {
        this.mode = Attestor.INIT;
        this.token = "";
        this.key = Base64.encodeToString(MacProvider.generateKey().getEncoded(), Base64.NO_WRAP);
        this.key = "NT/bbTHn+r6xO5GGNwgfjygG7DbqCgyP/QXsWSfyb0rO7b2S/xI78NoNfh2nWvr0h7T5buXQHn3t7bjzpMkP6w==";
        this.alt_key = "";
        this.platformSpecifics = new AndroidPlatformSpecifics(context);
        this.attestation = new ApproovAttestation(platformSpecifics);
        this.tokenReceiver = new TokenReceiver();

        // see this for setting up repeating task -
        // http://stackoverflow.com/questions/18353689/how-to-repeat-a-task-after-a-fixed-amount-of-time-in-android

        setMode(Attestor.ON);
    }

    public void setMode(int mode) {
        if (mode == this.mode) {
            return;
        }

        if (Attestor.OFF <= mode && mode <= Attestor.ON ) {
            this.mode = mode;
        } else {
            this.mode = Attestor.ON;
        }

        refreshToken();
    }

    public int getMode() {
        return mode;
    }

    private String setToken(String token) {
        String oldToken = this.token;
        if (token != null) {
            this.token = token;
        } else {
            this.token = "";
        }

        return this.token;
    }

    private void refreshToken() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        switch (this.mode) {
            case Attestor.OFF:
                this.token = null;
                Log.d(TAG, "no token");
                return;
            case Attestor.VALID:
                calendar.add(Calendar.MONTH, 1);
                this.token = Jwts.builder()
                        .setHeaderParam("typ", "JWT")
                        .setIssuedAt(now)
                        .setExpiration(calendar.getTime())
                        .claim("key", this.key)
                        .signWith(SignatureAlgorithm.HS256, this.key)
                        .compact();
                Log.d(TAG, "valid token = " + token);
                return;
            case Attestor.INVALID:
                calendar.add(Calendar.MONTH, 1);
                this.token = Jwts.builder()
                        .setHeaderParam("typ", "JWT")
                        .setIssuedAt(now)
                        .setExpiration(calendar.getTime())
                        .claim("key", this.alt_key)
                        .signWith(SignatureAlgorithm.HS256, this.key)
                        .compact();
                Log.d(TAG, "invalid token = " + token);
                return;
            case Attestor.EXPIRED:
                calendar.add(Calendar.MONTH, -1);
                this.token = Jwts.builder()
                        .setHeaderParam("typ", "JWT")
                        .setIssuedAt(now)
                        .setExpiration(calendar.getTime())
                        .claim("key", this.key)
                        .signWith(SignatureAlgorithm.HS256, this.key)
                        .compact();
                Log.d(TAG, "expired token = " + token);
                return;
            default:
                attestation.fetchApproovToken();
                // token receiver will update asynchronously
                Log.d(TAG, "attested token = " + token);
                return;
        }
    }

    public String getToken() {
        return token;
    }

    public void pause(Context context) {
        context.unregisterReceiver(tokenReceiver);
    }

    public void resume(Context context) {
        context.registerReceiver(tokenReceiver,
                new IntentFilter("com.criticalblue.approov.TokenUpdate"));

        refreshToken();
    }

    public void register(Context context) {
        context.registerReceiver(tokenReceiver,
                new IntentFilter("com.criticalblue.approov.TokenUpdate"));
        attestation.fetchApproovToken();
    }

    public void unregister(Context context) {
        context.unregisterReceiver(tokenReceiver);
    }

    public final class TokenReceiver extends BroadcastReceiver {
        TokenReceiver() { }

        @Override
        public void onReceive(Context context, Intent intent) {
            String receivedToken;

            Log.w(TAG, "onReceive");

            ApproovAttestation.AttestationResult result =
                    (ApproovAttestation.AttestationResult) intent.getSerializableExtra("Result");

            receivedToken = intent.getStringExtra("Token");

            if (result == ApproovAttestation.AttestationResult.SUCCESS) {
                setToken(receivedToken);
                Log.w(TAG, "Received token: " + receivedToken);
            } else {
                Log.w(TAG, "Received token failure");
            }
        }
    }
}

// end of file
