package com.cxplan.projection.mediate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Looper;
import android.os.SystemProperties;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import com.cxplan.common.util.LogUtil;
import com.cxplan.common.util.RelectionUtil;
import com.cxplan.common.util.StringUtil;
import com.cxplan.projection.mediate.inputer.IMEConnection;
import com.cxplan.projection.mediate.io.ControllerConnection;
import com.cxplan.projection.mediate.io.MessageServer;
import com.cxplan.projection.mediate.message.handler.CommandHandlerFactory;
import com.cxplan.projection.mediate.model.DeviceInfo;
import com.cxplan.projection.mediate.util.WindowManagerUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class CXApplication {

    private static final String TAG = Constant.TAG_PREFIX + "Application";
    private static final String TAG_EXEC = Constant.TAG_PREFIX + "EXEC";

    public static final String PREFERENCE_NAME = "main";
    public static boolean isIMERunning = false;//Whether the CXTouchIME is running.
    public static String mIMEI = "";
    private static CXApplication instance;

    //The connection object between controller and device.
    private ControllerConnection connection;
    private IMEConnection inputerConnection;
    private DeviceInfo deviceInfo;
    private Context context;
    //The device ID
    private String uid;
    //The work directory. this field can be specified by vm options
    //The default value is "/data/local/tmp/cx_mediate"
    public static String workDirectory;
    private ExecutorService threadPool;

    private MessageServer messageServer;
    private boolean inProjection = false;

    //minicap config
    private int imageQuality = 80;

    private CXApplication() {
        init();
    }

    private void init() {
        //work directory.
        if (workDirectory == null) {
            workDirectory = "/data/local/tmp/cx_mediate";
        }
        File workDirFile = new File(workDirectory);
        if (!workDirFile.exists()) {
            workDirFile.mkdirs();
        }

        //context
        if (context == null) {
            try {
                context = createContext();
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage(), e);
                throw new RuntimeException("Create context failed: " + e.getMessage());
            }
        }

        //monkey manager
        MonkeyManager.initialize();
        LogUtil.i(TAG, "MonkeyManager is ok.");
        //Load all command handler
        CommandHandlerFactory.loadHandler("com.cxplan.projection.mediate.message.handler", context);
        LogUtil.i(TAG, "CommandHandler is ok.");
        //uid
        uid = android.os.Build.SERIAL;
        if (StringUtil.isEmpty(uid) || "unknown".equals(uid)) {
            uid = SystemProperties.get("ro.serialno");
        }
        System.out.println("The serial number: " + uid);
        //IMEI
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = null;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            LogUtil.e(TAG, "There is no permission to retrieve IMEI and phone number information.");
        } else {
            mIMEI = tm.getDeviceId();
            phoneNumber = tm.getLine1Number();
            LogUtil.i(TAG,"imei: " + mIMEI + "  phone number: " + phoneNumber);
        }

        deviceInfo = new DeviceInfo();
        deviceInfo.setId(uid);
        deviceInfo.setZoomRate(0.5f);
        deviceInfo.setPhone(phoneNumber);
        readScreenSize(deviceInfo);

        //connection
        connection = new ControllerConnection();
        inputerConnection = new IMEConnection();
    }

    private void readScreenSize(DeviceInfo deviceInfo) {
        Point point = new Point();
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealSize(point);
        int rotation;
        try {
            rotation = WindowManagerUtil.getRotation();
        } catch (Exception e) {
            rotation = windowManager.getDefaultDisplay().getRotation();
        }
        // video size
        final int width = point.x;
        final int height = point.y;
        if (rotation % 2 == 0) {
            deviceInfo.setScreenWidth(width);
            deviceInfo.setScreenHeight(height);
        } else {
            deviceInfo.setScreenWidth(height);
            deviceInfo.setScreenHeight(width);
        }
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public boolean isConnectedWithController() {
        return connection != null && connection.isConnected();
    }
    public ControllerConnection getControllerConnection() {
        return connection;
    }

    public IMEConnection getInputerConnection() {
        return inputerConnection;
    }

    public ExecutorService getThreadPool() {
        if (threadPool == null) {
            threadPool = Executors.newCachedThreadPool();
        }
        return threadPool;
    }

    public int getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

    public String getName() {
        return null;//TODO
    }

    public void setName(String name) {
        //TODO
    }

    public Context getContext() {
        return context;
    }

    public boolean isInProjection() {
        return inProjection;
    }

    public void setInProjection(boolean inProjection) {
        this.inProjection = inProjection;
    }
    /**
     * Start message server.
     */
    public synchronized void startMessageServer() {
        if (messageServer == null) {
            messageServer = new MessageServer();
        }
        messageServer.start();
    }

    /**
     * Return the application instance.
     */
    public static CXApplication getInstance() {
        if( instance == null) {
            instance = new CXApplication();
        }
        return instance;
    }

    static synchronized Context createContext() throws Exception {
        Looper.prepare();
        Object mActivityThread = null;

        try {
            mActivityThread = RelectionUtil.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");
            if (mActivityThread != null) {
                LogUtil.i(TAG,"currentActivityThread is ok.");
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "currentActivityThread ERROR: " + e.getMessage());
        }

        if (mActivityThread == null) {
            try {
                Constructor constructor = RelectionUtil.getDeclaredConstructor("android.app.ActivityThread");
                mActivityThread = constructor.newInstance();
                LogUtil.i(TAG,"constructor thread is ok.");
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
            }
        }

        if (mActivityThread == null) {
            try {
                mActivityThread = RelectionUtil.invokeStaticMethod("android.app.ActivityThread", "systemMain");
                LogUtil.i(TAG,"systemMain instance is ok.");
            } catch (Exception e) {
                LogUtil.e(TAG, "systemMain ERROR: " + e.getMessage());
            }
        }

        if (mActivityThread != null) {
            Object mContext = RelectionUtil.invokeMethod(mActivityThread, "getSystemContext", null);
            if (mContext == null) {
                try {
                    mContext = RelectionUtil.invokeStaticMethod("android.app.ContextImpl", "createSystemContext", mActivityThread);
                } catch (Exception e) {
                    LogUtil.e(TAG, "createSystemContext ERROR: " + e.getMessage());
                }

            }

            LogUtil.e(TAG, "context:" + mContext);

            return (Context)mContext;

        }

        return null;

    }

    public static Process exeCmd(String cmd, String[] env, boolean wait) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd, env);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        new StreamGrabber(process.getErrorStream(), 1).start();
        new StreamGrabber(process.getInputStream(), 0).start();

        int exitVal = 0;
        if (wait) {
            try {
                exitVal = process.waitFor();
                LogUtil.i(TAG, "ExitValue: " + exitVal);
            } catch (InterruptedException e) {
                LogUtil.e(TAG, e.getMessage(), e);
            }
        }

        return process;
    }

    static class StreamGrabber extends Thread
    {
        InputStream is;
        OutputStream os;

        int logLevel;//0: info 1: error

        StreamGrabber(InputStream is, int logLevel)
        {
            this(is, logLevel,null);
        }

        StreamGrabber(InputStream is, int logLevel, OutputStream redirect)
        {
            this.is = is;
            this.logLevel = logLevel;
            this.os = redirect;
        }

        public void run()
        {
            try
            {
                PrintWriter pw = null;
                if (os != null)
                    pw = new PrintWriter(os);

                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ( (line = br.readLine()) != null)
                {
                    if (pw != null)
                        pw.println(line);
                    if (logLevel == 1) {
                        LogUtil.e(TAG_EXEC, "-->" + line);
                    } else {
                        LogUtil.i(TAG_EXEC, "-->" + line);
                    }
                }
                if (pw != null)
                    pw.flush();
            } catch (IOException e)
            {
                LogUtil.e(TAG_EXEC, e.getMessage(), e);
            }
        }
    }
}
