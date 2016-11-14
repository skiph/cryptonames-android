package com.maplerise.cryptonames.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NamesResponse {
    @SerializedName("names")
    private List<String> results;

    @SerializedName("tag")
    private String tag;

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public String getTag() {
        return tag;
    }

        public void setTag(String tag) {
        this.tag = tag;
    }
}
