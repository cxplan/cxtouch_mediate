package com.cxplan.mediate.message.handler;

import com.cxplan.mediate.io.ClientConnection;
import com.cxplan.mediate.io.DefaultIoHandlerAdapter;

import org.apache.mina.core.session.IoSession;

/**
 * Created on 2017/5/9.
 *
 * @author kenny
 */
public abstract class AbstractCommandHandler implements ICommandHandler{

    private String command;

    public AbstractCommandHandler(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    protected <T extends ClientConnection> T getConnection(IoSession session) {
        return (T) session.getAttribute(DefaultIoHandlerAdapter.CLIENT_SESSION);
    }
}
