package com.testmodule;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.service.autofill.AutofillService;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MyAutofillService extends AutofillService {

    public MyAutofillService() {
        Log.e("CONSTUCTOR", "CALEED");
    }


    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
        Log.e("LOL", "LOL");

    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {

    }
}


