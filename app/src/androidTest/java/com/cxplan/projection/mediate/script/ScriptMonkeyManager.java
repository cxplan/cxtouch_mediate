package com.cxplan.projection.mediate.script;

import android.app.UiAutomation;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.CXScriptHelper;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.Constant;

import java.lang.reflect.InvocationTargetException;

/**
 * @author kenny
 * Created on 2019/3/28
 */
public class ScriptMonkeyManager {
    private static final String TAG = Constant.TAG_PREFIX + "ScriptMonkey";
    private static boolean isDown = false;
    private static long downTime = 0L;

    private static UiAutomation uiAutomation;

    public static void init() {
        uiAutomation = CXScriptHelper.getUiAutomation();
    }
    /**
     * The mouse is pressed down.
     * @param x the x coordinate of point where the mouse is
     * @param y the y coordinate of point where the mouse is
     */
    public static void mouseDown(float x, float y) {
        if (isDown) {
            return;
        }
        isDown = true;
        downTime = SystemClock.uptimeMillis();
        try {
            injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_DOWN, downTime, downTime, x, y, 1.0F);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * The mouse is moving.
     * @param x the x coordinate of point where the mouse is
     * @param y the y coordinate of point where the mouse is
     * @param downDelta the time gap between last mouse down time and current event.
     */
    public static void mouseMove(float x, float y, long downDelta) {
        if (!isDown) {
            return;
        }
        if (downDelta < 1) {
            downDelta = SystemClock.uptimeMillis() - downTime;
        }
        try {
            injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_MOVE, downTime, downTime + downDelta, x, y, 1.0F);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * The mouse is up.
     * @param x the x coordinate of point where the mouse is
     * @param y the y coordinate of point where the mouse is
     * @param downDelta the time gap between last mouse down time and current event.
     */
    public static void mouseUp(float x, float y, long downDelta) {
        if (!isDown) {
            return;
        }
        isDown = false;
        if (downDelta < 1) {
            downDelta = SystemClock.uptimeMillis() - downTime;
        }
        try {
            injectMotionEvent(InputDevice.SOURCE_TOUCHSCREEN, MotionEvent.ACTION_UP, downTime, downTime + downDelta, x, y, 1.0F);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }

    private static void injectMotionEvent(int source, int action, long downTime, long eventTime, float x, float y, float pressure)
    {
        MotionEvent localMotionEvent = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0F, 0, 1.0F, 1.0F, 0, 0);
        localMotionEvent.setSource(source);
        uiAutomation.injectInputEvent(localMotionEvent, true);
    }
}
