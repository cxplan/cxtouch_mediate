package com.cxplan.mediate.message.handler;

import com.cxplan.mediate.message.Message;
import com.cxplan.mediate.message.MessageException;

import org.apache.mina.core.session.IoSession;

/**
 * Created on 2018/5/19.
 *
 * @author kenny
 */
public interface ICommandHandler {

    /**
     * The business logic respond to message.
     * @param session session object
     * @param message received message object.
     * @return
     */
    void process(IoSession session, Message message) throws MessageException;

    String getCommand();
}
