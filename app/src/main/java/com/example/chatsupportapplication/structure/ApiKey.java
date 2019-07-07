package com.example.chatsupportapplication.structure;

import android.util.Log;

import com.applozic.mobicommons.json.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

public class ApiKey implements Serializable {
    private String apiKey;

    private String appid;

    public ApiKey(String apiKey, String appid) {
        this.apiKey = apiKey;
        this.appid = appid;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }
}
