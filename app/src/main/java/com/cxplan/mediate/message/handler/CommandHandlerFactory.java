package com.cxplan.mediate.message.handler;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.util.DeviceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import dalvik.system.DexFile;

/**
 * Created on 2017/5/9.
 *
 * @author kenny
 */
public class CommandHandlerFactory {

    private static final String TAG = Constant.TAG_PREFIX + "CommandHandlerFactory";

    private static Map<String, ICommandHandler> handlerMap;
    private static boolean isInitialized = false;
    static {
        handlerMap = new HashMap<String, ICommandHandler>();
        InputStream input = CommandHandlerFactory.class.getResourceAsStream("/resource/handler.properties");
        Properties prop = new Properties();
        try {
            prop.load(input);
        } catch (IOException e) {
            LogUtil.e(TAG, "Loading handler failed: " + e.getMessage(), e);
        }

        ICommandHandler dummyHandler = new DummyCommandHandler();

        for (Map.Entry<Object, Object> entry: prop.entrySet()) {
            String command = (String)entry.getKey();
            String clazz = (String)entry.getValue();
            if (clazz.equals("dummy")) {
                handlerMap.put(command, dummyHandler);
                continue;
            }
            try {
                Class<?> handlerClass = CommandHandlerFactory.class.getClassLoader().loadClass(clazz);
                ICommandHandler handler = (ICommandHandler) handlerClass.newInstance();
                handlerMap.put(command, handler);
            } catch (ClassNotFoundException e) {
                LogUtil.e(TAG, "Loading handler failed: " + e.getMessage(), e);
            } catch (InstantiationException e) {
                LogUtil.e(TAG, "Loading handler failed: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                LogUtil.e(TAG, "Loading handler failed: " + e.getMessage(), e);
            }
        }
    }

    public static boolean isIsInitialized() {
        return isInitialized;
    }

    public static void loadHandler(String packageName, Context context) {
        DexFile df = null;
        try {
            df = new DexFile(getDexFilePath(context));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
            String s = iter.nextElement();
            if (!s.startsWith(packageName)) {
                continue;
            }
            try {
                Class<?> clazz = CommandHandlerFactory.class.forName(s);
                if (ICommandHandler.class.isAssignableFrom(clazz)
                        && !Modifier.isAbstract(clazz.getModifiers())) {
                    System.out.println(clazz.getName());
                    ICommandHandler handler = (ICommandHandler)clazz.newInstance();
                    handlerMap.put(handler.getCommand(), handler);
                }
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
            }
        }

        LogUtil.i(TAG, "The command handlers are loaded successfully: " + handlerMap.size());
        isInitialized = true;
    }

    public static ICommandHandler getHandler(String command) {
        return handlerMap.get(command);
    }

    public static void addHandler(String command, ICommandHandler handler) {
        if (handlerMap.containsKey(command)) {
            LogUtil.e(TAG, "The command handler:" + command + " has been registered! ");
            return;
        }
        handlerMap.put(command, handler);
    }

    private static String getDexFilePath(Context context) {
        PackageInfo pi = DeviceUtils.getPackageInfo(Constant.packageName,
                context);
        if (pi == null) {
            throw new RuntimeException("The library doesn't exist: " + Constant.packageName);
        }

        return pi.applicationInfo.sourceDir;
    }
}
