package com.testmodule;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class MainActivity extends ReactActivity {
  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */

// FOR POPUP
//    private static final String EXTRA_HINTS = "hints";
//    private static final String EXTRA_URI = "androidUri";
//    private static Bundle mInitialProps = null;
//
//      private static
//      @Nullable
//      Activity mActivity;
//
//  public static class RNTesterActivityDelegate extends ReactActivityDelegate {
//    public RNTesterActivityDelegate(ReactActivity activity, String mainComponentName) {
//      super(activity, mainComponentName);
//      mActivity = activity;
//    }
//
//  @Override
//    protected void onCreate(Bundle savedInstanceState) {
//      Bundle bundle = mActivity.getIntent().getExtras();
//
//      if (bundle != null) {
//        mInitialProps = new Bundle();
//        if(bundle.containsKey("username")) {
//          mInitialProps.putString("username", bundle.getString("username"));
//          mInitialProps.putString("password", bundle.getString("password"));
//          mInitialProps.putString(EXTRA_URI, bundle.getString(EXTRA_URI));
//        }
////        else if(bundle.containsKey(EXTRA_URI)) {
////          mInitialProps.putString(EXTRA_URI, bundle.getString(EXTRA_URI));
////        } else if(bundle.containsKey("password")) {
////          mInitialProps.putString("password", bundle.getString("password"));
////        }
//
//      }
//      super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    protected Bundle getLaunchOptions() {
//      return mInitialProps;
//    }
//  }
//
//
//
//  @Override
//  protected ReactActivityDelegate createReactActivityDelegate() {
//    return new RNTesterActivityDelegate(this, getMainComponentName());
//  }


  @Override
  protected String getMainComponentName() {
    return "testModule";
  }
}
