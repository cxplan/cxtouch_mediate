package com.cxplan.projection.mediate.script.message.handler;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.CXScriptHelper;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cxplan.common.util.LogUtil;
import com.cxplan.common.util.StringUtil;
import com.cxplan.projection.mediate.CXApplication;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.MonkeyManager;
import com.cxplan.projection.mediate.inputer.InputerReceiver;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.MessageUtil;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;
import com.cxplan.projection.mediate.script.Launcher;
import com.cxplan.projection.mediate.script.ScriptApplication;
import com.cxplan.projection.mediate.script.ScriptMonkeyManager;
import com.cxplan.projection.mediate.script.domain.ScriptRect;
import com.cxplan.projection.mediate.script.domain.ViewNode;
import com.cxplan.projection.mediate.script.message.CommandConstant;

import org.apache.mina.core.session.IoSession;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class ScriptMonkeyCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "monkey";

    public ScriptMonkeyCommandHandler() {
        super(MessageUtil.CMD_DEVICE_MONKEY);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        short type = message.getParameter("type");
        try {
            switch (type) {
                case Constant.MONKEY_MOUSE_DOWN:
                    mouseDown(message);
                    break;
                case Constant.MONKEY_MOUSE_MOVE:
                    mouseMove(message);
                    break;
                case Constant.MONKEY_MOUSE_UP:
                    mouseUp(message);
                    break;
                case Constant.MONKEY_PRESS:
                    processPress(message);
                    break;
                case Constant.MONKEY_SCROLL:
                    scroll(message);
                    break;
                case Constant.MONKEY_WAKE:
                    wake(message);
                    break;
                default:
                    LogUtil.e(TAG, "The monkey operation is not supported: " + type);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
            throw new MessageException(e.getMessage(), e);
        }
    }

    private void processPress(Message message) {
        int keyCode = message.getParameter("kc");
        ScriptApplication.getInstance().getDevice().pressKeyCode(keyCode);
    }

    private void scroll(Message message) {
        final float x = message.getParameter("x");
        final float y = message.getParameter("y");
        final Float vScrollValue = message.getParameter("vs");
        final Float hScrollValue = message.getParameter("hs");
        MonkeyManager.scroll(x, y, hScrollValue == null ? 0f : hScrollValue,
                vScrollValue == null ? 0f : vScrollValue);
    }

    private void wake(Message message) {
        try {
            ScriptApplication.getInstance().getDevice().wakeUp();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage(), e);
        }
    }

    private void mouseDown(Message message) throws MessageException {
        final int x = message.getParameter("x");
        final int y = message.getParameter("y");
        int seqNum = message.getParameter("seq");
        ScriptMonkeyManager.mouseDown(x, y);

        AccessibilityNodeInfo node = CXScriptHelper.findNode(x, y);
        String dataString = StringUtil.toJSONString(CXScriptHelper.convertViewModel(node));
        Message viewNodeMessage = new Message(CommandConstant.CMD_CONTROLLER_SCRIPT_VIEW_NODE);
        viewNodeMessage.setParameter("data", dataString);
        viewNodeMessage.setParameter("seq", seqNum);
        ScriptApplication.getInstance().getControllerConnection().sendMessage(viewNodeMessage);
    }

    private void mouseMove(Message message) {
        final float x = message.getParameter("x");
        final float y = message.getParameter("y");
        Long tmpdelta = message.getParameter("delta");
        if (tmpdelta == null) {
            tmpdelta = -1L;
        }
        final Long delta = tmpdelta;
        ScriptMonkeyManager.mouseMove(x, y, delta);
    }
    private void mouseUp(Message message) {
        final float x = message.getParameter("x");
        final float y = message.getParameter("y");
        Long tmpdelta = message.getParameter("delta");
        if (tmpdelta == null) {
            tmpdelta = -1L;
        }
        final Long delta = tmpdelta;
        ScriptMonkeyManager.mouseUp(x, y, delta);
    }
}
