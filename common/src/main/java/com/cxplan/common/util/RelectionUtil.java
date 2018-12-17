package com.cxplan.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RelectionUtil {

    public static Object newInstance(String className, Object... args) throws Exception {
        Class newoneClass = Class.forName(className);
        Constructor cons;
        if (args != null && args.length > 0) {
            Class[] argsClass = new Class[args.length];
            for (int i = 0, j = args.length; i < j; i++) {
                argsClass[i] = args[i].getClass();
            }
            cons = newoneClass.getConstructor(argsClass);
            return cons.newInstance(args);
        } else {
            cons = newoneClass.getConstructor();
            return cons.newInstance();
        }
    }

    public static Object invokeStaticMethod(String className, String methodName,
                                     Object... args) throws Exception {
        Class ownerClass = Class.forName(className);
        Method method;
        if (args != null && args.length > 0) {
            Class[] argsClass = new Class[args.length];
            for (int i = 0, j = args.length; i < j; i++) {
                argsClass[i] = args[i].getClass();
            }

            method = ownerClass.getMethod(methodName,argsClass);
        } else {
            method = ownerClass.getMethod(methodName);
        }

        return method.invoke(null, args);
    }

    public static Constructor getDeclaredConstructor(String className) throws Exception {
        Class ownerClass = Class.forName(className);
        return ownerClass.getConstructor();
    }

    public static Object invokeMethod(Object owner, String methodName, Object[] args) throws Exception {
        Class ownerClass = owner.getClass();
        Method method;
        if (args != null && args.length > 0) {
            Class[] argsClass = new Class[args.length];
            for (int i = 0, j = args.length; i < j; i++) {
                argsClass[i] = args[i].getClass();
            }
            method = ownerClass.getMethod(methodName,argsClass);
        } else {
            method = ownerClass.getMethod(methodName);
        }

        return method.invoke(owner, args);
    }

    public static Object getStaticField(String className, String fieldName)
            throws Exception {
        Class ownerClass = Class.forName(className);
        Field field = ownerClass.getField(fieldName);
        Object fieldObject = field.get(ownerClass);
        return fieldObject;
    }

    public static Object getField(Object owner, String fieldName) throws Exception {
        Class ownerClass = owner.getClass();
        Field field = ownerClass.getField(fieldName);
        Object fieldObject = field.get(owner);
        return fieldObject;
    }
}
