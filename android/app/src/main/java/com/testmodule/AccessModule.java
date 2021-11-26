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

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.app.assist.AssistStructure;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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

import static android.content.Context.MODE_PRIVATE;

public class AccessModule extends ReactContextBaseJavaModule   {

    private static JSONArray mReadableArray;
    private static ReactApplicationContext reactContext;
    private  SharedPreferences myPrefs;

    public AccessModule(@Nonnull ReactApplicationContext context) {
        super(context);
        this.reactContext = reactContext;

//        WritableMap params = Arguments.createMap();
//        params.putString("onConnect", "true");
//        sendEvent(reactContext, "onConnect", params);

        if (!isAppOnForeground((this.getReactApplicationContext()))) {
            Intent intent = new Intent(this.getReactApplicationContext(), MyTaskService.class);
            Bundle bundle = new Bundle();

            bundle.putString("foo", "bar");
            intent.putExtras(bundle);

            this.getReactApplicationContext().startService(intent);
        }

        myPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
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
    public void sendData(ReadableArray readableArray) throws JSONException, IOException {
        mReadableArray = convertArrayToJson(readableArray);
        SharedPreferences.Editor e = myPrefs.edit();
        e.putString("data", readableArray.toString()); // add or overwrite someValue
        e.commit(); // this saves to disk and notifies observers
        
//        for (int i = 0; i < mReadableArray.length(); i++) {
//            Log.e("LOL",  mReadableArray.getJSONObject(i).toString());
//        }
        return;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeToFile(String data) throws IOException {
        String path =
                Environment.getExternalStorageDirectory() + "/" + "testmodule/";
        // Create the folder.
        File dir = new File(path);
        if (!dir.getParentFile().exists())
            dir.getParentFile().mkdirs();
        if (!dir.exists())
            dir.createNewFile();

        // Create the file.
        File file = new File(dir +"/data.txt");
        Log.e("DIR", dir.toString() + " " + file.toString());

        // Save your stream, don't forget to flush() it before closing it.

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
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