package com.cxplan.projection.mediate.display;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.IBinder;
import android.view.Surface;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.CXApplication;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.util.WindowManagerUtil;

import java.lang.reflect.Method;

public class CXVirtualDisplay {
    private static final String TAG = Constant.TAG_PREFIX + "display";

    public static Point getDeviceDisplaySize() {
        int width = CXApplication.getInstance().getDeviceInfo().getScreenWidth();
        int height = CXApplication.getInstance().getDeviceInfo().getScreenHeight();
        int virtualWidth;
        int virtualHeight;
        int rotation = 0;
        try {
            rotation = WindowManagerUtil.getRotation();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
        if (rotation % 2 == 0) {
            virtualWidth = width;
            virtualHeight = height;
        } else {
            virtualHeight = width;
            virtualWidth = height;
        }

        return new Point(virtualWidth, virtualHeight);
    }

    private static Method destroyDisplayMethod;
    private static IBinder binder;

    public static boolean isCreated() {
        return binder != null;
    }
    public static void create(String str, int width, int height, Surface surface) {
        try {
            Class cls = Class.forName("android.view.SurfaceControl");
            binder = (IBinder) cls.getDeclaredMethod("createDisplay", new Class[]{String.class, Boolean.TYPE}).invoke(null, new Object[]{str, Boolean.valueOf(false)});
            Method setDisplaySurfaceMethod = cls.getDeclaredMethod("setDisplaySurface", new Class[]{IBinder.class, Surface.class});
            final Method setDisplayProjectionMethod = cls.getDeclaredMethod("setDisplayProjection", new Class[]{IBinder.class, Integer.TYPE, Rect.class, Rect.class});
            Method setDisplayLayerStackMethod = cls.getDeclaredMethod("setDisplayLayerStack", new Class[]{IBinder.class, Integer.TYPE});
            final Method openTransactionMethod = cls.getDeclaredMethod("openTransaction", new Class[0]);
            final Method closeTransactionMethod = cls.getDeclaredMethod("closeTransaction", new Class[0]);

            Rect displayRect = new Rect(0, 0, width, height);
            Point displaySize = getDeviceDisplaySize();
            Rect rect = new Rect(0, 0, displaySize.x, displaySize.y);
            openTransactionMethod.invoke(null, new Object[0]);
            setDisplaySurfaceMethod.invoke(null, new Object[]{binder, surface});
            setDisplayProjectionMethod.invoke(null, new Object[]{binder, Integer.valueOf(0), rect, displayRect});
            setDisplayLayerStackMethod.invoke(null, new Object[]{binder, Integer.valueOf(0)});
            closeTransactionMethod.invoke(null, new Object[0]);

            if (destroyDisplayMethod == null) {
                destroyDisplayMethod = cls.getDeclaredMethod("destroyDisplay", new Class[]{IBinder.class});
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void release() {
        if (destroyDisplayMethod != null && binder!= null) {
            try {
                destroyDisplayMethod.invoke(null, new Object[]{binder});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            binder = null;
        }
    }

}
