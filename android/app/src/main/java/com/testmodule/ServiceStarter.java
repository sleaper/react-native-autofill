package com.testmodule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class ServiceStarter extends BroadcastReceiver {
    public void onReceive(Context context, Intent arg1)
    {
        Intent service = new Intent(context, MyTaskService.class);
        Bundle bundle = new Bundle();

        bundle.putString("foo", "bar");
        service.putExtras(bundle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
        Log.i("Autostart", "started");
    }
}
