package com.testmodule;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.assist.AssistStructure;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyAutofillService extends AutofillService implements NativeModule {

    private static final String TAG = "DebugService";

    private boolean mAuthenticateResponses;
    private boolean mAuthenticateDatasets;
    private int mNumberDatasets;
    private static MainApplication sApplication;
    private static String androidUri;
    private static JSONArray data;
    private SharedPreferences myPrefs;
    private static int _pendingIntentId = 0;
    private static final String EXTRA_URI = "androidUri";
    private int nodeCount = 1;

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
            data = new JSONArray(myPrefs.getString("data", "[]"));
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

        sendEvent("onConnected", "true");

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


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal,
                              FillCallback callback) {
        //For now we are using sharedPreferences it is not ideal, we should at least encrypt it
        // Find autofillable fields
        AssistStructure structure = getLatestAssistStructure(request);
        ArrayMap<String, AutofillId> fields = getAutofillableFields(structure);

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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        // Get the structure from the request
        List<FillContext> context = request.getFillContexts();

        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Traverse the structure looking for data to save // get here JSONObject??
        Object fields = null;
        try {
            fields = getSavaFields(structure);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //TODO
        // wrong formating of the object
        // fuck popup and web pages
        // Remove sending data on event OnConnect
        // OnConnect heck data in prefStorage if null == send def from react native, if == something return prefStorage to react native



        data.put(fields);
        Log.e("OnSaveRequest", data.toString());

        SharedPreferences.Editor e = myPrefs.edit();
        e.putString("data", data.toString());
        e.apply();

        sendEvent("pswSaved", data.toString());

        // Persist the data, if there are no errors, call onSuccess()
        callback.onSuccess();
    }

    /**
     * Parses the {@link AssistStructure} representing the activity being autofilled, and returns a
     * map of autofillable fields (represented by their autofill ids) mapped by the hint associate
     * with them.
     *
     * <p>An autofillable field is a {@link AssistStructure.ViewNode} whose {@link #getHint(AssistStructure.ViewNode)} metho
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    @NonNull
    private ArrayMap<String, AutofillId> getAutofillableFields(@NonNull AssistStructure structure) {
        ArrayMap<String, AutofillId> fields = new ArrayMap<>();

        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.ViewNode node = structure.getWindowNodeAt(i).getRootViewNode();
            addAutofillableFields(fields, node);

        }

       return fields;
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @NonNull
    private Object getSavaFields(@NonNull AssistStructure structure) throws JSONException {
        JSONObject jsonObj= new JSONObject();

        int nodes = structure.getWindowNodeCount();
        for (int i = 0; i < nodes; i++) {
            AssistStructure.ViewNode node = structure.getWindowNodeAt(i).getRootViewNode();
            addFieldsToSave(node, jsonObj);
        }

        return jsonObj;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void addFieldsToSave(@NonNull AssistStructure.ViewNode node, JSONObject obj) throws JSONException {
        String androidUriToSave;
        String hint = getHint(node);

        if (hint != null) {
            AutofillId id = node.getAutofillId();


            StringBuilder webDomainBuilder = new StringBuilder();
            parseWebDomain(node, webDomainBuilder);
            String webDomain = webDomainBuilder.toString();
            if(webDomain.length() > 0) {
                androidUriToSave = webDomain;

                obj.put("androidUri", androidUriToSave);
                if(node.getInputType() == 225 || node.getInputType() == 224) {
                    obj.put("passwordHint", hint);
                    obj.put("password", node.getText());
                } else if(node.getInputType() == 1 && node.getText() != null){
                    obj.put("usernameHint", hint);
                    obj.put("username", node.getText());
                }

            } else {
                androidUriToSave = retriveNewApp();

                obj.put("androidUri", androidUriToSave);
                if(nodeCount == 1) {
                    obj.put("usernameHint", hint);
                    obj.put("username", node.getText());
                } else if(nodeCount == 2){
                    obj.put("passwordHint", hint);
                    obj.put("password", node.getText());
                }

                nodeCount = nodeCount + 1;
            }

        }

        int childrenSize = node.getChildCount();
        for (int i = 0; i < childrenSize; i++) {
            addFieldsToSave( node.getChildAt(i), obj);
        }
    }

    /**
     * Adds any autofillable view from the {@link AssistStructure.ViewNode} and its descendants to the map.
     */

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void addAutofillableFields(@NonNull Map<String, AutofillId> fields,
                                       @NonNull AssistStructure.ViewNode node) {
        String hint = getHint(node);

        if (hint != null) {
            AutofillId id = node.getAutofillId();

            // if is node from web save webDomain
            // If is node from web save by getInputType
            StringBuilder webDomainBuilder = new StringBuilder();
            parseWebDomain(node, webDomainBuilder);
            String webDomain = webDomainBuilder.toString();
//            if(webDomain.length() > 0) {
//                androidUri = webDomain;
//
//                if(node.getInputType() == 225 || node.getInputType() == 224 || node.getInputType() == 1) {
//                    Log.v(TAG, "Setting node.getInputType '" + node.getInputType());
//                    fields.put(hint, id);
//                }
//
//            } else {
                androidUri = retriveNewApp();
                Log.e("PLS", androidUri);
                if (!fields.containsKey(hint)) {
                    Log.v(TAG, "Setting hint '" + hint + "' on " + id);
                    fields.put(hint, id);
                } else {
                    Log.v(TAG, "Ignoring hint '" + hint + "' on " + id
                            + " because it was already set");
                }

            //}

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

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Nullable
    protected String getHint(@NonNull AssistStructure.ViewNode node) {

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

    @RequiresApi(api = Build.VERSION_CODES.P)
    static FillResponse createResponse(@NonNull Context context,
                                       @NonNull ArrayMap<String, AutofillId> fields, int numDatasets,
                                       boolean authenticateDatasets) throws JSONException {

        String packageName = context.getPackageName();
        FillResponse.Builder response = new FillResponse.Builder();

        Log.e("DATA", data.toString());
        if(data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                if(item.getString("androidUri").equals(androidUri)) {
                    //Log.e("TEST", androidUri + " " + item.getString("androidUri"));
                    //hasUri = true;
                    Dataset unlockedDataset = fieldDataset(fields, packageName, item);
                    response.addDataset(unlockedDataset);
                }
            }
        }

        Collection<AutofillId> ids = fields.values();
        AutofillId[] requiredIds = new AutofillId[ids.size()];
        ids.toArray(requiredIds);
        response.setSaveInfo(
                // We're simple, so we're generic
                new SaveInfo.Builder(SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD, requiredIds).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build());


        // 3.Profit!
        return response.build();
    }



    static Dataset fieldDataset(@NonNull Map<String, AutofillId> fields,
                                      @NonNull String packageName, JSONObject item) throws JSONException {
        Dataset.Builder dataset = new Dataset.Builder();

         for (Map.Entry<String, AutofillId> field : fields.entrySet()) {
             String hint = field.getKey();
             AutofillId id = field.getValue();



             if(hint.equals(item.getString("usernameHint"))) {
                 //Finish hte filling here
                 //String displayValue = username;
                 RemoteViews presentation = newDatasetPresentation(packageName, item.getString("username"));
                 dataset.setValue(id, AutofillValue.forText(item.getString("username")), presentation);
             } else if(hint.equals(item.getString("passwordHint"))) {
                 //Finish hte filling here
                 //String displayValue = username;
                 RemoteViews presentation = newDatasetPresentation(packageName, item.getString("username"));
                 dataset.setValue(id, AutofillValue.forText(item.getString("password")), presentation);
             }


//            //Finish hte filling here
//            //String displayValue = username;
//            RemoteViews presentation = newDatasetPresentation(packageName, username);
//            dataset.setValue(id, AutofillValue.forText(value), presentation);
         }

         return dataset.build();
    }

    static Dataset vaultDataset(@NonNull ArrayMap<String, AutofillId> fields,
                                @NonNull String packageName, Context context) {
        Dataset.Builder dataset = new Dataset.Builder();

        Intent intent = new Intent(context, MainActivity.class);
        Log.e("FIELDS", fields.toString());
        int size = fields.size();
        String[] hints = new String[size];
        AutofillId[] ids = new AutofillId[size];
        for (int i = 0; i < size; i++) {
            hints[i] = fields.keyAt(i);
            ids[i] = fields.valueAt(i);
        }

        Log.e("TEST", androidUri);
        Log.e("TEST", Arrays.toString(hints));

        intent.putExtra("username", hints[0]);
        intent.putExtra("password", hints[1]);
        intent.putExtra(EXTRA_URI, androidUri);


        for (Map.Entry<String, AutofillId> field : fields.entrySet()) {
            AutofillId id = field.getValue();

            IntentSender pendingIntent = PendingIntent.getActivity(context, ++_pendingIntentId, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT).getIntentSender();

            dataset.setAuthentication(pendingIntent);

            RemoteViews presentation = BuildOverlayPresentation("test", "test", packageName);
            dataset.setValue(id, null, presentation);
        }

        return dataset.build();
    }

    public static RemoteViews BuildOverlayPresentation(String text, String subtext, String packageName)
    {
        RemoteViews view = new RemoteViews(packageName, R.layout.autofill_listitem);
        view.setTextViewText(R.id.text1, text);
        view.setTextViewText(R.id.text2, subtext);
        //view.setImageViewResource(R.id.icon, iconId);
        return view;
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


    void sendEvent(String evetName, String data) {
        ReactNativeHost reactNativeHost = sApplication.getReactNativeHost();
        ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

        if (reactContext != null) {
            WritableMap params = Arguments.createMap();
            params.putString(evetName, data);
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


