package com.testmodule.util;

import android.app.Activity;
import android.os.Bundle;

import com.testmodule.R;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity);
    }
}