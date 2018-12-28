package com.cxplan.projection.mediate.message.handler.controller;

import com.cxplan.common.util.StringUtil;
import com.cxplan.projection.mediate.CXApplication;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.MessageUtil;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;

import org.apache.mina.core.session.IoSession;

/**
 * Created on 2018/5/19.
 *
 * @author kenny
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
