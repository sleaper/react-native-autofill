package com.testmodule;

import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.NativeModule;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyAutofillService extends AutofillService implements NativeModule {

    public MyAutofillService() {
        Log.e("CONSTUCTOR", "CALEED");
    }


    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        Log.e("LOL onFillRequest", "LOL onFillRequest");

    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        Log.e("LOL onFillRequest", "LOL onFillRequest");

    }


    public void getInlineSuggestionsRequest () {
        Log.e("TEST", "InlineSuggestionsRequest");
        return;
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


