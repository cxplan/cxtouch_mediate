package com.cxplan.projection.mediate.service;

import com.cxplan.common.util.LogUtil;
import com.cxplan.common.util.StringUtil;
import com.cxplan.projection.mediate.CXApplication;
import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.MessageUtil;
import com.cxplan.projection.mediate.process.Main;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class DeviceService extends BaseService {
    private static final String TAG = Constant.TAG_PREFIX + "deviceService";

    /**
     * This method will be used When rotation of screen is changed.
     * This method send a change notice to controller. Meanwhile shutdown expired minicap service,
     * and then start a new minicap process using latest parameters.
     *
     * @param rotation the new rotation flag.
     * @throws MessageException
     */
    public void rotationChanged(int rotation) throws MessageException {
        LogUtil.i(TAG, "The rotation is changed: " + rotation);

        Message message = new Message(MessageUtil.CMD_CONTROLLER_IMAGE);
        message.setParameter("type", (short)1);
        message.setParameter("msg", "The rotation is changed!");
        message.setParameter("ro", (short)rotation);
        sendMessage(message);

        if (!CXApplication.getInstance().isInProjection()) {
            LogUtil.i(TAG, "The device is not in projection now.");
            return;
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        Main.shutdownMinicap();
        Main.startMinicap();

        //notify controller to open image channel.
        message.setParameter("type", (short)2);
        message.setParameter("msg", null);
        sendMessage(message);

        LogUtil.i(TAG, "The result of starting minicap is sent to controller");
    }

    /**
     * When a new text is copied to clipboard, this method should be
     * invoked to tell client the current content.
     *
     * @param text the current text content on clipboard.
     */
    public void clipboardChanged(String text) throws MessageException {
        LogUtil.i(TAG, "The content on clipboard is changed: " + text);
        if (StringUtil.isEmpty(text)) {
            return;
        }

        Message message = new Message(MessageUtil.CMD_CONTROLLER_CLIPBOARD);
        message.setParameter("c", text);
        sendMessage(message);
    }

    /**
     * Disable custom IME, and switch to other IME version.
     * This method will send a notice to tell CXTouch IME to restore old ime.
     */
    public void disableCXIME() {
        CXApplication.isIMERunning = false;
        Message message = new Message(MessageUtil.CMD_DEVICE_INPUTER);
        message.setParameter("type", (short)1);
        try {
            CXApplication.getInstance().getInputerConnection().sendMessage(message);
        } catch (MessageException e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }
}
