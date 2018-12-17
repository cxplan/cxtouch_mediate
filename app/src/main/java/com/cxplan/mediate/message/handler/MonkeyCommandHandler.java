package com.cxplan.mediate.message.handler;

import android.content.ComponentName;
import android.content.Intent;

import com.cxplan.common.util.LogUtil;
import com.cxplan.common.util.StringUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.Constant;
import com.cxplan.mediate.MonkeyManager;
import com.cxplan.mediate.inputer.InputerReceiver;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.process.Main;

import org.apache.mina.core.session.IoSession;

public class MonkeyCommandHandler extends AbstractCommandHandler {
    private static final String TAG = Constant.TAG_PREFIX + "monkey";

    public MonkeyCommandHandler() {
        super(MessageUtil.CMD_DEVICE_MONKEY);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        short type = message.getParameter("type");
//        LogUtil.e(TAG, "monkey: " + type);
        switch (type) {
            case Constant.MONKEY_TYPE:
                processType(message);
                break;
            case Constant.MONKEY_MOUSE_DOWN:
                mouseDown(message);
                break;
            case Constant.MONKEY_MOUSE_MOVE:
                mouseMove(message);
                break;
            case Constant.MONKEY_MOUSE_UP:
                mouseUp(message);
                break;
            case Constant.MONKEY_SWITCH_INPUTER:
                switchInputer(message);
                break;
            case Constant.MONKEY_PRESS:
                procesPress(message);
                break;
            default:
                LogUtil.e(TAG, "The monkey operation is not supported: " + type);
        }
    }

    private void processType(Message message) {
        String text = message.getParameter("s");
        if (StringUtil.isEmpty(text)) {
            return;
        }

        if (CXApplication.isIMERunning) {//Custom IME is running.
            Intent intent = new Intent();
            intent.putExtra("s", text);
            intent.setComponent(new ComponentName(Constant.packageName, InputerReceiver.class.getName()));
            LogUtil.e(TAG, "start inputer receiver: ");

            try {
                MonkeyManager.sendBroadcast(intent);
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage(), e);
            }
        } else {
            MonkeyManager.injectCharKeyEvent(text.toCharArray());
        }
    }

    private void switchInputer(Message message) {
        boolean value = message.getParameter("isTouchIME");
        CXApplication.isIMERunning = value;
    }
    private void procesPress(Message message) {
        int keyCode = message.getParameter("kc");
        MonkeyManager.press(keyCode);
    }

    private void mouseDown(Message message) {
        final float x = message.getParameter("x");
        final float y = message.getParameter("y");
        Runnable task = new Runnable() {
            @Override
            public void run() {
                MonkeyManager.mouseDown(x, y);
            }
        };

        Main.invoke(task);

    }
    private void mouseMove(Message message) {
        final float x = message.getParameter("x");
        final float y = message.getParameter("y");
        Long tmpdelta = message.getParameter("delta");
        if (tmpdelta == null) {
            tmpdelta = -1L;
        }
        final Long delta = tmpdelta;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                MonkeyManager.mouseMove(x, y, delta);
            }
        };
        Main.invoke(task);
    }
    private void mouseUp(Message message) {
        final float x = message.getParameter("x");
        final float y = message.getParameter("y");
        Long tmpdelta = message.getParameter("delta");
        if (tmpdelta == null) {
            tmpdelta = -1L;
        }
        final Long delta = tmpdelta;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                MonkeyManager.mouseUp(x, y, delta);
            }
        };
        Main.invoke(task);
    }
}
