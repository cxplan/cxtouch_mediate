package com.cxplan.mediate.message.handler;

import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;
import com.cxplan.mediate.message.MessageUtil;

import org.apache.mina.core.session.IoSession;


/**
 * Created by kenny on 2017/5/15.
 */

public class PingCommandHandler extends AbstractCommandHandler {

    private static final String TAG = "PingCommandHandler";
    public PingCommandHandler() {
        super(MessageUtil.CMD_PING);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {
        Message heartMsg = new Message(MessageUtil.CMD_PING_HEART);
        MessageUtil.sendMessage(session, heartMsg);
    }
}
