package com.cxplan.mediate;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.IDisplayManager;
import android.hardware.input.InputManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.service.DeviceService;
import com.cxplan.mediate.util.WindowManagerUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class MonkeyManager {
    private static String TAG = Constant.TAG_PREFIX + "monkey";

    private static InputManager inputManager;
    private static Method injectInputMethod;
    private static KeyCharacterMap keyCharacterMap;
    //power
    private static Object powerManager;
    private static Method isInteractiveMethod;
    //activity manager
    private static Object activityManager;
    private static Method broadcastIntent;
    //window manager
    private static IWindowManager windowManager;
    private static IDisplayManager displayManager;

    static boolean isInitialized = false;
    public static Instrumentation inst = new Instrumentation();
    static boolean isDown = false;//Indicate whether the touch is down.
    static long downTime = -1L;

    public static void initialize() {
        if (isInitialized) {
            return;
        }

        //retrieve inputer manager object.
        if (inputManager == null) {
            try {
                inputManager = (InputManager)InputManager.class.getDeclaredMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                injectInputMethod = InputManager.class.getMethod("injectInputEvent", new Class[] { InputEvent.class, Integer.TYPE });
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage(), e);
            }
        }
        //key event cached
        if (keyCharacterMap == null) {
            keyCharacterMap = KeyCharacterMap.load(-1);
        }

        Class<?> serviceManagerClazz ;
        Method getServiceMethod = null;
        //power manager
        try {
            serviceManagerClazz = Class.forName("android.os.ServiceManager");
            getServiceMethod = serviceManagerClazz.getMethod("getService", String.class);
            Object powerService = getServiceMethod.invoke(null, Context.POWER_SERVICE);
            Class<?> cStub =  Class.forName("android.os.IPowerManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            powerManager = asInterface.invoke(null, powerService);
            isInteractiveMethod = powerManager.getClass().getMethod("isInteractive");
        } catch (Exception e) {
            LogUtil.e(TAG, "initializing power manager failed: " + e.getMessage(), e);
            e.printStackTrace();
        }

        //activity manager
        try {
            activityManager = Class.forName("android.app.ActivityManagerNative").getDeclaredMethod("getDefault", new Class[0]).invoke(null, new Object[0]);
            Method[] methods = activityManager.getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (!m.getName().equals("broadcastIntent")) {
                    continue;
                }
                if ((m.getParameterTypes().length == 13)
                        || (m.getParameterTypes().length == 11)
                        || (m.getParameterTypes().length == 12)) {
                    broadcastIntent = m;
                } else {
                    String message = "Invalid broadcastIntent";
                    System.out.println(message);
                    LogUtil.e(TAG, message);
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "initializing power manager failed: " + e.getMessage(), e);
            e.printStackTrace();
        }
        //window manager
        try {
            windowManager = IWindowManager.Stub.asInterface((IBinder)(getServiceMethod).invoke(null, new Object[] { "window" }));
        } catch (Exception e) {
            LogUtil.e(TAG, "initializing window manager failed: " + e.getMessage(), e);
            e.printStackTrace();
        }
        try {
            displayManager = IDisplayManager.Stub.asInterface((IBinder)(getServiceMethod).invoke(null, new Object[] { "display" }));
        } catch (Exception e) {
            LogUtil.e(TAG, "initializing display manager failed: " + e.getMessage(), e);
            e.printStackTrace();
        }
        //watch rotation to screen
        try {
            WindowManagerUtil.watchRotation(windowManager, new IRotationWatcher.Stub() {
                public void onRotationChanged(int rotation)
                        throws RemoteException
                {
                    DeviceService service = new DeviceService();
                    try {
                        service.rotationChanged(rotation);
                    } catch (MessageException e) {
                        LogUtil.e(TAG, e.getMessage(), e);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.getMessage(), e);
        }

        try {
            System.out.println("Current rotation: " + WindowManagerUtil.getRotation(windowManager, displayManager));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IWindowManager getWindowManager() {
        return windowManager;
    }

    public static IDisplayManager getDisplayManager() {
        return displayManager;
    }

    public static void injectKeyEvent(KeyEvent event)
            throws InvocationTargetException, IllegalAccessException
    {
        if (inputManager == null) {
            LogUtil.e(TAG, "The inputer manager is not initialized");
            return;
        }
        if (injectInputMethod == null) {
            LogUtil.e(TAG, "The inject input method is not initialized");
            return;
        }
//        inst.sendKeySync(event);
        Boolean ret = (Boolean)injectInputMethod.invoke(inputManager, new Object[] { event, Integer.valueOf(2) });
        if (!ret) {
            LogUtil.e(TAG, "inject event failed: " + event);
        }
    }
    public static void injectCharKeyEvent(char... chars) {
        if (keyCharacterMap == null) {
            LogUtil.e(TAG, "The key event cached is not initialized");
            return;
        }
        KeyEvent[] events = keyCharacterMap.getEvents(chars);
        if (events == null || events.length == 0) {
            return;
        }
        try {
            for (KeyEvent event : events) {
                injectKeyEvent(event);
            }
        } catch (Exception ex) {
            LogUtil.e(TAG, ex.getMessage(), ex);
        }
    }

    /**
     * Scroll screen down or up.
     *
     * @param x the x coordinate of point.
     * @param y the y coordinate of point.
     * @param hScrollValue horizontal scroll increment, {@link MotionEvent#AXIS_HSCROLL}
     * @param vScrollValue vertical scroll increment, {@link MotionEvent#AXIS_VSCROLL}
     */
    public static void scroll(float x, float y, float hScrollValue, float vScrollValue) {
        long l = SystemClock.uptimeMillis();
        MotionEvent.PointerProperties[] pps = new MotionEvent.PointerProperties[1];
        pps[0] = new MotionEvent.PointerProperties();
        pps[0].clear();
        pps[0].id = 0;
        MotionEvent.PointerCoords[] mpc = new MotionEvent.PointerCoords[1];
        mpc[0] = new MotionEvent.PointerCoords();
        mpc[0].clear();
        mpc[0].x = x;
        mpc[0].y = y;
        mpc[0].pressure = 1.0F;
        mpc[0].size = 1.0F;
        mpc[0].setAxisValue(MotionEvent.AXIS_HSCROLL, hScrollValue);
        mpc[0].setAxisValue(MotionEvent.AXIS_VSCROLL, vScrollValue);
        MotionEvent event = MotionEvent.obtain(l, l, MotionEvent.ACTION_SCROLL,
                1, pps, mpc, 0, 0, 1.0F,
                1.0F, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        try {
            injectInputMethod.invoke(inputManager, event, 0);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
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

    public static void sendBroadcast(Intent paramIntent)
            throws Exception
    {
        if (broadcastIntent.getParameterTypes().length == 11) {
            broadcastIntent.invoke(activityManager, new Object[] { null, paramIntent, null, null, Integer.valueOf(0), null, null, null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2) });
        } else if (broadcastIntent.getParameterTypes().length == 12) {
            broadcastIntent.invoke(activityManager, new Object[] { null, paramIntent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2) });
        } else if (broadcastIntent.getParameterTypes().length == 13) {
            broadcastIntent.invoke(activityManager, new Object[]{null, paramIntent, null, null, Integer.valueOf(0), null, null, null, Integer.valueOf(-1), null, Boolean.valueOf(true), Boolean.valueOf(false), Integer.valueOf(-2)});
        }
    }

    public static void turnScreenOnOff()
            throws RemoteException, InvocationTargetException, IllegalAccessException
    {
        boolean isScreenOn = (Boolean) isInteractiveMethod.invoke(powerManager);
        press(KeyEvent.KEYCODE_POWER);
    }

    public static void turnScreenOn() throws InvocationTargetException, IllegalAccessException {
        boolean isScreenOn = (Boolean) isInteractiveMethod.invoke(powerManager);
        if (isScreenOn) {
            return;
        }
        press(KeyEvent.KEYCODE_POWER);
    }

    public static void turnScreenOff() throws InvocationTargetException, IllegalAccessException {
        boolean isScreenOn = (Boolean) isInteractiveMethod.invoke(powerManager);
        if (!isScreenOn) {
            return;
        }
        press(KeyEvent.KEYCODE_POWER);
    }

    /**
     * Send a key event to device from keyboard.
     *
     * @param keyCode code of key.
     * @see KeyEvent#KEYCODE_POWER
     * @see KeyEvent#KEYCODE_HOME
     */
    public static void press(int keyCode) {
        try {
            sendKeyEvent(InputDevice.SOURCE_KEYBOARD, keyCode, false);
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }

    private static void sendKeyEvent(int source, int code, boolean longpress)
            throws InvocationTargetException, IllegalAccessException
    {
        long uptimeMillis = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(uptimeMillis, uptimeMillis, KeyEvent.ACTION_DOWN, code, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, source));
        if (longpress) {
            injectKeyEvent(new KeyEvent(uptimeMillis, uptimeMillis, KeyEvent.ACTION_DOWN, code, 1, 0,
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_LONG_PRESS,
                    source));
        }
        injectKeyEvent( new KeyEvent(uptimeMillis, uptimeMillis, KeyEvent.ACTION_UP, code, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, source));
    }

    //mouse
    private static void injectMotionEvent(int source, int action, long downTime, long eventTime, float x, float y, float pressure)
            throws InvocationTargetException, IllegalAccessException
    {
        MotionEvent localMotionEvent = MotionEvent.obtain(downTime, eventTime, action, x, y, pressure, 1.0F, 0, 1.0F, 1.0F, 0, 0);
        localMotionEvent.setSource(source);
        inst.sendPointerSync(localMotionEvent);
//        injectInputMethod.invoke(inputManager, new Object[] { localMotionEvent, Integer.valueOf(0) });
    }
}
