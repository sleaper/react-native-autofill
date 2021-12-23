package com.testmodule;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.app.assist.AssistStructure;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;

import javax.annotation.Nonnull;

import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static android.content.Context.MODE_PRIVATE;
import static android.os.Process.myUid;

public class AccessModule extends ReactContextBaseJavaModule   {

    private static JSONArray mReadableArray;
    private static ReactApplicationContext reactContext;
    private SharedPreferences myPrefs;

    public AccessModule(@Nonnull ReactApplicationContext context) {
        super(context);
        this.reactContext = reactContext;

        if (!isAppOnForeground((this.getReactApplicationContext()))) {
            Intent intent = new Intent(this.getReactApplicationContext(), MyTaskService.class);
            Bundle bundle = new Bundle();

            //bundle.putString("foo", "bar");
            intent.putExtras(bundle);

            this.getReactApplicationContext().startService(intent);
        }

        myPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
    }

    private boolean isAppOnForeground(Context context) {
        /**
         We need to check if app is in foreground otherwise the app will crash.
         http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
         **/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    
    public static JSONArray getReadableArray() {
        return mReadableArray;
    }

    @Override
    public String getName() {
        return "AccessModule";
    }


    public void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
            if (reactContext != null) {
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    @ReactMethod
    public void sendData(ReadableArray readableArray) throws JSONException {
        Log.e("sendedData", readableArray.toString());
        if(readableArray.size() > 0) {
            mReadableArray = convertArrayToJson(readableArray);
            SharedPreferences.Editor e = myPrefs.edit();
            e.putString("data", readableArray.toString()); // add or overwrite someValue
            e.apply(); // this saves to disk and notifies observers
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @ReactMethod
    public void requirePermission() {

//        AppOpsManager appOps = (AppOpsManager) this.getReactApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS, myUid(), this.getReactApplicationContext().getPackageName());
//        Log.e("MODE", String.valueOf(mode));
//
//        if(mode == 3) {
//            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            this.getReactApplicationContext().startActivity(intent);
//        }


//        if (ContextCompat.checkSelfPermission(this.getReactApplicationContext(), Manifest.permission.PACKAGE_USAGE_STATS)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//            Log.e("PERMISSION", "DENIED");
//
//            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            this.getReactApplicationContext().startActivity(intent);
//        }
    }



    private static JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }

    private static JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, convertMapToJson(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, convertArrayToJson(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }

}