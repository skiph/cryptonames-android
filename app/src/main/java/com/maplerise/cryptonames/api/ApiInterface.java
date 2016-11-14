package com.maplerise.cryptonames.api;

import com.maplerise.cryptonames.model.NamesResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("names")
    Call<NamesResponse> getNames(@Query("tag") String tag);
}
