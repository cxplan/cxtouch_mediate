package com.cxplan.projection.mediate.script;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.Configurator;

import com.cxplan.common.util.LogUtil;
import com.cxplan.common.util.StringUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.MonkeyManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author kenny
 */
@RunWith(AndroidJUnit4.class)
public class Launcher {

    static {
        Constant.TAG_PREFIX = "cxplan.script.";
    }

    private static final String TAG = Constant.TAG_PREFIX + "ScriptLauncher";

    private static LinkedBlockingQueue<Runnable> eventQueue;
    private int port = 2020;

    @Test
    public void testStartServer() {
        LogUtil.e(TAG, "To start cxscript service");
        init();
        //initialize application
        ScriptApplication.getInstance();
        //monkey manager
        ScriptMonkeyManager.init();
        //Start script message server.
        new ScriptMessageServer(port).start();

        //task queue
        eventQueue = new LinkedBlockingQueue<>(2000);
        while (true) {
            Runnable task;
            try {
                task = eventQueue.take();
            } catch (InterruptedException e) {
                break;
            }

            try {
                task.run();
            } catch (Exception ex) {
                LogUtil.e(TAG, ex.getMessage(), ex);
            }
        }
        System.out.println("CXScript Runtime is ended");
    }

    public static void invoke(Runnable task) {
        if (eventQueue == null) {
            LogUtil.e(TAG, "The script service is not initialized.");
            return;
        }
        eventQueue.offer(task);
    }

    private void init() {
        Bundle param = InstrumentationRegistry.getArguments();
        String portStr = param.getString("port");
        if (StringUtil.isNotEmpty(portStr)) {
            port = Integer.parseInt(portStr);
        }

        Configurator.getInstance().setWaitForIdleTimeout(5 * 1000);
    }
}
