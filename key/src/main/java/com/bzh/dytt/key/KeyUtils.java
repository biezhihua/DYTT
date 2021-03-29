package com.bzh.dytt.key;

import android.content.Context;
import android.util.Log;

public class KeyUtils {

    private static final String TAG = "KeyUtils";

    private static Context sContext;

    static {
        System.loadLibrary("key-lib");
    }

    private static native String getKey(Context context, String currentTime, String imei);

    public static String getHeaderKey(long currentTime) {
        if (sContext != null) {
            String key = getKey(sContext, String.valueOf(currentTime), "");
            Log.d(TAG, "getHeaderKey: key " + key);
            return key;
        }
        Log.d(TAG, "getHeaderKey: sContext is null");
        return "";
    }

    public static void init(Context context) {
        if (context != null) {
            sContext = context.getApplicationContext();
        }
    }
}
