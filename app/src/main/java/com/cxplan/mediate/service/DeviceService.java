package com.cxplan.mediate.service;

import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.process.Main;

public class DeviceService extends BaseService {

    public void rotationChanged(int rotation) throws MessageException {
        Message message = new Message(MessageUtil.CMD_CONTROLLER_IMAGE);
        sendMessage(message);

        if (!CXApplication.getInstance().isInProjection()) {
            return;
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        Main.shutdownMinicap();
        Main.startMinicap();

    }
}
