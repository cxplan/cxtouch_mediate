package com.cxplan.projection.mediate.script.message.handler;

import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.CXScriptHelper;
import android.support.test.uiautomator.UiObject2;

import com.cxplan.common.util.LogUtil;
import com.cxplan.common.util.StringUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.ClientConnection;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;
import com.cxplan.projection.mediate.script.ScriptApplication;
import com.cxplan.projection.mediate.script.domain.ViewNode;
import com.cxplan.projection.mediate.script.message.CommandConstant;

import org.apache.mina.core.session.IoSession;

import java.util.concurrent.TimeoutException;

/**
 * @author kenny
 */
public class WaitViewCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Span";
    public WaitViewCommandHandler() {
        super(CommandConstant.CMD_DEVICE_SCRIPT_WAIT_VIEW);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ClientConnection connection = getConnection(session);
        long timeout = message.getParameter("timeout");
        String viewString = message.getParameter("view");
        ViewNode view = StringUtil.json2Object(viewString, ViewNode.class);

        LogUtil.i(TAG, "wait for idle: " + timeout);
        ScriptApplication.getInstance().getDevice().waitForIdle(timeout);

        byte ret = 1;//Default is success
        try {
            CXScriptHelper.getUiAutomation()
                    .waitForIdle(500, timeout);
        } catch (TimeoutException e) {
            LogUtil.w(TAG, "Could not detect idle state.");
            ret = 3;//timeout
        }

        BySelector selector = By.clazz(view.getClassName());
        selector.pkg(view.getPackageName());
        if (view.getText() != null) {
            selector.text(view.getText());
        }
        if (view.getContentDesc() != null) {
            selector.desc(view.getContentDesc());
        }
        if (view.getResourceId() != null) {
            selector.res(view.getResourceId());
        }
        selector.clickable(view.isClickable()).longClickable(view.isLongClickable()).
                enabled(view.isEnabled()).checkable(view.isCheckable()).focusable(view.isFocusable());
        UiObject2 object = ScriptApplication.getInstance().getDevice().findObject(selector);
        if (object == null) {
            if (ret == 1) {
                ret = 2;//The component is not found.
                LogUtil.e(TAG, "Expected View: " + view.toString());
                LogUtil.e(TAG, "Selector: " + selector.toString());
            }
        } else {
            if (ret == 3) {
                ret = 1;
            }
        }

        Message retMsg = Message.createResultMessage(message);
        retMsg.setParameter("ret", ret);
        connection.sendMessage(retMsg);
    }

}
