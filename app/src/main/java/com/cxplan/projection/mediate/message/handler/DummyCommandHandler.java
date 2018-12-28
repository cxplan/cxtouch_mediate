package com.cxplan.projection.mediate.message.handler;

import com.cxplan.projection.mediate.message.Message;
import com.cxplan.projection.mediate.message.MessageException;

import org.apache.mina.core.session.IoSession;


/**
 * Created on 2017/5/9.
 *
 * @author kenny
 */
public class DummyCommandHandler extends AbstractCommandHandler {

    public DummyCommandHandler() {
        super("dummy");
    }


    @Override
    public void process(IoSession session, Message message) throws MessageException {

    }
}
