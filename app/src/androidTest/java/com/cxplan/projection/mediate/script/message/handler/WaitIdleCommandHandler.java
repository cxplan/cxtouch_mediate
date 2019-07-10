package com.cxplan.projection.mediate.script.message.handler;

import android.graphics.Rect;
import android.renderscript.Script;
import android.support.test.uiautomator.CXScriptHelper;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cxplan.common.util.LogUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.ClientConnection;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;
import com.cxplan.projection.mediate.script.ScriptApplication;
import com.cxplan.projection.mediate.script.message.CommandConstant;

import org.apache.mina.core.session.IoSession;

import java.util.concurrent.TimeoutException;

/**
 * @author kenny
 */
public class WaitIdleCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Span";
    public WaitIdleCommandHandler() {
        super(CommandConstant.CMD_DEVICE_SCRIPT_WAIT_IDLE);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ClientConnection connection = getConnection(session);
        long timeout = message.getParameter("timeout");

        LogUtil.i(TAG, "wait for idle: " + timeout);
        ScriptApplication.getInstance().getDevice().waitForIdle(timeout);

        byte ret = 1;//Default is success
        try {
            CXScriptHelper.getUiAutomation()
                    .waitForIdle(500, timeout);
        } catch (TimeoutException e) {
            LogUtil.w(TAG, "Could not detect idle state.");
            ret = 0;//timeout
        }

        Message retMsg = Message.createResultMessage(message);
        retMsg.setParameter("ret", ret);
        connection.sendMessage(retMsg);
    }

}
