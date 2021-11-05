package com.testmodule;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.ArrayList;
import java.util.List;

import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.autofill.AutofillId;

import androidx.annotation.RequiresApi;

public class AccessModule extends ReactContextBaseJavaModule {
    public AccessModule(ReactApplicationContext context) {
        super(context);
    }

    public boolean test = false;

    @Override
    public String getName() {
        return "AccessModule";
    }

    @ReactMethod
    public void createAccessModule(String name, String location) {
        //Log.e("TEST", String.valueOf(test));

    }

    
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onFillRequest(AssistStructure assistStructure, Bundle bundle, CancellationSignal cancellationSignal) {
        Log.e("LOOL", String.valueOf(test));
       List<AssistStructure.ViewNode> emailFields = new ArrayList<>();

       identifyEmailField(assistStructure.getWindowNodeAt(0).getRootViewNode(), emailFields);
       if(emailFields.size() == 0) {
           return;
       }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void identifyEmailField(AssistStructure.ViewNode node, List<AssistStructure.ViewNode> emailFields) {
        if(node.getClassName().contains("EditText")) {
            String viewId = node.getIdEntry();
            if(viewId != null && (viewId.contains("email") || viewId.contains("username"))) {
                emailFields.add(node);
                return;
            }
        }
        for(int i = 0; i<node.getChildCount(); i++) {
            identifyEmailField(node.getChildAt(i), emailFields);
        }
    }



    class ParsedStructure {
        AutofillId usernameId;
        AutofillId passwordId;
    }

    class UserData {
        String username;
        String password;
    }





}