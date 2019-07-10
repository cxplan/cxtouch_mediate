package com.cxplan.cxscript;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import com.cxplan.common.util.LogUtil;
import com.cxplan.cxscript.message.ScriptControllerConnection;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.message.handler.CommandHandlerFactory;

/**
 * @author kenny
 */
public class ScriptApplication {
    private static String TAG = Constant.TAG_PREFIX + "ScriptApplication";

    private static ScriptApplication application;

    public static ScriptApplication getInstance() {
        if (application == null) {
            application = new ScriptApplication();
        }

        return application;
    }

    private UiDevice device;
    private ScriptControllerConnection controllerConnection;

    private ScriptApplication() {
        //build device object.
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        //Load all command handler
        Constant.packageName = InstrumentationRegistry.getContext().getPackageName();
        CommandHandlerFactory.loadHandler("com.cxplan.cxscript.io.handler",
                InstrumentationRegistry.getContext());

        LogUtil.i(TAG, "Initializing ScriptApplication is finished!");
    }

    public UiDevice getDevice() {
        return device;
    }

    public void setControllerConnection(ScriptControllerConnection connection) {
        if (this.controllerConnection != null) {
            this.controllerConnection.close();
        }
        this.controllerConnection = connection;
    }

    public ScriptControllerConnection getControllerConnection() {
        if (controllerConnection == null) {
            throw new RuntimeException("The controller doesn't connect to device!");
        }
        return controllerConnection;
    }
}
