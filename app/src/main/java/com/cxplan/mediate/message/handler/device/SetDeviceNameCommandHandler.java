package com.cxplan.mediate.message.handler.device;

import com.cxplan.common.util.StringUtil;
import com.cxplan.mediate.CXApplication;
import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;
import com.cxplan.mediate.message.handler.AbstractCommandHandler;

import org.apache.mina.core.session.IoSession;

/**
 * Created by kenny on 2017/6/9.
 */

public class SetDeviceNameCommandHandler extends AbstractCommandHandler {
    public SetDeviceNameCommandHandler() {
        super(MessageUtil.CMD_DEVICE_SET_NAME);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        String name = message.getParameter("name");
        if (StringUtil.isEmpty(name)) {
            throw new MessageException("The name can't be empty!");
        }

        CXApplication.getInstance().setName(name);

        Message ret = Message.createResultMessage(message);
        ret.setParameter("name", name);
        MessageUtil.sendMessage(session, ret);
    }
}
