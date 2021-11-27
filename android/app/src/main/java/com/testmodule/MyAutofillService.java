package com.testmodule;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.assist.AssistStructure;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyAutofillService extends AutofillService implements NativeModule {

    private static final String TAG = "DebugService";

    private boolean mAuthenticateResponses;
    private boolean mAuthenticateDatasets;
    private int mNumberDatasets;
    private static MainApplication sApplication;
    private static String AutofillWebDomain;
    private static String androidUri;
    private static JSONArray data;
    private  SharedPreferences myPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = (MainApplication) this.getApplication();
    }

    @Override
    public void onConnected() {
        super.onConnected();
        Log.e("OnConnected", "OnConnected");
        
        mAuthenticateResponses = false;
        mAuthenticateDatasets = false;
        mNumberDatasets = 1;


        myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        try {
            data = new JSONArray(myPrefs.getString("data", "false"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!isAppOnForeground((this))) {
            Intent intent = new Intent(this, MyTaskService.class);
            Bundle bundle = new Bundle();

            bundle.putString("foo", "bar");
            intent.putExtras(bundle);

            this.startService(intent);
        }

        sendEvent("onConnected");

    }

    private String retriveNewApp() {
        if (Build.VERSION.SDK_INT >= 21) {
            String currentApp = null;
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
            Log.e(TAG, "Current App in foreground is: " + currentApp);

            return currentApp;

        } else {

            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String mm=(manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            Log.e(TAG, "Current App in foreground is: " + mm);
            return mm;

        }
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



    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal,
                              FillCallback callback) {
        Log.e("TESTTESTSTEST", data.toString());
        //For now we are using sharedPreferences it is not ideal, we should at least encrypt it


        // Find autofillable fields
        AssistStructure structure = getLatestAssistStructure(request);

        ArrayMap<String, AutofillId> fields = null;
        try {
            fields = getAutofillableFields(structure);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "autofillable fields:" + fields);


        //GEt here name of the application
        // Search data and take by androidUri

        if (fields.isEmpty()) {
            toast("No autofill hints found");
            callback.onSuccess(null);
            return;
        }


        // Create response...
        FillResponse response;
        if (mAuthenticateResponses) {
            int size = fields.size();
            String[] hints = new String[size];
            AutofillId[] ids = new AutofillId[size];
            for (int i = 0; i < size; i++) {
                hints[i] = fields.keyAt(i);
                ids[i] = fields.valueAt(i);
            }

            IntentSender authentication = AuthActivity.newIntentSenderForResponse(this, hints,
                    ids, mAuthenticateDatasets);
            RemoteViews presentation = newDatasetPresentation(getPackageName(),
                    "Tap to auth response");

            response = new FillResponse.Builder()
                    .setAuthentication(ids, authentication, presentation).build();
        } else {

            try {
                response = createResponse(this, fields, mNumberDatasets, mAuthenticateDatasets);
            } catch (JSONException e) {
                response = null;
                e.printStackTrace();
            }

        }

        // ... and return it
        callback.onSuccess(response);
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        // Get the structure from the request
        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Traverse the structure looking for data to save
        ArrayMap<String, AutofillId> fields = null;
        try {
            fields = getAutofillableFields(structure);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("OnSaveRequest", fields.toString());

        // Persist the data, if there are no errors, call onSuccess()
        callback.onSuccess();
    }

    /**
     * Parses the {@link AssistStructure} representing the activity being autofilled, and returns a
     * map of autofillable fields (represented by their autofill ids) mapped by the hint associate
     * with them.
     *
     * <p>An autofillable field is a {@link AssistStructure.ViewNode} whose {@link #getHint(AssistStructure.ViewNode)} metho
     */
    @NonNull
    private ArrayMap<String, AutofillId> getAutofillableFields(@NonNull AssistStructure structure) throws JSONException {
        ArrayMap<String, AutofillId> fields = new ArrayMap<>();
        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.ViewNode node = structure.getWindowNodeAt(i).getRootViewNode();
            addAutofillableFields(fields, node);
        }
        return fields;
    }

    /**
     * Adds any autofillable view from the {@link AssistStructure.ViewNode} and its descendants to the map.
     */

    private void addAutofillableFields(@NonNull Map<String, AutofillId> fields,
                                       @NonNull AssistStructure.ViewNode node) throws JSONException {
        String hint = getHint(node);

        if (hint != null) {
            AutofillId id = node.getAutofillId();

//            for (int i = 0; i < data.length(); i++) {
//                JSONObject item = data.getJSONObject(i);
//                if(hint.equals(item.getString("usernameHint")) || hint.equals(item.getString("passwordHint"))) {
//                    fields.put(hint, id);
//                    Log.d(TAG, "Setting hint '" + hint + "' on " + id);
//                } else {
//                    Log.d(TAG, "Ignoring hint '" + hint + "' on " + id
//                            + " because we dont have it in db");
//                }
//            }

            if (!fields.containsKey(hint)) {
                Log.v(TAG, "Setting hint '" + hint + "' on " + id);
                fields.put(hint, id);
            } else {
                Log.v(TAG, "Ignoring hint '" + hint + "' on " + id
                        + " because it was already set");
            }



            // if is node from web safe webDomain
            StringBuilder webDomainBuilder = new StringBuilder();
            parseWebDomain(node, webDomainBuilder);
            String webDomain = webDomainBuilder.toString();
            if(webDomain.length() > 0) {
                Log.e("SETTER", "setting Domain" + " " + webDomain);
                androidUri = webDomain;
            } else {
                androidUri = retriveNewApp();
                Log.e("InAPP", retriveNewApp());
            }
        }

        int childrenSize = node.getChildCount();
        for (int i = 0; i < childrenSize; i++) {
            addAutofillableFields(fields, node.getChildAt(i));
        }
    }

    private void parseWebDomain(AssistStructure.ViewNode viewNode, StringBuilder validWebDomain) {
        String webDomain = viewNode.getWebDomain();
        if (webDomain != null) {
            if (validWebDomain.length() > 0) {
                if (!webDomain.equals(validWebDomain.toString())) {
                    throw new SecurityException("Found multiple web domains: valid= "
                            + validWebDomain + ", child=" + webDomain);
                }
            } else {
                validWebDomain.append(webDomain);
            }
        }
    }

    @Nullable
    protected String getHint(@NonNull AssistStructure.ViewNode node) {

        // Compare viewHints
        //If were any found return them ???
        // safe viewHints in onSafeRequest

        // First try the explicit autofill hints...
        String[] hints = node.getAutofillHints();
        if (hints != null) {
            // We're simple, we only care about the first hint
            return hints[0].toLowerCase();
        }

        // Then try some rudimentary heuristics based on other node properties

        String viewHint = node.getHint();

        String hint = inferHint(node, viewHint);

        if (hint != null) {
            Log.d(TAG, "Found hint using view hint(" + viewHint + "): " + hint);
            return hint;
        } else if (!TextUtils.isEmpty(viewHint)) {
            Log.v(TAG, "No hint using view hint: " + viewHint);
        }

        String resourceId = node.getIdEntry();
        hint = inferHint(node, resourceId);
        if (hint != null) {
            Log.d(TAG, "Found hint using resourceId(" + resourceId + "): " + hint);
            return hint;
        } else if (!TextUtils.isEmpty(resourceId)) {
            Log.v(TAG, "No hint using resourceId: " + resourceId);
        }

        CharSequence text = node.getText();
        CharSequence className = node.getClassName();
        if (text != null && className != null && className.toString().contains("EditText")) {
            hint = inferHint(node, text.toString());
            if (hint != null) {
                // NODE: text should not be logged, as it could contain PII
                Log.d(TAG, "Found hint using text(" + text + "): " + hint);
                return hint;
            }
        } else if (!TextUtils.isEmpty(text)) {
            // NODE: text should not be logged, as it could contain PII
            Log.v(TAG, "No hint using text: " + text + " and class " + className);
        }
        return null;
    }

    /**
     * Uses heuristics to infer an autofill hint from a {@code string}.
     * (Searching for some default hints like password or username)
     * @return standard autofill hint, or {@code null} when it could not be inferred.
     */
    @Nullable
    protected static String inferHint(AssistStructure.ViewNode node, @Nullable String actualHint) {
        if (actualHint == null) return null;

        String hint = actualHint.toLowerCase();
        if (hint.contains("label") || hint.contains("container")) {
            Log.v(TAG, "Ignoring 'label/container' hint: " + hint);
            return null;
        }

        if (hint.contains("password")) return View.AUTOFILL_HINT_PASSWORD;
        if (hint.contains("username")
                || (hint.contains("login") && hint.contains("id")))
            return View.AUTOFILL_HINT_USERNAME;
        if (hint.contains("email")) return View.AUTOFILL_HINT_EMAIL_ADDRESS;
        if (hint.contains("name")) return View.AUTOFILL_HINT_NAME;
        if (hint.contains("phone")) return View.AUTOFILL_HINT_PHONE;

        // When everything else fails, return the full string - this is helpful to help app
        // developers visualize when autofill is triggered when it shouldn't (for example, in a
        // chat conversation window), so they can mark the root view of such activities with
        // android:importantForAutofill=noExcludeDescendants
        if (node.isEnabled() && node.getAutofillType() != View.AUTOFILL_TYPE_NONE) {
            Log.v(TAG, "Falling back to " + actualHint);
            return actualHint;
        }
        return null;
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.P)
    static FillResponse createResponse(@NonNull Context context,
                                       @NonNull ArrayMap<String, AutofillId> fields, int numDatasets,
                                       boolean authenticateDatasets) throws JSONException {

        boolean hasUri = false;

        for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                if(item.getString("androidUri").equals(androidUri)) {
                    Log.e("TEST", androidUri + " " + item.getString("androidUri"));
                    hasUri = true;
                }
        }

        String packageName = context.getPackageName();
        FillResponse.Builder response = new FillResponse.Builder();

        //Check for the same android Uri in data
        if(hasUri) {
            Log.e("HAS", "HAS");
            Dataset unlockedDataset = newUnlockedDataset(fields, packageName);
            response.addDataset(unlockedDataset);

        } else {
            Log.e("NOT", fields.toString());
            Dataset emptyDataset = newEmptyDataset(fields, packageName);
            response.addDataset(emptyDataset);
        }


//        Dataset unlockedDataset = newUnlockedDataset(fields, packageName);
//        response.addDataset(unlockedDataset);

        //Here we must remove the first view bcs it is the browser search input
        Collection<AutofillId> ids = fields.values();
        AutofillId[] requiredIds = new AutofillId[ids.size()];
        ids.toArray(requiredIds);
        response.setSaveInfo(
                // We're simple, so we're generic
                new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_GENERIC , requiredIds).setTriggerId(fields.get("password")).build());

        // 3.Profit!

        return response.build();
    }



    static Dataset newUnlockedDataset(@NonNull Map<String, AutofillId> fields,
                                      @NonNull String packageName) throws JSONException {
         Dataset.Builder dataset = new Dataset.Builder();

         for (Map.Entry<String, AutofillId> field : fields.entrySet()) {
             String hint = field.getKey();
             AutofillId id = field.getValue();


             String value = null;
             String username = null;

             for (int i = 0; i < data.length(); i++) {
                 JSONObject item = data.getJSONObject(i);
                 username = item.getString("username");
                 if(hint.equals(item.getString("usernameHint"))) {
                     value = item.getString("username");
                     //Finish hte filling here
                     //String displayValue = username;
                     RemoteViews presentation = newDatasetPresentation(packageName, username);
                     dataset.setValue(id, AutofillValue.forText(value), presentation);
                 } else if(hint.equals(item.getString("passwordHint"))) {
                     value = item.getString("password");
                     //Finish hte filling here
                     //String displayValue = username;
                     RemoteViews presentation = newDatasetPresentation(packageName, username);
                     dataset.setValue(id, AutofillValue.forText(value), presentation);
                 }
             }

//             //Finish hte filling here
//             //String displayValue = username;
//             RemoteViews presentation = newDatasetPresentation(packageName, username);
//             dataset.setValue(id, AutofillValue.forText(value), presentation);
         }

         return dataset.build();
    }

    static Dataset newEmptyDataset(@NonNull Map<String, AutofillId> fields,
                                      @NonNull String packageName) {
        Dataset.Builder dataset = new Dataset.Builder();

        // Builder object requires a non-null presentation.
        RemoteViews notUsed = new RemoteViews(packageName, android.R.layout.simple_list_item_1);

        for (Map.Entry<String, AutofillId> field : fields.entrySet()) {
            AutofillId id = field.getValue();

            dataset.setValue(id, null, notUsed);
        }

        return dataset.build();
    }


    void sendEvent(String evetName) {
        ReactNativeHost reactNativeHost = sApplication.getReactNativeHost();
        ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

        if (reactContext != null) {
            WritableMap params = Arguments.createMap();
            params.putString(evetName, "true");
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(evetName, params);
        }
    }

    /**
     * Displays a toast with the given message.
     */
    private void toast(@NonNull CharSequence message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Helper method to get the {@link AssistStructure} associated with the latest request
     * in an autofill context.
     */
    @NonNull
    static AssistStructure getLatestAssistStructure(@NonNull FillRequest request) {
        List<FillContext> fillContexts = request.getFillContexts();
        return fillContexts.get(fillContexts.size() - 1).getStructure();
    }

    /**
     * Helper method to create a dataset presentation with the given text.
     */
    @NonNull
    static RemoteViews newDatasetPresentation(@NonNull String packageName,
                                              @NonNull CharSequence text) {
        RemoteViews presentation =
                new RemoteViews(packageName, R.layout.multidataset_service_list_item);
        presentation.setTextViewText(R.id.text, text);
        presentation.setImageViewResource(R.id.icon, R.mipmap.ic_launcher);
        return presentation;
    }

    @NonNull
    @Override
    public String getName() {
        return "MyAutofillService";
    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean canOverrideExistingModule() {
        return false;
    }

    @Override
    public void onCatalystInstanceDestroy() {

    }

    @Override
    public void invalidate() {

    }
}


