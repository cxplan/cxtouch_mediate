package com.cxplan.common.util;

import android.util.Log;

public class LogUtil {
    private static final String TAG = "LogUtil";

    private LogUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = true;

    // Default tag
    public static void i(String msg) {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg) {
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void w(String msg) {
        if (isDebug)
            Log.w(TAG, msg);
    }

    public static void e(String msg) {
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg) {
        if (isDebug)
            Log.v(TAG, msg);
    }

    public static void i(String tag, String msg) {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (isDebug)
            Log.d(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (isDebug)
            Log.w(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (isDebug)
            if (msg.length() > 4000) {
                for (int i = 0; i < msg.length(); i += 4000) {
                    if (i + 4000 < msg.length())
                        LogUtil.e(tag + i, msg.substring(i, i + 4000));
                    else
                        LogUtil.e(tag + i, msg.substring(i, msg.length()));
                }
            } else {
                Log.e(tag, msg);
            }
    }
    public static void e(String msg, Throwable e) {
        if (isDebug)
            Log.e(TAG, msg, e);
    }
    public static void e(String tag, String msg, Throwable e) {
        if (isDebug)
            Log.e(tag, msg, e);
    }

    public static void v(String tag, String msg) {
        if (isDebug)
            Log.v(tag, msg);
    }
}
