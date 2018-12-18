package com.cxplan.mediate.service;

import com.cxplan.common.util.LogUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.process.Main;

public class DeviceService extends BaseService {
    private static final String TAG = "deviceService";

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
        message.setParameter("msg", "The rotation is changed!");
        sendMessage(message);

        if (!CXApplication.getInstance().isInProjection()) {
            return;
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }

        Main.shutdownMinicap();
        Main.startMinicap();

    }
}
