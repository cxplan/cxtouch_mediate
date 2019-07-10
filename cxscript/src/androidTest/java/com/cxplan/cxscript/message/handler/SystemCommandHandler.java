package com.cxplan.cxscript.message.handler;

import com.cxplan.cxscript.message.CommandConstant;
import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;
import com.cxplan.projection.mediate.message.handler.AbstractCommandHandler;

import org.apache.mina.core.session.IoSession;

/**
 * @author kenny
 */
public class SystemCommandHandler extends AbstractCommandHandler {
    public SystemCommandHandler() {
        super(CommandConstant.CMD_CONTROLLER_SYSTEM);
    }

    @Override
    public void process(IoSession session, Message message) throws MessageException {

    }
}
