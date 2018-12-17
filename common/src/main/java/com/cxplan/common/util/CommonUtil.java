package com.cxplan.common.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damon on 2017/10/12.
 *
 * @description:
 */

public class CommonUtil {


    public static byte[] int2LowEndianBytes(int value) {
        byte[] ret = new byte[4];
        ret[0] =(byte) (value & 0xFF);
        ret[1] =(byte) ((value >> 8) & 0xFF);
        ret[2] =(byte) ((value >> 16) & 0xFF);
        ret[3] =(byte) ((value >> 24) & 0xFF);

        return ret;
    }

    public static void writeIntLowEndian(int value, OutputStream outputStream) throws IOException {
        if (outputStream == null) {
            throw new RuntimeException("The output stream object is empty!");
        }

        byte[] data = int2LowEndianBytes(value);

        outputStream.write(data);

    }

    public static int readIntLowEndian(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));
    }

    public static int readIntUpEndian(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        int ch3 = in.read();
        int ch4 = in.read();

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public static List<String> spanPermissionAvailability(Context context, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return new ArrayList<>();
        }

        List<String> notAvailableList = new ArrayList<>(permissions.length);
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {
                notAvailableList.add(permission);
            }
        }

        return notAvailableList;
    }

    public static boolean checkPermission(Activity context, String permission) {
        if (ContextCompat.checkSelfPermission(context,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            showDialogTipUserGoToAppSettting(context, permission);
            return false;
        } else {
            return true;
        }
    }

    public static void showPermissionTip(final Activity context, String text){
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("权限不可用").setMessage("还有如下权限未授权：" + text)
                .setPositiveButton("立即开启",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAppSetting(context);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setCancelable(false).show();

    }
    private static void showDialogTipUserGoToAppSettting(final Activity context, String permission){
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("存储权限不可用").setMessage("请在-应用设置-权限-中，允许[" + permission + "]权限")
                .setPositiveButton("立即开启",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAppSetting(context);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setCancelable(false).show();

    }
    // go to setting window.
    private static void goToAppSetting(Activity context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivityForResult(intent, 123);
    }
}