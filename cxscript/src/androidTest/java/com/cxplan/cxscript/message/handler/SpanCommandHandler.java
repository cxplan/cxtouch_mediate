package com.cxplan.cxscript.message.handler;

import android.graphics.Rect;
import android.support.test.uiautomator.CXScriptHelper;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cxplan.common.util.LogUtil;
import com.cxplan.cxscript.ScriptApplication;
import com.cxplan.cxscript.message.CommandConstant;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.ClientConnection;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;

import org.apache.mina.core.session.IoSession;

/**
 * @author kenny
 */
public class SpanCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Span";
    public SpanCommandHandler() {
        super(CommandConstant.CMD_DEVICE_SPAN);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ClientConnection connection = getConnection(session);
        int x = message.getParameter("x");
        int y = message.getParameter("y");

        LogUtil.i(TAG, "The coordinate is {" + x + ", " + y + "}");

        UiDevice device = ScriptApplication.getInstance().getDevice();
        AccessibilityNodeInfo node = CXScriptHelper.findNode(x, y);

        Message retMsg = Message.createResultMessage(message);
        retMsg.setParameter("ret", node == null ? 0 : 1);
        if (node != null) {
            UiObject2 object = CXScriptHelper.buildObjectFromNode(node);;
            retMsg.setParameter("className", object.getClassName());
            retMsg.setParameter("desc", object.getContentDescription());
            retMsg.setParameter("resName", object.getResourceName());
            retMsg.setParameter("text", object.getText());
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            StringBuilder sb = new StringBuilder(32);
            sb.append(rect.left).append(" ").append(rect.top).append(" ").
                    append(rect.width()).append(" ").append(rect.height());
            retMsg.setParameter("rect", sb.toString());
        }
        connection.sendMessage(retMsg);
    }
}
