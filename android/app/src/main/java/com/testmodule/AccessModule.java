package com.testmodule;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.content.Context;
import android.content.SharedPreferences;

import android.app.assist.AssistStructure;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class AccessModule extends ReactContextBaseJavaModule {

    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;

    public AccessModule(ReactApplicationContext context) {
        super(context);

        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public String getName() {
        return "AccessModule";
    }

    @ReactMethod
    public void saveItem(String key, String value) {

        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString(key, value);
        editor.commit();

        Log.e("SAVED", "SAVED");
    }

    @ReactMethod
    public void getItem(String key,  Promise promise) {


        try {
            String value = sharedpreferences.getString(key, "not Found");
            promise.resolve(value);
        } catch(Exception e) {
            promise.reject("Create Event Error", e);
        }
    }



}