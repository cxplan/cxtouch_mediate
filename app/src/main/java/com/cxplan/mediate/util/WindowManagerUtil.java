package com.cxplan.mediate.util;

import android.hardware.display.IDisplayManager;
import android.os.RemoteException;
import android.view.DisplayInfo;
import android.view.IRotationWatcher;
import android.view.IWindowManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WindowManagerUtil {
    static Method watchRotationMethod;
    static boolean watchRotationNeedInt = false;

    public static int getRotation(IWindowManager windowManager, IDisplayManager displayManager)
            throws RemoteException, NoSuchFieldException, IllegalAccessException {
        try {
            int i = windowManager.getRotation();
            return i;
        } catch (Error error) {
            DisplayInfo di = displayManager.getDisplayInfo(0);
            return ((Integer) DisplayInfo.class.getDeclaredField("rotation").get(di)).intValue();
        }
    }

    public static void watchRotation(IWindowManager windowManager, IRotationWatcher rotationWatcher)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            if (watchRotationMethod == null) {
                watchRotationMethod = windowManager.getClass().getDeclaredMethod("watchRotation", IRotationWatcher.class);
            }
            if (watchRotationNeedInt) {
                watchRotationMethod.invoke(windowManager, rotationWatcher, 0);
            } else {
                watchRotationMethod.invoke(windowManager, new Object[]{rotationWatcher});
            }
        } catch (NoSuchMethodException exception) {
            watchRotationMethod = windowManager.getClass().getDeclaredMethod("watchRotation", new Class[]{IRotationWatcher.class, Integer.TYPE});
            watchRotationNeedInt = true;
            watchRotationMethod.invoke(windowManager, rotationWatcher, 0);
        }
    }
}
