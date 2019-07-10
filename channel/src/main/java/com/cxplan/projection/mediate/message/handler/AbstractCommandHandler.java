package com.cxplan.projection.mediate.message.handler;

import com.cxplan.projection.mediate.Constant;
import com.cxplan.projection.mediate.io.ClientConnection;

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
        return (T) session.getAttribute(Constant.CLIENT_SESSION);
    }
}
