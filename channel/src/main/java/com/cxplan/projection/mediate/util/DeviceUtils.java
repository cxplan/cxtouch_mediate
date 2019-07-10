package com.cxplan.projection.mediate.util;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.Constant;

import java.util.List;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created on 2017/12/6.
 *
 * @author kenny
 */

public class DeviceUtils {

    private static final String TAG = Constant.TAG_PREFIX + "DeviceUtils";

    public static void openWX(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            LogUtil.e("Opening weixin failed: "+ e.getMessage(),e);
        }
    }

    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {

        String deviceId = null;
        try {
            LogUtil.i(TAG, "context = " + context);
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
            Log.i(TAG, "deviceId = " + deviceId);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
        return deviceId;
    }

    /**
     * Return the version name of app.
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
//            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static PackageInfo getPackageInfo(String packageName, Context context) {
        PackageInfo appInfo = null;
        try { PackageManager pm = context.getPackageManager();
            appInfo = pm.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            return appInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appInfo;
    }
}
