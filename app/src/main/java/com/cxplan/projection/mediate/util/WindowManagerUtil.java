package com.cxplan.projection.mediate.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.os.RemoteException;
import android.view.DisplayInfo;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.Surface;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.CXApplication;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.MonkeyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class WindowManagerUtil {
    private static final String TAG = Constant.TAG_PREFIX + "WinUtil";

    static Method watchRotationMethod;
    static boolean watchRotationNeedInt = false;

    public static int getRotationAngle() throws NoSuchFieldException, IllegalAccessException, RemoteException {
        int value = getRotation(MonkeyManager.getWindowManager(), MonkeyManager.getDisplayManager());
        if (value == Surface.ROTATION_0) {
            return 0;
        } else if (value == Surface.ROTATION_90) {
            return 90;
        } else if (value == Surface.ROTATION_180) {
            return 180;
        } else if (value == Surface.ROTATION_270) {
            return 270;
        } else {
            throw new RuntimeException("Unknown rotation:" + value);
        }
    }
    public static int getRotation() throws NoSuchFieldException, IllegalAccessException, RemoteException {
        int value = getRotation(MonkeyManager.getWindowManager(), MonkeyManager.getDisplayManager());
        return value;
    }
    /**
     * Return rotation value, constants as per
     * {@link android.view.Surface}.
     *
     * @param windowManager window manager object.
     * @param displayManager display manager object.
     * @throws RemoteException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
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

    /**
     * Take a screenshot which size is according to specified zoom rate.
     * @throws Exception
     */
    public static Bitmap screenshot(float zoomRate) throws Exception {
        String surfaceClassName;
        int width = CXApplication.getInstance().getDeviceInfo().getScreenWidth();
        int height = CXApplication.getInstance().getDeviceInfo().getScreenHeight();
        int virtualWidth ;
        int virtualHeight;
        int rotation = getRotation();
        if (rotation % 2 == 0) {
            virtualWidth = (int)(width * zoomRate);
            virtualHeight = (int)(height * zoomRate);
        } else {
            virtualHeight = (int)(width * zoomRate);
            virtualWidth = (int)(height * zoomRate);
        }
        LogUtil.i(TAG, "screenshot:" + virtualWidth + "x" + virtualHeight + ", rotation:" + rotation);
        if (Build.VERSION.SDK_INT <= 17) {
            surfaceClassName = "android.view.Surface";
        } else {
            surfaceClassName = "android.view.SurfaceControl";
        }
        Bitmap b = (Bitmap) Class.forName(surfaceClassName).
                getDeclaredMethod("screenshot", Integer.TYPE, Integer.TYPE).
                invoke(null, virtualWidth, virtualHeight);
        if (rotation == 0) {
            return b;
        }
        Matrix m = new Matrix();
        if (rotation == 1) {
            m.postRotate(-90.0f);
        } else if (rotation == 2) {
            m.postRotate(-180.0f);
        } else if (rotation == 3) {
            m.postRotate(-270.0f);
        }
        return Bitmap.createBitmap(b, 0, 0, virtualHeight, virtualWidth, m, false);
    }
}
