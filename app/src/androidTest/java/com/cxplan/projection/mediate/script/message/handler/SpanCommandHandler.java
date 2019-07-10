package com.cxplan.projection.mediate.script.message.handler;

import android.graphics.Rect;
import android.support.test.uiautomator.CXScriptHelper;
import android.support.test.uiautomator.UiObject2;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cxplan.common.util.LogUtil;
import com.cxplan.common.util.StringUtil;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.ClientConnection;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;
import com.cxplan.projection.mediate.script.domain.ViewNode;
import com.cxplan.projection.mediate.script.message.CommandConstant;

import org.apache.mina.core.session.IoSession;

/**
 * @author kenny
 */
public class SpanCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "Span";
    public SpanCommandHandler() {
        super(CommandConstant.CMD_DEVICE_SCRIPT_SPAN);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        ClientConnection connection = getConnection(session);
        int x = message.getParameter("x");
        int y = message.getParameter("y");

        LogUtil.i(TAG, "The coordinates is {" + x + ", " + y + "}");

        AccessibilityNodeInfo node = CXScriptHelper.findNode(x, y);
        Message retMsg = Message.createResultMessage(message);
        retMsg.setParameter("ret", node == null ? 0 : 1);
        if (node != null) {
            ViewNode viewNode = CXScriptHelper.convertViewModel(node);;
            retMsg.setParameter("data", StringUtil.toJSONString(viewNode));
        }
        connection.sendMessage(retMsg);
    }

}
