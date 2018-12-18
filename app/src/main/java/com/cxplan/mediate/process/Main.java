package com.cxplan.mediate.process;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static final String TAG = Constant.TAG_PREFIX + "main";

    private static LinkedBlockingQueue<Runnable> eventQueue;
    private static Process minicapProcess;

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            CXApplication.workDirectory = args[0].trim();
        }
        //write pid to file.
        int pid = android.os.Process.myPid();
        System.out.println("Current PID: " + pid);

        //1 initial application including context and device information.
        final CXApplication instance;
        try {
            instance = CXApplication.getInstance();
            System.out.println("initialize context finished: " + instance.getDeviceInfo());
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
            throw e;
        }

        System.out.println("Work directory: " + CXApplication.workDirectory);
        try {
            FileUtil.writeBytes((pid + "").getBytes(Charset.forName("utf-8")), new File(CXApplication.workDirectory, "cxmonkey.pid"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //2 message server
        instance.startMessageServer();
        //3 image service.
//        startMinicap();
        System.out.println("Execute command  finished");

        eventQueue = new LinkedBlockingQueue<>(2000);
        while (true) {
            Runnable task;
            try {
                task = eventQueue.take();
            } catch (InterruptedException e) {
                break;
            }

            task.run();
        }
        System.out.println("Runtime is ended");
    }

    public static void invoke(Runnable task) {
        eventQueue.offer(task);
    }

    public static void shutdownMinicap() {
        if (minicapProcess != null) {
            minicapProcess.destroy();
            minicapProcess = null;
        }
    }

    public static boolean isMinicapActive() {
        if (minicapProcess == null) {
            return false;
        }
        try {
            minicapProcess.exitValue();
            return false;
        } catch(IllegalThreadStateException ex) {
            return true;
        }
    }

    public static void startMinicap() {
        if (isMinicapActive()) {
            LogUtil.i(TAG, "The minicap is started already!");
            return;
        }

        if (minicapProcess != null) {
            minicapProcess.destroy();
        }
        String cmd = buildMinicapCommand();
        LogUtil.e(TAG, cmd);
        String[] env = new String[]{"LD_LIBRARY_PATH=/data/local/tmp"};
        minicapProcess = CXApplication.exeCmd(cmd, env, false);

        String message = "The minicap service is started!";
        LogUtil.i(TAG, message);
        System.out.println(message);
    }

    private static String buildMinicapCommand() {
        //LD_LIBRARY_PATH=/data/local/tmp nohup /data/local/tmp/minicap -P 720x1280@360x640/0 >
        // /sdcard/cxplan/image.log 2>&1 &
        int width = CXApplication.getInstance().getDeviceInfo().getScreenWidth();
        int height = CXApplication.getInstance().getDeviceInfo().getScreenHeight();
        double scale = CXApplication.getInstance().getDeviceInfo().getZoomRate();
        int quality = CXApplication.getInstance().getImageQuality();
        StringBuilder sb = new StringBuilder("/data/local/tmp/minicap -S -P ");
        sb.append(height).append("x").append(width).
                append("@").append((int)(height * scale)).append("x").append((int)(width * scale)).
                append("/0 -Q ").append(quality);
        return sb.toString();
    }
}
