package com.maplerise.cryptonames.api;

import android.util.Log;

import com.maplerise.cryptonames.auth.Attestor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = ApiClient.class.getSimpleName();

    public static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getOldClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    //private static String AUTH = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MDg5OTAzOTAsImlhdCI6MTQ3NzM2Nzk5MH0.1WJ1pESMESxNfzx2je_nqKJ8VRiyoW_MmgJ4j5OAsis";

    private static Attestor attestor;

    public static void setAttestor(Attestor attestor) {
        ApiClient.attestor = attestor;
    }

    // add activity to getClient, and use activity to get app to get latest token

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    String token = attestor.getToken();
                    Log.d(TAG, "token = " + token);

                    Request.Builder requestBuilder = original.newBuilder();

                    // add auth token if available

                    if (token != null) {
                        requestBuilder.header("auth", token);
                    }

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            OkHttpClient client = httpClient.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
