package com.cxplan.projection.mediate.service;

import com.cxplan.projection.mediate.CXApplication;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public class BaseService {

    public void sendMessage(Message message) throws MessageException {
        if (!CXApplication.getInstance().isConnectedWithController()) {
            throw new MessageException("The connection with controller is not ok");
        }

        CXApplication.getInstance().getControllerConnection().sendMessage(message);
    }
}
