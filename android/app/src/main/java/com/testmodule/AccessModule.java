package com.testmodule;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.app.assist.AssistStructure;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class AccessModule<LocalBroadcastReceiver> extends ReactContextBaseJavaModule {

    SharedPreferences sharedpreferences;

    private ReactContext mReactContext;
    private LocalBroadcastReceiver mLocalBroadcastReceiver;


    public static final String MyPREFERENCES = "MyPrefs" ;

    public AccessModule(ReactApplicationContext context) {
        super(context);
        this.mReactContext = context;
        Log.e("CONTEXT", mReactContext.toString());

        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public AccessModule() {

    }


    @Override
    public String getName() {
        return "AccessModule";
    }


    public void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {

        if(reactContext != null) {
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        } else {
                Log.e("NONE", "NONE");
            }
        }
    @ReactMethod
    public void addListener(String eventName) {
        // Set up any upstream listeners or background tasks as necessary

    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Remove upstream listeners, stop unnecessary background tasks
    }

}